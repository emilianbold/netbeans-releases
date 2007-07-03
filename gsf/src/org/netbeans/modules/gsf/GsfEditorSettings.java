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
package org.netbeans.modules.gsf;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.KeyStroke;

import org.netbeans.editor.BaseKit;
import org.netbeans.editor.MultiKeyBinding;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.TokenCategory;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.ext.ExtKit;


/**
 * The classes in here no one should ever implement, as I would think all this
 * information could be defined in a more declarative way: either via a simple interface
 * implementation where returning specific flags enables a set of settings
 * or a table or some external xml-like file.
 * Maybe that is all there already in NetBeans but I could not find it.
 * This is called from the ModuleInstall class and it's key for the editor to work.
 */
public class GsfEditorSettings extends Settings.AbstractInitializer {
    public static final Boolean defaultCodeFoldingEnable = Boolean.TRUE;

    // gls=Generic Language Support
    private static final String SETTINGS_NAME = "gls-editor-settings-initializer"; // NOI18N

    public GsfEditorSettings() {
        super(SETTINGS_NAME);
    }

    public void updateSettingsMap(Class kitClass, Map settingsMap) {
        if (kitClass == null) {
            return;
        }

        if (kitClass == GsfEditorKitFactory.GsfEditorKit.class) {
            SettingsUtil.updateListSetting(settingsMap, SettingsNames.KEY_BINDING_LIST,
                getGenericKeyBindings());

            settingsMap.put(SettingsNames.CODE_FOLDING_ENABLE, defaultCodeFoldingEnable);

            // This is wrong; I should be calling Formatter.indentSize() to get the default,
            // but I can't get to the mime type from here. In 6.0 the editor settings are
            // being redone so I can hopefully fix this soon.
            settingsMap.put(SettingsNames.SPACES_PER_TAB, Integer.valueOf(2));

            //} else if (kitClass.getName().startsWith("com.sun.semplice.generated.GeneratedEditorKit")) {
            //    try {
            //        GsfEditorKit kit = (GsfEditorKit)kitClass.newInstance();
            //        String mimeType = kit.getContentType();
            //        Language l = LanguageRegistry.getInstance().getLanguageByMimeType(mimeType);
            //        if (l != null) {
            //            final Scanner scanner = l.getScanner();
            //            if (scanner != null) {
            //                settingsMap.put(SettingsNames.IDENTIFIER_ACCEPTOR, new Acceptor() {
            //                    public final boolean accept(char c) {
            //                        return scanner.isIdentifierChar(c);
            //                    }
            //                });
            //            }
            //        }
            //    } catch (Exception ex) {
            //        ErrorManager.getDefault().notify(ex);
            //    }
        }
    }

    public static MultiKeyBinding[] getGenericKeyBindings() {
        int MENU_MASK = java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

        return new MultiKeyBinding[] {
            new MultiKeyBinding(new KeyStroke[] {
                    KeyStroke.getKeyStroke(KeyEvent.VK_J, MENU_MASK),
                    KeyStroke.getKeyStroke(KeyEvent.VK_D, 0)
                }, "macro-debug-var" // NOI18N
            ),
            new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_T,
                    MENU_MASK | InputEvent.SHIFT_MASK), ExtKit.commentAction),
            new MultiKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_D,
                    MENU_MASK | InputEvent.SHIFT_MASK), ExtKit.uncommentAction)
        };
    }
}
