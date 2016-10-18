package com.example.kube;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.openshift.api.model.TemplateBuilder;

import java.util.Map;
/**
 * Created by swinchester on 11/10/2016.
 */
public class ServiceKubernetesModelProcessor {

    public void on(TemplateBuilder builder) {

        builder.addNewServiceObject()
                .withNewMetadata().withName(ConfigConstants.APP_NAME)
                .withLabels(ConfigConstants.getLabels())
                .endMetadata()
                .withNewSpec()
                .withPorts(new ImmutableList.Builder<ServicePort>()
                        .add(new ServicePort(null, null , 8080, "TCP", new IntOrString(8080))).build())
                .addToSelector(getSelector())
                .endSpec()
                .endServiceObject()
                .addNewServiceObject()
                .withNewMetadata().withName("secure-" + ConfigConstants.APP_NAME)
                .withLabels(ConfigConstants.getLabels())
                .endMetadata()
                .withNewSpec()
                .withPorts(new ImmutableList.Builder<ServicePort>()
                        .add(new ServicePort(null, null , 8443, "TCP", new IntOrString(8443))).build())
                .addToSelector(getSelector())
                .endSpec()
                .endServiceObject()
                .build();
    }



    public static Map<String, String> getSelector() {
        return ImmutableMap.<String, String> builder()
                .put("deploymentConfig", ConfigConstants.APP_NAME)
                .build();
    }
}
