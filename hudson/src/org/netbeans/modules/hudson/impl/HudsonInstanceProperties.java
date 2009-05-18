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

package org.netbeans.modules.hudson.impl;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.netbeans.modules.hudson.constants.HudsonInstanceConstants.*;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

/**
 * Instance properties for Hudson instance
 *
 * @author Michal Mocnak
 */
public class HudsonInstanceProperties extends HashMap<String,String> {
    
    private Sheet.Set set;
    
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    public HudsonInstanceProperties(String name, String url, String sync) {
        put(INSTANCE_NAME, name);
        put(INSTANCE_URL, url);
        put(INSTANCE_SYNC, sync);
    }
    
    public HudsonInstanceProperties(Map<String,String> properties) {
        super(properties);
    }

    @Override
    public synchronized String put(String key, String value) {
        String o = super.put(key, value);
        pcs.firePropertyChange(key, o, value);
        return o;
    }
    
    @Override
    public synchronized String remove(Object key) {
        String o = super.remove((String) key);
        pcs.firePropertyChange((String) key, o, null);
        return o;
    }

    public boolean isPersisted() {
        return true;
    }
    
    public Sheet.Set getSheetSet() {
        if (null == set) {
            set = Sheet.createPropertiesSet();
            
            // Set display name
            set.setDisplayName(get(INSTANCE_NAME));
            
            // Put properties in
            set.put(new PropertySupport[] {
                new HudsonInstanceProperty(INSTANCE_NAME,
                        NbBundle.getMessage(HudsonInstanceProperties.class, "TXT_Instance_Prop_Name"),
                        NbBundle.getMessage(HudsonInstanceProperties.class, "DESC_Instance_Prop_Name"),
                        true, false),
                        new HudsonInstanceProperty(INSTANCE_URL,
                        NbBundle.getMessage(HudsonInstanceProperties.class, "TXT_Instance_Prop_Url"),
                        NbBundle.getMessage(HudsonInstanceProperties.class, "DESC_Instance_Prop_Url"),
                        true, false),
                        new PropertySupport<Integer>(INSTANCE_SYNC, Integer.class,
                        NbBundle.getMessage(HudsonInstanceProperties.class, "TXT_Instance_Prop_Sync"),
                        NbBundle.getMessage(HudsonInstanceProperties.class, "DESC_Instance_Prop_Sync"),
                        true, true) {
                            public Integer getValue() {
                                return Integer.valueOf(get(INSTANCE_SYNC));
                            }
                            public void setValue(Integer val) {
                                put(INSTANCE_SYNC, val.toString());
                            }
                        }
            });
        }
        
        return set;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    public List<PropertyChangeListener> getCurrentListeners() {
        return Arrays.asList(pcs.getPropertyChangeListeners());
    }

    private class HudsonInstanceProperty extends PropertySupport<String> {
        
        private String key;
        
        public HudsonInstanceProperty(String key, String name, String desc, boolean read, boolean write) {
            super(key, String.class, name, desc, read, write);
            
            this.key = key;
        }
        
        @Override
        public void setValue(String value) {
            put(key, value);
        }
        
        @Override
        public String getValue() {
            return get(key);
        }
    }

    public static List<String> split(String prop) {
        return prop != null && prop.trim().length() > 0 ?
            Arrays.asList(prop.split("/")) : // NOI18N
            Collections.<String>emptyList();
    }

    public static String join(List<String> pieces) {
        StringBuilder b = new StringBuilder();
        for (String piece : pieces) {
            assert !piece.contains("/") : piece;
            if (b.length() > 0) {
                b.append('/');
            }
            b.append(piece);
        }
        return b.toString();
    }

}
