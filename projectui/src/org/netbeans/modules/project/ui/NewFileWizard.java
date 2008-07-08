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
package org.netbeans.modules.project.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.project.uiapi.ProjectChooserFactory;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

import org.openide.loaders.TemplateWizard;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

public final class NewFileWizard extends TemplateWizard {

    private Project currP;
    private MessageFormat format;
    // private String[] recommendedTypes;
    private Project getCurrentProject() {
        return currP;
    }

    private void setCurrentProject(Project p) {
        this.currP = p;
    }

    public NewFileWizard(Project project /*, String recommendedTypes[] */) {
        setCurrentProject(project);
        putProperty(ProjectChooserFactory.WIZARD_KEY_PROJECT, getCurrentProject());
        format = new MessageFormat(NbBundle.getBundle(NewFileWizard.class).getString("LBL_NewFileWizard_MessageFormat"));
        // this.recommendedTypes = recommendedTypes;        
        //setTitleFormat( new MessageFormat( "{0}") );
        addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                // check ProjectChooserFactory.WIZARD_KEY_PROJECT property
                if (ProjectChooserFactory.WIZARD_KEY_PROJECT.equals(evt.getPropertyName())) {
                    Project newProject = (Project) evt.getNewValue();
                    if (!getCurrentProject().equals(newProject)) {
                        // set the new project and force reload panels in wizard
                        setCurrentProject(newProject);
                        try {
                            //reload (DataObject.find (Templates.getTemplate (NewFileWizard.this)));
                            // bugfix #44481, check if the template is null
                            if (Templates.getTemplate(NewFileWizard.this) != null) {
                                DataObject obj = DataObject.find(Templates.getTemplate(NewFileWizard.this));

                                // read the attributes declared in module's layer
                                Object unknownIterator = obj.getPrimaryFile().getAttribute("instantiatingIterator"); //NOI18N
                                if (unknownIterator == null) {
                                    unknownIterator = obj.getPrimaryFile().getAttribute("templateWizardIterator"); //NOI18N
                                }
                                // set default NewFileIterator if no attribute is set
                                if (unknownIterator == null) {
                                    try {
                                        obj.getPrimaryFile().setAttribute("instantiatingIterator", NewFileIterator.genericFileIterator()); //NOI18N
                                    } catch (java.io.IOException e) {
                                        // can ignore it because a iterator will created though
                                    }
                                }
                                Hacks.reloadPanelsInWizard(NewFileWizard.this, obj);
                            }
                        } catch (DataObjectNotFoundException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    public void updateState() {
        super.updateState();
        String substitute = (String) getProperty("NewFileWizard_Title"); // NOI18N
        String title;
        if (substitute == null) {
            title = NbBundle.getBundle(NewFileWizard.class).getString("LBL_NewFileWizard_Title"); // NOI18N
        } else {
            Object[] args = new Object[]{
                NbBundle.getBundle(NewFileWizard.class).getString("LBL_NewFileWizard_Subtitle"), // NOI18N
                substitute
            };
            title = format.format(args);
        }
        super.setTitle(title);
    }

    public void setTitle(String ignore) {
    }

    protected WizardDescriptor.Panel<WizardDescriptor> createTemplateChooser() {
        WizardDescriptor.Panel<WizardDescriptor> panel = new TemplateChooserPanel(getCurrentProject() /*, recommendedTypes */);
        JComponent jc = (JComponent) panel.getComponent();
        jc.getAccessibleContext().setAccessibleName(NbBundle.getBundle(NewProjectWizard.class).getString("ACSN_NewFileWizard")); // NOI18N
        jc.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(NewProjectWizard.class).getString("ACSD_NewFileWizard")); // NOI18N
        return panel;
    }

    protected WizardDescriptor.Panel<WizardDescriptor> createTargetChooser() {
        Sources c = ProjectUtils.getSources(getCurrentProject());
        return Templates.createSimpleTargetChooser(getCurrentProject(), c.getSourceGroups(Sources.TYPE_GENERIC));
    }

    @Override
    protected Iterator createDefaultIterator() {
        // If the template has Page Layouts then use extended iterator
        // else use the default iterator
        if (hasPageLayouts(getTemplate().getPrimaryFile())) {
            return new DefaultIteratorExt();
        } else {
            return super.createDefaultIterator();
        }
    }

    /**
     * Check if any Page Layouts available associated with this template
     * @param template
     * @return
     */
    private boolean hasPageLayouts(FileObject template) {
        String pageLayoutsFolderName = "PageLayouts/" + template.getName(); // NOI18N
        FileObject pageLayoutsFolder = Repository.getDefault().getDefaultFileSystem().findResource(pageLayoutsFolderName);
        if (pageLayoutsFolder != null) {
            return pageLayoutsFolder.getChildren().length > 0;
        } else {
            return false;
        }
    }

    /**
     * Default iterator extended for optional Page Layout Chooser Panel
     */
    private final class DefaultIteratorExt implements Iterator {

        private PageLayoutChooserPanel pageLayoutChooserPanel = new PageLayoutChooserPanel(getTemplate().getPrimaryFile());
        private WizardDescriptor.Panel<WizardDescriptor> targetChooserPanel = targetChooser();
        private transient WizardDescriptor.Panel<WizardDescriptor>[] panels;
        private int index;

        DefaultIteratorExt() {
        }

        /** Name */
        public String name() {
            return ""; // NOI18N
        }

        /** Instantiates the template using informations provided by
         * the wizard.
         *
         * @param wiz the wizard
         * @return set of data objects that has been created (should contain
         *   at least one) 
         * @exception IOException if the instantiation fails
         */
        public Set<DataObject> instantiate(TemplateWizard wiz) throws IOException {
            String n = wiz.getTargetName();
            DataFolder folder = wiz.getTargetFolder();
            DataObject template = wiz.getTemplate();
           
            Map<String, Object> wizardProps = new HashMap<String, Object>();
            for (Map.Entry<String, ? extends Object> entry : wiz.getProperties().entrySet()) {
                wizardProps.put("wizard." + entry.getKey(), entry.getValue()); // NOI18N
            }
            if (panels[index] == pageLayoutChooserPanel) {
                PageLayoutData selectedPageLayout = pageLayoutChooserPanel.getSelectedPageLayout();
                FileObject resourceFolder = FileUtil.createFolder(folder.getPrimaryFile(), pageLayoutChooserPanel.getResourceFolder()); 
                selectedPageLayout.copyResources(resourceFolder, pageLayoutChooserPanel.canOverwrite());
                template = DataObject.find(selectedPageLayout.getFileObject());
                wizardProps.put("folder", pageLayoutChooserPanel.getResourceFolder());
            }

            DataObject obj = template.createFromTemplate(folder, n, wizardProps);

            // run default action (hopefully should be here)
            final Node node = obj.getNodeDelegate();
            final Action a = node.getPreferredAction();
            if (a != null) {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        a.actionPerformed(new ActionEvent(node, ActionEvent.ACTION_PERFORMED, "")); // NOI18N
                    }
                });
            }

            return Collections.singleton(obj);
        }

        /** No-op implementation.
         */
        @SuppressWarnings("unchecked") 
        public void initialize(TemplateWizard wiz) {
            panels = new WizardDescriptor.Panel[]{targetChooserPanel, pageLayoutChooserPanel};
            // Make sure list of steps is accurate.
            String[] beforeSteps = (String[]) wiz.getProperty(WizardDescriptor.PROP_CONTENT_DATA);
            int beforeStepLength = beforeSteps.length - 1;
            String[] steps = createSteps(beforeSteps);
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i + beforeStepLength - 1));
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                }
            }
        }

        private String[] createSteps(String[] beforeSteps) {
            int beforeStepLength = beforeSteps.length - 1;
            String[] res = new String[beforeStepLength + panels.length];
            for (int i = 0; i < res.length; i++) {
                if (i < (beforeStepLength)) {
                    res[i] = beforeSteps[i];
                } else {
                    res[i] = panels[i - beforeStepLength].getComponent().getName();
                }
            }
            return res;
        }

        /** No-op implementation.
         */
        public void uninitialize(TemplateWizard wiz) {
        }

        /** Get the current panel.
         * @return the panel
         */
        public Panel<WizardDescriptor> current() {
            return panels[index];
        }

        /** Test whether there is a next panel.
         * @return <code>true</code> if so
         */
        public boolean hasNext() {
            return index < panels.length - 1;
        }

        /** Test whether there is a previous panel.
         * @return <code>true</code> if so
         */
        public boolean hasPrevious() {
            return index > 0;
        }

        /** Move to the next panel.
         * I.e. increment its index, need not actually change any GUI itself.
         * @exception NoSuchElementException if the panel does not exist
         */
        public void nextPanel() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            index++;
        }

        /** Move to the previous panel.
         * I.e. decrement its index, need not actually change any GUI itself.
         * @exception NoSuchElementException if the panel does not exist
         */
        public void previousPanel() {
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }
            index--;
        }

        /** Add a listener to changes of the current panel.
         * The listener is notified when the possibility to move forward/backward changes.
         * @param l the listener to add
         */
        public void addChangeListener(javax.swing.event.ChangeListener l) {
        }

        /** Remove a listener to changes of the current panel.
         * @param l the listener to remove
         */
        public void removeChangeListener(javax.swing.event.ChangeListener l) {
        }
    }
}
/** Old impl might be usefull later in Wizards API

///** Wizard for creating new files in a project.
// *
// * @author  Jesse Glick, Petr Hrebejk
// */
//public class NewFileWizard implements TemplateWizard.Iterator, ChangeListener {
//        
//    /** Currently selected project */
//    private Project p;
//    /** Recommended template types, or null for any. */
//    private final String[] recommendedTypes;
//    /** Currently selected template to delegate subsequent panels to, or null. */
//    private InstantiatingIterator delegate = null;
//    /** True if currently on a panel created by the delegate. */
//    private boolean insideDelegate = false;
//    /** The template chooser panel (initially null). */
//    private WizardDescriptor.Panel templateChooser = null;
//    /** Change listeners. */
//    private final ChangeSupport changeSupport = new ChangeSupport(this);
//    /** Currently used wizard. */
//    private TemplateWizard wiz = null;
//    
//    /** Creates a new instance of NewFileWizard */
//    public NewFileWizard(Project p, String[] recommendedTypes) {
//        this.p = p;
//        this.recommendedTypes = recommendedTypes;
//    }
//    
//    public void initialize(TemplateWizard wiz) {
//        this.wiz = wiz;
//        wiz.putProperty(ProjectChooserFactory.WIZARD_KEY_PROJECT, p);
//    }
//
//    public void uninitialize(TemplateWizard wiz) {
//        this.wiz = null;
//        insideDelegate = false;
//        setDelegate(null);
//        templateChooser = null;
//    }
//
//    public Set instantiate(TemplateWizard wiz) throws IOException {
//        assert insideDelegate;
//        return delegate.instantiate(wiz);
//    }
//
//    public String name() {
//        if (insideDelegate) {
//            return delegate.name();
//        } else {
//            return "Choose Template"; // XXX I18N
//        }
//    }
//
//    /*
//    public WizardDescriptor.Panel current() {
//        if (insideDelegate) {
//            return delegate.current();
//        } else {
//            if (templateChooser == null) {
//                templateChooser = new TemplateChooserPanel();
//            }
//            return templateChooser;
//        }
//    }
//     */
//
//    public boolean hasNext() {
//        if (insideDelegate) {
//            return delegate.hasNext();
//        } else {
//            return delegate != null;
//        }
//    }
//
//    public boolean hasPrevious() {
//        return insideDelegate;
//    }
//
//    public void nextPanel() {
//        if (insideDelegate) {
//            delegate.nextPanel();
//        } else {
//            assert delegate != null;
//            insideDelegate = true;
//        }
//    }
//
//    public void previousPanel() {
//        assert insideDelegate;
//        if (delegate.hasPrevious()) {
//            delegate.previousPanel();
//        } else {
//            insideDelegate = false;
//        }
//    }
//
//    public void addChangeListener(ChangeListener l) {
//        changeSupport.addChangeListener(l);
//    }
//
//    public void removeChangeListener(ChangeListener l) {
//        changeSupport.removeChangeListener(l);
//    }
//
//    public void setDelegate(InstantiatingIterator nue) {
//        assert !insideDelegate;
//        if (delegate == nue) {
//            return;
//        }
//        if (delegate != null) {
//            delegate.removeChangeListener(this);
//            delegate.uninitialize(wiz);
//        }
//        if (nue != null) {
//            nue.initialize(wiz);
//            nue.addChangeListener(this);
//        }
//        delegate = nue;
//        changeSupport.fireChange();
//    }
//
//    public void stateChanged(ChangeEvent e) {
//        changeSupport.fireChange();
//    }
//    
//    private static InstantiatingIterator findTemplateWizardIterator(FileObject template, Project p) {
//        TemplateWizard.Iterator iter = (TemplateWizard.Iterator)template.getAttribute("templateWizardIterator"); // NOI18N
//        if (iter != null) {
//            return WizardIterators.templateIteratotBridge( iter );
//        } 
//        else {            
//            Sources c = ProjectUtils.getSources(p);
//            WizardDescriptor.Panel panels[] = new WizardDescriptor.Panel[1];            
//            panels[0] = Templates.createSimpleTargetChooser(p, template, c.getSourceGroups(Sources.TYPE_GENERIC));
//            return new WizardIterators.InstantiatingArrayIterator( panels, template );
//        }
//    }
//
//    }            
