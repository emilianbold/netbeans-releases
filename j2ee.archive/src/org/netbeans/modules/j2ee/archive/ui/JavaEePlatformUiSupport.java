/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.j2ee.archive.ui;

import java.util.Set;
import java.util.TreeSet;
import javax.swing.ComboBoxModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
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
                J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(serverInstanceIDs[i]);
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
                J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(id);
                if (platform != null && platform.getDisplayName().equals(jeepas[i].getJ2eePlatform().getDisplayName())) {
                    retVal = jeepas[i];
                }
            }
        }
        return retVal;
    }
    
    
    private static final class JavaEePlatformComboBoxModel //extends AbstractListModel implements ComboBoxModel {
            implements ListModel, ComboBoxModel {
        private transient JavaEePlatformAdapter[] j2eePlatforms;
        private transient String initialJ2eePlatform;
        private transient JavaEePlatformAdapter selectedJ2eePlatform;
        
        public JavaEePlatformComboBoxModel(String serverInstanceID) {
            initialJ2eePlatform = serverInstanceID;
            getJavaEePlatforms();
        }
        
        public Object getElementAt(int index) {
            return getJavaEePlatforms()[index];
        }
        
        public int getSize() {
            return getJavaEePlatforms().length;
        }
        
        public synchronized Object getSelectedItem() {
            return selectedJ2eePlatform;
        }
        
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
                    J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstanceIDs[i]);
                    if (j2eePlatform != null) {
                        //    if (j2eePlatform.getSupportedSpecVersions().contains(J2eeModule.JAVA_EE_50)) { // getSupportedModuleTypes().contains(J2eeModule.EJB)) {
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
                            if (j2eePlatform.getSupportedSpecVersions().contains(J2eeModule.JAVA_EE_5) &&
                                    j2eePlatform.getSupportedModuleTypes().contains(J2eeModule.EJB)) {
                                String shortName = Deployment.getDefault().getServerID(serverInstanceIDs[i]);
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
        
        public void addListDataListener(ListDataListener l) {
        }
        
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
        
        public String toString() {
            return platform.getDisplayName();
        }
        
        public int compareTo(Object o) {
            JavaEePlatformAdapter oa = (JavaEePlatformAdapter)o;
            return toString().compareTo(oa.toString());
        }
    }
}
