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

package org.netbeans.modules.kenai.ui.nodes;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.ui.dashboard.DashboardImpl;
import org.netbeans.modules.kenai.ui.spi.UIUtils;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Becicka
 */
public class KenaiInstancesManager {

    private static KenaiInstancesManager instance;
    private List<KenaiInstance> instances = new ArrayList();
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    public static final String PROP_INSTANCES = "prop_instances"; // NOI18N
    private Preferences prefs = NbPreferences.forModule(Kenai.class);
    private static final String INSTANCES_PREF="kenai.instances"; // NOI18N

    public static synchronized KenaiInstancesManager getDefault() {
        if (instance==null) {
            instance = new KenaiInstancesManager();
        }
        return instance;
    }

    private KenaiInstancesManager() {
        String s = prefs.get(INSTANCES_PREF, ""); // NOI18N
        if (s.length() > 1) {
            for (String inst : s.split(";")) { // NOI18N
                if (inst.length()>0) {
                    instances.add(new KenaiInstance(inst.split(",")[0], inst.split(",")[1])); // NOI18N
                }
            }
        }
        Kenai aDefault = Kenai.getDefault();
        String def = aDefault.getUrl().toString();
        String name = aDefault.getName();
        KenaiInstance i = new KenaiInstance(def, name);
        if (!instances.contains(i)) {
            instances.add(i);
            store();
        }
    }
    
    public void addInstance(KenaiInstance instance) {
        instances.add(instance);
        store();
        propertyChangeSupport.firePropertyChange(PROP_INSTANCES, null, instance);
    }

    private void store() {
        StringBuffer b = new StringBuffer();
        Iterator<KenaiInstance> it = instances.iterator();
        while (it.hasNext()) {
            b.append(it.next());
            if (it.hasNext()) {
                b.append(";"); // NOI18N
            }
        }
        prefs.put(INSTANCES_PREF, b.toString()); // NOI18N
    }


    public boolean removeInstance(KenaiInstance instance) {
        boolean r = instances.remove(instance);
        store();
        propertyChangeSupport.firePropertyChange(PROP_INSTANCES, instance, null);
        return r;
    }

    public Collection<KenaiInstance> getInstances() {
        return Collections.unmodifiableList(instances);
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

    public void setDefaultInstance(final URL url) {
        RequestProcessor.getDefault().post(new Runnable() {

            public void run() {
                Kenai.getDefault().setUrl(url);
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        UIUtils.tryLogin(false);
                        DashboardImpl.getInstance().refreshNonMemberProjects();
                    }
                });
            }
        });
    }
}


