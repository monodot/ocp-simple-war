package com.example.kube;

import io.fabric8.kubernetes.generator.annotation.KubernetesModelProcessor;
import io.fabric8.openshift.api.model.DeploymentConfigBuilder;
import io.fabric8.openshift.api.model.ImageStreamBuilder;
import io.fabric8.openshift.api.model.TemplateBuilder;

@KubernetesModelProcessor
public class MainKubernetesModelProcessor {

    public void withTemplateBuilder(TemplateBuilder builder) {

        new BuildConfigKubernetesModelProcessor().on(builder);
        new ImageStreamKubernetesModelProcessor().on(builder);
        new DeploymentConfigKubernetesModelProcessor().on(builder);
        new ServiceAccountKubernetesProcessor().onTemplate(builder);
        new ServiceKubernetesModelProcessor().on(builder);
        new RouteKubernetesModelProcessor().on(builder);
    }
}
