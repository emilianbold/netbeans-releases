/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
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
import org.openide.actions.SaveAction;
import org.openide.actions.CutAction;
import org.openide.actions.CopyAction;
import org.openide.actions.PasteAction;
import org.openide.actions.DeleteAction;

/** 
* Customized settings for NetBeans editor
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbEditorSettingsInitializer implements Settings.Initializer {

  private static boolean inited;
  
  public static void init() {
    if (!inited) {
      inited = true;
      Settings.addInitializer(new BaseSettingsInitializer(), Settings.CORE_LEVEL);
      Settings.addInitializer(new ExtSettingsInitializer(), Settings.CORE_LEVEL);
      Settings.addInitializer(new JavaSettingsInitializer(JavaKit.class));
      Settings.addInitializer(new HTMLSettingsInitializer(HTMLKit.class));
      Settings.addInitializer(new NbEditorSettingsInitializer());
      Settings.addInitializer(new PlainSettingsInitializer());
      Settings.addInitializer(new NbJavaSettingsInitializer());
      Settings.addInitializer(new NbHTMLSettingsInitializer());

      Settings.reset();
    }
  }

  public NbEditorSettingsInitializer() {
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
            SaveAction.class.getName(),
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

/*
 * Log
 *  4    Jaga      1.3         4/14/00  Miloslav Metelka resetting settings
 *  3    Jaga      1.2         4/13/00  Miloslav Metelka 
 *  2    Jaga      1.1         3/24/00  Miloslav Metelka 
 *  1    Jaga      1.0         3/15/00  Miloslav Metelka 
 * $
 */

