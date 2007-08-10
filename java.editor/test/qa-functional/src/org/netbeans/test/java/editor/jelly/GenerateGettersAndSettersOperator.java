/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.test.java.editor.jelly;

import org.netbeans.jemmy.operators.*;
import org.netbeans.modules.java.editor.codegen.GetterSetterGenerator;

/** 
 * @author Jiri Prox
 * @version 1.0
 */
public class GenerateGettersAndSettersOperator extends JDialogOperator {

    /** Creates new GenerateGettersAndSetters that can handle it.
     */
    
    public static final String GETTERS_AND_SETTERS = org.openide.util.NbBundle.getMessage(GetterSetterGenerator.class, "LBL_generate_getter_and_setter"); //NOI18N
    
    public static final String GETTERS_ONLY = org.openide.util.NbBundle.getMessage(GetterSetterGenerator.class, "LBL_generate_getter"); //NOI18N
    
    public static final String SETTERS_ONLY = org.openide.util.NbBundle.getMessage(GetterSetterGenerator.class, "LBL_generate_setter"); //NOI18N
    
    public GenerateGettersAndSettersOperator(String name) {
        super(name);
    }

    private JLabelOperator _lblSelectFieldsToGenerateGettersAndSettersFor;
    private JTreeOperator _treeTreeView$ExplorerTree;
    private JButtonOperator _btGenerate;
    private JButtonOperator _btCancel;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Select fields to generate getters and setters for:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSelectFieldsToGenerateGettersAndSettersFor() {
        if (_lblSelectFieldsToGenerateGettersAndSettersFor==null) {
            _lblSelectFieldsToGenerateGettersAndSettersFor = new JLabelOperator(this, "Select fields to generate getters and setters for:");
        }
        return _lblSelectFieldsToGenerateGettersAndSettersFor;
    }

    /** Tries to find null TreeView$ExplorerTree in this dialog.
     * @return JTreeOperator
     */
    public JTreeOperator treeTreeView$ExplorerTree() {
        if (_treeTreeView$ExplorerTree==null) {
            _treeTreeView$ExplorerTree = new JTreeOperator(this);
        }
        return _treeTreeView$ExplorerTree;
    }

    /** Tries to find "Generate" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btGenerate() {
        if (_btGenerate==null) {
            _btGenerate = new JButtonOperator(this, "Generate");
        }
        return _btGenerate;
    }

    /** Tries to find "Cancel" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCancel() {
        if (_btCancel==null) {
            _btCancel = new JButtonOperator(this, "Cancel");
        }
        return _btCancel;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** clicks on "Generate" JButton
     */
    public void generate() {
        btGenerate().push();
    }

    /** clicks on "Cancel" JButton
     */
    public void cancel() {
        btCancel().push();
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of GenerateGettersAndSetters by accessing all its components.
     */
    public void verify() {
        lblSelectFieldsToGenerateGettersAndSettersFor();
        treeTreeView$ExplorerTree();
        btGenerate();
        btCancel();
    }
   
}

