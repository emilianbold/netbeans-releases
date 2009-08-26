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
import org.netbeans.modules.ide.ergonomics.fod.ModulesInstaller;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.ide.ergonomics.fod.ConfigurationPanel;
import org.netbeans.modules.ide.ergonomics.fod.FoDFileSystem;
import org.netbeans.modules.ide.ergonomics.fod.FeatureInfo;
import org.netbeans.modules.ide.ergonomics.fod.FeatureManager;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.InstantiatingIterator;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.TemplateWizard;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

public class DescriptionStep implements WizardDescriptor.Panel<WizardDescriptor> {

    private ContentPanel panel;
    private ProgressHandle handle = null;
    private Collection<UpdateElement> forEnable = null;
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener> ();
    private static FindComponentModules finder = null;
    private FeatureInfo info;
    private WizardDescriptor wd;
    private final ConfigurationPanel configPanel;

    public DescriptionStep(boolean autoEnable) {
        configPanel = new ConfigurationPanel(new Callable<JComponent>() {

            public JComponent call() throws Exception {
                FoDFileSystem.getInstance().refresh();
                    waitForDelegateWizard ();
                return new JLabel(" ");
            }
        }, autoEnable);
    }

    public Component getComponent () {
        if (panel == null) {
            panel = new ContentPanel (getBundle ("DescriptionPanel_Name"));
            panel.addPropertyChangeListener (findModules);
        }
        return panel;
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
    
    private PresentModules findModules = new PresentModules();
    private class PresentModules extends Object
    implements Runnable, PropertyChangeListener {
        public void propertyChange (PropertyChangeEvent evt) {
            if (ContentPanel.FINDING_MODULES.equals (evt.getPropertyName ())) {
                FeatureManager.getInstance().create(this).schedule(0);
            }
        }
        public void run () {
            assert !SwingUtilities.isEventDispatchThread ();
            presentModulesForActivation ();
        }
    };

    private void presentModulesForActivation () {
        forEnable = getFinder ().getModulesForEnable ();
        presentModulesForEnable ();
    }
    
    private void presentModulesForEnable () {
        if (handle != null) {
            handle.finish ();
            panel.replaceComponents ();
            handle = null;
        }
        final  Collection<UpdateElement> elems = getFinder ().getModulesForEnable ();
        if (elems != null && !elems.isEmpty ()) {
            Collection<UpdateElement> visible = getFinder().getVisibleUpdateElements (elems);
            final String name = ModulesInstaller.presentUpdateElements (visible);
            configPanel.setInfo(info);
            configPanel.setPanelName(name);
            panel.replaceComponents(configPanel);
            forEnable = elems;
            fireChange ();
        } else {
            FoDFileSystem.getInstance().refresh();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    waitForDelegateWizard ();
                    fireChange ();
                }
            });
        }
    }
    
    private FindComponentModules getFinder () {
        assert finder != null : "Finder needs to be created first!";
        return finder;
    }

    private static String getBundle (String key, Object... params) {
        return NbBundle.getMessage (DescriptionStep.class, key, params);
    }

    public void readSettings (WizardDescriptor settings) {
        wd = settings;
        Object o = settings.getProperty (FeatureOnDemanWizardIterator.CHOSEN_TEMPLATE);
        assert o != null && o instanceof FileObject : o + " is not null and instanceof FileObject.";
        FileObject fileObject = (FileObject) o;
        info = FoDFileSystem.getInstance ().whichProvides(fileObject);
        finder = new FindComponentModules(info);
    }

    public void storeSettings (WizardDescriptor settings) {
        if (forEnable != null && ! forEnable.isEmpty ()) {
            settings.putProperty (FeatureOnDemanWizardIterator.CHOSEN_ELEMENTS_FOR_ENABLE, forEnable);
            fireChange ();
        }
    }

    private void waitForDelegateWizard () {
        Object o = wd.getProperty (FeatureOnDemanWizardIterator.CHOSEN_TEMPLATE);
        assert o != null && o instanceof FileObject :
            o + " is not null and instanceof FileObject";
        String templateResource = ((FileObject) o).getPath ();
        FileObject fo = null;
        WizardDescriptor.InstantiatingIterator<?> iterator = null;
        int i = 0;
        while (fo == null || iterator == null) {
            FoDFileSystem.getInstance().refresh();
            FoDFileSystem.getInstance().waitFinished();
            // hot-fixed wizard providers - temporary
            if (templateResource.startsWith("Servers/WizardProvider")) {
                try {
                    ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
                    Class<?> clazz = Class.forName("org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory", true, loader);
                    Collection c = Lookups.forPath("J2EE/DeploymentPlugins/" +
                            templateResource.substring(templateResource.indexOf('-') + 1, templateResource.indexOf('.')) + "/").lookupAll(clazz);
                    if (!c.isEmpty()) {
                        Object optFactory = c.iterator().next();
                        Method m = optFactory.getClass().getMethod("getAddInstanceIterator");
                        iterator = (InstantiatingIterator) m.invoke(optFactory);
                        fo = (FileObject) o;
                    }
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                    break;
                }
            } else {
                fo = FileUtil.getConfigFile(templateResource);
                iterator = readWizard(fo);
            }
            if (iterator instanceof FeatureOnDemanWizardIterator) {
                Logger LOG = Logger.getLogger(DescriptionStep.class.getName());
                LOG.warning(
                    "There is still wrong interator " + // NOI18N
                    iterator.getClass().getName() +
                    " for file object " + fo // NOI18N
                );
                FeatureManager.dumpModules(Level.INFO, Level.INFO);
                iterator = null;
                if (++i == 10) {
                    Logger.getLogger(DescriptionStep.class.getName()).severe("Giving up to find iterator for " + fo); // NOI18N
                    Logger.getLogger(DescriptionStep.class.getName()).severe(threadDump()); // NOI18N
                    boolean npe = false;
                    assert npe = true;
                    if (npe) {
                        throw new NullPointerException("Send us the messages.log please!"); // NOI18N
                    }
                    return; // give up
                }
                LOG.info("Forcing refresh"); // NOI18N
                // force refresh for the filesystem
                FoDFileSystem.getInstance().refreshForce();
                LOG.info("Done with refresh"); // NOI18N

                FileObject fake = FileUtil.getConfigFile(templateResource);
                if (fake == null) {
                    LOG.warning("no "+ templateResource + " on FoD: " + fake); // NOI18N
                    FileObject p = fo;
                    while (p != null) {
                        LOG.info("  parent: " + p + " children: " + Arrays.asList(p.getChildren())); // NOI18N
                        p = p.getParent();
                    }
                } else {
                    LOG.info("fake found " + fake); // NOI18N
                    LOG.info("its wizard is " + readWizard(fake)); // NOI18N
                }
            }
        }
        iterator.initialize (wd);
        wd.putProperty (FeatureOnDemanWizardIterator.DELEGATE_ITERATOR, iterator);
        fireChange ();
    }
    
    public static WizardDescriptor.InstantiatingIterator<?> readWizard(FileObject fo) {
        if (fo == null || !fo.isValid()) {
            return null;
        }

        Object o = fo.getAttribute ("instantiatingIterator");
        if (o == null || (o instanceof FeatureOnDemanWizardIterator)) {
            Object twi = fo.getAttribute ("templateWizardIterator");
            if (twi != null) {
                o = twi;
            }
        }
        if (o instanceof WizardDescriptor.InstantiatingIterator) {
            // OK
        } else if (o instanceof TemplateWizard.Iterator) {
            final TemplateWizard.Iterator it = (TemplateWizard.Iterator) o;
            o = new WizardDescriptor.InstantiatingIterator<WizardDescriptor>() {
                private TemplateWizard tw;

                public Set instantiate() throws IOException {
                    return it.instantiate(tw);
                }
                public void initialize(WizardDescriptor wizard) {
                    tw = (TemplateWizard)wizard;
                    try {
                        FileObject real = tw.getTemplate().getPrimaryFile();
                        if (!real.isValid()) {
                            real = FileUtil.getConfigFile(real.getPath());
                        }
                        tw.setTemplate(DataObject.find(real));
                        it.initialize(tw);
                    } catch (DataObjectNotFoundException ex) {
                        Logger.getLogger(DescriptionStep.class.getName()).severe(ex.toString());
                    }
                }

                public void uninitialize(WizardDescriptor wizard) {
                    it.uninitialize((TemplateWizard)wizard);
                    tw = null;
                }

                public Panel<WizardDescriptor> current() {
                    return it.current();
                }

                public String name() {
                    return it.name();
                }

                public boolean hasNext() {
                    return it.hasNext();
                }

                public boolean hasPrevious() {
                    return it.hasPrevious();
                }

                public void nextPanel() {
                    it.nextPanel();
                }

                public void previousPanel() {
                    it.previousPanel();
                }

                public void addChangeListener(ChangeListener l) {
                    it.addChangeListener(l);
                }

                public void removeChangeListener(ChangeListener l) {
                    it.removeChangeListener(l);
                }
            };
        }

        assert o != null && o instanceof WizardDescriptor.InstantiatingIterator :
            o + " is not null and instanceof WizardDescriptor.InstantiatingIterator";
        return (WizardDescriptor.InstantiatingIterator<?>)o;
    }

    private static String threadDump() {
        Map<Thread, StackTraceElement[]> all = Thread.getAllStackTraces();
        StringBuilder sb = new StringBuilder();
        sb.append("Thread dump:\n"); // NOI18N
        for (Map.Entry<Thread, StackTraceElement[]> entry : all.entrySet()) {
            sb.append(entry.getKey().getName()).append('\n');
            if (entry.getValue() == null) {
                sb.append("  no information\n"); // NOI18N
                continue;
            }
            for (StackTraceElement stackTraceElement : entry.getValue()) {
                sb.append("  ");
                sb.append(stackTraceElement.getClassName()).append('.');
                sb.append(stackTraceElement.getMethodName()).append(':');
                sb.append(stackTraceElement.getLineNumber()).append('\n');
            }
        }
        return sb.toString();
    }
}

