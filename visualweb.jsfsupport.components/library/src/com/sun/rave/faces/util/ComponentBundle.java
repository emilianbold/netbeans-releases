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

package com.sun.rave.faces.util;

import java.beans.Beans;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Wraps a java.util.ResourceBundle and utilizes a java.text.MessageFormat for message formatting.
 * @see java.util.ResourceBundle
 * @see java.text.MessageFormat
*/
public class ComponentBundle {
	
    private static MessageFormat mf = new MessageFormat(""); // NOI18N
    private ResourceBundle rb;
    private static final String DESIGN_TIME_IMPL_CLASS = "com.sun.rave.faces.util.DesignTimeComponentBundle"; 	//NOI18N

    
    protected ComponentBundle() {}

    /**
     * If a class loader is not passed in, I will only be able to fetch ResourceBundles that
     * are reachable from MY class loader. This is due to a bug in ResourceBundle that
     * goes up the stack only 2 levels in order to find the appropriate class loader.
     * Since I provide a level of indirection, I will ALWAYS be the callee, and therefore
     * at the 2nd level.
     * See source for ResourceBundle.getBundle() ResourceBundle.getLoader().
     * 
     * @param baseName
     */
    public void init(String baseName, ClassLoader classLoader) {
        rb = ResourceBundle.getBundle(baseName, mf.getLocale(), classLoader);
    }
    
    public String getMessage(String key) {
        
        String string = rb.getString(key);
        return string;
    }
    
    public String getMessage(String key, Object arg1) {
        return getMessage(key, new Object[]{arg1});
    }
    
    public String getMessage(String key, Object arg1, Object arg2) {
        return getMessage(key, new Object[]{arg1, arg2});
    }
    
    public String getMessage(String key, Object arg1, Object arg2, Object arg3) {
        return getMessage(key, new Object[]{arg1, arg2, arg3});
    }    
    
    public String getMessage(String key, Object[] args) {
        return getMessage(key, args, true);
    }

    public String getMessage(String key, Object[] args, boolean escapeSingleQuotes) {
        String pattern = getMessage(key);
        if (escapeSingleQuotes)
            pattern = pattern.replaceAll("'", "''"); // NOI18N
        String message;
        synchronized (mf) {
            mf.applyPattern(pattern);
            message = mf.format(args);
        }
        return message;
    }

    private static Map componentBundleMap = new Hashtable();    //use Hashtable instead of HashMap since we are lazily populating the map
    
    public static ComponentBundle getBundle(Class c) {

        return getBundle(c, null);
    }

    /**
     * Return the component bundle found in class c's package.
     * The name of the bundle file is Bundle.properties.  If suffix
     * is not null and not empty, then the bundle file will be
     * Bundle-&lt;suffix&gt;.properties.
     * 
     * @param c
     * @param suffix
     * @return
     */
    public static ComponentBundle getBundle(Class c, String suffix) {
        
        String className = c.getName();
        int lastDotIndex = className.lastIndexOf('.');
        String packageName = ""; // NOI18N
        if (lastDotIndex > -1) {
            packageName = className.substring(0, lastDotIndex + 1);
        }
        String baseName = packageName + "Bundle"; // NOI18N
        if (suffix != null && suffix.length() > 0) {
            baseName += suffix;
        }
        ComponentBundle cb = (ComponentBundle)componentBundleMap.get(baseName);
        if (cb == null) {
        	if (Beans.isDesignTime()) {
        		try {
        			cb = (ComponentBundle)Class.forName(DESIGN_TIME_IMPL_CLASS).newInstance();
        		}
        		catch (Exception e) {
        			throw new RuntimeException("Could not create instance of " + DESIGN_TIME_IMPL_CLASS, e);
        		}
        	}
        	else {
        		cb = new ComponentBundle();
        	}
        	cb.init(baseName, c.getClassLoader());
            componentBundleMap.put(baseName, cb);
        }
        return cb;
    }
}
