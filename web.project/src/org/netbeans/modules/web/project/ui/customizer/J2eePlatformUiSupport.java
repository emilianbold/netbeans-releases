/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.project.ui.customizer;

import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;

/**
 *
 * @author Andrei Badea, Radko Najman
 */
public class J2eePlatformUiSupport {
    
    private J2eePlatformUiSupport() {
    }
    
    public static ComboBoxModel createPlatformComboBoxModel(String serverInstanceId) {
        return new J2eePlatformComboBoxModel(serverInstanceId);
    }
    
    public static String getServerInstanceID(Object j2eePlatformModelObject) {
        if (j2eePlatformModelObject == null)
            return null;

        J2eePlatform j2eePlatform = ((J2eePlatformAdapter)j2eePlatformModelObject).getJ2eePlatform();
        String[] serverInstanceIDs = Deployment.getDefault().getServerInstanceIDs();
        for (int i = 0; i < serverInstanceIDs.length; i++) {
            J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(serverInstanceIDs[i]);
            if (platform != null && platform.getDisplayName().equals(j2eePlatform.getDisplayName())) {
                return serverInstanceIDs[i];
            }
        }
        
        return null;
    }
    
    /**
     * TODO: AB: Temporary fix of #54544.
     */
    public static void setSelectedPlatform(ComboBoxModel model, String serverInstanceID) {
        if (!(model instanceof J2eePlatformComboBoxModel))
            return;
        
        ((J2eePlatformComboBoxModel)model).setSelectedItem(serverInstanceID);
    }
    
    private static final class J2eePlatformComboBoxModel extends AbstractListModel implements ComboBoxModel {
        private J2eePlatformAdapter[] j2eePlatforms;
        private String initialJ2eePlatform;
        private J2eePlatformAdapter selectedJ2eePlatform;
        
        public J2eePlatformComboBoxModel(String serverInstanceID) {
            initialJ2eePlatform = serverInstanceID;
            getJ2eePlatforms();
        }
        
        public Object getElementAt(int index) {
            return getJ2eePlatforms()[index];
        }

        public int getSize() {
            return getJ2eePlatforms().length;
        }
        
        public Object getSelectedItem() {
            return selectedJ2eePlatform;
        }
        
        public void setSelectedItem(Object obj) {
            selectedJ2eePlatform = (J2eePlatformAdapter)obj;
        }
        
        public void setSelectedItem(String serverInstanceID) {
            for (int i = 0; i < j2eePlatforms.length; i++) {
                if (j2eePlatforms[i].getServerInstanceID().equals(serverInstanceID)) {
                    selectedJ2eePlatform = j2eePlatforms[i];
                    return;
                }
            }
        }
                
        private synchronized J2eePlatformAdapter[] getJ2eePlatforms() {
            if (j2eePlatforms == null) {
                String[] serverInstanceIDs = Deployment.getDefault().getServerInstanceIDs();
                Set orderedNames = new TreeSet();
                boolean activeFound = false;

                for (int i = 0; i < serverInstanceIDs.length; i++) {
                    J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstanceIDs[i]);
                    if (j2eePlatform != null) {
                        if (j2eePlatform.getSupportedModuleTypes().contains(J2eeModule.WAR)) {
                            J2eePlatformAdapter adapter = new J2eePlatformAdapter(j2eePlatform, serverInstanceIDs[i]);
                            orderedNames.add(adapter);
                        
                            if (selectedJ2eePlatform == null && !activeFound && initialJ2eePlatform != null) {
                                if (serverInstanceIDs[i].equals(initialJ2eePlatform)) {
                                    selectedJ2eePlatform = adapter;
                                    activeFound = true;
                                }
                            }
                        }
                    }
                }
                j2eePlatforms = (J2eePlatformAdapter[])orderedNames.toArray(new J2eePlatformAdapter[orderedNames.size()]);
            }
            return j2eePlatforms;
        }
    }
        
    private static final class J2eePlatformAdapter implements Comparable {
        private J2eePlatform platform;
        private String serverInstanceID;
        
        public J2eePlatformAdapter(J2eePlatform platform, String serverInstanceID) {
            this.platform = platform;
            this.serverInstanceID = serverInstanceID;
        }
        
        public J2eePlatform getJ2eePlatform() {
            return platform;
        }
        
        public String getServerInstanceID() {
            return serverInstanceID;
        }
        
        public String toString() {
            return platform.getDisplayName();
        }

        public int compareTo(Object o) {
            J2eePlatformAdapter oa = (J2eePlatformAdapter)o;
            return toString().compareTo(oa.toString());
        }
    }
}
