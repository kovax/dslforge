package org.beedom.dslforge;

import org.apache.tools.ant.BuildException
import org.apache.tools.ant.Task
import org.apache.tools.ant.types.FileSet


public class DSLEngineAntTask extends Task {

    public DSLEngineAntTask() {
        super();
    }

    String configFile;
    String configEnv;
    
    //org.apache.tools.ant.types.FileSet fileset

    public void execute() {
        /*if (!fileset) {
            throw new BuildException("No fileset was specified.");
        }*/

        def dsle = new DSLEngine(configFile, configEnv)
    }
}
