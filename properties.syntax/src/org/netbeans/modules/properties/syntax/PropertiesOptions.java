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

import org.netbeans.modules.editor.options.BaseOptions;
import org.netbeans.modules.editor.options.OptionSupport;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/**
 * Options for the properties editor kit.
 *
 * @author Libor Kramolis
 */
public class PropertiesOptions extends BaseOptions {

    /** Generated serial version UID. */
    static final long serialVersionUID = 2347735706857337892L;

    /** Name of property. */
    public static final String PROPERTIES = "properties"; // NOI18N
    
    /** Name of property. */
    public static final String SHADOW_TABLE_CELL_PROP = "shadowTableCell"; // NOI18N
    
    /** Array of prperty names. */
    static final String[] PROPERTIES_PROP_NAMES = OptionSupport.mergeStringArrays(
        BaseOptions.BASE_PROP_NAMES,
        new String[] {
            SHADOW_TABLE_CELL_PROP
        }
    );

    
    /** Constructor. */
    public PropertiesOptions() {
        super(PropertiesKit.class, PROPERTIES);
    }

    
    /**
     * Gets localized string. 
     * @return localized string */
    protected String getString(String s) {
        try {
            String res = NbBundle.getBundle(PropertiesOptions.class).getString(s);
        return (res == null) ? super.getString(s) : res;
        } catch (Exception e) {
            return super.getString(s);
        }
    }

    /** Gets <code>SHADOW_TABLE_CELL_PROP</code> property. */
    public Color getShadowTableCell() {
        return (Color) getSettingValue(SHADOW_TABLE_CELL_PROP);
    }

    /** Sets <code>SHADOW_TABLE_CELL_PROP</code> property.*/
    public void setShadowTableCell(Color color) {
        setSettingValue(SHADOW_TABLE_CELL_PROP, color);
    }
    
    /** Gets help context. Overrides superclass method. */
    public HelpCtx getHelpCtx() {
        return new HelpCtx("editing.editor.properties"); // NOI18N
    }
}
