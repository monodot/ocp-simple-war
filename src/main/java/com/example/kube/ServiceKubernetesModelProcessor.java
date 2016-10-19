package com.example.kube;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.IntOrStringBuilder;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.openshift.api.model.TemplateBuilder;
import io.fabric8.utils.Ports;

import java.util.Map;

import static io.fabric8.kubernetes.api.model.KubernetesKind.List;

/**
 * Created by swinchester on 11/10/2016.
 */
public class ServiceKubernetesModelProcessor {

    public void on(TemplateBuilder builder) {

        builder.addNewServiceObject()
                .withNewSpec()
                    .withPorts()
                        .addNewPort()
                            .withPort(ConfigConstants.JWS_HTTP_PORT)
                            .withTargetPort(new IntOrStringBuilder().withIntVal(ConfigConstants.JWS_HTTP_PORT).build())
                        .endPort()
                    .withSelector(getSelector())
                .endSpec()
                .withNewMetadata()
                    .withName(ConfigConstants.APP_NAME)
                    .withLabels(ConfigConstants.getLabels())
                    .withAnnotations(ImmutableMap.<String, String>of(
                            "description",
                            "The web server's http port."))
                .endMetadata()
                .endServiceObject()


                .addNewServiceObject()
                .withNewSpec()
                    .withPorts()
                        .addNewPort()
                            .withPort(ConfigConstants.JWS_HTTPS_PORT)
                            .withTargetPort(new IntOrStringBuilder().withIntVal(ConfigConstants.JWS_HTTPS_PORT).build())
                        .endPort()
                    .withSelector(getSelector())
                .endSpec()
                .withNewMetadata()
                    .withName("secure-" + ConfigConstants.APP_NAME)
                    .withLabels(ConfigConstants.getLabels())
                    .withAnnotations(ImmutableMap.<String, String>of(
                            "description",
                            "The web server's https port."))
                .endMetadata()
                .endServiceObject()


                .addNewServiceObject()
                .withNewSpec()
                    .withPorts()
                        .addNewPort()
                            .withPort(ConfigConstants.MYSQL_PORT)
                            .withTargetPort(new IntOrStringBuilder().withIntVal(ConfigConstants.MYSQL_PORT).build())
                        .endPort()
                    .withSelector(getSelectorDb())
                .endSpec()
                .withNewMetadata()
                    .withName(ConfigConstants.APP_NAME + "-mysql")
                    .withLabels(ConfigConstants.getLabels())
                    .withAnnotations(ImmutableMap.<String, String>of(
                            "description",
                            "The database server's port."))
                .endMetadata()
                .endServiceObject()

                .build();

    }

    public static Map<String, String> getSelector() {
        return ImmutableMap.<String, String> builder()
                .put("deploymentConfig", ConfigConstants.APP_NAME)
                .build();
    }

    public static Map<String, String> getSelectorDb() {
        return ImmutableMap.<String, String> builder()
                .put("deploymentConfig", ConfigConstants.APP_NAME + "-mysql")
                .build();
    }
}
