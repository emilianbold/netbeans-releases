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
