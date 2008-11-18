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

package org.netbeans.modules.mobility.project.ui.customizer;
/**
 *
 * @author  Administrator
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.mobility.project.ui.wizard.ConfigurationsSelectionPanel;
import org.netbeans.spi.mobility.cfgfactory.ProjectConfigurationFactory.ConfigurationTemplateDescriptor;
import org.netbeans.spi.project.ProjectConfiguration;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.DialogDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.util.Utilities;
import org.netbeans.modules.mobility.cldcplatform.J2MEPlatform;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.modules.mobility.project.UserConfigurationTemplatesProvider;
import org.netbeans.modules.mobility.project.ui.wizard.ConfigurationsSelectionPanelGUI;


/** Handles adding, removing, editing and ordering of configs.
 *
 * @author gc149856
 */
public final class VisualConfigSupport {
    
    static final String PRIVATE_PREFIX = "private."; //NOI18N
    
    final JList configurationList;
    final JButton addConfigButton;
    final JButton addMoreButton;
    final JButton renameConfigButton;
    final JButton removeConfigButton;
    final JButton duplicateButton;
    final JButton saveButton;
    
    private final DefaultListModel configurationModel;
    
    J2MEProjectProperties properties;
    
    public VisualConfigSupport(JList configurationList, JButton addConfigButton, JButton addMoreButton, JButton renameConfigButton, JButton removeConfigButton, JButton duplicateButton, JButton saveButton) {
        // Remember all controls
        this.configurationList = configurationList;
        this.configurationModel = new DefaultListModel();
        this.configurationList.setModel( configurationModel );
        this.configurationList.setCellRenderer( new ConfigurationCellRenderer() );
        
        this.addConfigButton = addConfigButton;
        this.addMoreButton = addMoreButton;
        this.renameConfigButton = renameConfigButton;
        this.removeConfigButton = removeConfigButton;
        this.duplicateButton = duplicateButton;
        this.saveButton = saveButton;
        
        // Register the listeners
        ConfigSupportListener csl = new ConfigSupportListener();
        
        // On all buttons
        addConfigButton.addActionListener( csl );
        addMoreButton.addActionListener( csl );
        renameConfigButton.addActionListener( csl );
        removeConfigButton.addActionListener( csl );
        duplicateButton.addActionListener( csl );
        saveButton.addActionListener( csl );
        
        // On list selection
        configurationList.getSelectionModel().addListSelectionListener( csl );
        
    }
    
    public void setPropertyMap(final J2MEProjectProperties properties) {
        this.properties = properties;
        final ProjectConfiguration configurations[] = properties.getConfigurations();
        synchronized (configurationModel) {
            configurationModel.clear();
            for (int i=0; i<configurations.length; i++) {
                configurationModel.addElement(configurations[i]);
            }
        }
        configurationList.setSelectedValue(properties.getActiveConfiguration(), true);
        
        // Set the initial state of the buttons
        refreshButtons();
    }
    
    // Private methods ---------------------------------------------------------
    
    private ProjectConfiguration[] getConfigurationItems() {
        synchronized (configurationModel) {
            ProjectConfiguration confs[] = new ProjectConfiguration[configurationModel.getSize()];
            for(int i=0; i<confs.length; i++) {
                confs[i] = (ProjectConfiguration)configurationModel.getElementAt(i);
            }
            return confs;
        }
    }
    
    protected void addNewConfig( ) {
        final ProjectConfiguration cfgs[] = getConfigurationItems();
        final HashSet<String> allNames = new HashSet<String>(cfgs.length);
        for (int i=0; i<cfgs.length; i++) {
            allNames.add(cfgs[i].getDisplayName());
        }
        final NewConfigurationPanel ncp = new NewConfigurationPanel(allNames);
        final DialogDescriptor dd = new DialogDescriptor(ncp, NbBundle.getMessage(VisualConfigSupport.class, "LBL_VCS_AddConfiguration"), true, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.OK_OPTION, null); //NOI18N
        ncp.setDialogDescriptor(dd);
        final String newName = NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(dd)) ? ncp.getName() : null;
        if (newName != null) {
            final ProjectConfiguration cfg = new ProjectConfiguration() {
                public String getDisplayName() {
                    return newName;
                }
            };
            configurationModel.addElement(cfg);
            configurationList.setSelectedValue(cfg, true);
            createFromTemplate(properties, newName, ncp.getTemplate());
            fireActionPerformed();
        }
        
    }
    
    protected void addMoreConfigs( ) {
        final ProjectConfiguration cfgs[] = getConfigurationItems();
        final HashSet<String> allNames = new HashSet<String>(cfgs.length);
        for (int i=0; i<cfgs.length; i++) {
            allNames.add(cfgs[i].getDisplayName());
        }
        final ConfigurationsSelectionPanelGUI ncp = new ConfigurationsSelectionPanelGUI(allNames);
        final ErrorPanel ep = new ErrorPanel();
        final JPanel p = new JPanel(new BorderLayout());
        p.getAccessibleContext().setAccessibleName(
                ncp.getAccessibleContext().getAccessibleName());
        p.getAccessibleContext().setAccessibleDescription(
                ncp.getAccessibleContext().getAccessibleDescription());
        p.add(ncp, BorderLayout.CENTER);
        p.add(ep, BorderLayout.SOUTH);
        final DialogDescriptor dd = new DialogDescriptor(p, NbBundle.getMessage(VisualConfigSupport.class, "LBL_VCS_AddConfiguration"), true, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.OK_OPTION, null); //NOI18N
        ncp.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent arg0) {
                boolean valid = ncp.valid();
                dd.setValid(valid);
                ep.setErrorMessage(valid ? null : NbBundle.getMessage(ConfigurationsSelectionPanel.class, "ERR_CfgSelPanel_NameCollision"));//NOI18N
            }
        });
        if (NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(dd))) {
            for (ConfigurationTemplateDescriptor tmp : ncp.getSelectedTemplates()) {
                final String newName =  tmp.getCfgName();
                final ProjectConfiguration cfg = new ProjectConfiguration() {
                    public String getDisplayName() {
                        return newName;
                    }
                };
                configurationModel.addElement(cfg);
                configurationList.setSelectedValue(cfg, true);
                createFromTemplate(properties, newName, tmp);
            }
            fireActionPerformed();
        }
        
    }
    
    public static void createFromTemplate(final J2MEProjectProperties properties, final String cfg, final ConfigurationTemplateDescriptor desc) {
        if (desc == null) return;
        AntProjectHelper helper = properties.getHelper();
        final EditableProperties priv = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        final EditableProperties proj = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String cfgName = desc.getCfgName();
        String prefix = J2MEProjectProperties.CONFIG_PREFIX + cfgName + '.'; 
        Map<String, String> p = desc.getPrivateProperties();
        if (p != null) for(Map.Entry<String, String> en : p.entrySet()) {
            if (!priv.containsKey(en.getKey())) priv.put(en.getKey(), en.getValue());
        }
        p = desc.getProjectGlobalProperties();
        if (p != null) for(Map.Entry<String, String> en : p.entrySet()) {
            if (!proj.containsKey(en.getKey())) proj.put(en.getKey(), en.getValue());
        }
        p = desc.getProjectConfigurationProperties();
        if (p != null) for(Map.Entry<String, String> en : p.entrySet()) {
            properties.putPropertyRawValue(J2MEProjectProperties.CONFIG_PREFIX + cfg + '.' + en.getKey(), en.getValue());
        }
        helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, priv);
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, proj);
    }
    
    private static void fixPlatform(final J2MEProjectProperties properties, final String configuration, final String platform) {
        final JavaPlatform p[] = JavaPlatformManager.getDefault().getPlatforms(null, new Specification(J2MEPlatform.SPECIFICATION_NAME, null));
        int best = 0, rating = 0;
        for (int i=0; rating < 5 && i<p.length; i++) {
            if (p[i] instanceof J2MEPlatform) {
                final int r = ratePlatform((J2MEPlatform)p[i], platform);
                if (r > rating) {
                    rating = r;
                    best = i;
                }
            }
        }
        if (rating > 0) {
            properties.putPropertyRawValue(J2MEProjectProperties.CONFIG_PREFIX+configuration+'.'+DefaultPropertiesDescriptor.PLATFORM_ACTIVE, ((J2MEPlatform)p[best]).getName());
            final CustomizerMIDP cmp = new CustomizerMIDP();
            cmp.initValues(properties, configuration);
            cmp.initGroupValues(true);
        }
    }
    
    private static int ratePlatform(final J2MEPlatform platform, final String expName) {
        final String name = platform.getName();
        if (name.equals(expName)) return 5;
        if (name.indexOf(expName)>=0) return 4;
        if (name.startsWith("J2ME_Wireless_Toolkit_2")) return 3;//NOI18N
        if (name.startsWith("J2ME_Wireless_Toolkit")) return 2;//NOI18N
        return 1;
    }
    
    protected void duplicateElement( ) {
        final Object cfgs[] = configurationList.getSelectedValues();
        assert cfgs.length > 0 : "Duplicate button should be disabled"; // NOI18N
        final ProjectConfiguration allCfgs[] = getConfigurationItems();
        final HashSet<String> allNames = new HashSet<String>(allCfgs.length);
        for (int i=0; i<allCfgs.length; i++) {
            allNames.add(allCfgs[i].getDisplayName());
        }
        if (cfgs.length == 1) {
            final CloneConfigurationPanel ccp = new CloneConfigurationPanel(allNames);
            final DialogDescriptor dd = new DialogDescriptor(ccp, NbBundle.getMessage(VisualConfigSupport.class, "LBL_VCS_DuplConfiguration", ((ProjectConfiguration)cfgs[0]).getDisplayName()), true, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.OK_OPTION, null); //NOI18N
            ccp.setDialogDescriptor(dd);
            final String newName = NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(dd)) ? ccp.getName() : null;
            if (newName != null) {
                copyProperties(((ProjectConfiguration)cfgs[0]).getDisplayName(), newName);
                configurationList.setSelectedValue(addCfg(newName), true);
                fireActionPerformed();
            }
        } else {
            final HashSet<String> cloneNames = new HashSet<String>(cfgs.length);
            for (int i=0; i<cfgs.length; i++) {
                cloneNames.add(((ProjectConfiguration)cfgs[i]).getDisplayName());
            }
            final CloneConfigurationPanel2 ccp = new CloneConfigurationPanel2(allNames, cloneNames);
            final DialogDescriptor dd = new DialogDescriptor(ccp, NbBundle.getMessage(VisualConfigSupport.class, "LBL_VCS_DuplConfigurations"), true, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.OK_OPTION, null); //NOI18N
            ccp.setDialogDescriptor(dd);
            if (NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(dd))) {
                String prefix = ccp.getPrefix();
                String suffix = ccp.getSuffix();
                for (int i=0; i<cfgs.length; i++) {
                    ProjectConfiguration cfg = (ProjectConfiguration)cfgs[i];
                    String newName = prefix + cfg.getDisplayName() + suffix;
                    copyProperties(cfg.getDisplayName(), newName);
                    configurationList.setSelectedValue(addCfg(newName), true);
                }
                fireActionPerformed();
            }
        }
    }
 
    private ProjectConfiguration addCfg(final String newName) {
        final ProjectConfiguration newCfg = new ProjectConfiguration() {
            public String getDisplayName() {
                return newName;
            }
        };
        configurationModel.addElement(newCfg);
        return newCfg;
    }
    
    private void copyProperties(final String srcCfg, final String targetCfg) {
        final String keys[] = properties.keySet().toArray(new String[properties.size()]);
        final String prefix = J2MEProjectProperties.CONFIG_PREFIX + srcCfg;
        for (int i=0; i<keys.length; i++) {
            if (keys[i].startsWith(prefix)) {
                Object backValue = properties.get(keys[i]);
                if (backValue instanceof Cloneable) try {
                    Method m = backValue.getClass().getMethod("clone", new Class[0]); //NOI18N
                    if (m != null) backValue = m.invoke(backValue, new Object[0]);
                } catch (Exception e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,e);                    	
                }
                properties.put(J2MEProjectProperties.CONFIG_PREFIX + targetCfg + keys[i].substring(prefix.length()), backValue);
            }
        }
    }
    
    protected void removeElement() {
        final int si = configurationList.getSelectedIndex();
        assert si > 0 : "Remove button should be disabled"; // NOI18N
        Object cfgs[] = configurationList.getSelectedValues();
        StringBuffer text = new StringBuffer(((ProjectConfiguration)cfgs[0]).getDisplayName()); //NOI18N
        for (int i=1; i<cfgs.length; i++) text.append("\", \"").append(((ProjectConfiguration)cfgs[i]).getDisplayName()); //NOI18N
        final NotifyDescriptor desc = new NotifyDescriptor.Confirmation(NbBundle.getMessage(VisualConfigSupport.class, "LBL_VCS_ReallyRemove", text), NotifyDescriptor.YES_NO_OPTION); //NOI18N
        if (NotifyDescriptor.YES_OPTION.equals(DialogDisplayer.getDefault().notify(desc))) {
            for (int i = cfgs.length-1; i>=0; i--) {
                configurationModel.removeElement(cfgs[i]);
                removeProperties(((ProjectConfiguration)cfgs[i]).getDisplayName());
            }
            configurationList.setSelectedIndex(si < configurationModel.getSize() ? si : si - 1);
            fireActionPerformed();
        }
    }
    
    protected void renameElement() {
       final Object cfgs[] = configurationList.getSelectedValues();
       assert cfgs.length > 0 : "Rename button should be disabled"; // NOI18N
       for (Object o : cfgs) {
            ProjectConfiguration cfg = (ProjectConfiguration)o;
            final ProjectConfiguration allCfgs[] = getConfigurationItems();
            final HashSet<String> allNames = new HashSet<String>(allCfgs.length);
            for (int i=0; i<allCfgs.length; i++) {
                allNames.add(allCfgs[i].getDisplayName());
            }
            final CloneConfigurationPanel ccp = new CloneConfigurationPanel(allNames);
            final DialogDescriptor dd = new DialogDescriptor(ccp, NbBundle.getMessage(VisualConfigSupport.class, "LBL_VCS_RenConfiguration", cfg.getDisplayName()), true, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.OK_OPTION, null); //NOI18N
            ccp.setDialogDescriptor(dd);
            Object ret = DialogDisplayer.getDefault().notify(dd);
            if (NotifyDescriptor.OK_OPTION.equals(ret)) {
                String newName = ccp.getName();
                copyProperties(cfg.getDisplayName(), newName);
                removeProperties(cfg.getDisplayName());
                configurationModel.removeElement(cfg);
                configurationList.setSelectedValue(addCfg(newName), true);
                fireActionPerformed();
            } else if (NotifyDescriptor.CANCEL_OPTION.equals(ret)) return;
        }
    }
    
    private void removeProperties(final String cfgName) {
        final String keys[] = properties.keySet().toArray(new String[properties.size()]);
        final String prefix = J2MEProjectProperties.CONFIG_PREFIX + cfgName;
        for (int i=0; i<keys.length; i++) {
            if (keys[i].startsWith(prefix))
                properties.remove(keys[i]);
        }
    }
    
    protected void saveElement() {
        final Object cfgs[] = configurationList.getSelectedValues();
        assert cfgs.length > 0 : "Save button should be disabled"; // NOI18N
        final FileObject[] tmps = getConfigurationTemplates();
        final HashSet<String> allNames = new HashSet<String>();
        for (int i=0; i<tmps.length; i++) {
            allNames.add(tmps[i].getName());
        }
        final JButton SAVE_OPTION = new JButton(NbBundle.getMessage(VisualConfigSupport.class, "LBL_VCS_SaveBtn")); //NOI18N
        SAVE_OPTION.setMnemonic(NbBundle.getMessage(VisualConfigSupport.class, "MNM_VCS_SaveBtn").charAt(0)); //NOI18N
        SAVE_OPTION.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(VisualConfigSupport.class, "ACSD_VCS_SaveBtn")); //NOI18N
        for (Object o : cfgs) {
            ProjectConfiguration cfg = (ProjectConfiguration)o;
            final SaveConfigurationPanel scp = new SaveConfigurationPanel(cfg.getDisplayName() + UserConfigurationTemplatesProvider.CFG_TEMPLATE_SUFFIX, allNames, SAVE_OPTION);
            final DialogDescriptor dd = new DialogDescriptor(scp, NbBundle.getMessage(VisualConfigSupport.class, "LBL_VCS_SaveConfiguration", cfg.getDisplayName()), true, new Object[] {SAVE_OPTION, NotifyDescriptor.CANCEL_OPTION}, SAVE_OPTION, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(SaveConfigurationPanel.class), null); //NOI18N
            Object ret = DialogDisplayer.getDefault().notify(dd);
            if (SAVE_OPTION.equals(ret)) {
                saveConfigurationTemplate(cfg.getDisplayName(), scp.getName());
            } else if (DialogDescriptor.CANCEL_OPTION.equals(ret)) return;
        }
    }
    
    public static FileObject[] getConfigurationTemplates() {
        final TreeSet<FileObject> a = new TreeSet<FileObject>(new Comparator<FileObject>() {
            public int compare(FileObject o1, FileObject o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        final FileObject dir = Repository.getDefault().getDefaultFileSystem().findResource(UserConfigurationTemplatesProvider.CFG_TEMPLATES_PATH);
        if (dir != null) {
            final Enumeration en = dir.getData(false);
            while (en.hasMoreElements()) {
                final FileObject fo = (FileObject)en.nextElement();
                if (UserConfigurationTemplatesProvider.CFG_EXT.equals(fo.getExt()) && Utilities.isJavaIdentifier(fo.getName())) a.add(fo);
            }
        }
        return a.toArray(new FileObject[a.size()]);
    }
    
    private void saveConfigurationTemplate(final String cfgName, final String tmpName) {
        try {
            Repository.getDefault().getDefaultFileSystem().runAtomicAction(new AtomicAction() {
                public void run() {
                    FileLock lock = null;
                    OutputStream out = null;
                    try {
                        final FileObject fo = FileUtil.createData(Repository.getDefault().getDefaultFileSystem().getRoot(), UserConfigurationTemplatesProvider.CFG_TEMPLATES_PATH+'/'+tmpName+'.'+UserConfigurationTemplatesProvider.CFG_EXT);
                        lock = fo.lock();
                        out = fo.getOutputStream(lock);
                        extractConfigurationTemplate(cfgName, tmpName).store(out, null);
                    } catch (Exception e) {
                        ErrorManager.getDefault().notify(e);
                    } finally {
                        if (out != null) try {out.close();} catch (IOException ioe) {}
                        if (lock != null) lock.releaseLock();
                    }
                }
            });
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    protected Properties extractConfigurationTemplate(final String cfgName, final String tmpName) {
        final Properties props = new Properties();
        final String keys[] = properties.keySet().toArray(new String[properties.size()]);
        final String prefix = J2MEProjectProperties.CONFIG_PREFIX + cfgName + '.';
        final String newPrefix = J2MEProjectProperties.CONFIG_PREFIX + tmpName + '.';
        final String libs = J2MEProjectProperties.CONFIG_PREFIX + cfgName + '.' + DefaultPropertiesDescriptor.LIBS_CLASSPATH;
        final int prefixL = prefix.length();
        String references = null;
        for (int i=0; i<keys.length; i++) {
            if (keys[i].startsWith(prefix)) {
                final String value = properties.getPropertyRawValue(keys[i]);
                if (value != null) {
                    props.put(newPrefix+keys[i].substring(prefixL), value);
                    if (libs.equals(keys[i])) references = value;
                }
            }
        }
        if (references != null) processReferences(props, references, properties.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH), properties.getHelper().getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH));
        return props;
    }
    
    private void processReferences(final Properties props, final String path, final EditableProperties pub, final EditableProperties priv) {
        int i, j=0;
        while ((i=path.indexOf("${", j)) >= 0) { //NOI18N
            if ((j=path.indexOf('}', i)) < 0) return;
            final String reference = path.substring(i+2, j);
            if (!props.contains(reference) && !props.contains(PRIVATE_PREFIX+reference)) {
                String value = priv.getProperty(reference);
                if (value != null) {
                    props.put(PRIVATE_PREFIX+reference, value);
                    processReferences(props, value, pub, priv);
                }
                value = pub.getProperty(reference);
                if (value != null) {
                    if (value.indexOf('$')<0) {
                        //probably a relative path references that should be stores in an absolute form in private properties
                        props.put(PRIVATE_PREFIX+reference, properties.getHelper().resolvePath(value));
                    } else {
                        props.put(reference, value);
                        processReferences(props, value, pub, priv);
                    }
                }
            }
        }
    }
    
    // Private innerclasses ----------------------------------------------------
    
    private class ConfigSupportListener implements ActionListener, ListSelectionListener {
        
        private ConfigSupportListener()
        {
            //Just to avoid creation of accessor class
        }
                
        
        // Implementation of ActionListener ------------------------------------
        
        /** Handles button events
         */
        public void actionPerformed( final ActionEvent e ) {
            
            final Object source = e.getSource();
            
            if ( source == addConfigButton ) {
                addNewConfig();
            } else if ( source == addMoreButton ) {
                addMoreConfigs();
            } else if ( source == renameConfigButton ) {
                renameElement();
            } else if ( source == removeConfigButton ) {
                removeElement();
            } else if ( source == duplicateButton ) {
                duplicateElement();
            } else if ( source == saveButton ) {
                saveElement();
            }
            
        }
        
        // ListSelectionModel --------------------------------------------------
        
        /** Handles changes in the selection
         */
        public void valueChanged( @SuppressWarnings("unused")
		final ListSelectionEvent e ) {
            refreshButtons();
            properties.setActiveConfiguration((ProjectConfiguration) configurationList.getSelectedValue());
        }
        
    }
    
    protected void refreshButtons() {
        final boolean sel = configurationList.getSelectedIndex() > 0;
        renameConfigButton.setEnabled(sel);
        removeConfigButton.setEnabled(sel);
        duplicateButton.setEnabled(sel);
        saveButton.setEnabled(sel);
    }
    
    
    private void fireActionPerformed() {
        refreshButtons();
        properties.setConfigurations(getConfigurationItems());
        properties.setActiveConfiguration((ProjectConfiguration) configurationList.getSelectedValue());
    }
    
    
    private static class ConfigurationCellRenderer extends DefaultListCellRenderer {
        
        private ConfigurationCellRenderer()
        {
            //Just to avoid creation of accessor class
        }
        
        public Component getListCellRendererComponent( final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
            super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
            if (value instanceof ProjectConfiguration) {
                setText( ((ProjectConfiguration)value).getDisplayName());
            }
            return this;
        }
        
    }
    
}



