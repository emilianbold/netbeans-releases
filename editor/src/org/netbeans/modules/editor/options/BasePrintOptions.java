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

package org.netbeans.modules.editor.options;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.BaseKit;

import org.openide.util.HelpCtx;

/**
* Options for the plain editor kit
*
* @author Miloslav Metelka
*/
public class BasePrintOptions extends OptionSupport {

    public static final String BASE = "base"; // NOI18N

    public static final String PRINT_PREFIX = "print_"; // NOI18N

    public static final String PRINT_LINE_NUMBER_VISIBLE_PROP = "printLineNumberVisible"; // NOI18N

    public static final String PRINT_COLORING_MAP_PROP = "printColoringMap"; // NOI18N

    static final String[] BASE_PROP_NAMES = {
        PRINT_LINE_NUMBER_VISIBLE_PROP,
        PRINT_COLORING_MAP_PROP,
    };

    static final long serialVersionUID =7740651671176408299L;
    public BasePrintOptions() {
        this(BaseKit.class, BASE);
    }

    public BasePrintOptions(Class kitClass, String typeName) {
        super(kitClass, typeName);
    }

    public String displayName() {
        String name;
        try {
            name = getString(OPTIONS_PREFIX + PRINT_PREFIX + getTypeName());
        } catch (Throwable t) {
            name = super.displayName();
        }
        return name;
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (BasePrintOptions.class);
    }

    public boolean getPrintLineNumberVisible() {
        return ((Boolean)getSettingValue(SettingsNames.PRINT_LINE_NUMBER_VISIBLE)).booleanValue();
    }
    public void setPrintLineNumberVisible(boolean b) {
        setSettingValue(SettingsNames.PRINT_LINE_NUMBER_VISIBLE, (b ? Boolean.TRUE : Boolean.FALSE));
    }

    public Map getPrintColoringMap() {
        Map cm = SettingsUtil.getColoringMap(getKitClass(), true, true);
        cm.put(null, getKitClass()); // add kit class
        return cm;
    }
    public void setPrintColoringMap(Map coloringMap) {
        coloringMap.remove(null); // remove kit class
        SettingsUtil.updateColoringSettings(getKitClass(), coloringMap, true);
    }

}

/*
 * Log
 *  16   Jaga      1.12.1.2    4/13/00  Miloslav Metelka 
 *  15   Jaga      1.12.1.1    3/24/00  Miloslav Metelka 
 *  14   Jaga      1.12.1.0    3/15/00  Miloslav Metelka Structural change
 *  13   Gandalf   1.12        2/15/00  Miloslav Metelka print coloring map 
 *       getter fixed
 *  12   Gandalf   1.11        1/18/00  Miloslav Metelka displayName()
 *  11   Gandalf   1.10        1/13/00  Miloslav Metelka Localization
 *  10   Gandalf   1.9         1/11/00  Petr Nejedly    Fix for missing print 
 *       options
 *  9    Gandalf   1.8         12/28/99 Miloslav Metelka 
 *  8    Gandalf   1.7         11/27/99 Patrik Knakal   
 *  7    Gandalf   1.6         11/5/99  Jesse Glick     Context help jumbo 
 *       patch.
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         8/27/99  Miloslav Metelka 
 *  4    Gandalf   1.3         8/17/99  Miloslav Metelka 
 *  3    Gandalf   1.2         7/21/99  Miloslav Metelka 
 *  2    Gandalf   1.1         7/21/99  Miloslav Metelka 
 *  1    Gandalf   1.0         7/20/99  Miloslav Metelka 
 * $
 */
