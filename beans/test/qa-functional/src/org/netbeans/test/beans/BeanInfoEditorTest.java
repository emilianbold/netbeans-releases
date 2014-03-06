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

package org.netbeans.test.beans;

import java.awt.Component;
import java.awt.Container;
import javax.swing.tree.TreeModel;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.AbstractButtonOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author jprox
 */
public class BeanInfoEditorTest extends BeansTestCase {

    public BeanInfoEditorTest(String testName) {
        super(testName);
    }
    
    private EditorOperator editor;
    
    public void testBI() {
        editor = openEditor("beans", "SourceBeanInfo");
        AbstractButtonOperator toolbarButton = editor.getToolbarButton("Designer View");
        toolbarButton.pushNoBlock();
        new EventTool().waitNoEvent(2000);
        editor = new EditorOperator("SourceBeanInfo");
        printAllComponents(editor);
        JTreeOperator tree = new JTreeOperator(editor);
        System.out.println("****************************************TREE****");
        browseTree(tree.getRoot(),tree.getModel(),"");
    }
    public void printAllComponents(Operator comp) {
        System.out.println("**************************");
        printComp((Component) comp.getSource(), "");
        System.out.println("**************************");
    }

    public void printComp(Component c, String s) {        
        System.out.println(s + c.getClass().getName());
        if (c instanceof Container) {
            for (Component com : ((Container)c).getComponents()) {
                printComp((Container) com, s + "__");
            }
        }
    }
    
    private void browseTree(Object root, TreeModel model, String string) {
        System.out.println(string + root.getClass().getName());
        int childCount = model.getChildCount(root);
        for (int i = 0; i < childCount; i++) {
            browseTree(model.getChild(root, i), model, string+"  ");
            
        }        
    }
    
    /*
    testEditing in beaninfo
    test change source file - add to bi
    test creating bi
    
    
    */
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(BeanInfoEditorTest.class)
                .enableModules(".*")
                .clusters(".*"));
    }

    
}
