/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.platform;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.javacard.Utils;
import org.netbeans.modules.javacard.GuiUtils;
import org.netbeans.modules.javacard.constants.CommonSystemFilesystemPaths;
import org.netbeans.modules.javacard.constants.JCConstants;
import org.netbeans.modules.javacard.constants.PlatformTemplateWizardKeys;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.FinishablePanel;
import org.openide.WizardDescriptor.Panel;
import org.openide.WizardDescriptor.ProgressInstantiatingIterator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.*;
import org.netbeans.modules.javacard.constants.JavacardPlatformKeyNames;
import org.netbeans.spi.project.support.ant.PropertyUtils;

/**
 *
 * @author Tim Boudreau
 */
final class JavacardPlatformWizardIterator implements ProgressInstantiatingIterator<WizardDescriptor>, ChangeListener {

    private FileObject baseDir;
    private DetectionPanel firstPanel;
    private PropertiesPanel secondPanel;
    private String displayName;
    private PlatformInfo info;
    private static final String PROP_DISPLAY_NAME = "platformDisplayName"; //NOI18N
    private static final String PROP_INFO = "platformInfo"; //NOI18N

    JavacardPlatformWizardIterator(FileObject baseDir) {
        this.baseDir = baseDir;
    }

    
    public Set instantiate() throws IOException {
        final Set[] result = new Set[1];
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(JavacardPlatformWizardIterator.class, "PROGRESS_CREATING_PLATFORM"));
        final JDialog dlg = GuiUtils.createModalProgressDialog(handle, false);
        final Runnable r = new Runnable() {

            public void run() {
                try {
                    result[0] = instantiate(handle);
                    dlg.setVisible(false);
                    dlg.dispose();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };
        dlg.addWindowListener (new WindowAdapter() {


            @Override
            public void windowOpened(WindowEvent e) {
                RequestProcessor.getDefault().post(r);
            }

        });
        dlg.setVisible(true);
        return result[0] == null ? Collections.EMPTY_SET : result[0];
    }


    public Set instantiate(final ProgressHandle h) throws IOException {
        h.setInitialDelay(20);
        h.setDisplayName(NbBundle.getMessage(JavacardPlatformWizardIterator.class, 
                "PROGRESS_CREATING_PLATFORM")); //NOI18N
        h.start(19);
        final JavacardPlatformImpl platform = new JavacardPlatformImpl(FileUtil.toFile(baseDir), displayName, info);
        h.progress(1);
        final FileObject platformsFolder = FileUtil.getConfigFile(
                CommonSystemFilesystemPaths.SFS_JAVA_PLATFORMS_FOLDER); //NOI18N
        h.progress(2);
        final FileObject[] fos = new FileObject[1];
        boolean hasOtherPlatforms = false;
        for (FileObject fo : platformsFolder.getChildren()) {
            hasOtherPlatforms = JCConstants.JAVACARD_PLATFORM_FILE_EXTENSION.equals(fo.getExt());
            if (hasOtherPlatforms) break;
        }
        h.progress(3);
        final String filename = !hasOtherPlatforms ? 
            JCConstants.DEFAULT_JAVACARD_PLATFORM_FILE_NAME :
            displayName.replace(' ', '_'); //NOI18N
        platformsFolder.getFileSystem().runAtomicAction(new AtomicAction() {


            public void run() throws IOException {
                FileObject fo = platformsFolder.createData(filename, JCConstants.JAVACARD_PLATFORM_FILE_EXTENSION); //NOI18N
                Properties settings = new Properties();
                settings.setProperty(JavacardPlatformKeyNames.PLATFORM_ID, filename);
                h.progress(4);
                FileObject eepromFolder = Utils.sfsFolderForDeviceEepromsForPlatformNamed(filename, true);
                settings.setProperty(JavacardPlatformKeyNames.PLATFORM_EEPROM_FOLDER, FileUtil.toFile(eepromFolder).getAbsolutePath());
                platform.write(fo, settings);
                h.progress(5);
                fos[0] = fo;
            }
        });
        h.progress(6);
        FileObject ob = platformsFolder.getFileObject(filename, JCConstants.JAVACARD_PLATFORM_FILE_EXTENSION);
        h.progress(7);
        final String path = FileUtil.toFile(ob).getAbsolutePath();
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws Exception {
                    h.progress(8);
                    EditableProperties props = PropertyUtils.getGlobalProperties();
                    h.progress(9);
                    //Make a new folder to store device/card configuration info
                    FileObject serversFolder = Utils.sfsFolderForDeviceConfigsForPlatformNamed(filename, true);
                    h.progress(10);
                    //Now create a default device template
                    String deviceTemplateName = "org-netbeans-modules-javacard/templates/ServerTemplate.jcard"; //NOI18N
                    h.progress(11);
                    DataObject deviceTemplate = DataObject.find(FileUtil.getConfigFile(deviceTemplateName));
                    DataFolder fld = DataFolder.findFolder(serversFolder);
                    String defaultDevice = JCConstants.TEMPLATE_DEFAULT_DEVICE_NAME;
                    defaultDevice = FileUtil.findFreeFileName(serversFolder, defaultDevice,
                            JCConstants.JAVACARD_DEVICE_FILE_EXTENSION);
                    Map<String,String> substitutions = new HashMap<String,String>();
                    substitutions.put(PlatformTemplateWizardKeys.PROJECT_TEMPLATE_DEVICE_NAME_KEY,
                            defaultDevice); //NOI18N
                    h.progress(12);
                    DataObject dob = deviceTemplate.createFromTemplate(fld, defaultDevice, substitutions);
                    if (secondPanel != null && secondPanel.comp != null) {
                        final EditableProperties deviceProps = new EditableProperties(true);
                        FileObject deviceFile = dob.getPrimaryFile();
                        InputStream in = new BufferedInputStream (deviceFile.getInputStream());
                        try {
                            deviceProps.load(in);
                        } finally {
                            in.close();
                        }
                        secondPanel.comp.write(new KeysAndValues.EditablePropertiesAdapter(deviceProps));
                        OutputStream out = new BufferedOutputStream(deviceFile.getOutputStream());
                        try {
                            deviceProps.store(out);
                        } finally {
                            out.close();
                        }
                    }
                    h.progress(13);


                    props.setProperty(JCConstants.GLOBAL_PROPERTIES_JCPLATFORM_DEFINITION_PREFIX +
                            filename, path); //NOI18N

                    //e.g. jcplatform.platform_default.devicepath
                    String propName = JCConstants.GLOBAL_PROPERTIES_JCPLATFORM_DEFINITION_PREFIX
                            + filename + JCConstants.GLOBAL_PROPERTIES_DEVICE_FOLDER_PATH_KEY_SUFFIX;
                    //And save that to the global properties
                    props.setProperty(propName, FileUtil.toFile(serversFolder).getPath());
                    h.progress(14);

                    PropertyUtils.putGlobalProperties(props);

                    h.progress(15);
                    return null;
                }
            });
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            } else {
                IOException ioe = new IOException();
                ioe.initCause(e);
                throw ioe;
            }
        }
        h.progress(16);
        DataObject dob = DataObject.find(fos[0]);
        Set<Object> result = new HashSet<Object>();
        h.progress(17);
        result.add(dob.getLookup().lookup(JavacardPlatformImpl.class));
        h.progress(18);
        return result;
    }


    public void initialize(WizardDescriptor w) {
        w.putProperty(PROP_DISPLAY_NAME, displayName);
    }


    public void uninitialize(WizardDescriptor w) {
        String nm = (String) w.getProperty(PROP_DISPLAY_NAME);
        if (nm != null) {
            displayName = nm;
            if (firstPanel != null) {
                firstPanel.setDisplayName(nm);
            }
        }
        firstPanel = null;
        secondPanel = null;
    }


    public Panel<WizardDescriptor> current() {
        if (firstPanel == null && ix == 0) {
            firstPanel = new DetectionPanel();
            firstPanel.addChangeListener(this);
        }
        if (secondPanel == null && ix == 1) {
            secondPanel = new PropertiesPanel();
            secondPanel.addChangeListener(this);
        }
        return ix == 0 ? firstPanel : secondPanel;
    }


    public String name() {
        return NbBundle.getMessage(JavacardPlatformWizardIterator.class,
                "MSG_DETECT_PLATFORM"); //NOI18N
        }
    private int ix = 0;

    //PENDING:  Could include second panel of the wizard and let the
    //user configure the default device


    public boolean hasNext() {
        return ix == 0;
    }


    public boolean hasPrevious() {
        return ix == 1;
    }


    public void nextPanel() {
        ix++;
    }


    public void previousPanel() {
        ix--;
    }
    private final ChangeSupport supp = new ChangeSupport(this);


    public void addChangeListener(ChangeListener arg0) {
        supp.addChangeListener(arg0);
    }


    public void removeChangeListener(ChangeListener arg0) {
        supp.removeChangeListener(arg0);
    }
    private boolean inChange;


    public void stateChanged(ChangeEvent e) {
        if (inChange) {
            return;
        }
        inChange = true;
        try {
            supp.fireChange();
        } finally {
            inChange = false;
        }
    }

    private final class DetectionPanel implements FinishablePanel<WizardDescriptor>, ChangeListener, PropertyChangeListener {

        private PlatformPanel comp;

        private PlatformPanel createComponent() {
            if (comp == null) {
                comp = new PlatformPanel(baseDir);
            }
            return comp;
        }


        public Component getComponent() {
            assert EventQueue.isDispatchThread();
            if (comp == null) {
                comp = createComponent();
                comp.addChangeListener(JavacardPlatformWizardIterator.this);
                comp.addChangeListener(this);
                comp.addPropertyChangeListener(this);
                String stepName = NbBundle.getMessage (JavacardPlatformWizardIterator.class,
                        "STEP_TITLE_VALIDATE_PLATFORM");
                String nextStepName = NbBundle.getMessage (JavacardPlatformWizardIterator.class,
                        "STEP_TITLE_DEFINE_DEVICE");

                comp.putClientProperty("WizardPanel_contentData", new String[] { stepName, nextStepName }); //NOI18N
                comp.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE); //NOI18N
                // Turn on numbering of all steps
                comp.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE); //NOI18N
                // Turn on subtitle creation on each step
                comp.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE); //NOI18N
                // Show steps on the left side with the image on the background
                comp.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE); //NOI18N
                // Turn on numbering of all steps
                comp.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE); //NOI18N
            }
            return comp;
        }


        public HelpCtx getHelp() {
            return HelpCtx.DEFAULT_HELP;
        }


        public void readSettings(WizardDescriptor w) {
            info = (PlatformInfo) w.getProperty(PROP_INFO);
            if (displayName == null) {
                displayName = info.getTitle();
            }
            if (info != null) {
                supp.fireChange();
            }
        }


        public void storeSettings(WizardDescriptor w) {
            w.putProperty(PROP_INFO, comp.getPlatformInfo());
        }


        public boolean isValid() {
            return comp == null ? false : info != null && !comp.isProblem();
        }


        public void addChangeListener(ChangeListener arg0) {
            supp.addChangeListener(arg0);
        }


        public void removeChangeListener(ChangeListener arg0) {
            supp.removeChangeListener(arg0);
        }


        public void stateChanged(ChangeEvent e) {
            info = comp.getPlatformInfo();
            supp.fireChange();
        }


        public void propertyChange(PropertyChangeEvent evt) {
            displayName = comp.getDisplayName();
        }

        private void setDisplayName(String nm) {
            if (comp != null) {
                comp.setDisplayName(nm);
            }
        }


        public boolean isFinishPanel() {
            return true;
        }
    }

    private final class PropertiesPanel implements FinishablePanel<WizardDescriptor>, ChangeListener {

        private final ChangeSupport supp = new ChangeSupport(this);
        private DevicePropertiesPanel comp;


        public boolean isFinishPanel() {
            return true;
        }


        public Component getComponent() {
            if (comp == null) {
                comp = new DevicePropertiesPanel();
                comp.addChangeListener(this);
            }
            return comp;
        }


        public HelpCtx getHelp() {
            return HelpCtx.DEFAULT_HELP;
        }


        public void readSettings(WizardDescriptor wiz) {
            getComponent();
            comp.setWizardDescriptor(wiz);
            comp.read(new KeysAndValues.WizardDescriptorAdapter(wiz));
        }


        public void storeSettings(WizardDescriptor wiz) {
            if (comp != null) {
                comp.write(new KeysAndValues.WizardDescriptorAdapter(wiz));
            }
        }


        public boolean isValid() {
            return comp == null ? false : comp.isAllDataValid();
        }


        public void addChangeListener(ChangeListener arg0) {
            supp.addChangeListener(arg0);
        }


        public void removeChangeListener(ChangeListener arg0) {
            supp.removeChangeListener(arg0);
        }


        public void stateChanged(ChangeEvent e) {
            supp.fireChange();
        }
    }
}
