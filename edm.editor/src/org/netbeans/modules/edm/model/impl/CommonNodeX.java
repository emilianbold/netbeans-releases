/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.edm.model.impl;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.w3c.dom.Element;

/**
 * 
 */
public class CommonNodeX {

    public static final String ATTR_NAME = "name";
    public static final String TAG_ATTRIBUTE = "attr";
    private static final String ATTR_STRING_VALUE = "stringvalue";
    private static final String ATTR_BOOLEAN_VALUE = "boolvalue";
    private static final String ATTR_INT_VALUE = "intvalue";
    private static final String ATTR_URL_VALUE = "urlvalue";
    private static final String KEY_RESOURCE_BUNDLE = "localizingBundle";
    private static final String KEY_TOOLBARCATEGORY = "ToolbarCategory";
    private static final String KEY_TOOLTIP = "ToolTip";
    private static final String KEY_DISPLAY_NAME = "DisplayName";
    private static final String KEY_ICON_URL = "iconURL";
    private ResourceBundle resBundle = null;
    protected Map attributes = new HashMap();
    protected String name = "unknown";
    private static final String LOG_CATEGORY = CommonNodeX.class.getName();
    private static transient final Logger mLogger = Logger.getLogger(CommonNodeX.class.getName());

    public CommonNodeX(Element elem) {
        this.name = elem.getAttribute(ATTR_NAME);
    }

    private void setResourceBundle() {
        if (resBundle == null) {
            String resBundleName = (String) this.attributes.get(KEY_RESOURCE_BUNDLE);
            if (resBundleName != null) {
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

        if (resBundle == null) {
            return key;
        }

        try {
            key = resBundle.getString(key);
        } catch (MissingResourceException ex) {
            // @TODO Log this exception
        }

        return key;
    }

    protected Object getAttributeValue(Element attrElement) {
        Object ret = null;
        if (attrElement.getAttributeNode(ATTR_STRING_VALUE) != null) {
            ret = attrElement.getAttribute(ATTR_STRING_VALUE);
        } else if (attrElement.getAttributeNode(ATTR_BOOLEAN_VALUE) != null) {
            ret = Boolean.valueOf(attrElement.getAttribute(ATTR_BOOLEAN_VALUE));
        } else if (attrElement.getAttributeNode(ATTR_INT_VALUE) != null) {
            ret = Integer.valueOf(attrElement.getAttribute(ATTR_INT_VALUE));
        } else if (attrElement.getAttributeNode(ATTR_URL_VALUE) != null) {
            try {
                ret = this.getClass().getResource(attrElement.getAttribute(ATTR_URL_VALUE));
            } catch (Exception ex) {
                mLogger.log(Level.INFO,"Exception:"+LOG_CATEGORY,ex);
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

    public String getDisplayName() {
        return this.getLocalizedValue((String) this.attributes.get(KEY_DISPLAY_NAME));
    }

    /**
     * Gets tool tip for the operator
     * @return tool tip
     */
    public String getToolTip() {
        return getLocalizedValue((String) this.attributes.get(KEY_TOOLTIP));
    }

    public int getToolbarType() {
        Integer toolbarType = (Integer) this.attributes.get(KEY_TOOLBARCATEGORY);
        int ret = org.netbeans.modules.edm.editor.graph.jgo.IOperatorXmlInfoModel.CATEGORY_ALL;
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
