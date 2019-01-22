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

package org.netbeans.modules.jmx.actions;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;
import org.openide.nodes.Node;
import org.openide.util.actions.NodeAction;

/** 
 * Action which just groups update actions.
 *
 * 
 * @version 1.0
 */
public class ManagementAllAction extends NodeAction implements Presenter.Menu, Presenter.Popup {

    /*
    public void actionPerformed (ActionEvent ev) {
        // do nothing; should not be called
    }
     */
    
    private final String actionName = NbBundle.getMessage(ManagementAllAction.class, "LBL_ManagementAction"); // NOI18N 
    
    protected void performAction(org.openide.nodes.Node[] node) {
        // do nothing; should not be called        
    }
    
    public String getName () {
        return actionName;
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx(ManagementAllAction.class);
    }   
    
    /** List of system actions to be displayed within this one's submenu. */
    private static final SystemAction[] grouped = new SystemAction[] {
                SystemAction.get(AddAttrAction.class),
                SystemAction.get(AddOpAction.class),
                SystemAction.get(AddNotifAction.class),
                SystemAction.get(AddRegisterIntfAction.class)};
                //null,
                //SystemAction.get(RegisterMBeanAction.class)
            //};

    public JMenuItem getMenuPresenter () {
        JMenu menu = new LazyMenu (getName ());
        return menu;
    }

    public JMenuItem getPopupPresenter () {
        JMenu menu = new JMenu (getName ());
        // Conventional not to set an icon here.
        for (int i = 0; i < grouped.length; i++) {
            SystemAction action = grouped[i];
            if (action == null) {
                menu.addSeparator ();
            } else if (action instanceof Presenter.Popup) {
                menu.add (((Presenter.Popup) action).getPopupPresenter ());
            }
        }
        return menu;
    }
    
    protected boolean enable (Node[] nodes) {
        return true;
    }

    /**
     * Lazy menu which when added to its parent menu, will begin creating the
     * list of submenu items and finding their presenters.
     */
    private final class LazyMenu extends JMenu {
        
        public LazyMenu(String name) {
            super(name);
        }
                
        public JPopupMenu getPopupMenu() {
            if (getItemCount() == 0) {
                for (int i = 0; i < grouped.length; i++) {
                    SystemAction action = grouped[i];
                    if (action == null) {
                        addSeparator ();
                    } else if (action instanceof Presenter.Menu) {
                        add (((Presenter.Menu) action).getMenuPresenter ());
                    }
                }
            }
            return super.getPopupMenu();
        }
        
    }

}
