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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.iep.editor.wizard;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.xml.xam.ui.ProjectConstants;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.loaders.DataFolder;
import org.openide.util.HelpCtx;

/**
 *
 * @author radval
 */
public class IEPWizardPanel2EmptyIEPFile implements WizardDescriptor.FinishablePanel {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private Component component;

    private WizardDescriptor mDescriptor;
    
    private WizardDescriptor.Panel mDelegate;
    
    public IEPWizardPanel2EmptyIEPFile(WizardDescriptor descriptor) {
        Project project = Templates.getProject( descriptor );
        Sources sources = project.getLookup().lookup(org.netbeans.api.project.Sources.class);
        
        List<SourceGroup> roots = new ArrayList<SourceGroup>();
        SourceGroup[] javaRoots = 
            sources.getSourceGroups(ProjectConstants.JAVA_SOURCES_TYPE);
        roots.addAll(Arrays.asList(javaRoots));
        if (roots.isEmpty()) {
            SourceGroup[] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            roots.addAll(Arrays.asList(sourceGroups));
        }
        
        DataFolder folder = DataFolder.findFolder(roots.get(0).getRootFolder());
        DataFolder projectFolder = 
            DataFolder.findFolder(project.getProjectDirectory());
//        try {
//            if (mDescriptor.getTargetFolder().equals(projectFolder)) {
//                wizard.setTargetFolder(folder);
//            }
//        } catch (IOException ioe) {
//            wizard.setTargetFolder(folder);
//        }
        
        SourceGroup[] sourceGroups = roots.toArray(new SourceGroup[roots.size()]);
        //folderPanel = new WsdlPanel(project);
        // creates simple wizard panel with bottom panel
        WizardDescriptor.Panel firstPanel = Templates.createSimpleTargetChooser(project, sourceGroups);
        mDelegate = firstPanel;
        
    }
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
//        if (component == null) {
//            component = new IEPVisualPanel2EmptyIEPFile();
//        }
//        return component;
        return mDelegate.getComponent();
    }

    public HelpCtx getHelp() {
        return mDelegate.getHelp();
        // Show no Help button for this panel:
        //return HelpCtx.DEFAULT_HELP;
    // If you have context help:
    // return new HelpCtx(SampleWizardPanel1.class);
    }

    public boolean isValid() {
        return mDelegate.isValid();
        // If it is always OK to press Next or Finish, then:
//        return true;
    // If it depends on some condition (form filled out...), then:
    // return someCondition();
    // and when this condition changes (last form field filled in...) then:
    // fireChangeEvent();
    // and uncomment the complicated stuff below.
    }

    public final void addChangeListener(ChangeListener l) {
        mDelegate.addChangeListener(l);
    }

    public final void removeChangeListener(ChangeListener l) {
        mDelegate.removeChangeListener(l);
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
    public void readSettings(Object settings) {
        mDescriptor = (WizardDescriptor) settings;
        
        
        mDelegate.readSettings(settings);
    }

    public void storeSettings(Object settings) {
        mDelegate.storeSettings(settings);
    }

    public boolean isFinishPanel() {
        return true;
    }
    
    
}
