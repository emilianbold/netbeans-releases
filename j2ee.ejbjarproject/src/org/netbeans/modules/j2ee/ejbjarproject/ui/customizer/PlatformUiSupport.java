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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProjectType;
import org.netbeans.modules.j2ee.ejbjarproject.UpdateHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Support class for {@link JavaPlatform} manipulation in j2seproject customizer.
 * @author tzezula
 */
public class PlatformUiSupport {
    
    private static final String DEFAULT_JAVAC_TARGET = "${default.javac.target}";  //NOI18N
    
    private PlatformUiSupport() {
    }
    
    /**
     * Creates {@link ComboBoxModel} of J2SE platforms.
     * The model listens on the {@link JavaPlatformManager} and update its
     * state according to changes
     * @param activePlatform the active project's platform 
     * @return {@link ComboBoxModel}
     */
    public static ComboBoxModel createComboBoxModel (String activePlatform) {
        return new PlatformComboBoxModel (activePlatform);
    }
       
    /**
     * Stores active platform into project's metadata
     * @param props project's shared properties
     * @param helper to read/update project.xml
     * @param platformDisplayName the patform's display name
     * @param sourceLevel source level
     */
    public static void storePlatform (EditableProperties props, UpdateHelper helper, String platformDisplayName, SpecificationVersion sourceLevel) {
        JavaPlatform platform = getPlatform(platformDisplayName);
        //null means active broken (unresolved) platform, no need to do anything
        if (platform != null) {
            String platformAntName = (String) platform.getProperties().get("platform.ant.name");    //NOI18N        
            assert platformAntName != null;
            props.put(EjbJarProjectProperties.JAVA_PLATFORM, platformAntName);
            Element root = helper.getPrimaryConfigurationData(true);
            boolean defaultPlatform = JavaPlatformManager.getDefault().getDefaultPlatform().getDisplayName().equals(platformDisplayName);        
            boolean changed = false;
            NodeList explicitPlatformNodes = root.getElementsByTagNameNS (EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,"explicit-platform");   //NOI18N
            if (defaultPlatform) {                
                if (explicitPlatformNodes.getLength()==1) {
                    root.removeChild(explicitPlatformNodes.item(0));
                    changed = true;
                }
                if (!DEFAULT_JAVAC_TARGET.equals(props.getProperty(EjbJarProjectProperties.JAVAC_TARGET))) {
                    props.setProperty (EjbJarProjectProperties.JAVAC_TARGET, DEFAULT_JAVAC_TARGET);
                }
            }
            else {
                Element explicitPlatform;
                
                switch (explicitPlatformNodes.getLength()) {
                    case 0:
                        explicitPlatform = root.getOwnerDocument().createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "explicit-platform"); //NOI18N
                        root.appendChild(explicitPlatform);
                        changed = true;
                        break;
                    case 1:
                        explicitPlatform = (Element)explicitPlatformNodes.item(0);
                        break;
                    default:
                        throw new AssertionError("Broken project.xml file"); // NOI18N
                }
                SpecificationVersion jdk13 = new SpecificationVersion("1.3"); // NOI18N
                String explicitSourceAttrValue = explicitPlatform.getAttribute("explicit-source-supported"); // NOI18N
                if (jdk13.compareTo(platform.getSpecification().getVersion())>=0 &&
                    "true".equals(explicitSourceAttrValue)) {   //NOI18N
                    explicitPlatform.setAttribute("explicit-source-supported","false"); //NOI18N
                    changed = true;
                }
                else if (jdk13.compareTo(platform.getSpecification().getVersion())<0 &&
                    !"true".equals(explicitSourceAttrValue)) {  //NOI18N
                    explicitPlatform.setAttribute("explicit-source-supported","true"); //NOI18N
                    changed = true;
                }
                if (sourceLevel == null) {
                    sourceLevel = platform.getSpecification().getVersion();
                }
                String javacSource = sourceLevel.toString();
                if (!javacSource.equals(props.getProperty(EjbJarProjectProperties.JAVAC_TARGET))) {                    
                    props.setProperty (EjbJarProjectProperties.JAVAC_TARGET, javacSource);
                }
            }
            if (changed) {
                helper.putPrimaryConfigurationData(root, true);
            }
        }
    }
    
    /**
     * Creates {@link ComboBoxModel} of source levels for active platform.
     * The model listens on the platform's {@link ComboBoxModel} and update its
     * state according to changes
     * @param platformComboBoxModel the platform's model used for listenning
     * @param initialValue initial source level value
     * @return {@link ComboBoxModel} of {@link SpecificationVersion}
     */
    public static ComboBoxModel createSourceLevelComboBoxModel (ComboBoxModel platformComboBoxModel, String initialValue) {
        return new SourceLevelComboBoxModel (platformComboBoxModel, initialValue);
    }
    
    private static JavaPlatform getPlatform (String displayName) {
        JavaPlatform[] platforms = JavaPlatformManager.getDefault().getPlatforms(displayName, new Specification("j2se",null));  //NOI18N
        return platforms.length == 0 ? null : platforms[0];
    }
    
    private static String getAntPlatformName (String displayName) {        
        JavaPlatform platform = getPlatform(displayName);
        return  platform == null ? null : (String) platform.getProperties().get("platform.ant.name");    //NOI18N
    }
           
    private static class PlatformComboBoxModel extends AbstractListModel implements ComboBoxModel, PropertyChangeListener {
        
        private JavaPlatformManager pm;
        private String[] platformNamesCache;
        private String initialPlatform;
        private String selectedPlatform;
        
        public PlatformComboBoxModel (String initialPlatform) {
            this.pm = JavaPlatformManager.getDefault();
            this.pm.addPropertyChangeListener(WeakListeners.propertyChange(this, this.pm));
            this.initialPlatform = initialPlatform;
        }
        
        public int getSize () {
            String[] platformNames = getPlatformNames ();
            return platformNames.length;
        }
        
        public Object getElementAt (int index) {
            String[] platformNames = getPlatformNames ();
            assert index >=0 && index< platformNames.length;
            return platformNames[index];
        }
        
        public Object getSelectedItem () {
            this.getPlatformNames(); //Force setting of selectedPlatform if it is not alredy done
            return this.selectedPlatform;
        }
        
        public void setSelectedItem (Object obj) {
            this.selectedPlatform = (String) obj;
            this.fireContentsChanged(this, -1, -1);
        }
        
        public void propertyChange (PropertyChangeEvent event) {
            if (JavaPlatformManager.PROP_INSTALLED_PLATFORMS.equals(event.getPropertyName())) {
                synchronized (this) {
                    this.platformNamesCache = null;
                }
                this.fireContentsChanged(this, -1, -1);
            }
        }
        
        private synchronized String[] getPlatformNames () {
            if (this.platformNamesCache == null) {
                JavaPlatform[] platforms = pm.getPlatforms (null, new Specification("j2se",null));    //NOI18N
                Set orderedNames = new TreeSet ();
                boolean activeFound = false;
                for (int i=0; i< platforms.length; i++) {
                    if (platforms[i].getInstallFolders().size()>0) {
                        String displayName = platforms[i].getDisplayName();
                        orderedNames.add (displayName);                    
                        if (!activeFound && initialPlatform != null) {
                            String antName = (String) platforms[i].getProperties().get("platform.ant.name");    //NOI18N
                            if (initialPlatform.equals(antName)) {
                                if (this.selectedPlatform == null) {
                                    this.selectedPlatform = displayName;
                                }
                                activeFound = true;
                            }
                        }
                    }                    
                }
                if (!activeFound) {
                    if (initialPlatform == null) {
                        if (this.selectedPlatform == null) {
                            this.selectedPlatform = JavaPlatformManager.getDefault().getDefaultPlatform().getDisplayName();
                        }
                    }
                    else {
                        orderedNames.add(this.initialPlatform);
                        if (this.selectedPlatform == null) {
                            this.selectedPlatform = initialPlatform;
                        }
                    }
                }
                this.platformNamesCache = (String[]) orderedNames.toArray(new String[orderedNames.size()]);
            }
            return this.platformNamesCache;                    
        }
        
    }
    
    private static class SourceLevelComboBoxModel extends AbstractListModel implements ComboBoxModel, ListDataListener {
        
        private static final String VERSION_PREFIX = "1.";      //The version prefix
        private static final int INITIAL_VERSION_MINOR = 2;     //1.2
        
        private SpecificationVersion selectedSourceLevel;
        private SpecificationVersion[] sourceLevelCache;
        private final ComboBoxModel platformComboBoxModel;
        private String activePlatform;
        
        public SourceLevelComboBoxModel (ComboBoxModel platformComboBoxModel, String initialValue) {            
            this.platformComboBoxModel = platformComboBoxModel;
            this.activePlatform = (String) this.platformComboBoxModel.getSelectedItem();
            this.platformComboBoxModel.addListDataListener (this);
            if (initialValue != null && initialValue.length()>0) {
                this.selectedSourceLevel = new SpecificationVersion (initialValue);
            }
        }
                
        public int getSize () {
            SpecificationVersion[] sLevels = getSourceLevels ();
            return sLevels.length;
        }
        
        public Object getElementAt (int index) {
            SpecificationVersion[] sLevels = getSourceLevels ();
            assert index >=0 && index< sLevels.length;
            return sLevels[index];
        }
        
        public Object getSelectedItem () {
            List sLevels = Arrays.asList(getSourceLevels ());
            if (this.selectedSourceLevel != null) {
                if (!sLevels.contains(this.selectedSourceLevel)) {
                    if (sLevels.size()>0) {
                        this.selectedSourceLevel = (SpecificationVersion) sLevels.get(sLevels.size()-1);
                    }
                    else {
                        this.selectedSourceLevel = null;
                    }
                }            
            }
            return this.selectedSourceLevel;
        }
        
        public void setSelectedItem (Object obj) {
            this.selectedSourceLevel = (SpecificationVersion) obj;
            this.fireContentsChanged(this, -1, -1);
        }
        
        public void intervalAdded(ListDataEvent e) {
        }

        public void intervalRemoved(ListDataEvent e) {
        }

        public void contentsChanged(ListDataEvent e) {
            String selectedPlatform = (String) this.platformComboBoxModel.getSelectedItem();
            JavaPlatform platform = getPlatform(selectedPlatform);
            if (platform != null) {
                SpecificationVersion version = platform.getSpecification().getVersion();
                if (this.selectedSourceLevel != null && this.selectedSourceLevel.compareTo(version)>0 &&
                    !shouldChangePlatform (selectedSourceLevel, version)) {
                    //Restore original
                    this.platformComboBoxModel.setSelectedItem(this.activePlatform);                            
                    return;
                }
            }
            this.activePlatform = selectedPlatform;            
            resetCache();
        }
        
        private void resetCache () {            
            synchronized (this) {
                this.sourceLevelCache = null;                
            }
            this.fireContentsChanged(this, -1, -1);
        }
        
        private SpecificationVersion[] getSourceLevels () {
            if (this.sourceLevelCache == null) {
                String selectedPlatform = (String) this.platformComboBoxModel.getSelectedItem();
                JavaPlatform platform = getPlatform(selectedPlatform);
                List/*<SpecificationVersion>*/ sLevels = new ArrayList ();
                //If platform == null broken platform, the source level range is unknown
                //The source level combo box should be empty and disabled
                if (platform != null) {
                    SpecificationVersion version = platform.getSpecification().getVersion();
                    int index = INITIAL_VERSION_MINOR;
                    SpecificationVersion template = new SpecificationVersion (VERSION_PREFIX + Integer.toString (index++));                    
                    while (template.compareTo(version)<=0) {
                        sLevels.add (template);
                        template = new SpecificationVersion (VERSION_PREFIX + Integer.toString (index++));
                    }
                }
                this.sourceLevelCache = (SpecificationVersion[]) sLevels.toArray(new SpecificationVersion[sLevels.size()]);
            }
            return this.sourceLevelCache;
        }               

        private static boolean shouldChangePlatform (SpecificationVersion selectedSourceLevel, SpecificationVersion platformSourceLevel) {
            JButton changeOption = new JButton (NbBundle.getMessage(PlatformUiSupport.class, "CTL_ChangePlatform"));
            changeOption.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PlatformUiSupport.class, "AD_ChangePlatform"));
            String message = MessageFormat.format (NbBundle.getMessage(PlatformUiSupport.class,"TXT_ChangePlatform"),new Object[] {
                selectedSourceLevel.toString(),
                platformSourceLevel.toString(),
            });
            return DialogDisplayer.getDefault().notify(
                new NotifyDescriptor (message,
                        NbBundle.getMessage(PlatformUiSupport.class,"TXT_ChangePlatformTitle"),
                        NotifyDescriptor.DEFAULT_OPTION,
                        NotifyDescriptor.WARNING_MESSAGE,
                        new Object[] {
                            changeOption,
                            NotifyDescriptor.CANCEL_OPTION
                        },
                        changeOption)) == changeOption;
        }        
    }
    
}
