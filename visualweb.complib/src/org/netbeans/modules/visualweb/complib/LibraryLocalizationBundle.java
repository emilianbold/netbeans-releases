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

package org.netbeans.modules.visualweb.complib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * ResourceBundle used to localize NetBeans libraries. Note this class must be
 * public.
 * 
 * @author Edwin Goei
 */
public class LibraryLocalizationBundle extends ResourceBundle {

    private static File propsFile = new File(IdeUtil.getComplibStateDir(),
            "LibraryLocalizationBundle.properties");

    private static HashMap<Object, Object> map;

    /**
     * The constructor must be public so an instance of it can be created.
     */
    public LibraryLocalizationBundle() {
    }

    private static HashMap<Object, Object> getMap() {
        // Load the last saved state once
        if (map == null) {
            Properties props = new Properties();
            try {
                props.load(new FileInputStream(propsFile));
            } catch (IOException e) {
                // File may not exist so ignore
            }
            map = new HashMap<Object, Object>(props);
        }
        return map;
    }

    /**
     * Add an entry
     * 
     * @param key
     * @param value
     */
    static void add(String key, String value) {
        getMap().put(key, value);
        save();
    }

    /**
     * Remove an entry
     * 
     * @param key
     */
    static void remove(String key) {
        getMap().remove(key);
        save();
    }

    private static void save() {
        // Transfer data into a Properties object to save it
        Properties props = new Properties();
        Set<Object> keys = map.keySet();
        for (Object object : keys) {
            String key = (String) object;
            String value = (String) map.get(key);
            props.setProperty(key, value);
        }

        try {
            props.store(new FileOutputStream(propsFile), null);
        } catch (IOException e) {
            IdeUtil.logError("Unable to save LibraryLocalizationBundle", e);
        }
    }

    public Object handleGetObject(String key) {
        if (key == null) {
            throw new NullPointerException();
        }

        return getMap().get(key);
    }

    public Enumeration<String> getKeys() {
        final Set<Object> keys = getMap().keySet();
        return new Enumeration<String>() {
            Iterator<Object> it = keys.iterator();

            public boolean hasMoreElements() {
                return it.hasNext();
            }

            public String nextElement() {
                // Elements are always a String
                return (String) it.next();
            }
        };
    }

}
