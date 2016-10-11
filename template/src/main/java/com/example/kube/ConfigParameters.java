package com.example.kube;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Created by swinchester on 17/05/2016.
 */
public class ConfigParameters {


    public static final String APP_NAME = "ocp-simple-war";
    public static final String GROUP_NAME = "com.example";

    public static final String CONFIGMAP_MOUNT_DIR = "/etc/configmap/";
    public static final String CONFIGMAP_NAME = "example-configmap";


    public static final String SECRET_MOUNT_DIR = "/etc/secret/";
    public static final String SECRET_NAME = "example-secret";

    public static final String SERVICE_ACCOUNT_NAME = "jws-service-account";

    public static Map<String, String> getLabels() {
        return ImmutableMap.<String, String> builder()
                .put("application", ConfigParameters.APP_NAME)
                .build();
    }
}
