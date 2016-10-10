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
                .withName(ConfigParameters.APP_NAME)
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
        from.setName(ConfigParameters.APP_NAME + ":${IS_TAG}");
        from.setKind("ImageStreamTag");
        from.setNamespace("${IS_PULL_NAMESPACE}");

        DeploymentTriggerImageChangeParams imageChangeParms = new DeploymentTriggerImageChangeParams();
        imageChangeParms.setFrom(from);
        imageChangeParms.setAutomatic(true);

        DeploymentTriggerPolicy imageChange = new DeploymentTriggerPolicy();
        imageChange.setType("ImageChange");
        imageChange.setImageChangeParams(imageChangeParms);
        imageChangeParms.setContainerNames(Lists.newArrayList(ConfigParameters.APP_NAME));

        List<DeploymentTriggerPolicy> triggers = new ArrayList<DeploymentTriggerPolicy>();
        triggers.add(configChange);
        triggers.add(imageChange);

        return triggers;
    }

    private List<ContainerPort> getPorts() {
        List<ContainerPort> ports = new ArrayList<ContainerPort>();

        ContainerPort http = new ContainerPort();
        http.setContainerPort(8181);
        http.setProtocol("TCP");
        http.setName("http");

        ContainerPort jolokia = new ContainerPort();
        jolokia.setContainerPort(8778);
        jolokia.setProtocol("TCP");
        jolokia.setName("jolokia");

        ports.add(http);
        ports.add(jolokia);

        return ports;
    }

    private Container getContainers() {
        Container container = new Container();
        container.setImage("${IS_PULL_NAMESPACE}/" + ConfigParameters.APP_NAME + ":${IS_TAG}");
        container.setImagePullPolicy("Always");
        container.setName(ConfigParameters.APP_NAME);
        container.setPorts(getPorts());
//        container.setLivenessProbe(getProbe());
//        container.setReadinessProbe(getProbe());
        container.setVolumeMounts(getVolumeMounts());
        return container;
    }


    private List<Volume> getVolumes(){

        Volume certSecrets = new Volume();
        certSecrets.setSecret(new SecretVolumeSource(ConfigParameters.SECRET_NAME));
        certSecrets.setName(ConfigParameters.SECRET_NAME);

        Volume configMap = new Volume();
        configMap.setConfigMap(new ConfigMapVolumeSource(null, ConfigParameters.CONFIGMAP_NAME));
        configMap.setName(ConfigParameters.CONFIGMAP_NAME);

        return new ImmutableList.Builder<Volume>().add(certSecrets).add(configMap).build();
    }


    private List<VolumeMount> getVolumeMounts(){
        return new ImmutableList.Builder<VolumeMount>()
                .add(new VolumeMount(ConfigParameters.SECRET_MOUNT_DIR,ConfigParameters.SECRET_NAME,true))
                .add(new VolumeMount(ConfigParameters.CONFIGMAP_MOUNT_DIR,ConfigParameters.CONFIGMAP_NAME,true))
                .build();

    }

    private Map<String, String> getSelectors() {
        Map<String, String> selectors = new HashMap<>();
        selectors.put("app", ConfigParameters.APP_NAME);
        selectors.put("deploymentconfig", ConfigParameters.APP_NAME);

        return selectors;
    }

//    private Probe getProbe() {
//        TCPSocketAction ldapAction = new TCPSocketAction();
//        ldapAction.setPort(new IntOrString(389));
//
//        Probe probe = new Probe();
//        probe.setInitialDelaySeconds(new Integer(15));
//        probe.setTimeoutSeconds(new Integer(5));
//        probe.setTcpSocket(ldapAction);
//
//        return probe;
//    }
}
