/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.project.ui.templates.support;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.SourceFolderContainer;
import org.netbeans.spi.project.support.SourceFolderContainers;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;

// XXX might need variant of createSimpleTargetChooser which returns Panel
// and specifies how it will store the selected name and location
// XXX impls should be moved to projects/projectui module

/**
 * Default implementations of template UI.
 * @author Jesse Glick
 */
public class Templates {
    
    /**
     * Special key added to a wizard to communicate the choice of project to
     * a custom template wizard iterator associated with a particular template.
     * @see #createTemplateProvider
     * @see WizardDescriptor#getProperty
     */
    public static final String WIZARD_KEY_PROJECT = "project"; // NOI18N
    
    private Templates() {}
    
    /**
     * Create a standard template chooser wizard for a project.
     * <p>
     * Files in the <code>Templates</code> folder of the system filesystem which
     * are marked as templates (true value of <code>template</code> attribute)
     * and not marked as special (null or true value of <code>simple</code> attribute)
     * are displayed if the user chooses to see all templates; by default only
     * "supported" templates are shown, which (if the list of recommended types is
     * not null) are restricted to those with a true value for at least one file attribute
     * included in the type list.
     * <p>
     * For each file, if it has a <code>templateWizardIterator</code> attribute (of type
     * {@link org.openide.loaders.TemplateWizard.Iterator}), that is used as is.
     * (To inform the custom wizard of the selected project, the {@link Project} object is
     * accessible from {@link WizardDescriptor#getProperty} with key {@link #WIZARD_KEY_PROJECT}.)
     * Otherwise {@link #createSimpleTargetChooser} is used, with source folders corresponding
     * to all those of type {@link SourceFolderContainer#TYPE_GENERIC} in the project
     * ({@link SourceFolderContainers#genericOnly} serving as a default in case the project's
     * lookup has no {@link SourceFolderContainer} instance).
     * @param p a project in which new objects may be created (according to {@link SourceFolderContainer})
     * @param recommendedTypes a list of template categories this project is
     *                         considered to support, or null for unrestricted
     * @return a factory for templates, using the standard <code>Templates</code>
     *         folder possibly restricted by type
     */
    public static TemplateWizard.Iterator createTemplateProvider(Project p, String[] recommendedTypes) {
        return new BasicTemplateWizard(p, recommendedTypes);
    }
    
    /**
     * Create a basic target chooser suitable for many kinds of templates.
     * The user is prompted to choose a location for the new file and a (base) name.
     * Instantiation is handled by {@link org.openide.loaders.DataObject#createFromTemplate}.
     * @param template the file to use as a template
     * @param folders a list of possible roots to create the new file in
     * @return a wizard panel(s) prompting the user to choose a name and location
     */
    public static TemplateWizard.Iterator createSimpleTargetChooser(FileObject template, SourceFolderContainer.SourceFolderGroup[] folders) {
        // XXX
        return null;
    }
    
    private static final class BasicTemplateWizard implements TemplateWizard.Iterator, ChangeListener {
        
        /** Associated project. */
        private final Project p;
        /** Recommended template types, or null for any. */
        private final String[] recommendedTypes;
        /** Currently selected template to delegate subsequent panels to, or null. */
        private TemplateWizard.Iterator delegate = null;
        /** True if currently on a panel created by the delegate. */
        private boolean insideDelegate = false;
        /** The template chooser panel (initially null). */
        private WizardDescriptor.Panel templateChooser = null;
        /** Change listeners. */
        private final List/*<ChangeListener>*/ listeners = new ArrayList();
        /** Currently used wizard. */
        private TemplateWizard wiz = null;
        
        BasicTemplateWizard(Project p, String[] recommendedTypes) {
            this.p = p;
            this.recommendedTypes = recommendedTypes;
        }
        
        public void initialize(TemplateWizard wiz) {
            this.wiz = wiz;
            wiz.putProperty(WIZARD_KEY_PROJECT, p);
        }
        
        public void uninitialize(TemplateWizard wiz) {
            this.wiz = null;
            insideDelegate = false;
            setDelegate(null);
            templateChooser = null;
        }
        
        public Set instantiate(TemplateWizard wiz) throws IOException {
            assert insideDelegate;
            return delegate.instantiate(wiz);
        }
        
        public String name() {
            if (insideDelegate) {
                return delegate.name();
            } else {
                return "Choose Template"; // XXX I18N
            }
        }
        
        public WizardDescriptor.Panel current() {
            if (insideDelegate) {
                return delegate.current();
            } else {
                if (templateChooser == null) {
                    templateChooser = new TemplateChooserPanel();
                }
                return templateChooser;
            }
        }
        
        public boolean hasNext() {
            if (insideDelegate) {
                return delegate.hasNext();
            } else {
                return delegate != null;
            }
        }
        
        public boolean hasPrevious() {
            return insideDelegate;
        }
        
        public void nextPanel() {
            if (insideDelegate) {
                delegate.nextPanel();
            } else {
                assert delegate != null;
                insideDelegate = true;
            }
        }
        
        public void previousPanel() {
            assert insideDelegate;
            if (delegate.hasPrevious()) {
                delegate.previousPanel();
            } else {
                insideDelegate = false;
            }
        }
        
        public void addChangeListener(ChangeListener l) {
            listeners.add(l);
        }
        
        public void removeChangeListener(ChangeListener l) {
            listeners.remove(l);
        }
        
        private void fireChange() {
            ChangeEvent e = new ChangeEvent(this);
            Iterator it = listeners.iterator();
            while (it.hasNext()) {
                ((ChangeListener)it.next()).stateChanged(e);
            }
        }
        
        public void setDelegate(TemplateWizard.Iterator nue) {
            assert !insideDelegate;
            if (delegate == nue) {
                return;
            }
            if (delegate != null) {
                delegate.removeChangeListener(this);
                delegate.uninitialize(wiz);
            }
            if (nue != null) {
                nue.initialize(wiz);
                nue.addChangeListener(this);
            }
            delegate = nue;
            fireChange();
        }
        
        public void stateChanged(ChangeEvent e) {
            fireChange();
        }
        
        private final class TemplateChooserPanel implements WizardDescriptor.Panel, ChangeListener {
            
            private final List/*<ChangeListener>*/ listeners = new ArrayList();
            private TemplateChooserPanelGUI gui;
            
            TemplateChooserPanel() {}
            
            public Component getComponent() {
                if (gui == null) {
                    gui = new TemplateChooserPanelGUI(recommendedTypes);
                    gui.addChangeListener(this);
                }
                return gui;
            }
            
            public HelpCtx getHelp() {
                // XXX
                return null;
            }
            
            public boolean isValid() {
                return delegate != null;
            }
            
            public void addChangeListener(ChangeListener l) {
                listeners.add(l);
            }
            
            public void removeChangeListener(ChangeListener l) {
                listeners.remove(l);
            }
            
            private void fireChange() {
                ChangeEvent e = new ChangeEvent(this);
                Iterator it = listeners.iterator();
                while (it.hasNext()) {
                    ((ChangeListener)it.next()).stateChanged(e);
                }
            }
            
            public void readSettings(Object settings) {}
            
            public void storeSettings(Object settings) {}
            
            public void stateChanged(ChangeEvent e) {
                FileObject template = gui.getTemplate();
                if (template != null) {
                    setDelegate(findTemplateWizardIterator(template, p));
                } else {
                    setDelegate(null);
                }
                fireChange();
            }
            
        }
        
    }
    
    private static TemplateWizard.Iterator findTemplateWizardIterator(FileObject template, Project p) {
        TemplateWizard.Iterator iter = (TemplateWizard.Iterator)template.getAttribute("templateWizardIterator"); // NOI18N
        if (iter != null) {
            return iter;
        } else {
            SourceFolderContainer c = (SourceFolderContainer)p.getLookup().lookup(SourceFolderContainer.class);
            if (c == null) {
                c = SourceFolderContainers.genericOnly(p);
            }
            return createSimpleTargetChooser(template, c.getSourceFolderGroups(SourceFolderContainer.TYPE_GENERIC));
        }
    }
    
}
