/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.project;

import java.lang.String;
import java.util.*;
import org.openide.util.NbCollections;

/**
 * Adapts a Properties object so it can be used as a Map&lt;String,String&gt;
 *
 * @author Tim Boudreau
 */
final class PropertiesMapAdapter implements Map<String,String> {
    private final Properties props;

    public PropertiesMapAdapter(Properties props) {
        this.props = props;
    }

    public Collection<String> values() {
        List<String> result = new LinkedList<String>();
        for (String key : keySet()) {
            result.add (props.getProperty(key));
        }
        return result;
    }


    public synchronized int size() {
        return props.size();
    }

    public synchronized Enumeration<Object> keys() {
        return props.keys();
    }


    public Set<String> keySet() {
        return NbCollections.checkedSetByFilter(props.keySet(), String.class, false);
    }


    public synchronized boolean isEmpty() {
        return props.isEmpty();
    }


    @Override
    public synchronized int hashCode() {
        return PropertiesMapAdapter.class.hashCode() * props.hashCode();
    }


    public synchronized String get(Object key) {
        return props.getProperty((String) key);
    }


    @Override
    public synchronized boolean equals(Object o) {
        return o != null && PropertiesMapAdapter.class == o.getClass()
                && ((PropertiesMapAdapter)o).props.equals(o);
    }


    @SuppressWarnings("Unchecked")
    public Set<Entry<String, String>> entrySet() {
//        HashSet<Entry<String, String>> result = new HashSet<Map.Entry<String,String>>();
//        for (String k : keySet()) {
//            result.add (new E(k, props.getProperty(k)));
//        }
//        return result;
        //Somewhat dangerous but less expensive
        Set set = props.entrySet();
        return (Set<Entry<String, String>>) set;
    }

    public synchronized Enumeration<Object> elements() {
        return props.elements();
    }


    public boolean containsValue(Object value) {
        return props.containsValue(value);
    }


    public synchronized boolean containsKey(Object key) {
        return props.containsKey(key);
    }

    public synchronized boolean contains(Object value) {
        return props.contains(value);
    }


    public String put(String key, String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    public String remove(Object key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    public void putAll(Map<? extends String, ? extends String> m) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    public void clear() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
/*
    private static final class E implements Map.Entry<String,String> {
        private final String key;
        private final String val;

        public E(String key, String val) {
            this.key = key;
            this.val = val;
        }


        public String getKey() {
            return key;
        }


        public String getValue() {
            return val;
        }


        public String setValue(String value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
 */
}
