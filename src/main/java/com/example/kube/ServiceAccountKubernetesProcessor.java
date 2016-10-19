package com.example.kube;

import io.fabric8.openshift.api.model.TemplateBuilder;

/**
 * Created by swinchester on 11/10/2016.
 */
public class ServiceAccountKubernetesProcessor {

    public void onTemplate(TemplateBuilder builder) {
        builder.addNewServiceAccountObject()
                .withNewMetadata().withName(ConfigConstants.SERVICE_ACCOUNT_NAME).endMetadata()
                .endServiceAccountObject().build();
    }
}
