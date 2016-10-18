package com.example.kube;

import com.google.common.collect.ImmutableMap;
import io.fabric8.openshift.api.model.TemplateBuilder;

import static com.example.kube.ConfigConstants.getLabels;

/**
 * Created by swinchester on 11/10/2016.
 */
public class RouteKubernetesModelProcessor {

    public void on(TemplateBuilder builder) {

        builder.addNewRouteObject()
                .withNewMetadata()
                    .withName(ConfigConstants.APP_NAME)
                    .withLabels(getLabels())
                    .withAnnotations(ImmutableMap.<String, String>of(
                            "description",
                            "Route for application's http service."))
                .endMetadata()
                .withNewSpec()
                    .withHost("${HOSTNAME_HTTP}")
                    .withNewTo()
                        .withName(ConfigConstants.APP_NAME)
                    .endTo()
                .endSpec()
                .endRouteObject()


                .addNewRouteObject()
                .withNewMetadata()
                    .withName(ConfigConstants.APP_NAME + "-https")
                    .withLabels(getLabels())
                    .withAnnotations(ImmutableMap.<String, String>of(
                            "description",
                            "Route for application's https service."))
                .endMetadata()
                .withNewSpec()
                    .withHost("${HOSTNAME_HTTPS}")
                    .withNewTo()
                        .withName("secure-" + ConfigConstants.APP_NAME)
                    .endTo()
                    .withNewTls()
                        .withTermination("passthrough")
                    .endTls()
                .endSpec()
                .endRouteObject()

                .build();
    }

}
