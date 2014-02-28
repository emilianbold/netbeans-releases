/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2me.project.wizard;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2me.project.J2MEProject;
import org.netbeans.modules.j2me.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 * Wizard to create a new J2ME file.
 *
 * @author Roman Svitanic
 */
public class NewJ2MEFileWizardIterator implements WizardDescriptor.AsynchronousInstantiatingIterator<WizardDescriptor> {

    static final String FOLDER = "Classes"; //NOI18N

    static final String JDK_5 = "jdk5"; //NOI18N

    private static final long serialVersionUID = 1L;

    public enum Type {

        FILE, PACKAGE, PKG_INFO
    }

    private final Type type;

    /**
     * Create a new wizard iterator.
     */
    public NewJ2MEFileWizardIterator() {
        this(Type.FILE);
    }

    private NewJ2MEFileWizardIterator(Type type) {
        this.type = type;
    }

    public static NewJ2MEFileWizardIterator createNewMidletWizardIterator() {
        return new NewJ2MEFileWizardIterator();
    }

    private WizardDescriptor.Panel[] createPanels(WizardDescriptor wizardDescriptor) {

        // Ask for Java folders
        Project project = Templates.getProject(wizardDescriptor);
        if (project == null) {
            throw new NullPointerException("No project found for: " + wizardDescriptor); //NOI18N
        }
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        assert groups != null : "Cannot return null from Sources.getSourceGroups: " + sources; //NOI18N
        groups = checkNotNull(groups, sources);
        if (groups.length == 0) {
            groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            groups = checkNotNull(groups, sources);
            return new WizardDescriptor.Panel[]{
                Templates.buildSimpleTargetChooser(project, groups).create()
            };
        } else {
            if (this.type == Type.FILE) {
                return new WizardDescriptor.Panel[]{
                    new J2METargetChooserPanel(project, groups, null, Type.FILE, false)};
            } else if (type == Type.PKG_INFO) {
                return new WizardDescriptor.Panel[]{
                    new J2METargetChooserPanel(project, groups, null, Type.PKG_INFO, true)};
            } else {
                assert type == Type.PACKAGE;

                SourceGroup[] resources = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_RESOURCES);
                assert resources != null;
                if (resources.length > 0) { // #161244
                    List<SourceGroup> all = new ArrayList<>();
                    all.addAll(Arrays.asList(groups));
                    all.addAll(Arrays.asList(resources));
                    groups = all.toArray(new SourceGroup[all.size()]);
                }
                return new WizardDescriptor.Panel[]{
                    new J2METargetChooserPanel(project, groups, null, Type.PACKAGE, false),};
            }
        }

    }

    private static SourceGroup[] checkNotNull(SourceGroup[] groups, Sources sources) {
        List<SourceGroup> sourceGroups = new ArrayList<>();
        for (SourceGroup sourceGroup : groups) {
            if (sourceGroup == null) {
                Exceptions.printStackTrace(new NullPointerException(sources + " returns null SourceGroup!")); //NOI18N
            } else {
                sourceGroups.add(sourceGroup);
            }
        }
        return sourceGroups.toArray(new SourceGroup[sourceGroups.size()]);
    }

    private String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        assert panels != null;
        // hack to use the steps set before this panel processed
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals(before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[(before.length - diff) + panels.length];
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
        FileObject dir = Templates.getTargetFolder(wiz);
        String targetName = Templates.getTargetName(wiz);

        DataFolder df = DataFolder.findFolder(dir);
        FileObject template = Templates.getTemplate(wiz);

        FileObject createdFile = null;
        if (this.type == Type.PACKAGE) {
            targetName = targetName.replace('.', '/'); // NOI18N
            createdFile = FileUtil.createFolder(dir, targetName);
        } else {
            DataObject dTemplate = DataObject.find(template);
            DataObject dobj = dTemplate.createFromTemplate(df, targetName);
            createdFile = dobj.getPrimaryFile();
        }

        final Object isMidletObject = Templates.getTemplate(wiz).getAttribute(J2METargetChooserPanelGUI.IS_MIDLET_TEMPLATE_ATTRIBUTE); // NOI18N
        boolean isMIDlet = false;
        if (isMidletObject instanceof Boolean) {
            isMIDlet = ((Boolean) isMidletObject).booleanValue();
        }

        if (isMIDlet) {
            final Project project = Templates.getProject(wiz);
            if (project instanceof J2MEProject) {
                ProjectManager.mutex().writeAccess(new Runnable() {
                    @Override
                    public void run() {
                        addMidletManifestAttribute((J2MEProject) project);
                    }
                });
            }
        }

        return Collections.singleton(createdFile);
    }

    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;

    @Override
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels(wiz);
        // Make sure list of steps is accurate.
        String[] beforeSteps = null;
        Object prop = wiz.getProperty(WizardDescriptor.PROP_CONTENT_DATA);
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }
        String[] steps = createSteps(beforeSteps, panels);
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i));
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            }
        }
    }

    @Override
    public void uninitialize(WizardDescriptor wiz) {
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
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    @Override
    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    private final transient Set<ChangeListener> listeners = new HashSet<>(1);

    @Override
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    protected final void fireChangeEvent() {
        ChangeListener[] ls;
        synchronized (listeners) {
            ls = listeners.toArray(new ChangeListener[listeners.size()]);
        }
        ChangeEvent ev = new ChangeEvent(this);
        for (ChangeListener l : ls) {
            l.stateChanged(ev);
        }
    }

    private void addMidletManifestAttribute(J2MEProject project) {
        String createdMidletName = (String) wiz.getProperty(J2METargetChooserPanel.MIDLET_NAME);
        String createdMidletClass = (String) wiz.getProperty(J2METargetChooserPanel.MIDLET_CLASSNAME);
        if (createdMidletName == null || createdMidletClass == null) {
            return;
        }
        AntProjectHelper h = project.getUpdateHelper().getAntProjectHelper();
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String midlets = ep.getProperty(J2MEProjectProperties.MANIFEST_MIDLETS);
        String[] midletsSplitted = midlets != null ? midlets.split("\n") : new String[0]; //NOI18N
        int midletCount = 0;
        for (String midlet : midletsSplitted) {
            if (midlet != null && midlet.startsWith("MIDlet-")) { //NOI18N
                midletCount++;
            }
        }
        StringBuilder midletsUpdate = new StringBuilder();
        midletsUpdate.append("MIDlet-").append(++midletCount).append(": "); //NOI18N
        midletsUpdate.append(createdMidletName);
        midletsUpdate.append(",,"); //NOI18N
        midletsUpdate.append(createdMidletClass);
        midletsUpdate.append("\n"); //NOI18N
        if (midlets != null) {
            midletsUpdate.append(midlets);
        }
        ep.put(J2MEProjectProperties.MANIFEST_MIDLETS, midletsUpdate.toString());
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        try {
            ProjectManager.getDefault().saveProject(project);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

}
