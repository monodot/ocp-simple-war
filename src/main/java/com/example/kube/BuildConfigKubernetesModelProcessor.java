package com.example.kube;

import io.fabric8.openshift.api.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

    public class BuildConfigKubernetesModelProcessor {

    public void on(TemplateBuilder builder) {
        builder.addNewBuildConfigObject()
                .withNewMetadata()
                    .withName(ConfigConstants.APP_NAME + "-bc")
                    .withLabels(getLabels())
                .endMetadata()
                .withNewSpec()
                    .withTriggers(getTriggers())
                    .withNewSource()
                        .withNewGit()
                            .withUri("${GIT_URI}")
                            .withRef("${SOURCE_REPOSITORY_REF}")
                        .endGit()
                        .withType("Git")
                        .withContextDir("${CONTEXT_DIR}")
                        .withNewSourceSecret()
                            .withName("${SOURCE_SECRET}")
                        .endSourceSecret()
                    .endSource()
                    .withNewStrategy()
                        .withNewSourceStrategy()
                            .withForcePull(true)
                            .addNewEnv()
                                .withName("CONTEXT_DIR")
                                .withValue("${CONTEXT_DIR}")
                            .endEnv()
                            .withNewFrom()
                                .withKind("ImageStreamTag")
                                .withName(ConfigConstants.IS_PULL_NAME + ":" + ConfigConstants.IS_PULL_TAG)
                                .withNamespace(ConfigConstants.IS_PULL_NS)
                            .endFrom()
                        .endSourceStrategy()
                        .withType("Source")
                    .endStrategy()
                    .withNewOutput()
                        .withNewTo()
                            .withKind("ImageStreamTag")
                            .withName(ConfigConstants.APP_NAME + ":${IS_TAG}")
                        .endTo()
                    .endOutput()
                .endSpec()
            .endBuildConfigObject()
            .build();
    }

    private List<BuildTriggerPolicy> getTriggers() {

        List<BuildTriggerPolicy> p = new ArrayList<BuildTriggerPolicy>();

        p.add(new BuildTriggerPolicyBuilder()
                .withType("GitHub")
                .withNewGithub()
                    .withSecret("${GITHUB_WEBHOOK_SECRET}")
                .endGithub()
                .build());

        p.add(new BuildTriggerPolicyBuilder()
                .withType("Generic")
                .withNewGeneric()
                    .withSecret("${GENERIC_WEBHOOK_SECRET}")
                .endGeneric()
                .build());

        p.add(new BuildTriggerPolicyBuilder()
                .withType("ImageChange")
                .withNewImageChange()
                .endImageChange()
                .build());

        p.add(new BuildTriggerPolicyBuilder()
                .withType("ConfigChange")
                .build());


        return p;
    }

    private Map<String, String> getLabels() {
        Map<String, String> labels = new HashMap<String,String>();
        labels.put("app", ConfigConstants.APP_NAME);
        labels.put("project", ConfigConstants.APP_NAME);
        labels.put("version", ConfigConstants.APP_VERSION);
        labels.put("group", ConfigConstants.GROUP_NAME);

        return labels;
    }

}
