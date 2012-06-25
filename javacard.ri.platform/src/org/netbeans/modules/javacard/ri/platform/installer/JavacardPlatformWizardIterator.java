/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.javacard.ri.platform.installer;

import java.awt.Component;
import java.awt.EventQueue;
import org.netbeans.modules.javacard.common.KeysAndValues;
import org.netbeans.modules.javacard.common.GuiUtils;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.FinishablePanel;
import org.openide.WizardDescriptor.Panel;
import org.openide.WizardDescriptor.ProgressInstantiatingIterator;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.*;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JDialog;
import org.netbeans.modules.javacard.spi.JavacardPlatform;
import org.netbeans.modules.propdos.AntStyleResolvingProperties;

/**
 *
 * @author Tim Boudreau
 */
final class JavacardPlatformWizardIterator implements ProgressInstantiatingIterator<WizardDescriptor>, ChangeListener {

    private FileObject baseDir;
    private String displayName;
    private PlatformInfo info;
    private WizardDescriptor wizard;
    private WizardDescriptor.FinishablePanel[] panels;
    
    private static final String PROP_DISPLAY_NAME = "platformDisplayName"; //NOI18N
    private static final String PROP_INFO = "platformInfo"; //NOI18N

    JavacardPlatformWizardIterator(FileObject baseDir) {
        this.baseDir = baseDir;
    }

    
    public Set instantiate() throws IOException {
        final Set[] result = new Set[1];
        final ProgressHandle handle = ProgressHandleFactory.createHandle(
                NbBundle.getMessage(JavacardPlatformWizardIterator.class,
                "PROGRESS_CREATING_PLATFORM")); //NOI18N
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
        EditableProperties deviceSettingsFromWizard = null;
        if (panels[1] != null && ((PropertiesPanel)panels[1]).comp != null) {
            deviceSettingsFromWizard = new EditableProperties(true);
            ((PropertiesPanel)panels[1]).comp.write(new KeysAndValuesEditablePropsAdapter(deviceSettingsFromWizard));
        }
        RIPlatformFactory f = new RIPlatformFactory(info, deviceSettingsFromWizard,
                baseDir, h, displayName);
        FileObject platformDef = f.createPlatform();
        DataObject dob = DataObject.find(platformDef);
        Set<Object> result = new HashSet<Object>();
        result.add(dob.getLookup().lookup(JavacardPlatform.class));
        return result;
    }

    public void initialize(WizardDescriptor w) {
        this.wizard = w;
        ix = 0;
        panels = new WizardDescriptor.FinishablePanel[] {
            new DetectionPanel(),
            new PropertiesPanel(),
        };
        String[] strs = new String[panels.length];
        for (int i = 0; i < strs.length; i++)
            strs[i] = panels[i].getComponent().getName();
        ((JComponent)panels[0].getComponent()).putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, strs); // NOI18N
    }

    public void uninitialize(WizardDescriptor w) {
        panels = null;
    }

    public Panel<WizardDescriptor> current() {
        return panels[ix];
    }

    public String name() {
        return NbBundle.getMessage(JavacardPlatformWizardIterator.class,
                "MSG_DETECT_PLATFORM"); //NOI18N
        }
    private int ix = 0;

    private boolean isRi() {
        return panels[0] == null ? true : ((DetectionPanel)panels[0]).isRi;
    }

    public boolean hasNext() {
        return isRi() ? ix == 0 : false;
    }

    public boolean hasPrevious() {
        return isRi() ? ix == 1 : false;
    }

    public void nextPanel() {
        ix = 1;
    }

    public void previousPanel() {
        ix = 0;
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
//            wizard.putProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(ix)); // NOI18N
            supp.fireChange();
        } finally {
            inChange = false;
        }
    }

    void change() {
        stateChanged(null);
    }

    private final class DetectionPanel implements FinishablePanel<WizardDescriptor>, ChangeListener, PropertyChangeListener {
        private PlatformPanel comp;
        private PlatformPanel createComponent() {
            if (comp == null) {
                comp = new PlatformPanel(baseDir);
                comp.setName(NbBundle.getMessage(DetectionPanel.class, "STEP_TITLE_VALIDATE_PLATFORM")); // NOI18N
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
            }
            return comp;
        }

        public HelpCtx getHelp() {
            return HelpCtx.DEFAULT_HELP;
        }

        boolean isRi = true;
        public void readSettings(WizardDescriptor w) {
            info = (PlatformInfo) w.getProperty(PROP_INFO);
            boolean wasRi = isRi;
            if (displayName == null) {
                displayName = info.getTitle();
                isRi = info.isRi();
                if (wasRi != isRi) {
                    change();
                }
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

        public boolean isFinishPanel() {
            return true;
        }
    }

    private final class PropertiesPanel implements FinishablePanel<WizardDescriptor>, ChangeListener {
        private final ChangeSupport supp = new ChangeSupport(this);
        private DevicePropertiesPanel comp;
        boolean inStateChanged;
        public boolean isFinishPanel() {
            return true;
        }

        public Component getComponent() {
            if (comp == null) {
                comp = new DevicePropertiesPanel();
                comp.addChangeListener(this);
                comp.setName(NbBundle.getMessage(PropertiesPanel.class, "STEP_TITLE_DEFINE_DEVICE")); // NOI18N
            }
            return comp;
        }

        public HelpCtx getHelp() {
            return HelpCtx.DEFAULT_HELP;
        }

        public void readSettings(WizardDescriptor wiz) {
            getComponent();
            comp.setWizardDescriptor(wiz);
            PlatformInfo info = (PlatformInfo) wiz.getProperty(PROP_INFO);
            EditableProperties prototypeValues = info.getDeviceDefaults();
            if (prototypeValues != null) {
                AntStyleResolvingProperties a = new AntStyleResolvingProperties(true);
                a.putAll(prototypeValues);
                comp.read(new KeysAndValues.PropertiesAdapter(a));
            }
            //#177744 resolve urls like http://${javacard.device.host} which
            //the UI validation in the panel will not like.  Only happens
            //if the user backs up and returns to the step, in which
            //case the properties are loaded from the WizardDescriptor which
            //does not understand Ant-style syntax
            AntStyleResolvingProperties a = new AntStyleResolvingProperties(true);
            for (Map.Entry<String,Object> e : wiz.getProperties().entrySet()) {
                //Properties does not handle nulls;  anything that is a
                //null, we're not interested in anyway
                if (e.getKey() != null && e.getValue() != null) {
                    a.put(e.getKey(), e.getValue());
                }
            }
            comp.read(new KeysAndValues.PropertiesAdapter(a));
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
            if (inStateChanged) {
                return;
            }
            inStateChanged = true;
            try {
                supp.fireChange();
            } finally {
                inStateChanged = false;
            }
        }
    }
}
