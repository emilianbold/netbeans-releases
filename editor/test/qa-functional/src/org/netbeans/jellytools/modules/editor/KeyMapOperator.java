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
