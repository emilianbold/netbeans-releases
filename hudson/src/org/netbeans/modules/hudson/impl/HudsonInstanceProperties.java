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

package org.netbeans.modules.hudson.impl;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import static org.netbeans.modules.hudson.constants.HudsonInstanceConstants.*;
import static org.netbeans.modules.hudson.impl.Bundle.*;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

/**
 * Instance properties for Hudson instance
 *
 * @author Michal Mocnak
 */
public class HudsonInstanceProperties extends HashMap<String,String> {
    
    private Sheet.Set set;
    private static final RequestProcessor RP = new RequestProcessor(
            HudsonInstanceProperties.class);
    
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
    public final synchronized String put(String key, String value) {
        String o = super.put(key, value);
        pcs.firePropertyChange(key, o, value);
        if (key.equals(INSTANCE_NAME)) {
            loadPreferences();
        }
        updatePreferences(key);
        return o;
    }
    
    @Override
    public synchronized String remove(Object key) {
        String o = super.remove((String) key);
        pcs.firePropertyChange((String) key, o, null);
        updatePreferences((String) key);
        return o;
    }

    public final boolean isPersisted() {
        String pers = get(INSTANCE_PERSISTED);
        return pers == null || TRUE.equals(pers);
    }

    @Messages({
        "TXT_Instance_Prop_Name=Name",
        "DESC_Instance_Prop_Name=Hudson's instance name",
        "TXT_Instance_Prop_Url=URL",
        "DESC_Instance_Prop_Url=Hudson's instance URL",
        "TXT_Instance_Prop_Sync=Autosynchronization time",
        "DESC_Instance_Prop_Sync=Autosynchronization time in minutes (if it's 0 the autosynchronization is off)"
    })
    public Sheet.Set getSheetSet() {
        if (null == set) {
            set = Sheet.createPropertiesSet();
            
            // Set display name
            set.setDisplayName(get(INSTANCE_NAME));
            
            // Put properties in
            set.put(new Node.Property<?>[] {
                new HudsonInstanceProperty(INSTANCE_NAME,
                        TXT_Instance_Prop_Name(),
                        DESC_Instance_Prop_Name(),
                        true, false),
                        new HudsonInstanceProperty(INSTANCE_URL,
                        TXT_Instance_Prop_Url(),
                        DESC_Instance_Prop_Url(),
                        true, false),
                        new PropertySupport<Integer>(INSTANCE_SYNC, Integer.class,
                        TXT_Instance_Prop_Sync(),
                        DESC_Instance_Prop_Sync(),
                        true, true) {
                            @Override public Integer getValue() {
                                return Integer.valueOf(get(INSTANCE_SYNC));
                            }
                            @Override public void setValue(Integer val) {
                                if (val == null || val < 0) {
                                    throw new IllegalArgumentException();
                                }
                                put(INSTANCE_SYNC, val.toString());
                            }
                            public @Override boolean canWrite() {
                                return isPersisted();
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
        
        HudsonInstanceProperty(String key, String name, String desc, boolean read, boolean write) {
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

    /**
     * Get Preferences that this properties use as persistent storage.
     */
    public Preferences getPreferences() {
        String nodeName = getNodeName();
        if (nodeName != null) {
            return HudsonManagerImpl.instancePrefs().node(nodeName);
        } else {
            return null;
        }
    }

    /**
     * Check if there are existing preferences for this properties.
     */
    private boolean hasPreferences() {
        String nodeName = getNodeName();
        if (nodeName != null) {
            try {
                return HudsonManagerImpl.instancePrefs().nodeExists(nodeName);
            } catch (BackingStoreException ex) {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Get Preferences node name for this properties instance.
     */
    private String getNodeName() {
        String name = get(INSTANCE_NAME);
        if (name != null && !name.isEmpty()) {
            return HudsonManagerImpl.simplifyServerLocation(name, true);
        } else {
            return null;
        }
    }

    /**
     * Update persistent preferences in a background thread.
     */
    private void updatePreferences(final String... keys) {
        RP.post(new Runnable() {
            @Override
            public void run() {
                Preferences prefs = getPreferences();
                if (prefs != null) {
                    for (String key : keys) {
                        String val = get(key);
                        if (val == null) {
                            prefs.remove(key);
                        } else {
                            prefs.put(key, val);
                        }
                    }
                }
            }
        });
    }

    /**
     * Load preferences in background thread.
     */
    private void loadPreferences() {
        RP.post(new Runnable() {
            @Override
            public void run() {
                if (hasPreferences()) {
                    Preferences prefs = getPreferences();
                    if (prefs != null) {
                        try {
                            String[] keys = prefs.keys();
                            for (String key : keys) {
                                if (INSTANCE_NAME.equals(key)
                                        || INSTANCE_URL.equals(key)) {
                                    continue;
                                }
                                String val = prefs.get(key, null);
                                if (val != null) {
                                    put(key, val);
                                }
                            }
                        } catch (BackingStoreException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
        });
    }
}
