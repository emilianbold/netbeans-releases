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
package org.netbeans.jellytools;

import org.netbeans.jellytools.actions.SaveAsTemplateAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

/**
 * Handle "Save As Template" dialog. It can be invoked on a node from popup
 * menu.
*/
public class SaveAsTemplateOperator extends NbDialogOperator {

    /** Components operators. */
    private JTreeOperator _tree;
    private JLabelOperator _lblSelectTheCategory;
    
    /** Creates new instance of SaveAsTemplateOperator. It waits for dialog
     * with title "Save As Template".
     */
    public SaveAsTemplateOperator() {
        super(Bundle.getString("org.openide.loaders.Bundle", "Title_SaveAsTemplate"));
    }
    
    /** Invokes Save As Template dialog on specified nodes.
     * @param nodes array of nodes to select before action call
     * @return  instance of SaveAsTemplateOperator
     */
    public static SaveAsTemplateOperator invoke(Node[] nodes) {
        new SaveAsTemplateAction().perform(nodes);
        return new SaveAsTemplateOperator();
    }
    
    /** Invokes Save As Template dialog on specified node.
     * @param node node to select before action call
     * @return  instance of SaveAsTemplateOperator
     */
    public static SaveAsTemplateOperator invoke(Node node) {
        return invoke(new Node[] {node});
    }
    
    /** Returns operator of templates tree.
     * @return  JTreeOperator instance of templates tree
     */
    public JTreeOperator tree() {
        if(_tree == null) {
            _tree = new JTreeOperator(this);
        }
        return _tree;
    }
    
    /** Returns operator of "Select the category..." label.
     * @return  JLabelOperator instance of "Select the category..." label
     */
    public JLabelOperator lblSelectTheCategory() {
        if (_lblSelectTheCategory == null) {
            _lblSelectTheCategory = new JLabelOperator(this,
                                  Bundle.getString("org.openide.loaders.Bundle",
                                                   "CTL_SaveAsTemplate"));
        }
        return _lblSelectTheCategory;
    }
    
    /** Returns root node of templates tree.
     * @return  Node instance of root node of templates tree
     */
    public Node getRootNode() {
        return new Node(tree(), "");
    }
    
    /** Selects given template in templates tree.
     * @param templatePath path to template (e.g. Classes|Main)
     */
    public void selectTemplate(String templatePath) {
        if(templatePath == null) {
            throw new JemmyException("Cannot accept null parameter");
        }
        new Node(tree(), templatePath).select();
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        btOK();
        btCancel();
        lblSelectTheCategory();
        tree();
    }
    
}
