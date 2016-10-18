package com.example.kube;

import com.google.common.collect.ImmutableList;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.openshift.api.model.DeploymentConfigBuilder;
import io.fabric8.openshift.api.model.DeploymentTriggerImageChangeParams;
import io.fabric8.openshift.api.model.DeploymentTriggerPolicy;
import io.fabric8.openshift.api.model.TemplateBuilder;
import io.fabric8.utils.Lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeploymentConfigKubernetesModelProcessor {

    public void on(DeploymentConfigBuilder builder) {
        builder.withSpec(builder.getSpec())
                .editSpec()
                    .withReplicas(1)
                    .withSelector(getSelectors())
                    .withNewStrategy()
                        .withType("Recreate")
                    .endStrategy()
                    .editTemplate()
                        .editSpec()
                            .withContainers(getContainers())
                            .withRestartPolicy("Always")
                            .withVolumes(getVolumes())
                        .endSpec()
                    .endTemplate()
                    .withTriggers(getTriggers())
                .endSpec()
            .build();
    }

    public void on(TemplateBuilder builder) {
        builder.addNewDeploymentConfigObject()
                .withNewMetadata()
                .withName(ConfigConstants.APP_NAME)
                .endMetadata()
                .withNewSpec()
                .withReplicas(1)
                .withSelector(getSelectors())
                .withNewStrategy()
                .withType("Recreate")
                .endStrategy()
                .withNewTemplate()
                .withNewMetadata()
                .withLabels(getSelectors())
                .endMetadata()
                .withNewSpec()
                .withContainers(getContainers())
                .withRestartPolicy("Always")
                .withVolumes(getVolumes())
                .endSpec()
                .endTemplate()
                .withTriggers(getTriggers())
                .endSpec()
                .endDeploymentConfigObject().build();
    }


    private List<DeploymentTriggerPolicy> getTriggers() {
        DeploymentTriggerPolicy configChange = new DeploymentTriggerPolicy();
        configChange.setType("ConfigChange");

        ObjectReference from = new ObjectReference();
        from.setName(ConfigConstants.APP_NAME + ":${IS_TAG}");
        from.setKind("ImageStreamTag");
        from.setNamespace("${IS_PULL_NAMESPACE}");

        DeploymentTriggerImageChangeParams imageChangeParms = new DeploymentTriggerImageChangeParams();
        imageChangeParms.setFrom(from);
        imageChangeParms.setAutomatic(true);

        DeploymentTriggerPolicy imageChange = new DeploymentTriggerPolicy();
        imageChange.setType("ImageChange");
        imageChange.setImageChangeParams(imageChangeParms);
        imageChangeParms.setContainerNames(Lists.newArrayList(ConfigConstants.APP_NAME));

        List<DeploymentTriggerPolicy> triggers = new ArrayList<DeploymentTriggerPolicy>();
        triggers.add(configChange);
        triggers.add(imageChange);

        return triggers;
    }

    private List<ContainerPort> getPorts() {
        List<ContainerPort> ports = new ArrayList<ContainerPort>();

        ContainerPort http = new ContainerPort();
        http.setContainerPort(8080);
        http.setProtocol("TCP");
        http.setName("http");

        ContainerPort https = new ContainerPort();
        https.setContainerPort(8443);
        https.setProtocol("TCP");
        https.setName("https");

        ContainerPort jolokia = new ContainerPort();
        jolokia.setContainerPort(8778);
        jolokia.setProtocol("TCP");
        jolokia.setName("jolokia");

        ports.add(http);
        ports.add(https);
        ports.add(jolokia);

        return ports;
    }

    private Container getContainers() {
        Container container = new Container();
        container.setImage("${IS_PULL_NAMESPACE}/" + ConfigConstants.APP_NAME + ":${IS_TAG}");
        container.setImagePullPolicy("Always");
        container.setName(ConfigConstants.APP_NAME);
        container.setPorts(getPorts());
        container.setEnv(getEnv());
        container.setResources(getResourceRequirements());
//        container.setLivenessProbe(getProbe());
        container.setReadinessProbe(getReadinessProbe());
        container.setVolumeMounts(getVolumeMounts());
        return container;
    }

    private List<EnvVar> getEnv(){
        return new ImmutableList.Builder<EnvVar>()
                .add(new EnvVar("APPDYNAMICS_CONTROLLER_HOST_NAME", "${APPDYNAMICS_CONTROLLER_HOST_NAME}", null))
                .add(new EnvVar("APPDYNAMICS_AGENT_ACCOUNT_NAME", "${APPDYNAMICS_AGENT_ACCOUNT_NAME}", null))
                .add(new EnvVar("APPDYNAMICS_CONTROLLER_PORT", "${APPDYNAMICS_CONTROLLER_PORT}", null))
                .add(new EnvVar("APPDYNAMICS_CONTROLLER_SSL_ENABLED", "${APPDYNAMICS_CONTROLLER_SSL_ENABLED}", null))
                .add(new EnvVar("APPDYNAMICS_AGENT_ACCOUNT_NAME", "${APPDYNAMICS_AGENT_ACCOUNT_NAME}", null))
                .add(new EnvVar("APPDYNAMICS_AGENT_ACCOUNT_ACCESS_KEY", "${APPDYNAMICS_AGENT_ACCOUNT_ACCESS_KEY}", null))
                .add(new EnvVar("APPDYNAMICS_AGENT_APPLICATION_NAME", "${APPDYNAMICS_AGENT_APPLICATION_NAME}", null))
                .add(new EnvVar("APPDYNAMICS_AGENT_TIER_NAME", "${APPDYNAMICS_AGENT_TIER_NAME}",
                        new EnvVarSource(null,new ObjectFieldSelector(null, "metadata.namespace"), null)))
                .add(new EnvVar("APPDYNAMICS_AGENT_NODE_NAME", "${APPDYNAMICS_AGENT_NODE_NAME}",
                        new EnvVarSource(null,new ObjectFieldSelector(null, "metadata.name"), null)))
                .build();

    }

    private List<Volume> getVolumes(){

        Volume certSecrets = new Volume();
        certSecrets.setSecret(new SecretVolumeSource(ConfigConstants.SECRET_NAME));
        certSecrets.setName(ConfigConstants.SECRET_NAME);

        Volume configMap = new Volume();
        configMap.setConfigMap(new ConfigMapVolumeSource(null, ConfigConstants.CONFIGMAP_NAME));
        configMap.setName(ConfigConstants.CONFIGMAP_NAME);

        return new ImmutableList.Builder<Volume>().add(certSecrets).add(configMap).build();
    }


    private List<VolumeMount> getVolumeMounts(){
        return new ImmutableList.Builder<VolumeMount>()
                .add(new VolumeMount(ConfigConstants.SECRET_MOUNT_DIR,ConfigConstants.SECRET_NAME,true))
                .add(new VolumeMount(ConfigConstants.CONFIGMAP_MOUNT_DIR,ConfigConstants.CONFIGMAP_NAME,true))
                .build();

    }

    private Map<String, String> getSelectors() {
        Map<String, String> selectors = new HashMap<>();
        selectors.put("app", ConfigConstants.APP_NAME);
        selectors.put("deploymentconfig", ConfigConstants.APP_NAME);

        return selectors;
    }

    private Probe getProbe() {
        TCPSocketAction ldapAction = new TCPSocketAction();
        ldapAction.setPort(new IntOrString(389));

        Probe probe = new Probe();
        probe.setInitialDelaySeconds(new Integer(15));
        probe.setTimeoutSeconds(new Integer(5));
        probe.setTcpSocket(ldapAction);

        return probe;
    }


    private Probe getReadinessProbe() {
        Probe readyProbe = new Probe();
        List<String> execCommands = new ImmutableList.Builder<String>()
                .add("/bin/bash")
                .add("-c")
                .add("curl -s -u ${JWS_ADMIN_USERNAME}:${JWS_ADMIN_PASSWORD} 'http://localhost:8080/manager/jmxproxy/?get=Catalina%3Atype%3DServer&att=stateName' |grep -iq 'stateName *= *STARTED'").build();
        readyProbe.setExec(new ExecAction(execCommands));
        readyProbe.setTimeoutSeconds(10);
        return readyProbe;

    }

    private ResourceRequirements getResourceRequirements() {
        ResourceRequirements resourceRequirements = new ResourceRequirements();
        resourceRequirements.setRequests(getRequests());
        resourceRequirements.setLimits(getLimits());

        return resourceRequirements;
    }

    private Map<String, Quantity> getRequests() {
        Map<String, Quantity> limits = new HashMap<String, Quantity>();
        limits.put("cpu", new Quantity("200m"));
        limits.put("memory", new Quantity("512Mi"));

        return limits;
    }

    private Map<String, Quantity> getLimits() {
        Map<String, Quantity> limits = new HashMap<String, Quantity>();
        limits.put("cpu", new Quantity("400m"));
        limits.put("memory", new Quantity("1024Mi"));

        return limits;
    }
}
