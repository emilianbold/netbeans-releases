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

package org.netbeans.modules.groovy.support.wizard;

import java.awt.Component;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.*;
import org.netbeans.modules.groovy.support.api.GroovySources;
import org.netbeans.modules.groovy.support.spi.GroovyExtender;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * Wizard to create a new Groovy file.
 */
public class GroovyFileWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {
    
    private static final long serialVersionUID = 1L;

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private transient int index;

    private transient WizardDescriptor.Panel[] panels;

    private transient WizardDescriptor wiz;
    
    public static GroovyFileWizardIterator create() {
        return new GroovyFileWizardIterator();
    } 
    
    private GroovyFileWizardIterator() {
    }    
    
    private WizardDescriptor.Panel[] createPanels (WizardDescriptor wizardDescriptor) {
        Project project = Templates.getProject(wizardDescriptor);
        Sources sources = ProjectUtils.getSources(project);
        List<SourceGroup> groupList = GroovySources.getGroovySourceGroups(sources);
        SourceGroup[] groups = groupList.toArray(new SourceGroup[groupList.size()]);
        assert groups != null : "Cannot return null from Sources.getSourceGroups: " + sources;
        if (groups.length == 0) {
            groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            return new WizardDescriptor.Panel[] {  
                Templates.buildSimpleTargetChooser(project, groups).create()
            };
        } else {
            return new WizardDescriptor.Panel[] {
                JavaTemplates.createPackageChooser(project, groups)
            };
        }
    }
    
    private String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        assert panels != null;
        // hack to use the steps set before this panel processed
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals (before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[ (before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent().getName();
            }
        }
        return res;
    }
    
    @Override
    public Set<FileObject> instantiate() throws IOException {
        assert false : "This method cannot be called if the class implements WizardDescriptor.ProgressInstantiatingIterator.";
        return null;
    }

    @Override
    public Set instantiate(ProgressHandle handle) throws IOException {
        handle.start();
        handle.progress(NbBundle.getMessage(GroovyFileWizardIterator.class, "LBL_NewGroovyFileWizardIterator_WizardProgress_CreatingFile"));

        FileObject dir = Templates.getTargetFolder(wiz);
        String targetName = Templates.getTargetName(wiz);

        DataFolder df = DataFolder.findFolder(dir);
        FileObject template = Templates.getTemplate(wiz);

        DataObject dTemplate = DataObject.find(template);
        String pkgName = getPackageName(dir);
        DataObject dobj;
        if (pkgName == null) {
            dobj = dTemplate.createFromTemplate(df, targetName);
        } else {
            dobj = dTemplate.createFromTemplate(df, targetName, Collections.singletonMap("package", pkgName)); // NOI18N
        }

        FileObject createdFile = dobj.getPrimaryFile();

        GroovyExtender extender = Templates.getProject(wiz).getLookup().lookup(GroovyExtender.class);
        if (extender != null && !extender.isActive()) {
            extender.activate();
        }

        handle.finish();
        return Collections.singleton(createdFile);
    }
    
    @Override
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels( wiz );
        // Make sure list of steps is accurate.
        String[] beforeSteps = null;
        Object prop = wiz.getProperty (WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = createSteps (beforeSteps, panels);
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent)c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
            }
        }
    }
    @Override
    public void uninitialize (WizardDescriptor wiz) {
        this.wiz = null;
        panels = null;
    }
    
    @Override
    public String name() {
        //return "" + (index + 1) + " of " + panels.length;
        return ""; // NOI18N
    }
    
    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }
    @Override
    public boolean hasPrevious() {
        return index > 0;
    }
    @Override
    public void nextPanel() {
        if (!hasNext()) throw new NoSuchElementException();
        index++;
    }
    @Override
    public void previousPanel() {
        if (!hasPrevious()) throw new NoSuchElementException();
        index--;
    }
    @Override
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    @Override
    public final void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    
    @Override
    public final void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    
    protected final void fireChangeEvent() {
        changeSupport.fireChange();
    }
    
    private static String getPackageName(FileObject targetFolder) {
        Project project = FileOwnerQuery.getOwner(targetFolder);
        Sources sources = ProjectUtils.getSources(project);
        List<SourceGroup> groups = GroovySources.getGroovySourceGroups(sources);
        String packageName = null;
        for (int i = 0; i < groups.size() && packageName == null; i++) {
            packageName = FileUtil.getRelativePath(groups.get(i).getRootFolder(), targetFolder);
        }
        if (packageName != null) {
            packageName = packageName.replaceAll("/", "."); // NOI18N
        }
        return packageName;
    }

}
