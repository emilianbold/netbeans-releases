/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.avatar_js.project;

import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 */
public class AvatarJSProjectTest extends NbTestCase {
    
    public AvatarJSProjectTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        File pd = new File(getWorkDir(), "pd");
        pd.mkdirs();
        
        File json = new File(pd, "package.json");
        FileWriter w = new FileWriter(json);
        w.write("{\n" +
  "  \"name\": \"chatrooms\",\n" +
  "  \"version\": \"0.0.1\",\n" +
  "  \"description\": \"Minimalist multiroom chat server\",\n" +
  "  \"dependencies\": {\n" +
  "    \"socket.io\": \"~0.9.6\",\n" +
  "    \"mime\": \"~1.2.7\"\n" +
  "  }\n" +
  "}\n");
        w.close();
        
        
    }
    
    public void testRecognizeAsProject() throws Exception {
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        assertNotNull("test root found", fo);
        FileObject pd = fo.getFileObject("pd");
        assertNotNull("child found", pd);
        
        boolean is = ProjectManager.getDefault().isProject(pd);
        assertTrue("It is a project", is);
        
        Project prj = ProjectManager.getDefault().findProject(pd);
        assertNotNull("Project found as there is package.json", prj);
        
        ActionProvider ap = prj.getLookup().lookup(ActionProvider.class);
        List<String> arr = Arrays.asList(ap.getSupportedActions());
        assertTrue(arr.contains(ActionProvider.COMMAND_RUN));
        
        assertFalse("Not enabled, no main file", ap.isActionEnabled(ActionProvider.COMMAND_RUN, prj.getLookup()));
    }
    
    public void testRunCanBeEnabled() throws Exception {
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        FileObject pd = fo.getFileObject("pd");
        Project prj = ProjectManager.getDefault().findProject(pd);
        assertNotNull("Project found as there is package.json", prj);
        
        ActionProvider ap = prj.getLookup().lookup(ActionProvider.class);
        assertFalse("Not enabled, no main file", ap.isActionEnabled(ActionProvider.COMMAND_RUN, prj.getLookup()));
        
        FileObject json = pd.getFileObject("package.json");
        OutputStream os = json.getOutputStream();
        Writer w = new OutputStreamWriter(os);
        w.write("{\n"
                + "  \"name\": \"chatrooms\",\n"
                + "  \"main\": \"run.js\",\n"
                + "  \"version\": \"0.0.1\",\n"
                + "  \"description\": \"Minimalist multiroom chat server\",\n"
                + "  \"dependencies\": {\n"
                + "    \"socket.io\": \"~0.9.6\",\n"
                + "    \"mime\": \"~1.2.7\"\n"
                + "  }\n"
                + "}\n");
        w.close();
        
        assertTrue("Now enabled, main file added", ap.isActionEnabled(ActionProvider.COMMAND_RUN, prj.getLookup()));
    }
    
}
