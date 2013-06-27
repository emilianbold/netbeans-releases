/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.odcs.api;

import java.net.MalformedURLException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.prefs.Preferences;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 * Manager of ODCS instances
 * @author Jan Becicka, Ondra Vrabec
 */
public final class ODCSManager {

    private static ODCSManager instance;
    private TreeMap<String, ODCSServer> instances = new TreeMap<String, ODCSServer>();
    PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    public static final String PROP_INSTANCES = "prop_instances"; // NOI18N
    private Preferences prefs = NbPreferences.forModule(ODCSServer.class);
    private static final String INSTANCES_PREF="odcs.instances"; // NOI18N

    /**
     * singleton instance
     * @return
     */
    public static synchronized ODCSManager getDefault() {
        if (instance==null) {
            instance = new ODCSManager();
        }
        return instance;
    }

    private ODCSManager() {
    }
    
    /**
     * Creates a new instance of ODCS server.
     * @param displayName display name
     * @param url
     * @return
     * @throws MalformedURLException
     */
    public synchronized ODCSServer createServer(String displayName, String url) throws MalformedURLException {
        return addInstance(ODCSServer.createInstance(displayName, url));
    }    
    
    private ODCSServer addInstance(ODCSServer instance) {
        synchronized (this) {
            initInstances();
            instances.put(instance.getUrl().toString(), instance);
            store();
        }
        propertyChangeSupport.firePropertyChange(PROP_INSTANCES, null, instance);
        return instance;
    }

    public void store() {
        StringBuilder b = new StringBuilder();
        Iterator<ODCSServer> it = instances.values().iterator();
        while (it.hasNext()) {
            ODCSServer n = it.next();
            b.append(n.getUrl()).append(',').append(n.getDisplayName());
            if (it.hasNext()) {
                b.append(';');
            }
        }
        prefs.put(INSTANCES_PREF, b.toString()); // NOI18N
    }


    /**
     * remove ODCS server instance from manager
     * @param instance
     */
    public void removeServer (ODCSServer instance) {
        synchronized (this) {
            initInstances();
            instance.logout();
            instances.remove(instance.getUrl().toString());
            store();
        }
        propertyChangeSupport.firePropertyChange(PROP_INSTANCES, instance, null);
    }

    /**
     * returns all ODCS server instances registered in this manager
     * @return
     */
    public synchronized Collection<ODCSServer> getServers () {
        initInstances();
        return new ArrayList(instances.values());
    }

    private boolean instancesInited = false;

    private void initInstances() {
        if (instancesInited) {
            return;
        }
        String s = prefs.get(INSTANCES_PREF, ""); // NOI18N
        if (s.length() > 1) {
            for (String inst : s.split(";")) { // NOI18N
                if (inst.length()>0) {
                    try {
                        instances.put(inst.split(",")[0], ODCSServer.createInstance(inst.split(",")[1], inst.split(",")[0])); //NOI18N
                    } catch (MalformedURLException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        } 
        instancesInited=true;
    }

    /**
     * get instance for specified url
     * @param url
     * @return
     */
    public synchronized ODCSServer getServer(String url) {
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