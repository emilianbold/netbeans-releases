/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.mobility.editor;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Utilities;
import org.netbeans.mobility.antext.preprocessor.PPLine;
import org.netbeans.modules.editor.MainMenuAction;
import org.netbeans.modules.mobility.editor.actions.AddElifBlockAction;
import org.netbeans.modules.mobility.editor.actions.AddProjectConfigurationAction;
import org.netbeans.modules.mobility.editor.actions.CreateDebugBlockAction;
import org.netbeans.modules.mobility.editor.actions.CreateIfElseBlockAction;
import org.netbeans.modules.mobility.editor.actions.PreprocessorEditorContextAction;
import org.netbeans.modules.mobility.editor.actions.RecommentAction;
import org.netbeans.modules.mobility.project.J2MEProjectUtils;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author suchys
 */
public class PreprocessorActions {

    public static final String generatePreprocessorPopupAction = "generate-preprocessor-popup"; // NOI18N
    public static final String PROJECT_CLIENT_PROPERTY = "projoject-client-property"; // NOI18N

    private static final RequestProcessor RP = new RequestProcessor(PreprocessorActions.class);

    protected static JMenu createMenu(JMenu menu, final JTextComponent c, ProjectConfigurationsHelper cfgHelper) {
        final String menuText = NbBundle.getMessage(PreprocessorActions.class, "Menu/Edit/PreprocessorBlocks"); //NOI18N
        if (menu == null) menu = new JMenu(); else menu.removeAll();
        Mnemonics.setLocalizedText(menu, menuText);
        final BaseKit kit = Utilities.getKit(c);
        if (kit == null) return menu;       
        ArrayList<PPLine> lineList = null;
        if (c != null && c.getDocument() != null) {
            lineList = (ArrayList<PPLine>)c.getDocument().getProperty(DocumentPreprocessor.PREPROCESSOR_LINE_LIST);
        }
        if (lineList == null) lineList = new ArrayList<PPLine>();
        addAction(kit, cfgHelper, lineList, c, menu, AddProjectConfigurationAction.NAME);
        menu.addSeparator();
        addAction(kit, cfgHelper, lineList, c, menu, CreateIfElseBlockAction.NAME);
        addAction(kit, cfgHelper, lineList, c, menu, AddElifBlockAction.NAME);
        addAction(kit, cfgHelper, lineList, c, menu, CreateDebugBlockAction.NAME);
        menu.addSeparator();
        addAction(kit, cfgHelper, lineList, c, menu, RecommentAction.NAME);
        return menu;
    }

    private static void addAction(final BaseKit kit, final ProjectConfigurationsHelper cfgProvider, final ArrayList<PPLine> preprocessorBlockList, final JTextComponent target, final JMenu menu, final String actionName) {
        final Action a = kit.getActionByName(actionName);
        if (a != null) {
            if (a instanceof PreprocessorEditorContextAction) {
                final String itemText = ((PreprocessorEditorContextAction)a).getPopupMenuText(cfgProvider, preprocessorBlockList, target) ;
                if (itemText != null) {
                    final JMenuItem item = new JMenuItem(itemText);
                    item.addActionListener(a);
                    // Try to get the accelerator
                    final Keymap km = kit.getKeymap();
                    if (km != null) {
                        final KeyStroke[] keys = km.getKeyStrokesForAction(a);
                        if (keys != null && keys.length > 0) {
                            item.setAccelerator(keys[0]);
                        }else if (a!=null){
                            final KeyStroke ks = (KeyStroke)a.getValue(Action.ACCELERATOR_KEY);
                            if (ks!=null) {
                                item.setAccelerator(ks);
                            }
                        }
                    }
                    item.setEnabled(((PreprocessorEditorContextAction)a).isEnabled(cfgProvider, preprocessorBlockList, target));
                    final Object helpID = a.getValue("helpID");//NOI18N
                    if (helpID != null && (helpID instanceof String))
                        item.putClientProperty("HelpID", helpID);//NOI18N
                    menu.add(item);
                    Mnemonics.setLocalizedText(item, item.getText());
                }
            }
        }
    }

    public static class GeneratePreprocessorPopupAction extends BaseAction {

        public GeneratePreprocessorPopupAction() {
            super(generatePreprocessorPopupAction);
            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(PreprocessorActions.class, generatePreprocessorPopupAction)); // NOI18N
            putValue(BaseAction.NO_KEYBINDING, Boolean.TRUE);
        }

        public void actionPerformed(@SuppressWarnings("unused")
		final ActionEvent evt, @SuppressWarnings("unused")
		final JTextComponent target) {}

        @Override
        public JMenuItem getPopupMenuItem(final JTextComponent target) {
            ProjectConfigurationsHelper cfgHelper = null;
            if (target != null && target.getDocument() != null) {
                cfgHelper = J2MEProjectUtils.getCfgHelperForDoc(target.getDocument());
            }
            return createMenu(null, target, cfgHelper);
        }
    }

    public static class PreprocessorMenuAction extends MainMenuAction {

        private final JMenu PREPROCESSOR_MENU = new JMenu();
        private boolean initialized;

        public static void addAccelerators(final Action a, final JMenuItem item, final JTextComponent target) {
            MainMenuAction.addAccelerators(a, item, target);
        }

        public PreprocessorMenuAction(){
            super(false, null);
            setMenu();
        }

        /** Overriden to keep BLANK_ICON, which is reseted by superclass
         * setMenu impl
         */
        @Override
        protected void setMenu() {
            synchronized (this) {
                initialized = false;
            }
            RP.post(new Runnable() {
                @Override
                public void run() {
                    JTextComponent target = Utilities.getLastActiveComponent();
                    final ProjectConfigurationsHelper cfgHelper =
                            target != null && target.getDocument() != null ?
                            J2MEProjectUtils.getCfgHelperForDoc(target.getDocument()) :
                            null;
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            synchronized (PreprocessorMenuAction.this) {
                                initialized = true;
                                createMenu(PREPROCESSOR_MENU, Utilities.getFocusedComponent(), cfgHelper);
                                final ActionMap am = getContextActionMap();
                                Action action = null;
                                final JMenuItem presenter = getMenuPresenter();

                                if (am!=null){
                                    action = am.get(getActionName());
                                    final Action presenterAction = presenter.getAction();
                                    if (presenterAction == null){
                                        if (action != null){
                                            presenter.setAction(action);
                                            menuInitialized = false;
                                        }
                                    }else{
                                        if ((action!=null && !action.equals(presenterAction))){
                                            presenter.setAction(action);
                                            menuInitialized = false;
                                        }else if (action == null){
                                            presenter.setEnabled(false);
                                        }
                                    }
                                }

                                if (!menuInitialized){
                                    Mnemonics.setLocalizedText(presenter, getMenuItemText());
                                    menuInitialized = true;
                                }

                                presenter.setEnabled(action != null);
                            }
                        }
                    });
                }
            });
        }

        protected String getMenuItemText(){
            return NbBundle.getMessage(PreprocessorActions.class, "Menu/Edit/PreprocessorBlocks"); //NOI18N
        }

        public synchronized JMenuItem getMenuPresenter() {
            if (!initialized) {
                PREPROCESSOR_MENU.setText(NbBundle.getMessage(PreprocessorActions.class, "LBL_Loading"));
            }
            return PREPROCESSOR_MENU;
        }

        protected String getActionName() {
            return generatePreprocessorPopupAction;
        }
    }
}
