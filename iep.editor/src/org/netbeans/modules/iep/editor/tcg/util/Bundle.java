/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.iep.editor.tcg.util;

import java.text.MessageFormat;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.ListResourceBundle;
import java.util.Map;
import java.util.ResourceBundle;

public class Bundle {
    private static Map mBundles = new HashMap();
    
    private ConfigProperties mProps = null;

    private Bundle() {
        ResourceBundle bundle = new NullBundle();
        mProps = resourceBundle2Properties(bundle);
    }
    
    private Bundle(String name) {
        ResourceBundle bundle = ResourceBundle.getBundle(name);
        mProps = resourceBundle2Properties(bundle);
    }

    public synchronized static Bundle getInstance(String name) {
        Bundle bundle = (Bundle) mBundles.get(name);
        if (bundle == null) {
            try {
                bundle = new Bundle(name);
            } catch (Exception e) {
                e.printStackTrace();
                bundle = new Bundle();
            }
            mBundles.put(name, bundle);
        }
        return bundle;
    }
    
    public String getString(String key) {
        return getString(key, null, null);
    }

    public String getString(String key, Object[] args) {
        return getString(key, args, null);
    }
    
    public String getString(String key, String def) {
        return getString(key, null, def);
    }

    public String getString(String key, Object[] args, String def) {
        String ret;
        try {
            ret = format(mProps.getProperty(key), args);
            if (def != null) {
                if (ret == null || ret.equals("")) {
                    ret = def;
                }
            }
        } catch (Exception e) {
            System.err.println(
                "Exception: " + e);
            try {
                ret = format(def, args);
            } catch (Exception e2) {
                System.err.println(
                    "Exception: " + e2);
                ret = def;
            }
        }    
        return ret;
    }

    public Enumeration getKeys() {
        return mProps.propertyNames();
    }
    
    private String format(String text, Object[] args) {
        String ret = text;
        if (text != null && args != null) {
            ret = MessageFormat.format(text, args);
        }
        return ret;
    }

    private static ConfigProperties resourceBundle2Properties(ResourceBundle bundle) {
        ConfigProperties props = new ConfigProperties();
        for (Enumeration e = bundle.getKeys(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            props.put(key, bundle.getString(key));
        }
        return props;
    }
    
    private static class NullBundle extends ListResourceBundle {
        private Object[][] contents = {};

        public Object[][] getContents() {
            return contents;
        }
    }
}
