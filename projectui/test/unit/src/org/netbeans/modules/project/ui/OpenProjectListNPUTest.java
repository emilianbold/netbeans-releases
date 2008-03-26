/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.project.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.Log;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ui.actions.TestSupport;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.lookup.Lookups;

/** Tests fix of issue 56454.
 *
 * @author Jiri Rechtacek
 */
public class OpenProjectListNPUTest extends NbTestCase {
    FileObject f1_1_open, f1_2_open, f1_3_close;
    FileObject f2_1_open;

    Project project1, project2;

    public OpenProjectListNPUTest (String testName) {
        super (testName);
    }
    
    protected boolean runInEQ() {
        return true;
    }

    protected void setUp () throws Exception {
        super.setUp ();
        MockServices.setServices(TestSupport.TestProjectFactory.class);
        clearWorkDir ();
        
        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
    
        FileObject p1 = TestSupport.createTestProject (workDir, "project1");
        f1_1_open = p1.createData("f1_1.java");
        f1_2_open = p1.createData("f1_2.java");
        f1_3_close = p1.createData("f1_3.java");

        project1 = ProjectManager.getDefault ().findProject (p1);
        OpenProjectList.getDefault().getTemplatesLRU(project1);
        
        ((TestSupport.TestProject) project1).setLookup (Lookups.singleton (TestSupport.createAuxiliaryConfiguration ()));
        
        FileObject p2 = TestSupport.createTestProject (workDir, "project2");
        f2_1_open = p2.createData ("f2_1.java");

        // project2 depends on projects1
        project2 = ProjectManager.getDefault ().findProject (p2);
        ((TestSupport.TestProject) project2).setLookup(Lookups.fixed(TestSupport.createAuxiliaryConfiguration()));
        
        // prepare set of open documents for both projects
        ProjectUtilities.OPEN_CLOSE_PROJECT_DOCUMENT_IMPL.open (f1_1_open);
        ProjectUtilities.OPEN_CLOSE_PROJECT_DOCUMENT_IMPL.open (f1_2_open);
        ProjectUtilities.OPEN_CLOSE_PROJECT_DOCUMENT_IMPL.open (f2_1_open);
        
        // close both projects with own open files
        OpenProjectList.getDefault().close(new Project[] {project1, project2}, false);
    }
    
    protected void tearDown () {
        OpenProjectList.getDefault().close(new Project[] {project1, project2}, false);
    }


    public void testOpen () throws Exception {
        OpenProjectList.getDefault().getTemplatesLRU(project1);
        
        
        assertTrue ("No project is open.", OpenProjectList.getDefault ().getOpenProjects ().length == 0);
        CharSequence log = Log.enable("org.netbeans.ui", Level.FINE);
        OpenProjectList.getDefault ().open (project1, true);        
        assertTrue ("Project1 is opened.", OpenProjectList.getDefault ().isOpen (project1));
        Pattern p = Pattern.compile("Opening.*1.*TestProject", Pattern.MULTILINE | Pattern.DOTALL);
        Matcher m = p.matcher(log);
        if (!m.find()) {
            fail("There should be TestProject\n" + log.toString());
        }
    }
}
