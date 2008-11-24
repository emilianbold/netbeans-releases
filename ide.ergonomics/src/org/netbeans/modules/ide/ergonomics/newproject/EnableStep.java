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

package org.netbeans.modules.ide.ergonomics.newproject;

import org.netbeans.modules.ide.ergonomics.fod.FindComponentModules;
import org.netbeans.modules.ide.ergonomics.fod.ModulesActivator;
import org.netbeans.modules.ide.ergonomics.fod.ModulesInstaller;
import java.awt.Component;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.ide.ergonomics.fod.FoDFileSystem;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public class EnableStep implements WizardDescriptor.FinishablePanel<WizardDescriptor> {
    private URL layer;
    private InstallPanel component;
    private Collection<UpdateElement> forEnable = null;
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener> ();
    private WizardDescriptor wd = null;
    private boolean doEnableRunning = false;

    public Component getComponent () {
        if (component == null) {
            doEnableRunning = false;
            component = new InstallPanel (
                    NbBundle.getMessage (EnableStep.class, "EnablePanel_Name"));
        }
        return component;
    }

    public HelpCtx getHelp () {
        return HelpCtx.DEFAULT_HELP;
    }

    public boolean isValid () {
        return false;
    }

    public synchronized void addChangeListener (ChangeListener l) {
        listeners.add(l);
    }

    public synchronized void removeChangeListener (ChangeListener l) {
        listeners.remove(l);
    }

    private Runnable doEnable () {
        return new Runnable () {
            public void run () {
                if (! doEnableRunning) {
                    doEnableRunning = true;
                    ModulesActivator activator = new ModulesActivator (forEnable);
                    ProgressHandle enableHandle = ProgressHandleFactory.createHandle (
                            getBundle ("ModulesStep_Enable",
                            presentElementsForEnable ()));
                    JComponent enableComponent = ProgressHandleFactory.createProgressComponent (enableHandle);
                    JComponent enableMainLabel = ProgressHandleFactory.createMainLabelComponent (enableHandle);
                    JComponent enableDetailLabel = ProgressHandleFactory.createDetailLabelComponent (enableHandle);
                    activator.assignEnableHandle (enableHandle);
                    component.displayEnableTask (
                            enableMainLabel,
                            enableDetailLabel,
                            enableComponent);
                    RequestProcessor.Task enable = activator.getEnableTask ();
                    enable.schedule (0);
                    enable.waitFinished ();
                    assert layer != null : "Layer must be known.";
                    FoDFileSystem.getInstance().refresh();
                    waitForDelegateWizard ();
                }
            }
        };
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

    @SuppressWarnings ("unchecked")
    public void readSettings (WizardDescriptor settings) {
        this.wd = settings;
        Object o = settings.getProperty (FeatureOnDemanWizardIterator.CHOSEN_ELEMENTS_FOR_ENABLE);
        assert o == null || o instanceof Collection :
            o + " is instanceof Collection<UpdateElement> or null.";
        forEnable = ((Collection<UpdateElement>) o);
        Object templateO = settings.getProperty (FeatureOnDemanWizardIterator.CHOSEN_TEMPLATE);
        assert templateO != null && templateO instanceof FileObject : templateO + " is not null and instanceof FileObject.";
        FileObject templateFO = (FileObject) templateO;
        layer = FoDFileSystem.getInstance().getDelegateFileSystem (templateFO);
        RequestProcessor.getDefault ().post (doEnable ());
    }

    public void storeSettings (WizardDescriptor settings) {
    }

    public boolean isFinishPanel () {
        return false;
    }
    
    private String presentElementsForEnable () {
        assert forEnable != null : "UpdateElements for enable are " + forEnable;
        Collection<UpdateElement> visible = FindComponentModules.getVisibleUpdateElements (forEnable);
        return ModulesInstaller.presentUpdateElements (visible);
    }
    
    private static String getBundle (String key, Object... params) {
        return NbBundle.getMessage (EnableStep.class, key, params);
    }
    
    private FileObject fo = null;
    
    private void waitForDelegateWizard () {
        Object o = wd.getProperty (FeatureOnDemanWizardIterator.CHOSEN_TEMPLATE);
        assert o != null && o instanceof FileObject :
            o + " is not null and instanceof FileObject";
        final String templateResource = ((FileObject) o).getPath ();
        fo = null;
        while (fo == null) {
            RequestProcessor.getDefault ().post (new Runnable () {
               public void run () {
                   fo = Repository.getDefault ().getDefaultFileSystem ().findResource (templateResource);
               } 
            }, 100).waitFinished ();
        }
        o = fo.getAttribute ("instantiatingIterator");
        if (o == null) {
            o = fo.getAttribute ("templateWizardIterator");
        }
        assert o != null && o instanceof WizardDescriptor.InstantiatingIterator :
            o + " is not null and instanceof WizardDescriptor.InstantiatingIterator";
        WizardDescriptor.InstantiatingIterator iterator = (WizardDescriptor.InstantiatingIterator) o;
        iterator.initialize (wd);
        wd.putProperty (FeatureOnDemanWizardIterator.DELEGATE_ITERATOR, iterator);
        fireChange ();
    }
    
}
