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

package org.netbeans.modules.editor.options;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import org.netbeans.editor.Settings;
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

    private static final String HELP_ID = "editing.printing"; // !!! NOI18N
    
    static final String[] BASE_PROP_NAMES = {
        PRINT_LINE_NUMBER_VISIBLE_PROP,
        PRINT_COLORING_MAP_PROP,
    };

    static final long serialVersionUID =7740651671176408299L;
    public BasePrintOptions() {
        this(BaseKit.class, BASE);
    }

    private transient Settings.Initializer printColoringMapInitializer;

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
        return new HelpCtx (HELP_ID);
    }

    /** Get the name of the <code>Settings.Initializer</code> related
     * to these options.
     */
    protected String getSettingsInitializerName() {
        return getTypeName() + "-print-options-initalizer";
    }

    protected void updateSettingsMap(Class kitClass, Map settingsMap) {
        super.updateSettingsMap(kitClass, settingsMap);

        if (printColoringMapInitializer != null) {
            printColoringMapInitializer.updateSettingsMap(kitClass, settingsMap);
        }
    }

    public boolean getPrintLineNumberVisible() {
        return ((Boolean)getSettingValue(SettingsNames.PRINT_LINE_NUMBER_VISIBLE)).booleanValue();
    }
    public void setPrintLineNumberVisible(boolean b) {
        setSettingBoolean(SettingsNames.PRINT_LINE_NUMBER_VISIBLE, b,
            PRINT_LINE_NUMBER_VISIBLE_PROP);
    }

    public Map getPrintColoringMap() {
        Map cm = SettingsUtil.getColoringMap(getKitClass(), true, true);
        cm.put(null, getKitClass().getName() ); // add kit class
        return cm;
    }
    public void setPrintColoringMap(Map coloringMap) {
        if (coloringMap != null) {
            coloringMap.remove(null); // remove kit class
            SettingsUtil.setColoringMap( getKitClass(), coloringMap, true );

            printColoringMapInitializer = SettingsUtil.getColoringMapInitializer(
                getKitClass(), coloringMap, true,
                getTypeName() + "-print-coloring-map-initializer"
            );


            firePropertyChange(PRINT_COLORING_MAP_PROP, null, null);
        }
    }

}
