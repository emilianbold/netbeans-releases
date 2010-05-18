/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.qa.form.binding;

import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jellytools.actions.*;
import org.netbeans.jellytools.*;
import org.netbeans.qa.form.ExtJellyTestCase;
import org.netbeans.jellytools.nodes.Node;
import java.util.*;
import org.netbeans.qa.form.BindDialogOperator;

/**
 * Beans Binding basic test
 *
 * @author Jiri Vagner
 */
public class SimpleBeansBinding extends ExtJellyTestCase {
    
    /** Constructor required by JUnit */
    public SimpleBeansBinding(String testName) {
        super(testName);
    }
    
    /* Method allowing to execute test directly from IDE. */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /** Creates suite from particular test cases. */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new SimpleBeansBinding("testSimpleBeansBinding")); // NOI18N
        return suite;
    }
    
    /** Tests basic beans binding features */
    public void testSimpleBeansBinding() {
        String jLabel1NodePath = "[JFrame]|jLabel1 [JLabel]";  // NOI18N
        String jLabel2NodePath = "[JFrame]|jLabel2 [JLabel]";  // NOI18N
        String actionPath = "Bind|text";  // NOI18N
        String bindSource = "jLabel2";  // NOI18N
        String bindExpression = "${text}";  // NOI18N
        
        // create frame
        String frameName = createJFrameFile();
        FormDesignerOperator designer = new FormDesignerOperator(frameName);
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        
        // add two labels
        Node actNode = new Node(inspector.treeComponents(), "JFrame"); // NOI18N
        String itemPath = "Add From Palette|Swing Controls|Label"; // NOI18N
        runPopupOverNode(itemPath, actNode);
        runPopupOverNode(itemPath, actNode);

        // invoke bind dialog
        actNode = new Node(inspector.treeComponents(), jLabel1NodePath);
        Action act = new ActionNoBlock(null, actionPath);
        act.perform(actNode);
        
        // bind jlabel1.text with jlabel2.text
        BindDialogOperator bindOp = new BindDialogOperator();
        bindOp.selectBindSource(bindSource);
        bindOp.setBindExpression(bindExpression);
        bindOp.ok();
        
        // invoke bind dialog again ...
        actNode = new Node(inspector.treeComponents(), jLabel1NodePath);
        act = new ActionNoBlock(null, actionPath);
        act.perform(actNode);
        
        // ... and check the values in binding dialog
        bindOp = new BindDialogOperator();
        assertEquals(bindOp.getSelectedBindSource(), bindSource);
        assertEquals(bindOp.getBindExpression(), bindExpression);
        bindOp.ok();

        // check generated binding code
        findInCode("createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, jLabel2, org.jdesktop.beansbinding.ELProperty.create(\"${text}\"), jLabel1, org.jdesktop.beansbinding.BeanProperty.create(\"text\"));", designer);  // NOI18N
        findInCode("bindingGroup.bind();", designer);  // NOI18N

        // get values of text properties of jLabels and test them
        assertEquals(ExtJellyTestCase.getTextValueOfLabel(inspector, jLabel1NodePath),
                ExtJellyTestCase.getTextValueOfLabel(inspector, jLabel2NodePath));
    }
}
