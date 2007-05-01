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
package org.netbeans.modules.sql.framework.model.impl;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.w3c.dom.Element;

import com.sun.sql.framework.utils.Logger;

/**
 * 
 */
public class CommonNodeX {
    public  static final String ATTR_NAME = "name";
    public  static final String TAG_ATTRIBUTE = "attr";
    
    private static final String ATTR_STRING_VALUE = "stringvalue" ;
    private static final String ATTR_BOOLEAN_VALUE = "boolvalue" ;
    private static final String ATTR_INT_VALUE = "intvalue" ;
    private static final String ATTR_URL_VALUE = "urlvalue" ;
   
    private static final String KEY_RESOURCE_BUNDLE = "localizingBundle" ;    
    private static final String KEY_TOOLBARCATEGORY = "ToolbarCategory";
    private static final String KEY_TOOLTIP = "ToolTip";
    private static final String KEY_DISPLAY_NAME = "DisplayName";
    private static final String KEY_ICON_URL = "iconURL" ;
    
    private ResourceBundle resBundle = null;
    protected Map attributes = new HashMap();
    
    protected String name = "unknown" ;
    
    private static final String LOG_CATEGORY = CommonNodeX.class.getName();
    
    public CommonNodeX(Element elem) {
        this.name = elem.getAttribute(ATTR_NAME);        
    }

    private void setResourceBundle(){
        if (resBundle == null){
            String resBundleName = (String) this.attributes.get(KEY_RESOURCE_BUNDLE);
            if (resBundleName != null){
                synchronized (this) {
                    resBundle = ResourceBundle.getBundle(resBundleName);
                }
            }
        }
    }
    
    protected String getLocalizedValue(String key) {
        if (key == null) {
            return key;
        } 

        if (resBundle == null) {
            setResourceBundle();
        }

        if (resBundle == null){
            return key;
        }
        
        try {
            key = resBundle.getString(key);
        } catch (MissingResourceException ex) {
            // @TODO Log this exception
        }

        return key;
    }

    protected Object getAttributeValue(Element attrElement){
        Object ret = null;
        if (attrElement.getAttributeNode(ATTR_STRING_VALUE) != null){
            ret = attrElement.getAttribute(ATTR_STRING_VALUE);
        } else if (attrElement.getAttributeNode(ATTR_BOOLEAN_VALUE) != null){
            ret = Boolean.valueOf(attrElement.getAttribute(ATTR_BOOLEAN_VALUE));
        } else if (attrElement.getAttributeNode(ATTR_INT_VALUE) != null){
            ret = Integer.valueOf(attrElement.getAttribute(ATTR_INT_VALUE));
        } else if (attrElement.getAttributeNode(ATTR_URL_VALUE) != null){
            try { 
                ret = this.getClass().getResource(attrElement.getAttribute(ATTR_URL_VALUE)); 
            } catch (Exception ex) {
                Logger.print(Logger.WARN, LOG_CATEGORY, this, ex);
            }
        }
        return ret;
    }
    
    /**
     * Returns Operator or Category name. No I18N
     * @return name
     */
    public String getName() {
        return this.name;
    }
    
    public String getDisplayName(){
        return this.getLocalizedValue((String) this.attributes.get(KEY_DISPLAY_NAME));
    }

    /**
     * Gets tool tip for the operator
     * @return tool tip
     */
    public String getToolTip() {
        return getLocalizedValue((String) this.attributes.get(KEY_TOOLTIP));
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.IOperatorXmlInfoCategory#getToolbarType()
     */
    public int getToolbarType() {
        Integer toolbarType = (Integer) this.attributes.get(KEY_TOOLBARCATEGORY);
        int ret = org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfoModel.CATEGORY_ALL;
        if (toolbarType != null) {
            ret = toolbarType.intValue();
        }
        return ret;
    }

    /**
     * Gets the icon for this operator
     * 
     * @return Icon for this category
     */
    public Icon getIcon() {
        return new ImageIcon((URL) this.attributes.get(KEY_ICON_URL));
    }
    
    public String toString() {
        return this.getDisplayName();
    }
}
