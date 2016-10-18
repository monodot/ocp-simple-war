package com.example.kube;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Created by swinchester on 17/05/2016.
 */
public class ConfigConstants {


    public static final String APP_NAME = "ocp-simple-war";
    public static final String APP_VERSION = "1.0.0-SNAPSHOT";
    public static final String GROUP_NAME = "com.example";

    public static final String CONFIGMAP_MOUNT_DIR = "/etc/configmap/";
    public static final String CONFIGMAP_NAME = "example-configmap";

    public static final String IS_PULL_NAME = "webserver30-tomcat8-appdynamics";
    public static final String IS_PULL_TAG = "latest";
    public static final String IS_PULL_NS = "build";

    public static final String SECRET_VOLUME_MOUNT_PATH = "/etc/jws-secret-volume";
    public static final String SECRET_NAME = "jws-app-secret";
    public static final String SECRET_VOLUME_MOUNT_NAME = "jws-certificate-volume";

    public static final String SERVICE_ACCOUNT_NAME = "jws-service-account";

    public static final int JWS_HTTP_PORT = 8080;
    public static final int JWS_HTTPS_PORT = 8443;
    public static final int MYSQL_PORT = 3306;
    public static final int JOLOKIA_PORT = 8778;

    public static final String PROTOCOL_TCP = "TCP";

    public static Map<String, String> getLabels() {
        return ImmutableMap.<String, String> builder()
                .put("application", ConfigConstants.APP_NAME)
                .build();
    }
}
