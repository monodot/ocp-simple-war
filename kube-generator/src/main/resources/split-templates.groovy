import groovy.json.*

def jsonText = new File("${project.build.directory}/classes/kubernetes.json").text
def thing = new JsonSlurper().parseText(jsonText)

def buildObjects = []
def deployObjects = []

def buildTemplate = thing.getClass().newInstance(thing)
def deployTemplate = thing.getClass().newInstance(thing)

thing.objects.each() {

    if(it.kind == 'BuildConfig' || it.kind == 'ImageStream'){
        buildObjects.add(it)

    }else{
        deployObjects.add(it)
    }
}

buildTemplate.objects = buildObjects
deployTemplate.objects = deployObjects

buildTemplate.metadata.name = "$templateName"
deployTemplate.metadata.name = "$templateName"

def buildTemplateFile = new File("${project.build.directory}/classes/kubernetes-build.json")
buildTemplateFile << JsonOutput.prettyPrint(JsonOutput.toJson(buildTemplate))
def runTemplateFile = new File("${project.build.directory}/classes/kubernetes-run.json")
runTemplateFile << JsonOutput.prettyPrint(JsonOutput.toJson(deployTemplate))