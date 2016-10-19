# ocp-simple-war

Simple WAR demo for OpenShift Container Platform.  This has custom s2i scripts, and a Java project which generates a kubernetes template which will allow you to build/deploy the [OpenShift QuickStarts Tomcat-Jdbc App](https://github.com/jboss-openshift/openshift-quickstarts/tree/master/tomcat-jdbc)

## s2i scripts

The s2i scripts in this project:

1.  assemble - has been extended to push in a new "launch.sh" script with a new "inject_environmentvariables" function, this is in order to add environment variables into the context.xml
2.  run - has been extended to append the JAVA_OPTS for the appdynamics agent

## Binary Deployments - How it works...

In the application BuildConfig you can supply an ENVIRONMENT Variable "ARTIFACT_URL" which specifies where to get the war file from.  This is curled into the deployment directory.

See the [Binary Deployments Blog](https://blog.openshift.com/binary-deployments-openshift-3/) for more details

```
if [ x"$ARTIFACT_URL" != "x" ]; then
    echo "grabbing the artifact $ARTIFACT_URL"
    curl -o ${DEPLOY_DIR}/ROOT.war -O ${ARTIFACT_URL}
    ...
```


### Adding JNDI environment variables...  How it works...

1.  The new .s2i/configuration/context.xml is copied into the container (with the <!-- ##ENVIRONMENT## --> tag).
2.  The scipts/launch.sh has been overrriden to ensure any supplied JNDI varaibles are populated based on the ENVIRONMENT variables passed to the container

Expected format (example):
```

JNDI_VAR_CLIENTCERTPATH_KEY="TSFwdClientCertPwdFilePath"
JNDI_VAR_CLIENTCERTPATH_VALUE="/opt/certs"
JNDI_VAR_CLIENTCERTPATH_TYPE="java.lang.String"


JNDI_VAR_ANOTHERKEY_KEY="SomeOtherString"
JNDI_VAR_ANOTHERKEY_VALUE="SomeOtherValue"
JNDI_VAR_ANOTHERKEY_TYPE="java.lang.String"

### will get injected into the context.xml:

 <Environment name="TSFwdClientCertPwdFilePath" value="/opt/certs" type="java.lang.String" />
 <Environment name="SomeOtherString" value="SomeOtherValue" type="java.lang.String" />

```

