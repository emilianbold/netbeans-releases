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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.iep.editor.tcg.ps;


import org.netbeans.modules.iep.editor.share.SharedConstants;
import org.netbeans.modules.iep.editor.tcg.exception.I18nException;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponentType;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponent;
import org.netbeans.modules.iep.editor.tcg.model.TcgProperty;
import org.netbeans.modules.iep.editor.tcg.model.TcgPropertyType;
import org.netbeans.modules.iep.editor.tcg.util.Bundle;
import org.netbeans.modules.iep.editor.tcg.util.Configuration;

public class TcgPsI18n implements SharedConstants {
    private static Bundle mAppBundle = Bundle.getInstance(Configuration.getVarByName("psBundle"));

    public static String getDisplayName(TcgComponentType type) {
        String titleKey = type.getTitle();
        String title = mAppBundle.getString(titleKey, titleKey);
        return title;
    }
    
    public static String getToolTip(TcgComponentType type) {
        String descKey = type.getDescription();
        String desc = mAppBundle.getString(descKey, descKey);
        return desc;
    }        

        
    public static String getDisplayName(TcgComponent comp) {
        if (comp.hasProperty(NAME_KEY)) {
            try {
                return comp.getProperty(NAME_KEY).getStringValue();
            } catch (I18nException e) {
                return comp.getTitle();
            }
        }
        return comp.getTitle();
    }
    
    public static String getToolTip(TcgComponent comp) {
        return getToolTip(comp.getType());
    }

    
    public static String getDisplayName(TcgPropertyType pt) {
        String titleKey = pt.getTitle();
        String title = mAppBundle.getString(titleKey, titleKey);
        return title;
    }
    
    public static String getToolTip(TcgPropertyType pt) {
        String descKey = pt.getDescription();
        String desc = mAppBundle.getString(descKey, descKey);
        return desc;
    }
    
    public static String getCatetoryDisplayName(TcgPropertyType pt) {
        String key = pt.getCategory();
        String display = mAppBundle.getString(key, key);
        return display;
    }
    
    public static String getI18nString(String key) {
        return mAppBundle.getString(key, key);
    }
    
}
