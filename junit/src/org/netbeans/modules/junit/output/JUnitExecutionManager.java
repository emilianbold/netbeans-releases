/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.junit.output;

import java.io.File;
import java.util.Properties;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.spi.AntSession;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.api.RerunHandler;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SingleMethod;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author answer
 */
public class JUnitExecutionManager implements RerunHandler{
    private File scriptFile = null;
    private String[] targets = null;
    private Properties properties;
    private TestSession testSession;
    private Lookup lookup = Lookup.EMPTY;

    public JUnitExecutionManager(AntSession session, TestSession testSession, Properties props) {
        this.testSession = testSession;
        this.properties = props;
        try{
            scriptFile = session.getOriginatingScript();
            targets = session.getOriginatingTargets();
            //transform known ant targets to the action names
            for(int i=0;i < targets.length; i++){
                if (targets[i].equals("test-single")){                      //NOI18N
                    targets[i] = ActionProvider.COMMAND_TEST_SINGLE;      
                } else if (targets[i].equals("debug-test")){                //NOI18N
                    targets[i] = ActionProvider.COMMAND_DEBUG_TEST_SINGLE;
                }
            }
            
            String javacIncludes = properties.getProperty("javac.includes");//NOI18N
            if (javacIncludes != null){
                FileObject testFO = testSession.getFileLocator().find(javacIncludes);
                lookup = Lookups.fixed(DataObject.find(testFO));
            }

            if (targets.length == 0 ){
                String className = properties.getProperty("classname");     //NOI18N
                String methodName = properties.getProperty("methodname");     //NOI18N
                if (className != null){
                    FileObject testFO = testSession.getFileLocator().find(className.replace('.', '/') + ".java"); //NOI18N
                    if (methodName != null){
                        SingleMethod methodSpec = new SingleMethod(testFO, methodName);
                        lookup = Lookups.singleton(methodSpec);
                    }else{
                        lookup = Lookups.fixed(DataObject.find(testFO));
                    }
                }
                if (scriptFile.getName().equals("junit.xml")){              //NOI18N
                    if (methodName != null){
                        targets = new String[]{SingleMethod.COMMAND_RUN_SINGLE_METHOD};
                    }else{
                        targets = new String[]{ActionProvider.COMMAND_TEST_SINGLE};
                    }
                }else if (scriptFile.getName().equals("junit-debug.xml")){  //NOI18N
                    if (methodName != null){
                        targets = new String[]{SingleMethod.COMMAND_DEBUG_SINGLE_METHOD};
                    }else{
                        targets = new String[]{ActionProvider.COMMAND_DEBUG_TEST_SINGLE};
                    }
                }
            }
        }catch(Exception e){}
    }

    public void rerun() {
        Project project = testSession.getProject();
        ActionProvider actionProvider = project.getLookup().lookup(ActionProvider.class);
        actionProvider.invokeAction(targets[0], lookup);
    }

    public boolean enabled() {
        if ((scriptFile == null) || (targets == null) || (targets.length == 0)){
            return false;
        }

        Project project = testSession.getProject();
        ActionProvider actionProvider = project.getLookup().lookup(ActionProvider.class);
        if (actionProvider != null){
            boolean runSupported = false;
            for (String action : actionProvider.getSupportedActions()) {
                if (action.equals(targets[0])) {
                    runSupported = true;
                    break;
                }
            }
            if (runSupported && actionProvider.isActionEnabled(targets[0], lookup)) {
                return true;
            }
        }

        return false;
    }

    public void addChangeListener(ChangeListener listener) {
    }

    public void removeChangeListener(ChangeListener listener) {
    }

}
