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

package org.netbeans.jellytools.modules.editor;

import javax.swing.ComboBoxModel;
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.HelpOperator;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

/**
 *
 * @author Jiri Prox Jiri.Prox@Sun.COM
 */
public class KeyMapOperator extends JDialogOperator{
    
    
    private static final String DUPLICATE_BUTTON  = "Duplicate";
    private static final String RESTORE_BUTTON  = "Restore";
    private static final String DELETE_BUTTON  = "Delete";
    private static final String ADD_BUTTON  = "Add ...";
    private static final String REMOVE_BUTTON  = "Remove";
    
    private JComboBoxOperator profile;
    private JButtonOperator duplicate;
    private JButtonOperator restore;
    private JButtonOperator delete;
    private JTreeOperator actions;
    private JListOperator shortcuts;
    private JButtonOperator add;
    private JButtonOperator remove;
    
    private static OptionsOperator options;
    
    /** Creates a new instance of KeyMapOperator */
    public KeyMapOperator() {
        super("Options");
    }
    
    public static KeyMapOperator invoke() {
        options = OptionsOperator.invoke();
        options.selectKeymap();        
        new EventTool().waitNoEvent(500);                        
        return new KeyMapOperator();
    }
    
    public JComboBoxOperator profile() {
        if(profile==null) {
            profile = new JComboBoxOperator(this);
        }
        return profile;
    }
    
    public JButtonOperator duplicate() {
        if(duplicate==null) {
            duplicate = new JButtonOperator(this, DUPLICATE_BUTTON);
        }
        return duplicate;
    }
    
    public JButtonOperator delete() {
        if(delete==null) {
            delete = new JButtonOperator(this, DELETE_BUTTON);
        }
        return delete;
    }
    
    public JButtonOperator restore() {
        if(restore==null) {
            restore = new JButtonOperator(this, RESTORE_BUTTON);
        }
        return restore;
    }
    
    public JTreeOperator actions() {
        if(actions==null) {
            actions = new JTreeOperator(this);
        }
        return actions;
    }
    
    public JListOperator shortcuts() {
        if(shortcuts==null) {
            shortcuts = new JListOperator(this);
        }
        return shortcuts;
    }
    
    public JButtonOperator add() {
        if(add==null) {
            add = new JButtonOperator(this, ADD_BUTTON);
        }
        return add;
    }
    
    public JButtonOperator remove() {
        if(remove==null) {
            remove = new JButtonOperator(this, REMOVE_BUTTON);
        }
        return remove;
    }
    
    public JButtonOperator ok() {
        return options.btOK();
    }
    
    public JButtonOperator cancel() {
        return options.btCancel();
    }
    
    public JButtonOperator help() {
        return options.btHelp();
    }
    
    public void selectAction(String path) {
        Node n = new Node(actions(), path);
        n.select();
    }
    
    public void selectProfile(String profile) {
        JComboBoxOperator combo = profile();
        if(combo.getSelectedItem().toString().equals(profile)) return; //no need to switch profile
        ComboBoxModel model = combo.getModel();        
        for (int i = 0; i < model.getSize(); i++) {
            Object item = model.getElementAt(i);
            if(item.toString().equals(profile)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        throw new IllegalArgumentException("Profile "+profile+" not found");
    }
    
    
    public void verify() {
        profile();
        duplicate();
        restore();
        actions();   
        shortcuts();
        add();
        remove();
        ok();
        cancel();
        help();
    }
    
    
    
    
    public static void main(String[] args) throws InterruptedException {
        //KeyMapOperator.invoke().verify();
        KeyMapOperator.invoke().selectAction("Other|select-line");
        
        
    }

    
}
