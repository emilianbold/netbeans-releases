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


package org.netbeans.modules.properties.syntax;


import java.awt.Color;
import java.awt.SystemColor;
import java.util.*;

import org.netbeans.editor.AcceptorFactory;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.TokenContext;


/** 
 * Initializes properties editor kit settings. 
 * 
 * @author  Mila Metelka
 */
public class PropertiesSettingsInitializer extends Settings.AbstractInitializer {

    /** Name assigned to initializer */
    public static final String NAME = "properties-settings-initializer";


    /** Construct <code>PropertiesSettingsInitializer</code>. */
    public PropertiesSettingsInitializer() {
        super(NAME);
    }


    /** Updates settings map for editor kit class. */
    public void updateSettingsMap(Class kitClass, Map settingsMap) {
        if (kitClass == PropertiesKit.class) {
            // initialize color for shadowing cells in table view
            settingsMap.put(PropertiesOptions.SHADOW_TABLE_CELL_PROP, new Color(SystemColor.controlHighlight.getRGB()));

            settingsMap.put(SettingsNames.ABBREV_MAP, new TreeMap());
            
            settingsMap.put(SettingsNames.IDENTIFIER_ACCEPTOR, AcceptorFactory.JAVA_IDENTIFIER);

            SettingsUtil.updateListSetting(settingsMap, SettingsNames.TOKEN_CONTEXT_LIST,
                new TokenContext[] {
                    PropertiesTokenContext.context
                }
            );
        }
    }
}
