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

package org.netbeans.modules.editor;

import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.ArrayList;
import javax.swing.KeyStroke;
import org.netbeans.editor.BaseSettingsInitializer;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.MultiKeyBinding;
import org.netbeans.editor.ext.ExtSettingsNames;
import org.netbeans.editor.ext.ExtSettingsInitializer;
import org.netbeans.editor.ext.html.HTMLSettingsInitializer;
import org.netbeans.editor.ext.java.JavaSettingsInitializer;
import org.netbeans.modules.editor.html.HTMLKit;
import org.netbeans.modules.editor.html.NbHTMLSettingsInitializer;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.editor.java.NbJavaSettingsInitializer;
import org.netbeans.modules.editor.plain.PlainKit;
import org.netbeans.modules.editor.plain.NbPlainSettingsInitializer;
import org.openide.actions.SaveAction;
import org.openide.actions.CutAction;
import org.openide.actions.CopyAction;
import org.openide.actions.PasteAction;
import org.openide.actions.DeleteAction;
import org.openide.windows.TopComponent;

/**
* Customized settings for NetBeans editor
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbEditorSettingsInitializer extends Settings.AbstractInitializer {

    public static final String NAME = "nb-editor-settings-initializer";

    private static boolean inited;

    public static void init() {
        if (!inited) {
            inited = true;
            Settings.addInitializer(new BaseSettingsInitializer(), Settings.CORE_LEVEL);
            Settings.addInitializer(new ExtSettingsInitializer(), Settings.CORE_LEVEL);
            Settings.addInitializer(new JavaSettingsInitializer(JavaKit.class));
            Settings.addInitializer(new HTMLSettingsInitializer(HTMLKit.class));
            Settings.addInitializer(new NbEditorSettingsInitializer());
            Settings.addInitializer(new NbPlainSettingsInitializer());
            Settings.addInitializer(new NbJavaSettingsInitializer());
            Settings.addInitializer(new NbHTMLSettingsInitializer());

            Settings.reset();
        }
    }

    public NbEditorSettingsInitializer() {
        super(NAME);
    }

    /** Update map filled with the settings.
    * @param kitClass kit class for which the settings are being updated.
    *   It is always non-null value.
    * @param settingsMap map holding [setting-name, setting-value] pairs.
    *   The map can be empty if this is the first initializer
    *   that updates it or if no previous initializers updated it.
    */
    public void updateSettingsMap(Class kitClass, Map settingsMap) {

        if (kitClass == NbEditorKit.class) {

            settingsMap.put(ExtSettingsNames.POPUP_MENU_ACTION_NAME_LIST,
                            new ArrayList(Arrays.asList(
                                              new String[] {
                                                  TopComponent.class.getName(),
                                                  null,
                                                  CutAction.class.getName(),
                                                  CopyAction.class.getName(),
                                                  PasteAction.class.getName(),
                                                  null,
                                                  DeleteAction.class.getName()
                                              }
                                          ))
                           );

        }

    }

}
