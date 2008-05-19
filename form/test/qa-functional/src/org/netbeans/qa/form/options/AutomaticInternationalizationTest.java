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

import org.netbeans.qa.form.*;
import org.netbeans.qa.form.visualDevelopment.*;
import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jellytools.actions.*;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.qa.form.ExtJellyTestCase;
import java.util.*;

/**
 * Automatic internationalization test
 *
 * @author Jiri Vagner
 */
public class AutomaticInternationalizationTest extends ExtJellyTestCase {
    
    /** Constructor required by JUnit */
    public AutomaticInternationalizationTest(String testName) {
        super(testName);
    }
    
    /** Method allowing to execute test directly from IDE. */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /** Creates suite from particular test cases. */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        
        suite.addTest(new AutomaticInternationalizationTest("testAutomaticInternationalizationEnabled")); // NOI18N
        suite.addTest(new AutomaticInternationalizationTest("testAutomaticInternationalizationDisabled")); // NOI18N
        
        return suite;
    }
    
    /**
     *  Tests component code with properties Automatic Internationalization = true
     */
    public void testAutomaticInternationalizationEnabled() {
        testAutomaticInternationalization(true);
    }

    /**
     *  Tests component code with properties Automatic Internationalization = false
     */
    public void testAutomaticInternationalizationDisabled() {
        testAutomaticInternationalization(false);
    }

    /**
     * Tests component code with different value of properties Automatic Internationalization
     * 
     * @param local "Automatic Internationalization" settings 
     */
    private void testAutomaticInternationalization(Boolean enabled) {
        OptionsOperator.invoke();
        //add timeout
        waitNoEvent(2000);
        
        OptionsOperator options = new OptionsOperator();
        options.switchToClassicView();
        //add timeout
        waitNoEvent(2000);
        
        options.selectOption("Editing|GUI Builder"); // NOI18N
        //add timeout
        waitNoEvent(2000);

        Property property = new Property(options.getPropertySheet("Editing|GUI Builder"), "Automatic Resource Management"); // NOI18N
        property.setValue(String.valueOf( enabled ? "On" : "Off"));
        options.close();
        //add timeout
        waitNoEvent(2000);        

        String name = createJFrameFile();        
        FormDesignerOperator designer = new FormDesignerOperator(name);
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        Node node = new Node(inspector.treeComponents(), "JFrame"); // NOI18N
        
        runPopupOverNode("Add From Palette|Swing Controls|Button", node); // NOI18N
        
        String baseName = "[JFrame]"; // NOI18N
        Node dialogNode = new Node(inspector.treeComponents(), baseName);
        String[] names = dialogNode.getChildren();
        
        inspector.selectComponent("[JFrame]|jButton1");
            
        Property prop = new Property(inspector.properties(), "text"); // NOI18N
        prop.setValue("Lancia Lybra");
        
        if (enabled)
            findInCode("jButton1.setText(bundle.getString(\"MyJFrame", designer);
        else
            findInCode("jButton1.setText(\"Lancia Lybra\");", designer);
        
        removeFile(name);
    }
}