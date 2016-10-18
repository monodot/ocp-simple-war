package com.example.kube;

import com.google.common.collect.ImmutableList;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.openshift.api.model.*;
import io.fabric8.utils.Lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeploymentConfigKubernetesModelProcessor {

/* TODO check if I'm still being used
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
*/

    public void on(TemplateBuilder builder) {
        builder.addNewDeploymentConfigObject()
                .withNewMetadata()
                    .withName(ConfigConstants.APP_NAME)
                    .withLabels(getLabels())
                .endMetadata()
                .withNewSpec()
                    .withNewStrategy()
                        .withType("Recreate")
                    .endStrategy()
                .withTriggers(getTriggers())
                .withReplicas(1)
                    .withSelector(getSelectors())
                    .withNewTemplate()
                        .withNewMetadata()
                            .withName(ConfigConstants.APP_NAME)
                            .withLabels(getLabelsTemplate())
                        .endMetadata()
                        .withNewSpec()
                            .withServiceAccountName(ConfigConstants.SERVICE_ACCOUNT_NAME)
                            .withTerminationGracePeriodSeconds(60L)
                            .withContainers(getContainers())
                            .withRestartPolicy("Always")
                            .withVolumes(getVolumes())
                        .endSpec()
                    .endTemplate()

                .endSpec()
                .endDeploymentConfigObject()

                // mysql builder

                .addNewDeploymentConfigObject()
                .withNewMetadata()
                    .withName("${APPLICATION_NAME}-mysql")
                    .withLabels(getLabels())
                .endMetadata()
                .withNewSpec()
                    .withNewStrategy()
                        .withType("Recreate")
                    .endStrategy()
                    .withTriggers(getTriggersDb())
                    .withReplicas(1)
                    .withSelector(getSelectorsDb())
                    .withNewTemplate()
                        .withNewMetadata()
                            .withLabels(getLabelsTemplateDb())
                            .withName("${APPLICATION_NAME}-mysql")
                        .endMetadata()
                        .withNewSpec()
                            .withTerminationGracePeriodSeconds(60L)
                            .withContainers(getContainersDb())
                        .endSpec()
                    .endTemplate()
                .endSpec()
                .endDeploymentConfigObject()

                .build();
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

    private List<DeploymentTriggerPolicy> getTriggersDb() {
/* TODO remove me when working
        DeploymentTriggerPolicy configChange = new DeploymentTriggerPolicy();
        configChange.setType("ConfigChange");

        ObjectReference from = new ObjectReference();
        from.setKind("ImageStreamTag");
        from.setName("mysql:latest");
        from.setNamespace("${IMAGE_STREAM_NAMESPACE}");

        DeploymentTriggerImageChangeParams imageChangeParms = new DeploymentTriggerImageChangeParams();
        imageChangeParms.setFrom(from);
        imageChangeParms.setAutomatic(true);

        DeploymentTriggerPolicy imageChange = new DeploymentTriggerPolicy();
        imageChange.setType("ImageChange");
        imageChange.setImageChangeParams(imageChangeParms);
        imageChangeParms.setContainerNames(Lists.newArrayList("${APPLICATION_NAME}-mysql"));

        List<DeploymentTriggerPolicy> triggers = new ArrayList<DeploymentTriggerPolicy>();
        triggers.add(imageChange);
        triggers.add(configChange);

*/
        List<DeploymentTriggerPolicy> triggers = new ArrayList<DeploymentTriggerPolicy>();
        triggers.add(new DeploymentTriggerPolicyBuilder()
                .withType("ImageChange")
                .withNewImageChangeParams()
                    .withAutomatic(true)
                    .withContainerNames(ConfigConstants.APP_NAME + "-mysql")
                    .withNewFrom()
                        .withKind("ImageStreamTag")
                        .withNamespace("${IMAGE_STREAM_NAMESPACE}")
                        .withName("mysql:latest")
                    .endFrom()
                .endImageChangeParams()
                    .build());

/*
        triggers.add(new DeploymentTriggerPolicyBuilder()
                .withType("ConfigChange")
                .build());
*/

        return triggers;
    }

    private List<ContainerPort> getPorts() {
        List<ContainerPort> ports = new ArrayList<ContainerPort>();

        ContainerPort jolokia = new ContainerPort();
        jolokia.setContainerPort(ConfigConstants.JOLOKIA_PORT);
        jolokia.setProtocol(ConfigConstants.PROTOCOL_TCP);
        jolokia.setName("jolokia");

        ContainerPort http = new ContainerPort();
        http.setContainerPort(ConfigConstants.JWS_HTTP_PORT);
        http.setProtocol(ConfigConstants.PROTOCOL_TCP);
        http.setName("http");

        ContainerPort https = new ContainerPort();
        https.setContainerPort(ConfigConstants.JWS_HTTPS_PORT);
        https.setProtocol(ConfigConstants.PROTOCOL_TCP);
        https.setName("https");

        ports.add(http);
        ports.add(https);
        ports.add(jolokia);

        return ports;
    }

    private List<ContainerPort> getPortsDb() {
        List<ContainerPort> ports = new ArrayList<ContainerPort>();

        ports.add(new ContainerPortBuilder()
                .withName("mysql")
                .withContainerPort(ConfigConstants.MYSQL_PORT)
                .withProtocol(ConfigConstants.PROTOCOL_TCP)
                .build());

        return ports;
    }

    private Container getContainers() {
        Container container = new Container();

        container.setName(ConfigConstants.APP_NAME);
        container.setImage("${IS_PULL_NAMESPACE}/" + ConfigConstants.APP_NAME + ":${IS_TAG}");
        container.setImagePullPolicy("Always");
        container.setReadinessProbe(getReadinessProbe());
        container.setPorts(getPorts());
        container.setEnv(getEnv());
        container.setResources(getResourceRequirements());
//        container.setLivenessProbe(getProbe());
        container.setVolumeMounts(getVolumeMounts());
        return container;
    }

    private List<Container> getContainersDb() {

/* TODO remove me
        Container container = new Container();
        container.setImage("mysql");
        container.setName("${APPLICATION_NAME}-mysql");
        container.setPorts(getPortsDb());
        container.setEnv(getEnvDb());
*/

        List<Container> containers = new ArrayList<Container>();

        containers.add(new ContainerBuilder()
                .withName(ConfigConstants.APP_NAME + "-mysql")
                .withImage("mysql")
                .withPorts(getPortsDb())
                .withEnv(getEnvDb())
                .build());

        return containers;
    }

    private List<EnvVar> getEnv(){

        List<EnvVar> envVars = new ArrayList<EnvVar>();

        return new ImmutableList.Builder<EnvVar>()
                .add(new EnvVar("DB_SERVICE_PREFIX_MAPPING", "${APPLICATION_NAME}-mysql=DB", null))
                .add(new EnvVar("DB_JNDI", "${DB_JNDI}", null))
                .add(new EnvVar("DB_USERNAME", "${DB_USERNAME}", null))
                .add(new EnvVar("DB_PASSWORD", "${DB_PASSWORD}", null))
                .add(new EnvVar("DB_DATABASE", "${DB_DATABASE}", null))
                .add(new EnvVar("DB_MIN_POOL_SIZE", "${DB_MIN_POOL_SIZE}", null))
                .add(new EnvVar("DB_MAX_POOL_SIZE", "${DB_MAX_POOL_SIZE}", null))
                .add(new EnvVar("DB_TX_ISOLATION", "${DB_TX_ISOLATION}", null))
                .add(new EnvVar("JWS_HTTPS_CERTIFICATE_DIR", ConfigConstants.SECRET_VOLUME_MOUNT_PATH, null))
                .add(new EnvVar("JWS_HTTPS_CERTIFICATE", "${JWS_HTTPS_CERTIFICATE}", null))
                .add(new EnvVar("JWS_HTTPS_CERTIFICATE_KEY", "${JWS_HTTPS_CERTIFICATE_KEY}", null))
                .add(new EnvVar("JWS_HTTPS_CERTIFICATE_PASSWORD", "${JWS_HTTPS_CERTIFICATE_PASSWORD}", null))
                .add(new EnvVar("JWS_ADMIN_USERNAME", "${JWS_ADMIN_USERNAME}", null))
                .add(new EnvVar("JWS_ADMIN_PASSWORD", "${JWS_ADMIN_PASSWORD}", null))

                .add(new EnvVar("APPDYNAMICS_CONTROLLER_HOST_NAME", "${APPDYNAMICS_CONTROLLER_HOST_NAME}", null))
                .add(new EnvVar("APPDYNAMICS_AGENT_ACCOUNT_NAME", "${APPDYNAMICS_AGENT_ACCOUNT_NAME}", null))
                .add(new EnvVar("APPDYNAMICS_CONTROLLER_PORT", "${APPDYNAMICS_CONTROLLER_PORT}", null))
                .add(new EnvVar("APPDYNAMICS_CONTROLLER_SSL_ENABLED", "${APPDYNAMICS_CONTROLLER_SSL_ENABLED}", null))
                .add(new EnvVar("APPDYNAMICS_AGENT_ACCOUNT_NAME", "${APPDYNAMICS_AGENT_ACCOUNT_NAME}", null))
                .add(new EnvVar("APPDYNAMICS_AGENT_ACCOUNT_ACCESS_KEY", "${APPDYNAMICS_AGENT_ACCOUNT_ACCESS_KEY}", null))
                .add(new EnvVar("APPDYNAMICS_AGENT_APPLICATION_NAME", "${APPDYNAMICS_AGENT_APPLICATION_NAME}", null))
                .add(new EnvVar(
                        "APPDYNAMICS_AGENT_TIER_NAME",
                        null,
                        new EnvVarSource(null,new ObjectFieldSelector(null, "metadata.namespace"), null)))
                .add(new EnvVar(
                        "APPDYNAMICS_AGENT_NODE_NAME",
                        null,
                        new EnvVarSource(null,new ObjectFieldSelector(null, "metadata.name"), null)))
                .build();

    }

    private List<EnvVar> getEnvDb() {
        return new ImmutableList.Builder<EnvVar>()
                .add(new EnvVar("MYSQL_USER", "${DB_USERNAME}", null))
                .add(new EnvVar("MYSQL_PASSWORD", "${DB_PASSWORD}", null))
                .add(new EnvVar("MYSQL_DATABASE", "${DB_DATABASE}", null))
                .add(new EnvVar("MYSQL_LOWER_CASE_TABLE_NAMES", "${MYSQL_LOWER_CASE_TABLE_NAMES}", null))
                .add(new EnvVar("MYSQL_MAX_CONNECTIONS", "${MYSQL_MAX_CONNECTIONS}", null))
                .add(new EnvVar("MYSQL_FT_MIN_WORD_LEN", "${MYSQL_FT_MIN_WORD_LEN}", null))
                .add(new EnvVar("MYSQL_FT_MAX_WORD_LEN", "${MYSQL_FT_MAX_WORD_LEN}", null))
                .add(new EnvVar("MYSQL_AIO", "${MYSQL_AIO}", null))
                .build();

    }

    private List<Volume> getVolumes(){

        List<Volume> volumes = new ArrayList<Volume>();

        volumes.add(new VolumeBuilder()
                .withName(ConfigConstants.SECRET_VOLUME_MOUNT_NAME)
                .withNewSecret()
                    .withSecretName(ConfigConstants.SECRET_NAME)
                .endSecret()
                .build());

        return volumes;

    }


    private List<VolumeMount> getVolumeMounts(){
        List<VolumeMount> volumeMounts = new ArrayList<VolumeMount>();

        volumeMounts.add(new VolumeMountBuilder()
                .withName(ConfigConstants.SECRET_VOLUME_MOUNT_NAME)
                .withMountPath(ConfigConstants.SECRET_VOLUME_MOUNT_PATH)
                .withReadOnly(true)
                .build());

        return volumeMounts;

    }

    private Map<String, String> getSelectors() {
        Map<String, String> selectors = new HashMap<>();
        //selectors.put("app", ConfigConstants.APP_NAME);
        selectors.put("deploymentconfig", ConfigConstants.APP_NAME);

        return selectors;
    }

    private Map<String, String> getSelectorsDb() {
        Map<String, String> selectors = new HashMap<>();
        selectors.put("deploymentConfig", "${APPLICATION_NAME}-mysql");

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

    private Map<String, String> getLabelsTemplate() {
        Map<String, String> labels = new HashMap<String,String>();
        labels.put("application", "${APPLICATION_NAME}");
        labels.put("deploymentConfig", "${APPLICATION_NAME}");

        return labels;
    }

    private Map<String, String> getLabelsTemplateDb() {
        Map<String, String> labels = new HashMap<String,String>();
        labels.put("application", "${APPLICATION_NAME}");
        labels.put("deploymentConfig", "${APPLICATION_NAME}-mysql");

        return labels;
    }

    private Map<String, String> getLabels() {
        Map<String, String> labels = new HashMap<String,String>();
        labels.put("application", "${APPLICATION_NAME}");

        return labels;
    }

}
