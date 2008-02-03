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

package org.netbeans.modules.j2ee.clientproject.ui.customizer;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.j2ee.clientproject.AppClientProjectType;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlRenderer;
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
    
    private static final SpecificationVersion JDK_5 = new SpecificationVersion ("1.5");  //NOI18N
    private static final SpecificationVersion JDK_6 = new SpecificationVersion ("1.6");  //NOI18N
    private static final Logger LOGGER = Logger.getLogger(PlatformUiSupport.class.getName());
    
    private PlatformUiSupport() {
    }
    
    /**
     * Creates {@link ComboBoxModel} of J2SE platforms.
     * The model listens on the {@link JavaPlatformManager} and update its
     * state according to changes
     * @param activePlatform the active project's platform
     * @return {@link ComboBoxModel}
     */
    public static ComboBoxModel createPlatformComboBoxModel(String activePlatform) {
        return new PlatformComboBoxModel(activePlatform);
    }
    
    
    /**
     * Creates a {@link ListCellRenderer} for rendering items of the {@link ComboBoxModel}
     * created by the {@link PlatformUiSupport#createPlatformComboBoxModel} method.
     * @return {@link ListCellRenderer}
     */
    public static ListCellRenderer createPlatformListCellRenderer() {
        return new PlatformListCellRenderer();
    }
    
    /**
     * Like {@link #storePlatform}, but platformName may be null (in which case the default platform is used)
     */
    public static void storePlatform(EditableProperties props, UpdateHelper helper, String platformName, SpecificationVersion sourceLevel) {
        PlatformKey platformKey;
        if (platformName != null) {
            platformKey = new PlatformKey(PlatformUiSupport.findPlatform(platformName));
        } else {
            platformKey = new PlatformKey(JavaPlatformManager.getDefault().getDefaultPlatform());
        }
        storePlatform(props, helper, platformKey, new SourceLevelKey(sourceLevel));
    }
    
    public static JavaPlatform findPlatform(String displayName) {
        JavaPlatform[] platforms = JavaPlatformManager.getDefault().getPlatforms(displayName, new Specification("j2se", null)); //NOI18N
        return platforms.length == 0 ? null : platforms[0];
    }
    
    /**
     * Stores active platform, javac.source and javac.target into the project's metadata
     * @param props project's shared properties
     * @param helper to read/update project.xml
     * @param platformKey the PatformKey got from the platform model
     * @param sourceLevel source level
     */
    public static void storePlatform(EditableProperties props, UpdateHelper helper, Object platformKey, Object sourceLevelKey) {
        assert platformKey instanceof PlatformKey;
        PlatformKey pk = (PlatformKey) platformKey;
        JavaPlatform platform = getPlatform(pk);
        //null means active broken (unresolved) platform, no need to do anything
        if (platform != null) {
            SpecificationVersion jdk13 = new SpecificationVersion("1.3");  //NOI18N
            String platformAntName = platform.getProperties().get("platform.ant.name");    //NOI18N
            assert platformAntName != null;
            props.put(AppClientProjectProperties.JAVA_PLATFORM, platformAntName);
            Element root = helper.getPrimaryConfigurationData(true);
            boolean defaultPlatform = pk.isDefaultPlatform();
            boolean changed = false;
            NodeList explicitPlatformNodes = root.getElementsByTagNameNS(AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE,"explicit-platform");   //NOI18N
            if (defaultPlatform) {
                if (explicitPlatformNodes.getLength()==1) {
                    root.removeChild(explicitPlatformNodes.item(0));
                    changed = true;
                }
            } else {
                Element explicitPlatform;
                switch (explicitPlatformNodes.getLength()) {
                    case 0:
                        explicitPlatform = root.getOwnerDocument().createElementNS(AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE, "explicit-platform"); //NOI18N
                        NodeList sourceRootNodes = root.getElementsByTagNameNS(AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");   //NOI18N
                        assert sourceRootNodes.getLength() == 1 : "Broken project.xml file"; //NOI18N
                        root.insertBefore(explicitPlatform, sourceRootNodes.item(0));
                        changed = true;
                        break;
                    case 1:
                        explicitPlatform = (Element)explicitPlatformNodes.item(0);
                        break;
                    default:
                        throw new AssertionError("Broken project.xml file");   //NOI18N
                }
                String explicitSourceAttrValue = explicitPlatform.getAttribute("explicit-source-supported");    //NOI18N
                if (jdk13.compareTo(platform.getSpecification().getVersion())>=0 &&
                        !"false".equals(explicitSourceAttrValue)) {   //NOI18N
                    explicitPlatform.setAttribute("explicit-source-supported","false"); //NOI18N
                    changed = true;
                } else if (jdk13.compareTo(platform.getSpecification().getVersion())<0 &&
                        !"true".equals(explicitSourceAttrValue)) {  //NOI18N
                    explicitPlatform.setAttribute("explicit-source-supported","true"); //NOI18N
                    changed = true;
                }
            }
            
            SpecificationVersion sourceLevel;
            if (sourceLevelKey == null) {
                sourceLevel = platform.getSpecification().getVersion();
            }
            else {
                assert sourceLevelKey instanceof SourceLevelKey;
                sourceLevel = ((SourceLevelKey)sourceLevelKey).getSourceLevel();
            }
            String javacSource = sourceLevel.toString();
            String javacTarget = javacSource;
            
            //Issue #116490
            // Customizer value | -source | -target
            // JDK 1.2            1.2        1.1
            // JDK 1.3            1.3        1.1
            // JDK 1.4            1.4        1.4
            // JDK 5              1.5        1.5
            // JDK 6              1.5        1.6
            // JDK 7              1.7        1.7  - should bring a new language features
            if (jdk13.compareTo(sourceLevel)>=0) {
                javacTarget = "1.1";        //NOI18N
            }
            else if (JDK_6.equals(sourceLevel)) {
                javacSource = JDK_5.toString();        //NOI18N
            }
            
            // #89131: these levels are not actually distinct from 1.5.
            if (javacSource.equals("1.6") || javacSource.equals("1.7")) {
                javacSource = "1.5";
            }
            if (!javacSource.equals(props.getProperty(AppClientProjectProperties.JAVAC_SOURCE))) {
                props.setProperty(AppClientProjectProperties.JAVAC_SOURCE, javacSource);
            }
            if (!javacTarget.equals(props.getProperty(AppClientProjectProperties.JAVAC_TARGET))) {
                props.setProperty(AppClientProjectProperties.JAVAC_TARGET, javacTarget);
            }
            
            if (changed) {
                helper.putPrimaryConfigurationData(root, true);
            }
        }
    }
    
    
    /**
     * Returns a {@link JavaPlatform} for an item obtained from the ComboBoxModel created by
     * the {@link PlatformUiSupport#createComboBoxModel} method
     * @param platformKey an item obtained from ComboBoxModel created by {@link PlatformUiSupport#createComboBoxModel}
     * @return JavaPlatform or null in case when platform is broken
     * @exception {@link IllegalArgumentException} is thrown in case when parameter in not an object created by
     * platform combobox model.
     */
    public static JavaPlatform getPlatform(Object platformKey) {
        if (platformKey instanceof PlatformKey) {
            return getPlatform((PlatformKey)platformKey);
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    /**
     * Creates {@link ComboBoxModel} of source levels for active platform.
     * The model listens on the platform's {@link ComboBoxModel} and update its
     * state according to changes
     * @param platformComboBoxModel the platform's model used for listenning
     * @param initialSourceLevel initial source level value
     * @param initialTargetLevel initial target level value
     * @return {@link ComboBoxModel} of {@link SpecificationVersion}
     */
    public static ComboBoxModel createSourceLevelComboBoxModel(ComboBoxModel platformComboBoxModel, String initialSourceLevel, String initialTargetLevel, String j2eePlatform) {
        return new SourceLevelComboBoxModel(platformComboBoxModel, initialSourceLevel, initialTargetLevel, j2eePlatform);
    }
    
    public static ListCellRenderer createSourceLevelListCellRenderer() {
        return new SourceLevelListCellRenderer();
    }
    
    
    private static JavaPlatform getPlatform(PlatformKey platformKey) {
        return platformKey.platform;
    }
    
    
    /**
     * This class represents a  JavaPlatform in the {@link ListModel}
     * created by the {@link PlatformUiSupport#createPlatformComboBoxModel}
     * method.
     */
    private static class PlatformKey implements Comparable {
        
        private String name;
        private JavaPlatform platform;
        
        /**
         * Creates a PlatformKey for a broken platform
         * @param name the ant name of the broken platform
         */
        public PlatformKey(String name) {
            assert name != null;
            this.name = name;
        }
        
        /**
         * Creates a PlatformKey for a platform
         * @param platform the {@link JavaPlatform}
         */
        public PlatformKey(JavaPlatform platform) {
            assert platform != null;
            this.platform = platform;
        }
        
        public int compareTo(Object o) {
            return this.getDisplayName().compareTo(((PlatformKey)o).getDisplayName());
        }
        
        @Override
        public boolean equals(Object other) {
            if (other instanceof PlatformKey) {
                PlatformKey otherKey = (PlatformKey)other;
                return (this.platform == null ? otherKey.platform == null : this.platform.equals(otherKey.platform)) &&
                        otherKey.getDisplayName().equals(this.getDisplayName());
            } else {
                return false;
            }
        }
        
        @Override
        public int hashCode() {
            return getDisplayName().hashCode();
        }
        
        @Override
        public String toString() {
            return getDisplayName();
        }
        
        public synchronized String getDisplayName() {
            if (this.name == null) {
                this.name = this.platform.getDisplayName();
            }
            return this.name;
        }
        
        public boolean isDefaultPlatform() {
            if (this.platform == null) {
                return false;
            }
            return this.platform.equals(JavaPlatformManager.getDefault().getDefaultPlatform());
        }
        
        public boolean isBroken() {
            return this.platform == null;
        }
        
    }
    
    private static final class SourceLevelKey implements Comparable {
        
        final SpecificationVersion sourceLevel;
        final boolean broken;
        
        public SourceLevelKey(final SpecificationVersion sourceLevel) {
            this(sourceLevel, false);
        }
        
        public SourceLevelKey(final SpecificationVersion sourceLevel, final boolean broken) {
            assert sourceLevel != null : "Source level cannot be null";     //NOI18N
            this.sourceLevel = sourceLevel;
            this.broken = broken;
        }
        
        public SpecificationVersion getSourceLevel() {
            return this.sourceLevel;
        }
        
        public boolean isBroken() {
            return this.broken;
        }
        
        public int compareTo(final Object other) {
            assert other instanceof SourceLevelKey : "Illegal argument of SourceLevelKey.compareTo()";  //NOI18N
            SourceLevelKey otherKey = (SourceLevelKey) other;
            return this.sourceLevel.compareTo(otherKey.sourceLevel);
        }
        
        @Override
        public boolean equals(final Object other) {
            return (other instanceof SourceLevelKey) &&
                    ((SourceLevelKey)other).sourceLevel.equals(this.sourceLevel);
        }
        
        @Override
        public int hashCode() {
            return this.sourceLevel.hashCode();
        }
        
        @Override
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            if (this.broken) {
                buffer.append("Broken: ");      //NOI18N
            }
            buffer.append(this.sourceLevel.toString());
            return buffer.toString();
        }
        
        public String getDisplayName () {
            String _tmp = sourceLevel.toString();
            if (JDK_5.compareTo(sourceLevel)<=0) {                
                _tmp = _tmp.replaceFirst("^1\\.([5-9]|\\d\\d+)$", "$1");        //NOI18N
            }            
            return NbBundle.getMessage(PlatformUiSupport.class, "LBL_JDK",_tmp);
        }
        
    }
    
    private static class PlatformComboBoxModel extends AbstractListModel implements ComboBoxModel, PropertyChangeListener {
        private static final long serialVersionUID = 1L;
        
        private final JavaPlatformManager pm;
        private PlatformKey[] platformNamesCache;
        private String initialPlatform;
        private PlatformKey selectedPlatform;
        
        public PlatformComboBoxModel(String initialPlatform) {
            this.pm = JavaPlatformManager.getDefault();
            this.pm.addPropertyChangeListener(WeakListeners.propertyChange(this, this.pm));
            this.initialPlatform = initialPlatform;
        }
        
        public int getSize() {
            PlatformKey[] platformNames = getPlatformNames();
            return platformNames.length;
        }
        
        public Object getElementAt(int index) {
            PlatformKey[] platformNames = getPlatformNames();
            assert index >=0 && index< platformNames.length;
            return platformNames[index];
        }
        
        public Object getSelectedItem() {
            this.getPlatformNames(); //Force setting of selectedPlatform if it is not alredy done
            return this.selectedPlatform;
        }
        
        public void setSelectedItem(Object obj) {
            this.selectedPlatform = (PlatformKey) obj;
            this.fireContentsChanged(this, -1, -1);
        }
        
        public void propertyChange(PropertyChangeEvent event) {
            if (JavaPlatformManager.PROP_INSTALLED_PLATFORMS.equals(event.getPropertyName())) {
                synchronized (this) {
                    this.platformNamesCache = null;
                }
                this.fireContentsChanged(this, -1, -1);
            }
        }
        
        private synchronized PlatformKey[] getPlatformNames() {
            if (this.platformNamesCache == null) {
                JavaPlatform[] platforms = pm.getPlatforms(null, new Specification("j2se",null));    //NOI18N
                Set<PlatformKey> orderedNames = new TreeSet<PlatformKey>();
                boolean activeFound = false;
                for (int i=0; i< platforms.length; i++) {
                    if (platforms[i].getInstallFolders().size()>0) {
                        PlatformKey pk = new PlatformKey(platforms[i]);
                        orderedNames.add(pk);
                        if (!activeFound && initialPlatform != null) {
                            String antName = platforms[i].getProperties().get("platform.ant.name");    //NOI18N
                            if (initialPlatform.equals(antName)) {
                                if (this.selectedPlatform == null) {
                                    this.selectedPlatform = pk;
                                    initialPlatform = null;
                                }
                                activeFound = true;
                            }
                        }
                    }
                }
                if (!activeFound) {
                    if (initialPlatform == null) {
                        if (this.selectedPlatform == null || !orderedNames.contains(this.selectedPlatform)) {
                            this.selectedPlatform = new PlatformKey(JavaPlatformManager.getDefault().getDefaultPlatform());
                        }
                    } else {
                        PlatformKey pk = new PlatformKey(this.initialPlatform);
                        orderedNames.add(pk);
                        if (this.selectedPlatform == null) {
                            this.selectedPlatform = pk;
                        }
                    }
                }
                this.platformNamesCache = orderedNames.toArray(new PlatformKey[orderedNames.size()]);
            }
            return this.platformNamesCache;
        }
        
    }
    
    private static class PlatformListCellRenderer implements ListCellRenderer {
        
        private final ListCellRenderer delegate;
        
        public PlatformListCellRenderer() {
            this.delegate = HtmlRenderer.createRenderer();
        }
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String name;
            if (value == null) {
                name = "";  //NOI18N
            } else {
                assert value instanceof PlatformKey : "Wrong model";  //NOI18N
                PlatformKey key = (PlatformKey) value;
                if (key.isBroken()) {
                    name = "<html><font color=\"#A40000\">" +    //NOI18N
                            NbBundle.getMessage(PlatformUiSupport.class,"TXT_BrokenPlatformFmt", key.getDisplayName());
                } else {
                    name = key.getDisplayName();
                }
            }
            return this.delegate.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus);
        }
    }
    
    private static class SourceLevelComboBoxModel extends AbstractListModel implements ComboBoxModel, ListDataListener {
        private static final long serialVersionUID = 1L;
        
        private static final String VERSION_PREFIX = "1.";      //The version prefix // NOI18N
        private static final int INITIAL_VERSION_MINOR = 2;     //1.2
        // if project is JAVA EE 5 show only 1.5 and higher
        private static final int INITIAL_VERSION_MINOR_JAVA_EE_5 = 5;     // 1.5
        
        private SpecificationVersion selectedSourceLevel;
        private SpecificationVersion originalSourceLevel;
        private SourceLevelKey[] sourceLevelCache;
        private final ComboBoxModel platformComboBoxModel;
        private PlatformKey activePlatform;
        private String j2eePlatform = null;
        
        public SourceLevelComboBoxModel(ComboBoxModel platformComboBoxModel, String initialSourceLevel, String initialTargetLevel) {
            this.platformComboBoxModel = platformComboBoxModel;
            this.activePlatform = (PlatformKey) this.platformComboBoxModel.getSelectedItem();
            this.platformComboBoxModel.addListDataListener(this);
            if (initialSourceLevel != null && initialSourceLevel.length()>0) {
                try {
                    originalSourceLevel = new SpecificationVersion (initialSourceLevel);
                } catch (NumberFormatException nfe) {
                    // If the javac.source has invalid value, do not preselect and log it.  
                    LOGGER.log(Level.INFO, "Invalid javac.source: " + initialSourceLevel);
                }
            }
            if (initialTargetLevel != null && initialTargetLevel.length() > 0) {
                try {
                    SpecificationVersion originalTargetLevel = new SpecificationVersion (initialTargetLevel);
                    if (this.originalSourceLevel == null || this.originalSourceLevel.compareTo(originalTargetLevel)<0) {
                        this.originalSourceLevel = originalTargetLevel;
                    }
                } catch (NumberFormatException nfe) {
                    //If the javac.target has invalid value, do not preselect and log it
                    LOGGER.warning("Invalid javac.target: "+initialTargetLevel);       //NOI18N
                }
            }
            this.selectedSourceLevel = this.originalSourceLevel;
        }
        
        public SourceLevelComboBoxModel(ComboBoxModel platformComboBoxModel, String initialSourceLevel, String initialTargetLevel, String j2eePlatform) {
            this(platformComboBoxModel, initialSourceLevel, initialTargetLevel);
            this.j2eePlatform = j2eePlatform;
        }
        
        public int getSize() {
            SourceLevelKey[] sLevels = getSourceLevels();
            return sLevels.length;
        }
        
        public Object getElementAt(int index) {
            SourceLevelKey[] sLevels = getSourceLevels();
            assert index >=0 && index< sLevels.length;
            return sLevels[index];
        }
        
        public Object getSelectedItem () {
            SourceLevelKey[] keys = getSourceLevels();
            for (int i=0; i<keys.length; i++) {
                if (keys[i].getSourceLevel().equals(this.selectedSourceLevel)) {
                    return keys[i];
                }
            }
            return null;
        }
        
        public void setSelectedItem (Object obj) {
            this.selectedSourceLevel = (obj == null ? null : ((SourceLevelKey) obj).getSourceLevel());
            this.fireContentsChanged(this, -1, -1);
        }
        
        public void intervalAdded(ListDataEvent e) {
        }
        
        public void intervalRemoved(ListDataEvent e) {
        }
        
        public void contentsChanged(ListDataEvent e) {
            PlatformKey selectedPlatform = (PlatformKey) this.platformComboBoxModel.getSelectedItem();
            JavaPlatform platform = getPlatform(selectedPlatform);
            if (platform != null) {
                SpecificationVersion version = platform.getSpecification().getVersion();
                if (this.selectedSourceLevel != null && this.selectedSourceLevel.compareTo(version)>0 &&
                        !shouldChangePlatform(selectedSourceLevel, version)) {
                    //Restore original
                    this.platformComboBoxModel.setSelectedItem(this.activePlatform);
                    return;
                }
                else {
                    this.originalSourceLevel = null;
                }
            }
            this.activePlatform = selectedPlatform;
            resetCache();
        }
        
        private void resetCache() {
            synchronized (this) {
                this.sourceLevelCache = null;
            }
            this.fireContentsChanged(this, -1, -1);
        }
        
        private SourceLevelKey[] getSourceLevels() {
            if (this.sourceLevelCache == null) {
                PlatformKey selectedPlatform = (PlatformKey) this.platformComboBoxModel.getSelectedItem();
                JavaPlatform platform = getPlatform(selectedPlatform);                
                List<SourceLevelKey> sLevels = new ArrayList<SourceLevelKey>();
                //If platform == null broken platform, the source level range is unknown
                //The source level combo box should be empty and disabled
                boolean selSourceLevelValid = false;
                if (platform != null) {                    
                    SpecificationVersion version = platform.getSpecification().getVersion();                                        
                    int index = INITIAL_VERSION_MINOR;
                    // #71619 - source level lower than 1.5 won't be shown for Java EE 5 project
                    if (j2eePlatform != null && j2eePlatform.equals(AppClientProjectProperties.JAVA_EE_5)) {
                        index = INITIAL_VERSION_MINOR_JAVA_EE_5;
                    }
                    SpecificationVersion template = new SpecificationVersion (VERSION_PREFIX + Integer.toString (index++));
                    boolean origSourceLevelValid = false;
                    
                    while (template.compareTo(version)<=0) {
                        if (template.equals(this.originalSourceLevel)) {
                            origSourceLevelValid = true;
                        }
                        if (template.equals(this.selectedSourceLevel)) {
                            selSourceLevelValid = true;
                        }
                        sLevels.add (new SourceLevelKey (template));
                        template = new SpecificationVersion (VERSION_PREFIX + Integer.toString (index++));
                    }
                    if (this.originalSourceLevel != null && !origSourceLevelValid) {
                        if (originalSourceLevel.equals(this.selectedSourceLevel)) {                            
                            selSourceLevelValid = true;
                        }
                        sLevels.add (new SourceLevelKey(this.originalSourceLevel,true));
                    }                                        
                }
                this.sourceLevelCache = sLevels.toArray(new SourceLevelKey[sLevels.size()]);
                if (!selSourceLevelValid) {
                    this.selectedSourceLevel = this.sourceLevelCache.length == 0 ? 
                        null : this.sourceLevelCache[this.sourceLevelCache.length-1].getSourceLevel();
                }
            }
            return this.sourceLevelCache;
        }
        
        private static boolean shouldChangePlatform(SpecificationVersion selectedSourceLevel, SpecificationVersion platformSourceLevel) {
            JButton changeOption = new JButton(NbBundle.getMessage(PlatformUiSupport.class, "CTL_ChangePlatform"));
            changeOption.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PlatformUiSupport.class, "AD_ChangePlatform"));
            String message = MessageFormat.format(NbBundle.getMessage(PlatformUiSupport.class,"TXT_ChangePlatform"),new Object[] {
                selectedSourceLevel.toString(),
                platformSourceLevel.toString(),
            });
            return DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor(message,
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
    
    private static class SourceLevelListCellRenderer implements ListCellRenderer {
        
        ListCellRenderer delegate;
        
        public SourceLevelListCellRenderer() {
            this.delegate = HtmlRenderer.createRenderer();
        }
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String message;
            if (value == null) {
                message = "";   //NOI18N
            }
            else {
                assert value instanceof SourceLevelKey;
                SourceLevelKey key = (SourceLevelKey) value;            
                if (key.isBroken()) {                
                    message = "<html><font color=\"#A40000\">" +    //NOI18N
                        NbBundle.getMessage(PlatformUiSupport.class,"TXT_InvalidSourceLevel",key.getDisplayName());
                }
                else {
                    message = key.getDisplayName();
                }
            }
            return this.delegate.getListCellRendererComponent(list, message, index, isSelected, cellHasFocus);
        }
    }
    
}
