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

package org.netbeans.modules.tbls.editor.ps;


import java.util.ResourceBundle;

import org.netbeans.modules.iep.model.share.SharedConstants;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.tbls.model.I18nException;
import org.netbeans.modules.tbls.model.TcgComponent;
import org.netbeans.modules.tbls.model.TcgComponentType;
import org.netbeans.modules.tbls.model.TcgPropertyType;

public class TcgPsI18n implements SharedConstants {
    //private static Bundle mAppBundle = Bundle.getInstance(Configuration.getVarByName("psBundle"));
    private static ResourceBundle mAppBundle = ResourceBundle.getBundle("org.netbeans.modules.iep.editor.ps.Bundle");
    
    public static String getDisplayName(TcgComponentType type) {
        String titleKey = type.getTitle();
        String title = null;
        try {
            title = mAppBundle.getString(titleKey);
        } catch(Exception ex) {
            if(title == null) {
                title = titleKey;
            }
        }
        
        return title;
    }
    
    public static String getToolTip(TcgComponentType type) {
        String descKey = type.getDescription();
        String desc = null;
        try {
            desc = mAppBundle.getString(descKey);
        } catch (Exception ex) {
            if(desc == null) {
                desc = descKey;
            }
        }
        
        return desc;
    }        

        
    public static String getDisplayName(TcgComponent comp) {
        if (comp.hasProperty(PROP_NAME)) {
            try {
                return comp.getProperty(PROP_NAME).getStringValue();
            } catch (I18nException e) {
                return comp.getTitle();
            }
        }
        return comp.getTitle();
    }
    
    public static String getDisplayName(OperatorComponent comp) {
        if (comp.getString(PROP_NAME) != null) {
                return comp.getString(PROP_NAME);
        }
        return comp.getTitle();
    }
    
    public static String getToolTip(TcgComponent comp) {
        return getToolTip(comp.getType());
    }

    
    public static String getDisplayName(TcgPropertyType pt) {
        String titleKey = pt.getTitle();
        String title = null;
        
        try {
            title = mAppBundle.getString(titleKey);
        } catch(Exception ex) {
            if(title == null) {
                title = titleKey;
            }
        }
        
        return title;
    }
    
    public static String getToolTip(TcgPropertyType pt) {
        String descKey = pt.getDescription();
        String desc = null;
        try {
            desc = mAppBundle.getString(descKey);
        } catch(Exception ex) {
            if(desc == null) {
                desc = descKey;
            }
        }
        
        return desc;
    }
    
    public static String getCatetoryDisplayName(TcgPropertyType pt) {
        String key = pt.getCategory();
        String display = null;
        try {
            display = mAppBundle.getString(key);
        } catch(Exception ex) {
            if(display == null) {
                display = key;
            }
        }
        
        return display;
    }
    
    public static String getI18nString(String key) {
        String result = null;
        try {
            result = mAppBundle.getString(key);
        } catch(Exception ex) {
            if(result == null) {
                result = key;
            }
        }
        
        
        return result;
    }
    
    
    public static String getI18nStringStripI18N(String key) {
        if (key.startsWith("i18n.")) {
            return getI18nString(TcgPsI18n.getI18nString(key.substring(5)));
        }
        
        return getI18nString(key);
    }
}
