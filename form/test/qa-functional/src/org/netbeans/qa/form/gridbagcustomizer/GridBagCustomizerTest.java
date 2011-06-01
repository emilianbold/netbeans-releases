/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.qa.form.gridbagcustomizer;

import junit.framework.Test;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.EditAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.qa.form.ExtJellyTestCase;

/**
 *
 * @author Adam Senk adam.senk[at]oracle.com
 * 
 * This test is testing new Grid Bag Layout customizer, that is included since NB 7.0
 */
public class GridBagCustomizerTest extends ExtJellyTestCase {
    
    public String FILE_NAME = "clear_JFrame";
    public String PACKAGE_NAME = "data";
    public String DATA_PROJECT_NAME = "SampleProject";
    public String FRAME_ROOT = "[JFrame]";
    
    public GridBagCustomizerTest(String testName){
        super(testName);
    }
    
     public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(GridBagCustomizerTest.class).addTest(
                "testInvokeAndClose"
                ).gui(true).clusters(".*").enableModules(".*"));

    }
     
     public void testInvokeAndClose(){
         
        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(DATA_PROJECT_NAME);
        prn.select();
        Node formnode = new Node(prn, "Source Packages|" + PACKAGE_NAME + "|" + FILE_NAME);
        formnode.select();
        log("Form node selected.");

        EditAction editAction = new EditAction();
        editAction.perform(formnode);
        log("Source Editor window opened.");

        OpenAction openAction = new OpenAction();
        openAction.perform(formnode);
        log("Form Editor window opened.");
        
        FormDesignerOperator designer = new FormDesignerOperator(FILE_NAME);
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        
        Node actNode = new Node(inspector.treeComponents(), "[JFrame]"); // NOI18N
        
        runPopupOverNode("Set Layout|Grid Bag Layout", actNode);
        runPopupOverNode("Customize Layout...", actNode);
        
        CustomizeLayoutOperator clo=new CustomizeLayoutOperator();
        clo.btCancel().push();
     }
    
}
