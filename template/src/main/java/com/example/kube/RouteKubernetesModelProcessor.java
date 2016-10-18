package com.example.kube;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.openshift.api.model.RoutePort;
import io.fabric8.openshift.api.model.TemplateBuilder;
import java.util.HashMap;
import java.util.Map;

import static com.example.kube.ConfigConstants.getLabels;

/**
 * Created by swinchester on 11/10/2016.
 */
public class RouteKubernetesModelProcessor {

    public void on(TemplateBuilder builder) {

        builder.addNewRouteObject()
                .withNewMetadata().withName("secure-" + ConfigConstants.APP_NAME)
                .withLabels(getLabels())
                .endMetadata()
                .withNewSpec()
                .withHost("${HOSTNAME_HTTPS}")
                .withNewTls().withTermination("passthrough").endTls()
                .withNewTo()
                .withKind("Service")
                .withName("secure-" + ConfigConstants.APP_NAME)
                .endTo()
                .endSpec()
                .endRouteObject()
                .addNewRouteObject()
                .withNewMetadata().withName(ConfigConstants.APP_NAME)
                .withLabels(getLabels())
                .endMetadata()
                .withNewSpec()
                .withHost("${HOSTNAME_HTTP}")
                .withNewTo()
                .withKind("Service")
                .withName(ConfigConstants.APP_NAME)
                .endTo()
                .endSpec()
                .endRouteObject()
                .build();
    }

}
