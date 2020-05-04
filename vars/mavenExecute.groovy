import com.sap.piper.DownloadCacheUtils
import groovy.transform.Field

import static com.sap.piper.Prerequisites.checkScript

@Field def STEP_NAME = getClass().getName()
@Field String METADATA_FILE = 'metadata/mavenExecute.yaml'

def call(Map parameters = [:]) {
    final script = checkScript(this, parameters) ?: this
    parameters = DownloadCacheUtils.injectDownloadCacheInMavenParameters(script, parameters)

    //todo null check

    // todos
    // input as string (convert to list?)
    // input is shell escaped which was required before but not now
    // -Dfoo.bar='a b c '
    // [ '-f', "'my path/pom.xml'"]


    // legacy handling
    // the old step allowed passing defines, flags and goals as string or list
    // the new step only allows lists

    if (!parameters.defines in List) {
        error "Expected parameters.defines ${parameters.defines} to be of type List, but it is ${parameters.defines.class}." //hint about bash escaping
    }
    if (!parameters.flags in List) {
        error "Expected parameters.flags ${parameters.flags} to be of type List, but it is ${parameters.flags.class}."
    }
    if (!parameters.goals in List) {
        error "Expected parameters.goals ${parameters.goals} to be of type List, but it is ${parameters.goals.class}."
    }



    List credentials = []
    piperExecuteBin(parameters, STEP_NAME, METADATA_FILE, credentials)

    String output = ''
    if (parameters.returnStdout) {
        String outputFile = '.pipeline/maven_output.txt'
        if (!fileExists(outputFile)) {
            error "[$STEP_NAME] Internal error. A text file with the contents of the maven output was expected " +
                "but does not exist at '$outputFile'. " +
                "Please file a ticket at https://github.com/SAP/jenkins-library/issues/new/choose"
        }
        output = readFile(outputFile)
    }
    return output
}
