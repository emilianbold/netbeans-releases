/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.project.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileView;
import org.netbeans.modules.javafx2.project.JFXProjectProperties.PreloaderSourceType;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

public class JFXPreloaderChooserWizardPanel3JAR implements WizardDescriptor.Panel<JFXPreloaderChooserWizard> {

    private static final Icon JAR_ICON = ImageUtilities.loadImageIcon(
            "org/netbeans/modules/javawebstart/resources/jar.gif",   // NOI18N
            false);

    private JFileChooser fileChooser;
    private static String lastDirectoryUsed;

    private JFXPreloaderChooserWizard wizard;

    private final ChangeSupport cs = new ChangeSupport(this);
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    //private Component component;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public Component getComponent() {
//        if (component == null) {
//            component = new JFXPreloaderChooserVisualPanel3JAR();
//        }
//        return component;
        if (fileChooser == null) { // create the UI component for the wizard step
            fileChooser = new JFileChooser(lastDirectoryUsed);
            fileChooser.setFileView(new JARFileView());
            fileChooser.setPreferredSize(new Dimension(400, 300));
            fileChooser.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

            // wizard API: set the caption and index of this panel
            fileChooser.setName(NbBundle.getMessage (JFXPreloaderChooserWizardPanel3JAR.class, "CTL_SelectJAR_Caption")); // NOI18N
            fileChooser.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, // NOI18N
                                          new Integer(1));

            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setAcceptAllFileFilterUsed(true);
            fileChooser.setControlButtonsAreShown(false);
            fileChooser.setMultiSelectionEnabled(false);

            fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory()
                           || f.getName().toLowerCase().endsWith(".jar"); // NOI18N
                }
                @Override
                public String getDescription() {
                    return NbBundle.getMessage (JFXPreloaderChooserWizardPanel3JAR.class, "CTL_JarArchivesMask"); // NOI18N
                }
            });

            fileChooser.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ev) {
                    if (JFileChooser.APPROVE_SELECTION.equals(ev.getActionCommand()))
                        wizard.stepToNext();
                    else if (JFileChooser.CANCEL_SELECTION.equals(ev.getActionCommand()))
                        fileChooser.getTopLevelAncestor().setVisible(false);
                }
            });

            fileChooser.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent ev) {
                    if (JFileChooser.SELECTED_FILES_CHANGED_PROPERTY
                                        .equals(ev.getPropertyName()))
                        cs.fireChange();
                }
            });

            fileChooser.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (JFXPreloaderChooserWizardPanel3JAR.class, "CTL_SelectJAR_Step")); // NOI18N
        }

        return fileChooser;
    }

    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx(SampleWizardPanel1.class);
    }

    @Override
    public boolean isValid() {
        if (fileChooser != null && fileChooser.getSelectedFiles().length > 0) {
            lastDirectoryUsed = fileChooser.getCurrentDirectory().getAbsolutePath();
            return true;
        }
        return false;
    }

    @Override
    public final void addChangeListener(ChangeListener l) {
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
    }
    /*
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // or can use ChangeSupport in NB 6.0
    public final void addChangeListener(ChangeListener l) {
    synchronized (listeners) {
    listeners.add(l);
    }
    }
    public final void removeChangeListener(ChangeListener l) {
    synchronized (listeners) {
    listeners.remove(l);
    }
    }
    protected final void fireChangeEvent() {
    Iterator<ChangeListener> it;
    synchronized (listeners) {
    it = new HashSet<ChangeListener>(listeners).iterator();
    }
    ChangeEvent ev = new ChangeEvent(this);
    while (it.hasNext()) {
    it.next().stateChanged(ev);
    }
    }
     */

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    @Override
    public void readSettings(JFXPreloaderChooserWizard settings) {
        wizard = settings;
    }

    @Override
    public void storeSettings(JFXPreloaderChooserWizard settings) {
        if (fileChooser != null) {
            File file = fileChooser.getSelectedFile();
            if (file == null) {
                return;
            }
            settings.setSourceType(PreloaderSourceType.JAR);
            settings.setSelectedSource(file);
        }
    }
    
    private class JARFileView extends FileView {
        
        @Override
        public Icon getIcon(File f) {
            if( f.getName().toLowerCase().endsWith(".jar") ) {
                return JAR_ICON;
            }
            return super.getIcon(f);
        }
        
    }
}
