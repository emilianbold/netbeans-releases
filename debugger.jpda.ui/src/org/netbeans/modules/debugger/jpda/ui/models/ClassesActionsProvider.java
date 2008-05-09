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

package org.netbeans.modules.debugger.jpda.ui.models;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import org.netbeans.api.debugger.Properties;
import org.netbeans.spi.debugger.ContextProvider;

import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.TreeModel;
import org.openide.awt.Mnemonics;

import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;


/**
 * @author   Jan Jancura
 */
public class ClassesActionsProvider implements NodeActionsProvider {
    
    private static Properties classesProperties = Properties.getDefault().
            getProperties("debugger").getProperties("classesView"); // NOI18N
    
    //private JPDADebugger debugger;
    private TreeModel tree;
    

    public ClassesActionsProvider (ContextProvider lookupProvider) {
        //debugger = (JPDADebugger) lookupProvider.lookupFirst(null, JPDADebugger.class);
        tree = lookupProvider.lookupFirst("ClassesView", TreeModel.class);
    }
    
    public Action[] getActions (Object node) throws UnknownTypeException {
        return new Action[] { new PackageViewTypeAction(tree) };
    }
    
    public void performDefaultAction (Object node) throws UnknownTypeException {
        return;
    }

    private static class PackageViewTypeAction extends AbstractAction implements Presenter.Popup {

        private TreeModel tree;
 
        PackageViewTypeAction(TreeModel tree) {
            super ("");
            this.tree = tree;
        }
        
        public boolean isEnabled () {
            return true;
        }

        public void actionPerformed (ActionEvent e) {
            // Ignored
        }
        
        public JMenuItem getPopupPresenter() {
            JMenu menu = new JMenu();
            Mnemonics.setLocalizedText(menu, NbBundle.getMessage(ClassesActionsProvider.class, "LBL_change_package_type"));
            menu.add(createChoice(true, NbBundle.getMessage(ClassesActionsProvider.class, "ChangePackageViewTypeAction_list")));
            menu.add(createChoice(false, NbBundle.getMessage(ClassesActionsProvider.class, "ChangePackageViewTypeAction_tree")));
            return menu;
        }

        private JMenuItem createChoice(final boolean flat, String label) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem();
            Mnemonics.setLocalizedText(item, label);
            boolean isFlat = classesProperties.getBoolean("flat", true);
            item.setSelected(flat == isFlat);
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    classesProperties.setBoolean("flat", flat);
                    // Refresh the classes view
                    // It's in a different module => we have to use reflection :-(
                    try {
                        Method fireTreeChangedMethod =
                                tree.getClass().getMethod("fireTreeChanged", new Class[] {});
                        try {
                            fireTreeChangedMethod.invoke(tree, new Object[] {});
                        } catch (IllegalArgumentException ex) {
                        } catch (InvocationTargetException ex) {
                        } catch (IllegalAccessException ex) {
                        }
                    } catch (SecurityException ex) {
                    } catch (NoSuchMethodException ex) {
                    }
                }
            });
            return item;
        }
    }
}
