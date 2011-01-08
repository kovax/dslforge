/*
* Copyright 2003-2011 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.beedom.dslforge.integrations


import org.apache.tools.ant.BuildException
import org.apache.tools.ant.Task
import org.apache.tools.ant.types.FileSet
import org.beedom.dslforge.DSLEngine;


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
