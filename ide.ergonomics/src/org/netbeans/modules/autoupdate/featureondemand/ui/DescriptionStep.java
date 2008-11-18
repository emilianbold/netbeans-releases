/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.autoupdate.featureondemand.ui;

import org.netbeans.modules.autoupdate.featureondemand.FindComponentModules;
import org.netbeans.modules.autoupdate.featureondemand.ModulesInstaller;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.autoupdate.featureondemand.FoDFileSystem;
import org.netbeans.modules.autoupdate.featureondemand.Feature2LayerMapping;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;

public class DescriptionStep implements WizardDescriptor.Panel<WizardDescriptor> {

    private ContentPanel panel;
    private boolean isValid = false;
    private ProgressHandle handle = null;
    private JComponent progressComponent;
    private JLabel mainLabel;
    private Collection<UpdateElement> forInstall = null;
    private Collection<UpdateElement> forEnable = null;
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener> ();
    private static FindComponentModules finder = null;
    private String codeName = null;

    public Component getComponent () {
        if (panel == null) {
            panel = new ContentPanel (getBundle ("DescriptionPanel_Name"));
            panel.addPropertyChangeListener (new PropertyChangeListener () {
                        public void propertyChange (PropertyChangeEvent evt) {
                            if (ContentPanel.FINDING_MODULES.equals (evt.getPropertyName ())) {
                                doFindingModues.run ();
                            }
                        }
                    });
        }
        return panel;
    }

    public HelpCtx getHelp () {
        return HelpCtx.DEFAULT_HELP;
    }

    public boolean isValid () {
        return isValid &&
                ((forInstall != null && ! forInstall.isEmpty ()) ||
                (forEnable != null && ! forEnable.isEmpty ()));
    }

    public synchronized void addChangeListener (ChangeListener l) {
        listeners.add(l);
    }

    public synchronized void removeChangeListener (ChangeListener l) {
        listeners.remove(l);
    }

    private void fireChange () {
        ChangeEvent e = new ChangeEvent (this);
        List<ChangeListener> templist;
        synchronized (this) {
            templist = new ArrayList<ChangeListener> (listeners);
        }
        for (ChangeListener l : templist) {
            l.stateChanged (e);
        }
    }
    
    private Runnable doFindingModues = new Runnable () {

                public void run () {
                    if (SwingUtilities.isEventDispatchThread ()) {
                        RequestProcessor.getDefault ().post (doFindingModues);
                        return;
                    }
                    RequestProcessor.Task findingTask = getFinder ().getFindingTask ();
                    if (findingTask != null && findingTask.isFinished ()) {
                        presentModulesForActivation ();
                    } else {
                        if (findingTask == null) {
                            findingTask = getFinder ().createFindingTask ();
                            findingTask.schedule (10);
                        }
                        if (findingTask.getDelay () > 0) {
                            findingTask.schedule (10);
                        }
                        findingTask.addTaskListener (new TaskListener () {
                                    public void taskFinished (Task task) {
                                        presentModulesForActivation ();
                                        return;
                                    }
                                });
                        presentFindingModules ();
                    }
                }
            };
            
    private void presentModulesForActivation () {
        forInstall = getFinder ().getModulesForInstall ();
        forEnable = getFinder ().getModulesForEnable ();
        if (forInstall != null && ! forInstall.isEmpty ()) {
            presentModulesForInstall ();
        } else if (forEnable != null && ! forEnable.isEmpty ()) {
            presentModulesForEnable ();
        } else {
            presentNone ();
        }
    }

    private void presentModulesForInstall () {
        if (handle != null) {
            handle.finish ();
            panel.replaceComponents ();
            handle = null;
        }
        Collection<UpdateElement> elems = getFinder ().getModulesForInstall ();
        if (elems != null && !elems.isEmpty ()) {
            isValid = true;
            Collection<UpdateElement> visible = FindComponentModules.getVisibleUpdateElements (elems);
            panel.replaceComponents (
                    visible.size () > 1 ?
                        new JLabel (getBundle ("DescriptionStep_BrokenModulesFound", ModulesInstaller.presentUpdateElements (visible))) :
                        new JLabel (getBundle ("DescriptionStep_BrokenModuleFound", ModulesInstaller.presentUpdateElements (visible))));
            forInstall = elems;
        } else {
            panel.replaceComponents (
                    new JLabel (getBundle ("DescriptionStep_NoMissingModules1")),
                    new JLabel ());
            isValid = false;
        }
        fireChange ();
    }
    
    private void presentNone () {
        panel.replaceComponents (
                new JLabel (getBundle ("DescriptionStep_NoMissingModules1")),
                new JLabel ());
        isValid = false;
    }
    
    private void presentModulesForEnable () {
        if (handle != null) {
            handle.finish ();
            panel.replaceComponents ();
            handle = null;
        }
        Collection<UpdateElement> elems = getFinder ().getModulesForEnable ();
        if (elems != null && !elems.isEmpty ()) {
            isValid = true;
            Collection<UpdateElement> visible = FindComponentModules.getVisibleUpdateElements (elems);
            panel.replaceComponents (
                    visible.size () > 1 ?
                        new JLabel (getBundle ("DescriptionStep_DisabledModulesFound", ModulesInstaller.presentUpdateElements (visible))) :
                        new JLabel (getBundle ("DescriptionStep_DisabledModuleFound", ModulesInstaller.presentUpdateElements (visible))));
            forEnable = elems;
        } else {
            panel.replaceComponents (
                    new JLabel (getBundle ("DescriptionStep_NoMissingModules1")),
                    new JLabel ());
            isValid = false;
        }
        fireChange ();
    }
    
    private FindComponentModules getFinder () {
        assert codeName != null : "Feature's code name is not null.";
        if (finder == null) {
            finder = new FindComponentModules (codeName);
        }
        return finder;
    }

    private void presentFindingModules () {
        handle = ProgressHandleFactory.createHandle (ContentPanel.FINDING_MODULES);
        progressComponent = ProgressHandleFactory.createProgressComponent (handle);
        mainLabel = new JLabel (getBundle ("DescriptionStep_FindingRuns_Wait"));
        JLabel detailLabel = new JLabel (getBundle ("DescriptionStep_FindingRuns"));

        handle.setInitialDelay (0);
        handle.start ();
        panel.replaceComponents (mainLabel, progressComponent, detailLabel);
    }

    private static String getBundle (String key, Object... params) {
        return NbBundle.getMessage (DescriptionStep.class, key, params);
    }

    public void readSettings (WizardDescriptor settings) {
        Object o = settings.getProperty (FeatureOnDemanWizardIterator.CHOSEN_TEMPLATE);
        assert o != null && o instanceof FileObject : o + " is not null and instanceof FileObject.";
        FileObject fo = (FileObject) o;
        URL layer = FoDFileSystem.getInstance ().getDelegateFileSystem (fo);
        codeName = Feature2LayerMapping.getInstance ().getCodeName (layer);
    }

    public void storeSettings (WizardDescriptor settings) {
        if (forInstall != null && ! forInstall.isEmpty ()) {
            settings.putProperty (FeatureOnDemanWizardIterator.CHOSEN_ELEMENTS_FOR_INSTALL, forInstall);
            Collection<UpdateElement> notNeedApproveLicense = new HashSet<UpdateElement> ();
            for (UpdateElement el : forInstall) {
                if (UpdateUnitProvider.CATEGORY.STANDARD == el.getSourceCategory ()) {
                    notNeedApproveLicense.add (el);
                }
            }
            if (! notNeedApproveLicense.isEmpty ()) {
                settings.putProperty (FeatureOnDemanWizardIterator.APPROVED_ELEMENTS, notNeedApproveLicense);
            }
            fireChange ();
        }
        if (forEnable != null && ! forEnable.isEmpty ()) {
            settings.putProperty (FeatureOnDemanWizardIterator.CHOSEN_ELEMENTS_FOR_ENABLE, forEnable);
            fireChange ();
        }
    }
}

