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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.iep.editor.ps;

import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OperatorComponent;

import java.awt.BorderLayout;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.tree.TreeModel;
import org.openide.util.NbBundle;

/**
 * InputSchemaTreePanel.java
 * 
 * Created on November 1, 2006, 1:52 PM
 * 
 * 
 * 
 * @author Bing Lu
 */
public class InputSchemaTreePanel extends JPanel {
    private InputSchemaTree mTree;
    
    public InputSchemaTreePanel(IEPModel model, OperatorComponent component) {
        String msg = NbBundle.getMessage(InputSchemaTreePanel.class, "InputSchemaTreePanel.INPUTS");
        setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), msg, TitledBorder.LEFT, TitledBorder.TOP));
        setLayout(new BorderLayout(5, 5));
        JScrollPane scrollPane = new JScrollPane();
        add(scrollPane, BorderLayout.CENTER);
        mTree = new InputSchemaTree(model, component);
        scrollPane.getViewport().add(mTree);
    }
    
    public InputSchemaTreePanel(IEPModel model, TreeModel treeModel) {
        String msg = NbBundle.getMessage(InputSchemaTreePanel.class, "InputSchemaTreePanel.INPUTS");
        setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), msg, TitledBorder.LEFT, TitledBorder.TOP));
        setLayout(new BorderLayout(5, 5));
        JScrollPane scrollPane = new JScrollPane();
        add(scrollPane, BorderLayout.CENTER);
        mTree = new InputSchemaTree(model, treeModel);
        scrollPane.getViewport().add(mTree);
    }
    
    public void setInputSchemaTreeModel(TreeModel model) {
        mTree.setModel(model);
    }
}
