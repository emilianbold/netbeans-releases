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

package org.netbeans.modules.visualweb.project.jsf.actions;

import java.util.Collection;
import org.netbeans.modules.visualweb.project.jsf.api.Importable;
import java.util.Iterator;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import org.netbeans.spi.project.ui.support.MainProjectSensitiveActions;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.NotifyDescriptor.Message;
import org.openide.awt.Actions;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.Presenter.Menu;
import org.openide.awt.StatusDisplayer;
import org.openide.DialogDisplayer;
import org.openide.awt.JMenuPlus;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.awt.Mnemonics;


/** Allows the user to import an item
 *
 * @author Tor Norbye
 */
public final class ImportAction extends CallableSystemAction implements Menu {

    static final long serialVersionUID = 1L;

    /** Constructs an import action */
    public ImportAction() {
    }

    public String getName() {
        return NbBundle.getMessage(ImportAction.class, "LBL_ImportAction"); // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new HelpCtx(ChangeViewAction.class);
    }

    protected boolean asynchronous() {
        return false;
    }

    /* Returns a submneu that will present this action in a Menu.
     * @return the JMenuItem representation for this action
     */
    public JMenuItem getMenuPresenter () {
	JMenu mainItem = new JMenuPlus();
        Mnemonics.setLocalizedText(mainItem, getName());
        mainItem.setIcon (SystemAction.get(ImportAction.class).getIcon());
        HelpCtx.setHelpIDString (mainItem, ImportAction.class.getName());
        mainItem.addMenuListener(new MainItemListener());
        return mainItem;
    }

    /* Returns a submneu that will present this action in a PopupMenu.
     * @return the JMenuItem representation for this action
     */
    public JMenuItem getPopupPresenter() {
	JMenu mainItem = new JMenuPlus();
        Mnemonics.setLocalizedText(mainItem, getName());
        HelpCtx.setHelpIDString (mainItem, ImportAction.class.getName());
        mainItem.addMenuListener(new MainItemListener());
        return mainItem;
    }

    public void performAction () {
        // all functionality is accomplished by menu listeners
    }

    /** Listens to selection of the main import item and expands it
     * into a submenu listing available import types
     */
    private static final class MainItemListener implements MenuListener {
        public void menuCanceled (MenuEvent e) {
        }

        public void menuDeselected (MenuEvent e) {
            JMenu menu = (JMenu)e.getSource();
            menu.removeAll();
        }

        public void menuSelected (MenuEvent e) {
            JMenu menu = (JMenu)e.getSource();

            // Add the import items to the menu
            Collection<Importable> importables = (Collection<Importable>) Lookup.getDefault().lookupAll(Importable.class); 
            for( Importable importable : importables ){
                JMenuItem item = createMenuItem(importable);
                menu.add(item);
            }
           
            menu.add(createAddFileMenuItem(ImportFileAction.TYPE_IMAGE));
            menu.add(createAddFileMenuItem(ImportFileAction.TYPE_STYLESHEET));
            menu.add(createAddFileMenuItem(ImportFileAction.TYPE_JAVA));
            menu.add(createAddFileMenuItem(ImportFileAction.TYPE_OTHER));
        }
        
        private JMenuItem createMenuItem(Importable type) {
            Action action = MainProjectSensitiveActions.mainProjectSensitiveAction(type, type.getDisplayName(), null);
            return new JMenuItem(action);
        }
        
        // <TEMP>
        private JMenuItem createAddFileMenuItem(int type) {
            return new JMenuItem(ImportFileAction.createAction(type));
        }
        // </TEMP>
    }
}

