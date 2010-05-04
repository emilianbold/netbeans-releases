/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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


package org.netbeans.modules.j2ee.archive.ui;

import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;

/**
 *
 * @author Andrei Badea
 * @thief  vince kraemer
 */
public class JavaEePlatformUiSupport {
    
    private JavaEePlatformUiSupport() {
    }
    
    public static ComboBoxModel createPlatformComboBoxModel(String serverInstanceId) {
        return new JavaEePlatformComboBoxModel(serverInstanceId);
    }
    
    public static String getServerInstanceID(Object j2eePlatformModelObject) {
        String retVal = null;
        if (j2eePlatformModelObject != null) {
            
            J2eePlatform j2eePlatform = ((JavaEePlatformAdapter)j2eePlatformModelObject).getJ2eePlatform();
            String[] serverInstanceIDs = Deployment.getDefault().getServerInstanceIDs();
            for (int i = 0; retVal == null && i < serverInstanceIDs.length; i++) {
                J2eePlatform platform = null;
                try {
                    platform = Deployment.getDefault().getServerInstance(serverInstanceIDs[i]).getJ2eePlatform();
                } catch (InstanceRemovedException ex) {
                    Logger.getLogger("global").log(Level.INFO, serverInstanceIDs[i], ex);
                }
                if (platform != null && platform.getDisplayName().equals(j2eePlatform.getDisplayName())) {
                    retVal = serverInstanceIDs[i];
                }
            }
        }
        
        return retVal;
    }
    
    public static Object getServerInstanceID(String id) {
        Object retVal = null;
        if (null != id) {
            
            JavaEePlatformComboBoxModel jeepcbm = new JavaEePlatformComboBoxModel(null);
            JavaEePlatformAdapter[] jeepas = jeepcbm.getJavaEePlatforms();
            for (int i = 0; retVal == null && i < jeepas.length; i++) {
                J2eePlatform platform = null;
                try {
                    platform = Deployment.getDefault().getServerInstance(id).getJ2eePlatform();
                } catch (InstanceRemovedException ex) {
                    Logger.getLogger("global").log(Level.INFO, id, ex);
                }
                if (platform != null && platform.getDisplayName().equals(jeepas[i].getJ2eePlatform().getDisplayName())) {
                    retVal = jeepas[i];
                }
            }
        }
        return retVal;
    }
    
    
    private static final class JavaEePlatformComboBoxModel //extends AbstractListModel implements ComboBoxModel {
            implements ListModel, ComboBoxModel {
        private JavaEePlatformAdapter[] j2eePlatforms;
        private String initialJ2eePlatform;
        private JavaEePlatformAdapter selectedJ2eePlatform;
        
        public JavaEePlatformComboBoxModel(String serverInstanceID) {
            initialJ2eePlatform = serverInstanceID;
            getJavaEePlatforms();
        }
        
        @Override
        public Object getElementAt(int index) {
            return getJavaEePlatforms()[index];
        }
        
        @Override
        public int getSize() {
            return getJavaEePlatforms().length;
        }
        
        @Override
        public synchronized Object getSelectedItem() {
            return selectedJ2eePlatform;
        }
        
        @Override
        public synchronized void setSelectedItem(Object obj) {
            selectedJ2eePlatform = (JavaEePlatformAdapter)obj;
        }
        
        private synchronized JavaEePlatformAdapter[] getJavaEePlatforms() {
            if (j2eePlatforms == null) {
                String[] serverInstanceIDs = Deployment.getDefault().getServerInstanceIDs();
                Set orderedNames = new TreeSet();
                boolean activeFound = false;
                JavaEePlatformAdapter firstAdapter = null;
                
                boolean sjasFound = false;
                for (int i = 0; i < serverInstanceIDs.length; i++) {
                    J2eePlatform j2eePlatform = null;
                    try {
                        j2eePlatform = Deployment.getDefault().getServerInstance(serverInstanceIDs[i]).getJ2eePlatform();
                    } catch (InstanceRemovedException ex) {
                        Logger.getLogger("global").log(Level.INFO, serverInstanceIDs[i], ex);
                    }
                    if (j2eePlatform != null) {
                        JavaEePlatformAdapter adapter = new JavaEePlatformAdapter(j2eePlatform);
                        orderedNames.add(adapter);
                        
                        if (selectedJ2eePlatform == null && !activeFound && initialJ2eePlatform != null) {
                            if (serverInstanceIDs[i].equals(initialJ2eePlatform)) {
                                selectedJ2eePlatform = adapter;
                                activeFound = true;
                            }
                        }
                        if (firstAdapter == null || !sjasFound) {
                            // try to pick a glassfish instance
                            if (j2eePlatform.getSupportedProfiles().contains(Profile.JAVA_EE_5) &&
                                    j2eePlatform.getSupportedTypes().contains(J2eeModule.Type.EJB)) {
                                String shortName = null;
                                try {
                                    shortName = Deployment.getDefault().getServerInstance(serverInstanceIDs[i]).getServerID();
                                } catch (InstanceRemovedException ex) {
                                    Logger.getLogger("global").log(Level.INFO, serverInstanceIDs[i], ex);
                                }
                                if ("J2EE".equals(shortName)) { // NOI18N
                                    firstAdapter = adapter;
                                    sjasFound = true;
                                }
                                else // prefer JBoss instance if GF not found
                                if ("JBoss4".equals(shortName)) { // NOI18N
                                    firstAdapter = adapter;
                                }
                            }
                        }
                    }
                    
                }
                if (selectedJ2eePlatform == null) {
                    if (null != firstAdapter) {
                        selectedJ2eePlatform = firstAdapter;
                    }
                }
                
                //j2eePlatforms = (J2eePlatform[])orderedNames.values().toArray(new J2eePlatform[orderedNames.size()]);
                j2eePlatforms = (JavaEePlatformAdapter[])orderedNames.toArray(new JavaEePlatformAdapter[orderedNames.size()]);
            }
            return j2eePlatforms;
        }
        
        @Override
        public void addListDataListener(ListDataListener l) {
        }
        
        @Override
        public void removeListDataListener(ListDataListener l) {
        }
    }
    
    public static boolean getJ2eePlatformAndSpecVersionMatch(Object j2eePlatformModelObject, Object j2eeSpecVersionModelObject) {
        if (!(j2eePlatformModelObject instanceof JavaEePlatformAdapter && j2eeSpecVersionModelObject instanceof String)) {
            return false;
        }
        
        J2eePlatform j2eePlatform = ((JavaEePlatformAdapter)j2eePlatformModelObject).getJ2eePlatform();
        String specVersion = (String)j2eeSpecVersionModelObject;
        return j2eePlatform.getSupportedSpecVersions().contains(specVersion);
    }
    
    private static final class JavaEePlatformAdapter implements Comparable {
        private J2eePlatform platform;
        
        public JavaEePlatformAdapter(J2eePlatform platform) {
            this.platform = platform;
        }
        
        public J2eePlatform getJ2eePlatform() {
            return platform;
        }
        
        @Override
        public String toString() {
            return platform.getDisplayName();
        }
        
        @Override
        public int compareTo(Object o) {
            JavaEePlatformAdapter oa = (JavaEePlatformAdapter)o;
            return toString().compareTo(oa.toString());
        }
    }
}
