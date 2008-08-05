/*
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.


The contents of this file are subject to the terms of either the GNU
General Public License Version 2 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://www.netbeans.org/cddl-gplv2.html
or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License file at
nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
particular file as subject to the "Classpath" exception as provided
by Sun in the GPL Version 2 section of the License file that
accompanied this code. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

Contributor(s):

The Original Software is NetBeans. The Initial Developer of the Original
Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
Microsystems, Inc. All Rights Reserved.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
 */

package org.netbeans.test.j2ee;

import java.io.File;
import java.io.IOException;
import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.junit.NbModuleSuite;
import org.openide.filesystems.FileStateInvalidException;

/**
 *
 * @author blaha
 */
public class GenerateDTOTest extends J2eeTestCase {
    private static String beanName = "TestingEntity";
    private static String dtoName = beanName + "DTO";
    
    //
    public File getFile(String fileName) {
        return new File(getDataDir(), EJBValidation.EAR_PROJECT_NAME+"/"+EJBValidation.EAR_PROJECT_NAME+"-ejb/src/java/test/"+fileName);
    }
    
    /** Creates a new instance of GenerateDTOTest */
    public GenerateDTOTest(String name) {
        super(name);
    }
    
    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(GenerateDTOTest.class);
        conf = addServerTests(conf,"testGenerateDTO","testDeleteDTO");
        conf = conf.enableModules(".*").clusters(".*");
        return NbModuleSuite.create(conf);
    }

    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run only selected test case
        TestRunner.run(new GenerateDTOTest("testGenerateDTO"));
        TestRunner.run(new GenerateDTOTest("testDeleteDTO"));
    }
    
    public void testGenerateDTO() throws FileStateInvalidException, IOException {
        Node node = new Node(new ProjectsTabOperator().getProjectRootNode(EJBValidation.EJB_PROJECT_NAME),
                Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbjar.project.ui.Bundle", "LBL_node")
                +"|"+beanName);
        node.performPopupAction(org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.Bundle", "LBL_GenerateDTOAction"));
        deleteDateAndAuthor(); // delete date and author line
        
        assertFile(getFile(dtoName + ".java"), getGoldenFile(), new File(getWorkDir(),"testGenerateDTO.diff"));
    }
    
    
    public void testDeleteDTO(){
        new org.netbeans.jemmy.EventTool().waitNoEvent(5000);
        Node node = new Node(new ProjectsTabOperator().getProjectRootNode(EJBValidation.EJB_PROJECT_NAME),
                Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbjarproject.Bundle", "NAME_src.dir") +
                "|" + "test" + "|" + dtoName + ".java");
        node.performPopupAction(Bundle.getStringTrimmed("org.openide.actions.Bundle", "Delete"));
        new NbDialogOperator(Bundle.getStringTrimmed("org.openide.explorer.Bundle", "MSG_ConfirmDeleteObjectTitle")).yes();
        node.waitNotPresent();
        assertFalse("File " + dtoName + ".java isn't deleted.", getFile(dtoName + ".java").exists()); //check file
    }
    
    private void deleteDateAndAuthor(){
        final EditorOperator editor = EditorWindowOperator.getEditor(dtoName);
        new org.netbeans.jemmy.EventTool().waitNoEvent(3000);
        editor.deleteLine(11); //author
        editor.deleteLine(10); //date
        editor.save();
        editor.close();
    }
    
}
