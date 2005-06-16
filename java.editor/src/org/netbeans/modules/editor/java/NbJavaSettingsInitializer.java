/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.java;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.KeyStroke;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.MultiKeyBinding;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.ext.ExtSettingsNames;
import org.openide.actions.SaveAction;
import org.openide.actions.CutAction;
import org.openide.actions.CopyAction;
import org.openide.actions.PasteAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.NewAction;
import org.openide.actions.ToolsAction;
import org.openide.actions.PropertiesAction;
import org.openide.windows.TopComponent;
import org.netbeans.editor.ext.ExtSettingsDefaults;
import org.netbeans.editor.ext.java.JavaSettingsDefaults;
import org.netbeans.editor.ext.java.JavaSettingsNames;

/**
* Nb settings for Java.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbJavaSettingsInitializer extends Settings.AbstractInitializer {

    public static final String NAME = "nb-java-settings-initializer"; // NOI18N

    public NbJavaSettingsInitializer() {
        super(NAME);
    }

    private static final int ALT_MASK = System.getProperty("mrj.version") != null ?
        InputEvent.CTRL_MASK : InputEvent.ALT_MASK;

    /** Update map filled with the settings.
    * @param kitClass kit class for which the settings are being updated.
    *   It is always non-null value.
    * @param settingsMap map holding [setting-name, setting-value] pairs.
    *   The map can be empty if this is the first initializer
    *   that updates it or if no previous initializers updated it.
    */
    public void updateSettingsMap(Class kitClass, Map settingsMap) {

        if (kitClass == JavaKit.class) {

            SettingsUtil.updateListSetting(settingsMap, SettingsNames.KEY_BINDING_LIST, getJavaKeyBindings());

            SettingsUtil.updateListSetting(settingsMap, SettingsNames.KEY_BINDING_LIST,
                                           new MultiKeyBinding[] {
                                               new MultiKeyBinding(
                                                   KeyStroke.getKeyStroke(KeyEvent.VK_O, ALT_MASK),
                                                   JavaKit.gotoSourceAction
                                               ),
                                               new MultiKeyBinding(
                                                   KeyStroke.getKeyStroke(KeyEvent.VK_F1, ALT_MASK),
                                                   JavaKit.gotoHelpAction
                                               ),
                                           }
                                          );

//            settingsMap.put(ExtSettingsNames.UPDATE_PD_AFTER_MOUNTING,
  //                          ExtSettingsDefaults.defaultUpdatePDAfterMounting);

            settingsMap.put(ExtSettingsNames.SHOW_DEPRECATED_MEMBERS,
                            ExtSettingsDefaults.defaultShowDeprecatedMembers);
            
            settingsMap.put(SettingsNames.CODE_FOLDING_ENABLE, JavaSettingsDefaults.defaultCodeFoldingEnable);
            settingsMap.put(JavaSettingsNames.CODE_FOLDING_COLLAPSE_METHOD, JavaSettingsDefaults.defaultCodeFoldingCollapseMethod);
            settingsMap.put(JavaSettingsNames.CODE_FOLDING_COLLAPSE_INNERCLASS, JavaSettingsDefaults.defaultCodeFoldingCollapseInnerClass);
            settingsMap.put(JavaSettingsNames.CODE_FOLDING_COLLAPSE_IMPORT, JavaSettingsDefaults.defaultCodeFoldingCollapseImport);
            settingsMap.put(JavaSettingsNames.CODE_FOLDING_COLLAPSE_JAVADOC, JavaSettingsDefaults.defaultCodeFoldingCollapseJavadoc);
            settingsMap.put(JavaSettingsNames.CODE_FOLDING_COLLAPSE_INITIAL_COMMENT, JavaSettingsDefaults.defaultCodeFoldingCollapseInitialComment);
            
        }

    }

    public MultiKeyBinding[] getJavaKeyBindings() {
        int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        boolean isMac = System.getProperty("mrj.version") != null;
        return new MultiKeyBinding[] {
                   new MultiKeyBinding(
                       new KeyStroke[] {
                           KeyStroke.getKeyStroke(KeyEvent.VK_U, ALT_MASK),
                           KeyStroke.getKeyStroke(KeyEvent.VK_G, 0)
                       },
                       JavaKit.makeGetterAction
                   ),
                   new MultiKeyBinding(
                       new KeyStroke[] {
                           KeyStroke.getKeyStroke(KeyEvent.VK_U, ALT_MASK),
                           KeyStroke.getKeyStroke(KeyEvent.VK_S, 0)
                       },
                       JavaKit.makeSetterAction
                   ),
                   new MultiKeyBinding(
                       new KeyStroke[] {
                           KeyStroke.getKeyStroke(KeyEvent.VK_U, ALT_MASK),
                           KeyStroke.getKeyStroke(KeyEvent.VK_I, 0)
                       },
                       JavaKit.makeIsAction
                   ),
                   new MultiKeyBinding(
                       KeyStroke.getKeyStroke(KeyEvent.VK_I,
                           ALT_MASK | InputEvent.SHIFT_MASK),
                       JavaKit.fastImportAction
                   ),
                   new MultiKeyBinding(
                       KeyStroke.getKeyStroke(KeyEvent.VK_B,
                           mask),
                       JavaKit.gotoSuperImplementationAction
                   ),
                   new MultiKeyBinding(
                       KeyStroke.getKeyStroke(KeyEvent.VK_F,
                           ALT_MASK | InputEvent.SHIFT_MASK),
                       JavaKit.fixImportsAction
                   ),
                   new MultiKeyBinding(
                       KeyStroke.getKeyStroke(KeyEvent.VK_S,
                           ALT_MASK | mask),
                       JavaKit.tryCatchAction
                       ),
                   new MultiKeyBinding(
                       KeyStroke.getKeyStroke(KeyEvent.VK_G, ALT_MASK | (isMac ? InputEvent.SHIFT_MASK : 0)),
                       org.netbeans.editor.ext.ExtKit.gotoDeclarationAction
                   ),
                   new MultiKeyBinding(
                       KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,
                           mask | InputEvent.SHIFT_MASK),
                       JavaKit.javaDocShowAction
                   ),
                   
                   new MultiKeyBinding(
                       KeyStroke.getKeyStroke(KeyEvent.VK_S,
                           InputEvent.SHIFT_MASK | ALT_MASK),
                       JavaKit.selectNextElementAction
                   ),
                   
                   new MultiKeyBinding(
                       KeyStroke.getKeyStroke(KeyEvent.VK_A,
                           InputEvent.SHIFT_MASK | ALT_MASK),
                       JavaKit.selectPreviousElementAction
                   )
                   
               };
    }

}
