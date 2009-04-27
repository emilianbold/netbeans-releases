/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.websvc.rest.wadl.design.wizard;

import java.awt.Component;
import java.awt.Container;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.websvc.rest.wadl.model.WadlModel;
import org.netbeans.modules.xml.api.EncodingUtil;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.ErrorManager;

public final class NewWadlWizardIterator implements TemplateWizard.Iterator {

    private int index;
    private WizardDescriptor.Panel[] panels;
    private WizardDescriptor.Panel folderPanel;
    private static final Logger logger = Logger.getLogger(NewWadlWizardIterator.class.getName());

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private Panel[] createPanels(Project project, TemplateWizard wizard) {
        Sources sources = project.getLookup().lookup(org.netbeans.api.project.Sources.class);
        List<SourceGroup> roots = new ArrayList<SourceGroup>();
        SourceGroup[] javaRoots =
                sources.getSourceGroups("java");
        roots.addAll(Arrays.asList(javaRoots));
        if (roots.isEmpty()) {
            SourceGroup[] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            roots.addAll(Arrays.asList(sourceGroups));
        }
        DataFolder folder = DataFolder.findFolder(roots.get(0).getRootFolder());
        DataFolder projectFolder =
                DataFolder.findFolder(project.getProjectDirectory());
        try {
            if (wizard.getTargetFolder().equals(projectFolder)) {
                wizard.setTargetFolder(folder);
            }
        } catch (IOException ioe) {
            wizard.setTargetFolder(folder);
        }
        folderPanel = new WadlPanel(project);
        // creates simple wizard panel with bottom panel
        WizardDescriptor.Panel firstPanel =
                new WizardNewWadlStep(Templates.createSimpleTargetChooser(project, null, folderPanel));
        JComponent c = (JComponent) firstPanel.getComponent();
        // the bottom panel should listen to changes on file name text field
        ((WadlPanel) folderPanel).setNameTF(findFileNameField(c, Templates.getTargetName(wizard)));

        return new WizardDescriptor.Panel[]{
                    firstPanel
                };
    }

    public Set<DataObject> instantiate(final TemplateWizard wiz) throws IOException {
        //Copy contents of temp model to a new file.
        //find the dataobject for the new file and return it.
        final FileObject dir = Templates.getTargetFolder(wiz);
        final String encoding = (String) wiz.getProperty(WadlPanel.ENCODING);
        final String name = Templates.getTargetName(wiz);
        FileSystem filesystem = dir.getFileSystem();
        final FileObject[] fileObject = new FileObject[1];
        FileSystem.AtomicAction fsAction = new FileSystem.AtomicAction() {

            public void run() throws IOException {
                FileObject fo = dir.createData(name, "wadl"); //NOI18N
                FileLock lock = null;
                try {
                    lock = fo.lock();
                    OutputStream out = fo.getOutputStream(lock);
                    out = new BufferedOutputStream(out);
                    Writer writer = new OutputStreamWriter(out, encoding);
                    //TODO
                    WadlModel tempModel = (WadlModel) wiz.getProperty(WizardNewWadlStep.TEMP_WADLMODEL);
                    Document doc = tempModel.getBaseDocument();
                    try {
                        writer.write(doc.getText(0, doc.getLength()));
                        writer.flush();
                    } catch (BadLocationException e) {
                        ErrorManager.getDefault().notify(e);
                    } finally {
                        writer.close();
                    }

                    fileObject[0] = fo;
                } finally {
                    if (lock != null) {
                        lock.releaseLock();
                    }
                }
            }
        };

        filesystem.runAtomicAction(fsAction);

        Set set = new HashSet(1);
        DataObject createdObject = DataObject.find(fileObject[0]);
        set.add(createdObject);
        return set;
    }

    public void initialize(TemplateWizard wiz) {
        index = 0;
        Project project = Templates.getProject(wiz);
        panels = createPanels(project, wiz);

        // Creating steps.
        Object prop = wiz.getProperty(WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
        String[] beforeSteps = null;
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
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
            }
        }

        String encoding = EncodingUtil.getProjectEncoding(project.getProjectDirectory());
        if (encoding == null) {
            encoding = "UTF8";
        }
        wiz.putProperty(WadlPanel.ENCODING, encoding);

    }

    public void uninitialize(TemplateWizard wiz) {

        panels = null;
        folderPanel = null;
        wiz.putProperty(WizardNewWadlStep.TEMP_WADLMODEL, null);
        
        File file = (File) wiz.getProperty(WizardNewWadlStep.TEMP_WADLFILE);
        wiz.putProperty(WizardNewWadlStep.TEMP_WADLFILE, null);
        
        if (file != null && file.exists()) {
            file = FileUtil.normalizeFile(file);
            FileObject fileObj = FileUtil.toFileObject(file);
            if (fileObj != null) {
                DataObject dobj;
                try {
                    dobj = DataObject.find(fileObj);
                    dobj.delete();
                } catch (Exception e) {
                    //ignore.
                }
                
            }
        }

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
    
    public static String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        //assert panels != null;
        // hack to use the steps set before this panel processed
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals(before[before.length - 1])) ? 1 : 0; // NOI18N
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
    
    //from schema wizard
    private JTextField findFileNameField(Component panel, String text) {
        Collection<Component> allComponents = new ArrayList<Component>();
        getAllComponents(new Component[]{panel}, allComponents);
        for (Component c : allComponents) {
            // we assume that the first text field is the file text field
            if (c instanceof JTextField) {
                JTextField tf = (JTextField) c;
                //if (text.equals(tf.getText())) {
                return tf;
            //}
            }
        }
        return null;
    }

    /*
     * Recursively gets all components in the components array and puts it in allComponents
     */
    public void getAllComponents(Component[] components, Collection<Component> allComponents) {
        for (int i = 0; i < components.length; i++) {
            if (components[i] != null) {
                allComponents.add(components[i]);
                if (((Container) components[i]).getComponentCount() != 0) {
                    getAllComponents(((Container) components[i]).getComponents(), allComponents);
                }
            }
        }
    }
}
