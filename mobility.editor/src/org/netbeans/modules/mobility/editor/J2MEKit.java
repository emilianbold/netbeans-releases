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

/*
 * Created on Feb 27, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.mobility.editor;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import javax.swing.text.TextAction;

import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Utilities;
import org.netbeans.mobility.antext.preprocessor.PPLine;
import org.netbeans.modules.editor.MainMenuAction;
import org.netbeans.modules.editor.java.JavaKit;
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

/**
 * @author Adam Sotona
 *
 */
public class J2MEKit extends JavaKit {
    
    static final long serialVersionUID = -5445285962533684922L;
    
    public static final String generatePreprocessorPopupAction = "generate-preprocessor-popup"; // NOI18N
    public static final String PROJECT_CLIENT_PROPERTY = "projoject-client-property"; // NOI18N
    
    protected Action[] createActions() {
        return TextAction.augmentList(super.createActions(), new Action[]{
            new GeneratePreprocessorPopupAction(),
            new AddProjectConfigurationAction(),
            new CreateIfElseBlockAction(),
            new AddElifBlockAction(),
            new CreateDebugBlockAction(),
            new RecommentAction()
        });
    }
    
	protected static JMenu createMenu(JMenu menu, final JTextComponent c) {
        final String menuText = NbBundle.getMessage(J2MEKit.class, "Menu/Edit/PreprocessorBlocks"); //NOI18N
        if (menu == null) menu = new JMenu(); else menu.removeAll();
        Mnemonics.setLocalizedText(menu, menuText);
        final BaseKit kit = Utilities.getKit(c);
        if (kit == null) return menu;
        ProjectConfigurationsHelper cfgHelper = null;
        ArrayList<PPLine> lineList = null;
        if (c != null && c.getDocument() != null) {
            cfgHelper = J2MEProjectUtils.getCfgHelperForDoc(c.getDocument());
            lineList = (ArrayList<PPLine>)c.getDocument().getProperty(J2MEEditorDocument.PREPROCESSOR_LINE_LIST);
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
    
    private static void addAction(final BaseKit kit, final ProjectConfigurationProvider cfgProvider, final ArrayList<PPLine> preprocessorBlockList, final JTextComponent target, final JMenu menu, final String actionName) {
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
                }
            }
        }
    }
    
    public static class GeneratePreprocessorPopupAction extends BaseAction {
        
        public GeneratePreprocessorPopupAction() {
            super(generatePreprocessorPopupAction);
            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(J2MEKit.class, generatePreprocessorPopupAction)); // NOI18N
            putValue(BaseAction.NO_KEYBINDING, Boolean.TRUE);
        }
        
        public void actionPerformed(@SuppressWarnings("unused")
		final ActionEvent evt, @SuppressWarnings("unused")
		final JTextComponent target) {}
        
        public JMenuItem getPopupMenuItem(final JTextComponent target) {
            return createMenu(null, target);
        }
    }
    
    public static class PreprocessorMenuAction extends MainMenuAction {
        
        private JMenu PREPROCESSOR_MENU;
        
        public static void addAccelerators(final Action a, final JMenuItem item, final JTextComponent target) {
            MainMenuAction.addAccelerators(a, item, target);
        }
        
        public PreprocessorMenuAction(){
            super();
            setMenu();
        }
        
        /** Overriden to keep BLANK_ICON, which is reseted by superclass
         * setMenu impl
         */
        protected synchronized void setMenu() {
            PREPROCESSOR_MENU = createMenu(PREPROCESSOR_MENU, Utilities.getFocusedComponent());
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
            if (!BLANK_ICON.equals(PREPROCESSOR_MENU.getIcon())) {
                PREPROCESSOR_MENU.setIcon(BLANK_ICON);
            }
        }
        
        protected String getMenuItemText(){
            return NbBundle.getMessage(J2MEKit.class, "Menu/Edit/PreprocessorBlocks"); //NOI18N
        }
        
        public JMenuItem getMenuPresenter() {
            return PREPROCESSOR_MENU;
        }
        
        protected String getActionName() {
            return generatePreprocessorPopupAction;
        }
    }
    
}



