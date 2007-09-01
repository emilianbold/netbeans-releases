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


package org.netbeans.modules.compapp.projects.jbi.ui;

import java.io.IOException;
import org.netbeans.modules.compapp.projects.jbi.ui.actions.DeleteModuleAction;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.VisualClassPathItem;
import org.openide.actions.DeleteAction;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

import org.openide.util.HelpCtx;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

import java.awt.Image;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;


/**
 * A simple node with no children. Often used in conjunction with some kind of underlying data
 * model, where each node represents an element in that model. In this case, you should see the
 * Container Node template which will permit you to create a whole tree of such nodes with the
 * proper behavior.
 *
 * @author Tientien Li
 */
public class JbiModuleNode extends AbstractNode implements Node.Cookie {
    static private Action[] actions = null;
    private VisualClassPathItem model;
    
    // will frequently accept an element from some data model in the constructor:
    public JbiModuleNode(VisualClassPathItem key) {
        super(Children.LEAF);
        model = key;
        setName("preferablyUniqueNameForThisNodeAmongSiblings"); // NOI18N or, super.setName if needed
        setDisplayName(key.getShortName()); 

        //setShortDescription(NbBundle.getMessage(JbiModuleNode.class, "HINT_ModuleNode"));
    }

    // Create the popup menu:
    public Action[] getActions(boolean context) {
        if (null == actions) {
            actions = new Action[] {   
                    SystemAction.get(DeleteAction.class), 
                };
            getCookieSet().add(this);
        }

        return actions;
    }
    
    public boolean canDestroy() {
        return true;
    }

    public void destroy() throws IOException {
        super.destroy();
        
        DeleteModuleAction deleteModuleAction = 
                SystemAction.get(DeleteModuleAction.class);
        deleteModuleAction.performAction(new Node[] {this});
    }

    private Image getProjIcon(){
        Icon ic = null;
        Image ret = null;
        if (this.model != null){
            ic = this.model.getProjectIcon();
            if (ic instanceof ImageIcon){
                ret = ((ImageIcon)ic).getImage();
            }
        }
        
        return ret;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param type DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Image getIcon(int type) {
        Image ret = getProjIcon();
        if (ret == null){
            ret = Utilities.loadImage("org/netbeans/modules/compapp/projects/jbi/ui/resources/jar.gif"); // NOI18N
        } 
        
        return ret;
    }

    /**
     * DOCUMENT ME!
     *
     * @param type DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
