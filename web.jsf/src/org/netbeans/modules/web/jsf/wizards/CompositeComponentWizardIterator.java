/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.wizards;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.wizards.Utilities;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.Exceptions;

/**
 *
 * @author alexeybutenko
 */
public final class CompositeComponentWizardIterator implements TemplateWizard.Iterator {

    private int index;
    private WizardDescriptor wizard;
    private WizardDescriptor.Panel[] panels;
    private String selectedText;
    private static final String RESOURCES_FOLDER = "resources";  //NOI18N
    private static final String COMPONENT_FOLDER = "ezcomp";  //NOI18N
    private static final String TYPE_RESOURCES = "resources"; //NOI18N


    public Set<DataObject> instantiate(TemplateWizard wiz) throws IOException {
        DataObject result = null;
        String targetName = Templates.getTargetName(wizard);
        FileObject targetDir = Templates.getTargetFolder(wizard);
        DataFolder df = DataFolder.findFolder(targetDir);

        FileObject template = Templates.getTemplate( wizard );
        DataObject dTemplate = DataObject.find(template);
        HashMap<String, String> templateProperties = new HashMap<String, String>();
        if (selectedText != null) {
            templateProperties.put("implementation", selectedText);   //NOI18N
        }

        result  = dTemplate.createFromTemplate(df,targetName,templateProperties);
        return Collections.singleton(result);
    }



    public void initialize(TemplateWizard wizard) {
        this.wizard = wizard;
        selectedText = (String) wizard.getProperty("selectedText");

        Project project = Templates.getProject( wizard );
        Sources sources = project.getLookup().lookup(org.netbeans.api.project.Sources.class);

        SourceGroup[] sourceGroups = sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);

        WizardDescriptor.Panel folderPanel;
        if (sourceGroups == null || sourceGroups.length == 0) {
            sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        }
        //Add resources and component folder
        FileObject resourceFolder = sourceGroups[0].getRootFolder().getFileObject(RESOURCES_FOLDER);
        if (resourceFolder==null) {
            try {
                resourceFolder = sourceGroups[0].getRootFolder().createFolder(RESOURCES_FOLDER);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        assert resourceFolder != null;
        FileObject componentFolder = resourceFolder.getFileObject(COMPONENT_FOLDER);
        if (componentFolder == null) {
            try {
                componentFolder = resourceFolder.createFolder(COMPONENT_FOLDER);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        assert componentFolder !=null;

        SourceGroup resourcesSG[] = new SourceGroup[1];

        resourcesSG[0] = new ResourcesSourceGroup(resourceFolder);

        Templates.setTargetFolder(wizard, componentFolder);

        if (selectedText != null) {
            folderPanel = Templates.createSimpleTargetChooser(project, resourcesSG, new CompositeComponentWizardPanel(wizard, selectedText));
        } else {
            folderPanel = Templates.createSimpleTargetChooser(project, resourcesSG);
        }

        panels = new WizardDescriptor.Panel[] { folderPanel };

        Object prop = wizard.getProperty (WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
        String[] beforeSteps = null;
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = Utilities.createSteps(beforeSteps, panels);

        for (int i = 0; i < panels.length; i++) {
            JComponent jc = (JComponent)panels[i].getComponent ();
            if (steps[i] == null) {
                steps[i] = jc.getName ();
            }
	    jc.putClientProperty (WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer (i)); // NOI18N
	    jc.putClientProperty (WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
	}
}

    public void uninitialize(TemplateWizard wizard) {
        panels = null;
    }

    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    public String name() {
        return index + 1 + ". from " + panels.length;
    }

    public boolean hasNext() {
        return index < panels.length - 1;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    public void addChangeListener(ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }

    // If something changes dynamically (besides moving between panels), e.g.
    // the number of panels changes in response to user input, then uncomment
    // the following and call when needed: fireChangeEvent();
    /*
    private Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // or can use ChangeSupport in NB 6.0
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
    // You could safely ignore this method. Is is here to keep steps which were
    // there before this wizard was instantiated. It should be better handled
    // by NetBeans Wizard API itself rather than needed to be implemented by a
    // client code.
    private String[] createSteps() {
        String[] beforeSteps = null;
        Object prop = wizard.getProperty("WizardPanel_contentData");
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }

        if (beforeSteps == null) {
            beforeSteps = new String[0];
        }

        String[] res = new String[(beforeSteps.length - 1) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (beforeSteps.length - 1)) {
                res[i] = beforeSteps[i];
            } else {
                res[i] = panels[i - beforeSteps.length + 1].getComponent().getName();
            }
        }
        return res;
    }

    private class ResourcesSourceGroup implements SourceGroup {

        private final FileObject loc;
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

        public ResourcesSourceGroup(FileObject root) {
            loc = root;
        }


        public FileObject getRootFolder() {
            return loc;
        }

        public String getName() {
            return TYPE_RESOURCES;
        }

        public String getDisplayName() {
            return "Composite Components Resources";
        }

        public Icon getIcon(boolean opened) {
            return null;
        }

        public boolean contains(FileObject file) throws IllegalArgumentException {
            if (file == loc) {
                return true;
            }
            String path = FileUtil.getRelativePath(loc, file);
            if (path == null) {
                throw new IllegalArgumentException(file + " is not inside " + loc);
            }
            if (file.isFolder()) {
                path += "/"; // NOI18N
            }
            Project p = Templates.getProject(wizard);
            if (file.isFolder() && file != p.getProjectDirectory() && ProjectManager.getDefault().isProject(file)) {
                // #67450: avoid actually loading the nested project.
                return false;
            }
                // XXX disabled for typed source roots; difficult to make fast (#97215)
            Project owner = FileOwnerQuery.getOwner(file);
            if (owner != null && owner != p) {
                return false;
            }
            File f = FileUtil.toFile(file);
            if (f != null && SharabilityQuery.getSharability(f) == SharabilityQuery.NOT_SHARABLE) {
                return false;
            } // else MIXED, UNKNOWN, or SHARABLE; or not a disk file
            return true;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
    }

}
