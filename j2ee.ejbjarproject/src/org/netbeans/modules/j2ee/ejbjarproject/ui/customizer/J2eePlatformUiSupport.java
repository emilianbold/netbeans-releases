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

package org.netbeans.modules.j2ee.ejbjarproject.ui.customizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class J2eePlatformUiSupport {
    
    private static final String J2EE_1_4_DISPLAY_NAME = NbBundle.getMessage(J2eePlatformUiSupport.class, "J2EE_1_4_displayName"); // NOI18N 
    private static final String J2EE_1_3_DISPLAY_NAME = NbBundle.getMessage(J2eePlatformUiSupport.class, "J2EE_1_3_displayName"); // NOI18N  

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
    
    public static ComboBoxModel createSpecVersionComboBoxModel(String j2eeSpecVersion) {
        return new J2eeSpecVersionComboBoxModel(j2eeSpecVersion);
    }
    
    public static String getSpecVersion(Object j2eeSpecVersionModelObject) {
        return ((J2eePlatformComboBoxItem)j2eeSpecVersionModelObject).getCode();
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
        
        private synchronized J2eePlatformAdapter[] getJ2eePlatforms() {
            if (j2eePlatforms == null) {
                String[] serverInstanceIDs = Deployment.getDefault().getServerInstanceIDs();
                Set orderedNames = new TreeSet();
                boolean activeFound = false;

                for (int i = 0; i < serverInstanceIDs.length; i++) {
                    J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstanceIDs[i]);
                    if (j2eePlatform != null) {
                        if (j2eePlatform.getSupportedModuleTypes().contains(J2eeModule.EJB)) {
                            J2eePlatformAdapter adapter = new J2eePlatformAdapter(j2eePlatform);
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
                //j2eePlatforms = (J2eePlatform[])orderedNames.values().toArray(new J2eePlatform[orderedNames.size()]);
                j2eePlatforms = (J2eePlatformAdapter[])orderedNames.toArray(new J2eePlatformAdapter[orderedNames.size()]);
            }
            return j2eePlatforms;
        }
     }
    
    private static final class J2eeSpecVersionComboBoxModel extends AbstractListModel implements ComboBoxModel {
        private J2eePlatformComboBoxItem[] j2eeSpecVersions;
        
        private J2eePlatformComboBoxItem initialJ2eeSpecVersion;
        private J2eePlatformComboBoxItem selectedJ2eeSpecVersion;
    
        public J2eeSpecVersionComboBoxModel(String j2eeSpecVersion) {
            initialJ2eeSpecVersion = new J2eePlatformComboBoxItem(j2eeSpecVersion);
            
            List orderedListItems = new ArrayList();
            orderedListItems.add(new J2eePlatformComboBoxItem(EjbJarProjectProperties.J2EE_1_4));
            if (!initialJ2eeSpecVersion.getCode().equals(EjbJarProjectProperties.J2EE_1_4))
                orderedListItems.add(0, new J2eePlatformComboBoxItem(EjbJarProjectProperties.J2EE_1_3));
            
            j2eeSpecVersions = (J2eePlatformComboBoxItem[])orderedListItems.toArray(new J2eePlatformComboBoxItem[orderedListItems.size()]);
            selectedJ2eeSpecVersion = initialJ2eeSpecVersion;
        }
        
        public Object getElementAt(int index) {
            return j2eeSpecVersions[index];
        }
        
        public int getSize() {
            return j2eeSpecVersions.length;
        }
        
        public Object getSelectedItem() {
            return selectedJ2eeSpecVersion;
        }
        
        public void setSelectedItem(Object obj) {
            selectedJ2eeSpecVersion = (J2eePlatformComboBoxItem)obj;
        }
    }
    
    private static final class J2eePlatformComboBoxItem{
        private String code;
        private String displayName;

        public J2eePlatformComboBoxItem (String code, String displayName){
            this.code = code;
            this.displayName = displayName;        
        }

        public J2eePlatformComboBoxItem (String code){
             this(code, findDisplayName(code));        
        }

        private static String findDisplayName(String code){
            if(code.equals(EjbJarProjectProperties.J2EE_1_4)) return J2EE_1_4_DISPLAY_NAME;
            if(code.equals(EjbJarProjectProperties.J2EE_1_3)) return J2EE_1_3_DISPLAY_NAME;
            return code; //version display name not found, use the version code for display name        
        }

        public String getCode(){
            return code;        
        }

        public String toString(){
            return displayName;        
        }
    }

    public static boolean getJ2eePlatformAndSpecVersionMatch(Object j2eePlatformModelObject, Object j2eeSpecVersionModelObject) {
        if (!(j2eePlatformModelObject instanceof J2eePlatformAdapter && j2eeSpecVersionModelObject instanceof String))
            return false;
        
        J2eePlatform j2eePlatform = ((J2eePlatformAdapter)j2eePlatformModelObject).getJ2eePlatform();
        String specVersion = (String)j2eeSpecVersionModelObject;
        return j2eePlatform.getSupportedSpecVersions().contains(specVersion);
    }
    
    private static final class J2eePlatformAdapter implements Comparable {
        private J2eePlatform platform;
        
        public J2eePlatformAdapter(J2eePlatform platform) {
            this.platform = platform;
        }
        
        public J2eePlatform getJ2eePlatform() {
            return platform;
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
