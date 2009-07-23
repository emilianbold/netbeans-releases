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

package org.netbeans.qa.form.options;

import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.qa.form.ExtJellyTestCase;

/**
 * Componentes declaration test
 *
 * @author Jiri Vagner
 */
public class GeneratedComponentsDestionationTest extends ExtJellyTestCase {
    
    /** Constructor required by JUnit */
    public GeneratedComponentsDestionationTest(String testName) {
        super(testName);
    }
    
    /** Method allowing to execute test directly from IDE. */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /** Creates suite from particular test cases. */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        
        suite.addTest(new GeneratedComponentsDestionationTest("testGeneratedComponentsDestionationLocal")); // NOI18N
        suite.addTest(new GeneratedComponentsDestionationTest("testGeneratedComponentsDestionationClassField")); // NOI18N
        
        return suite;
    }
    
    /** Tests generation component declaration code with properties LocalVariables=true
     * Test for issue 95518
     */
    public void testGeneratedComponentsDestionationLocal() {
        testGeneratedComponentsDestionation(true);
    }

    /**
     * Tests generation component declaration code with properties LocalVariables=false
     */
    public void testGeneratedComponentsDestionationClassField() {
        testGeneratedComponentsDestionation(false);
    }

    /**
     * Tests generation component declaration code with properties LocalVariables=false
     * 
     * @param local "Local Variables" settings 
     */
    private void testGeneratedComponentsDestionation(Boolean local) {
        OptionsOperator.invoke();
        OptionsOperator options = new OptionsOperator();
//        options.switchToClassicView();
        waitAMoment();
        
        options.selectOption("Editing|GUI Builder"); // NOI18N
        waitAMoment();        

        Property property = new Property(options.getPropertySheet("Editing|GUI Builder"), "Variables Modifier"); // NOI18N
        property.setValue("private"); // NOI18N
        
        property = new Property(options.getPropertySheet("Editing|GUI Builder"), "Local Variables"); // NOI18N
        property.setValue(String.valueOf(local));
        options.close();
        waitAMoment();        
        
        String name = createJFrameFile();
        waitAMoment();        
        
        FormDesignerOperator designer = new FormDesignerOperator(name);
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        Node node = new Node(inspector.treeComponents(), "JFrame"); // NOI18N
        
        runPopupOverNode("Add From Palette|Swing Controls|Label", node); // NOI18N
        waitAMoment();        
        
        String code = "private javax.swing.JLabel jLabel1";  // NOI18N
        if (local)
            missInCode(code, designer);
        else
            findInCode(code, designer);
        
        waitAMoment();        
        removeFile(name);
    }
}