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

package com.netbeans.developer.modules.text;

import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.ArrayList;
import javax.swing.KeyStroke;
import com.netbeans.editor.BaseSettingsInitializer;
import com.netbeans.editor.Settings;
import com.netbeans.editor.SettingsUtil;
import com.netbeans.editor.MultiKeyBinding;
import com.netbeans.editor.ext.ExtSettingsNames;
import com.netbeans.editor.ext.ExtSettingsInitializer;
import com.netbeans.editor.ext.html.HTMLSettingsInitializer;
import com.netbeans.editor.ext.java.JavaSettingsInitializer;
import com.netbeans.developer.modules.text.html.HTMLKit;
import com.netbeans.developer.modules.text.html.NbHTMLSettingsInitializer;
import com.netbeans.developer.modules.text.java.JavaKit;
import com.netbeans.developer.modules.text.java.NbJavaSettingsInitializer;
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
      Settings.addInitializer(new BaseSettingsInitializer());
      Settings.addInitializer(new ExtSettingsInitializer());
      Settings.addInitializer(new JavaSettingsInitializer(JavaKit.class));
      Settings.addInitializer(new HTMLSettingsInitializer(HTMLKit.class));
      Settings.addInitializer(new NbEditorSettingsInitializer());
      Settings.addInitializer(new NbJavaSettingsInitializer());
      Settings.addInitializer(new NbHTMLSettingsInitializer());
    }
  }

  public NbEditorSettingsInitializer() {
  }

  public Map updateSettingsMap(Class kitClass, Map settingsMap) {

    if (kitClass == NbEditorKit.class) {

      if (settingsMap == null) {
        settingsMap = new HashMap();
      }

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

    return settingsMap;
  }
  
}

/*
 * Log
 *  2    Jaga      1.1         3/24/00  Miloslav Metelka 
 *  1    Jaga      1.0         3/15/00  Miloslav Metelka 
 * $
 */

