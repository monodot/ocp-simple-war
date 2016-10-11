import groovy.json.*

def jsonText = new File("${project.build.directory}/classes/kubernetes.json").text
def kubeMap = new JsonSlurper().parseText(jsonText)

def buildObjects = []
def deployObjects = []

def buildTemplate = kubeMap.getClass().newInstance(kubeMap)
def deployTemplate = kubeMap.getClass().newInstance(kubeMap)

kubeMap.objects.each() {

    if(it.kind == 'BuildConfig' || it.kind == 'ImageStream'){
        buildObjects.add(it)

    }else{
        deployObjects.add(it)
    }
}

def buildParams = ['REGISTRY','IS_PULL_NAMESPACE', 'IS_TAG', 'GIT_URI']
def removeFromDeployParams = ['GIT_URI']

buildTemplate.objects = buildObjects
deployTemplate.objects = deployObjects

def buildParamList = kubeMap.parameters.findAll{ buildParams.contains(it.name) == true }
def deployParamList = kubeMap.parameters.findAll{ removeFromDeployParams.contains(it.name) != true }

buildTemplate.parameters = buildParamList
deployTemplate.parameters = deployParamList

def buildTemplateFile = new File("${project.build.directory}/classes/kubernetes-build.json")
buildTemplateFile << JsonOutput.prettyPrint(JsonOutput.toJson(buildTemplate))
def runTemplateFile = new File("${project.build.directory}/classes/kubernetes-run.json")
runTemplateFile << JsonOutput.prettyPrint(JsonOutput.toJson(deployTemplate))