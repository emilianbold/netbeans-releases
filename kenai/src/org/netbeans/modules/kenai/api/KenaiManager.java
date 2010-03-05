/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai.api;

import java.net.MalformedURLException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.prefs.Preferences;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 * Manager of Kenai instances
 * @author Jan Becicka
 */
public final class KenaiManager {

    private static KenaiManager instance;
    private TreeMap<String, Kenai> instances = new TreeMap<String, Kenai>();
    PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    public static final String PROP_INSTANCES = "prop_instances"; // NOI18N
    private Preferences prefs = NbPreferences.forModule(Kenai.class);
    private static final String INSTANCES_PREF="kenai.instances"; // NOI18N

    /**
     * singleton instance
     * @return
     */
    public static synchronized KenaiManager getDefault() {
        if (instance==null) {
            instance = new KenaiManager();
        }
        return instance;
    }

    private KenaiManager() {
    }
    
    /**
     * Creates a new instance of kenai server.
     * You probably want to use {@link #getKenai(java.lang.String)}
     * @param name display name
     * @param url
     * @return
     * @throws MalformedURLException
     */
    public synchronized Kenai createKenai(String name, String url) throws MalformedURLException {
        return addInstance(Kenai.createInstance(name, url));
    }
    
    private Kenai addInstance(Kenai instance) {
        synchronized (this) {
            initInstances();
            instances.put(instance.getUrl().toString(), instance);
            store();
        }
        propertyChangeSupport.firePropertyChange(PROP_INSTANCES, null, instance);
        return instance;
    }

    private void store() {
        StringBuffer b = new StringBuffer();
        Iterator<Kenai> it = instances.values().iterator();
        while (it.hasNext()) {
            Kenai n = it.next();
            b.append(n.getUrl()).append(',').append(n.getName());
            if (it.hasNext()) {
                b.append(';');
            }
        }
        prefs.put(INSTANCES_PREF, b.toString()); // NOI18N
    }


    /**
     * remove kenai instance from manager
     * @param instance
     */
    public void removeKenai(Kenai instance) {
        synchronized (this) {
            initInstances();
            instance.logout();
            instances.remove(instance.getUrl().toString());
            store();
        }
        propertyChangeSupport.firePropertyChange(PROP_INSTANCES, instance, null);
    }

    /**
     * returns all kenai instances registered in this manager
     * @return
     */
    public synchronized Collection<Kenai> getKenais() {
        initInstances();
        return instances.values();
    }

    private boolean instancesInited = false;

    private void initInstances() {
        if (instancesInited)
            return;
        String s = prefs.get(INSTANCES_PREF, ""); // NOI18N
        if (s.length() > 1) {
            for (String inst : s.split(";")) { // NOI18N
                if (inst.length()>0) {
                    try {
                        instances.put(inst.split(",")[0], Kenai.createInstance(inst.split(",")[1], inst.split(",")[0])); // NOI18N
                    } catch (MalformedURLException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        } else {
            if (instances.isEmpty()) {
                try {
                    instances.put("https://kenai.com", Kenai.createInstance("kenai.com", "https://kenai.com"));
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        instancesInited=true;
    }

    /**
     * get kenai instance for specified url
     * @param url
     * @return
     */
    public synchronized Kenai getKenai(String url) {
        initInstances();
        return instances.get(url);
    }

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
}


