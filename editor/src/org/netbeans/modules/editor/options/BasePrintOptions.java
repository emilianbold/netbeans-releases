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

package org.netbeans.modules.editor.options;

import java.util.Map;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.BaseKit;
import org.netbeans.modules.editor.NbEditorSettingsInitializer;
import org.openide.text.PrintSettings;
import org.openide.util.HelpCtx;

/**
* Options for the plain editor kit
*
* @author Miloslav Metelka
*/
public class BasePrintOptions extends OptionSupport {

    public static final String BASE = "base"; // NOI18N

    public static final String PRINT_PREFIX = "print_"; // NOI18N

    public static final String PRINT_COLORING_MAP_PROP = "printColoringMap"; // NOI18N

    private static final String HELP_ID = "editing.fontsandcolors"; // !!! NOI18N
    
    static final String[] BASE_PROP_NAMES = {
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

    public void init(){
        refreshContextListeners();
    }
    
    private void refreshContextListeners() {
        PrintSettings ps = (PrintSettings) PrintSettings.findObject(PrintSettings.class, true);
        // Start listening on AllOptions and PrintSettings
        ContextOptionsListener.processExistingAndListen(ps);
    }    

    public HelpCtx getHelpCtx () {
        return new HelpCtx (HELP_ID);
    }

    /** Get the name of the <code>Settings.Initializer</code> related
     * to these options.
     */
    protected String getSettingsInitializerName() {
        return getTypeName() + "-print-options-initalizer"; // NOI18N
    }

    protected void updateSettingsMap(Class kitClass, Map settingsMap) {
        super.updateSettingsMap(kitClass, settingsMap);

        if (printColoringMapInitializer != null) {
            printColoringMapInitializer.updateSettingsMap(kitClass, settingsMap);
        }
    }
    

    public boolean getPrintLineNumberVisible() {
        return ((Boolean)getSettingValue(SettingsNames.LINE_NUMBER_VISIBLE)).booleanValue();
    }
    public void setPrintLineNumberVisible(boolean b) {
    }

    public Map getPrintColoringMap() {
        NbEditorSettingsInitializer.init();
        Map cm = SettingsUtil.getColoringMap(getKitClass(), true, true);
        cm.put(null, getKitClass().getName() ); // add kit class
        return cm;
    }
    public void setPrintColoringMap(Map coloringMap) {
        NbEditorSettingsInitializer.init();
        if (coloringMap != null) {
            coloringMap.remove(null); // remove kit class
            SettingsUtil.setColoringMap( getKitClass(), coloringMap, true );

            printColoringMapInitializer = SettingsUtil.getColoringMapInitializer(
                getKitClass(), coloringMap, true,
                getTypeName() + "-print-coloring-map-initializer" // NOI18N
            );


            firePropertyChange(PRINT_COLORING_MAP_PROP, null, null);
        }
    }

}
