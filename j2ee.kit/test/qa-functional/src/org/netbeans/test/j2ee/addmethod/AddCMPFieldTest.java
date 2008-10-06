/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.test.j2ee.addmethod;

import java.io.IOException;
import javax.swing.JTextField;
import org.netbeans.jellytools.*;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jellytools.modules.java.editor.GenerateCodeOperator;

/**
 *
 * @author lm97939
 */
public class AddCMPFieldTest extends AddMethodBase {
    
    protected String methodName;
    protected String returnType;
    private String description;
    private Boolean localGetter;
    private Boolean localSetter;
    private Boolean remoteGetter;
    private Boolean remoteSetter;
    
    /** Creates a new instance of AddMethodTest */
    public AddCMPFieldTest(String name) {
        super(name);
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run only selected test case
        junit.textui.TestRunner.run(new AddCMPFieldTest("testAddCMPField1InEB"));
    }
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        System.out.println("########  "+getName()+"  #######");
    }
    
    public void testAddCMPField1InEB()  throws IOException{
        beanName = "TestingEntity";
        editorPopup = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.Bundle", "LBL_AddCmpFieldAction");
        methodName = "cmpTestField1x";
        description = null;
        returnType = "String";
        isDDModified = true;
        saveFile = true;
        addMethod();
    }

    public void testAddCMPField2InEB()  throws IOException{
        beanName = "TestingEntity";
        editorPopup = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.Bundle", "LBL_AddCmpFieldAction");
        methodName = "cmpTestField2x";
        description = null; //"Test Field";
        returnType = "int";
        localGetter = Boolean.TRUE;
        localSetter = Boolean.FALSE; 
        remoteGetter = Boolean.TRUE;
        remoteSetter = Boolean.TRUE; 
        isDDModified = true;
        saveFile = true;
        addMethod();
    }
    
    protected void addMethod() throws IOException {
        EditorOperator editor = EditorWindowOperator.getEditor(beanName+"Bean.java");
        editor.select(11);

        // invoke Add Business Method dialog
        GenerateCodeOperator.openDialog(editorPopup, editor);
        AddCMPFieldDialog dialog = new AddCMPFieldDialog();
        JLabelOperator lblOper = new JLabelOperator(dialog, "Name");
        new JTextFieldOperator((JTextField)lblOper.getLabelFor()).setText(methodName);
        if (description != null) {
            dialog.setDescription(description);
            lblOper = new JLabelOperator(dialog, "Description");
            new JTextFieldOperator((JTextField)lblOper.getLabelFor()).setText(description);
        }
        
        lblOper = new JLabelOperator(dialog, "Type");
        new JTextFieldOperator((JTextField)lblOper.getLabelFor()).setText(returnType);
                
        if (localGetter != null)
            dialog.checkLocalGetter(localGetter.booleanValue());
        if (localSetter != null)
            dialog.checkLocalSetter(localSetter.booleanValue());
        if (remoteGetter != null)
            dialog.checkRemoteGetter(remoteGetter.booleanValue());
        if (remoteSetter != null)
            dialog.checkRemoteSetter(remoteSetter.booleanValue());
        dialog.ok();
        
        if (saveFile) 
            editor.save();
        
        waitForEditorText(editor, methodName);
        
        compareFiles();
    }
    
}
