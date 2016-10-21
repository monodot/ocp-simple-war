package com.example.kube;

import io.fabric8.kubernetes.api.model.ObjectReference;
import io.fabric8.openshift.api.model.ImageStreamBuilder;
import io.fabric8.openshift.api.model.TagReference;
import io.fabric8.openshift.api.model.TemplateBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageStreamKubernetesModelProcessor {

    public void on(ImageStreamBuilder builder) {
        builder.withSpec(builder.getSpec())
                .withNewMetadata()
                    .withName(ConfigConstants.APP_NAME)
                .endMetadata()
                .withNewSpec()
                    .withTags(getTags())
                .endSpec()
            .build();
    }

    public void on(TemplateBuilder builder) {
        builder.addNewImageStreamObject()
                .withNewMetadata()
                .withName(ConfigConstants.APP_NAME)
                .endMetadata()
                .withNewSpec()
                .withTags(getTags())
                .endSpec()
                .endImageStreamObject()
                .build();
    }


    private List<TagReference> getTags() {
        ObjectReference fromLatest = new ObjectReference();
        fromLatest.setName(ConfigConstants.APP_NAME);
        fromLatest.setKind("ImageStreamTag");

        Map<String, String> latestAnnotations = new HashMap<String, String>();
        latestAnnotations.put("tags", "${IS_TAG}");

        TagReference latest = new TagReference();
        latest.setName("${IS_TAG}");
        latest.setFrom(fromLatest);
        latest.setAnnotations(latestAnnotations);

        List<TagReference> tags = new ArrayList<TagReference>();
        tags.add(latest);

        return tags;
    }
}
