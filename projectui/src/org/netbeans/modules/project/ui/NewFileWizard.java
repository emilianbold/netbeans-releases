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

package org.netbeans.modules.project.ui;

import java.awt.Component;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.project.uiapi.ProjectChooserFactory;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.SourceGroup;
//import org.netbeans.spi.project.ui.templates.support.InstantiatingIterator;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;

public final class NewFileWizard extends TemplateWizard {
        
    private Project p;
    // private String[] recommendedTypes;

    public NewFileWizard( Project p /*, String recommendedTypes[] */ ) {
        this.p = p;
        // this.recommendedTypes = recommendedTypes;        
        setTitle( "New File" );
        setTitleFormat( new MessageFormat( "{0}") );
    }

    protected org.openide.WizardDescriptor.Panel createTemplateChooser() {
        return new TemplateChooserPanel( p /*, recommendedTypes */ );

    }        

    protected org.openide.WizardDescriptor.Panel createTargetChooser() {
        Sources c = ProjectUtils.getSources(p);
        return Templates.createSimpleTargetChooser(p, c.getSourceGroups(Sources.TYPE_GENERIC));
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
//    private final List/*<ChangeListener>*/ listeners = new ArrayList();
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
//        listeners.add(l);
//    }
//
//    public void removeChangeListener(ChangeListener l) {
//        listeners.remove(l);
//    }
//
//    private void fireChange() {
//        ChangeEvent e = new ChangeEvent(this);
//        Iterator it = listeners.iterator();
//        while (it.hasNext()) {
//            ((ChangeListener)it.next()).stateChanged(e);
//        }
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
//        fireChange();
//    }
//
//    public void stateChanged(ChangeEvent e) {
//        fireChange();
//    }
//    
//    private static InstantiatingIterator findTemplateWizardIterator(FileObject template, Project p) {
//        TemplateWizard.Iterator iter = (TemplateWizard.Iterator)template.getAttribute("templateWizardIterator"); // NOI18N
//        if (iter != null) {
//            return WizardIterators.templateIteratotBridge( iter );
//        } 
//        else {            
//            Sources c = (Sources)p.getLookup().lookup(Sources.class);
//            if (c == null) {
//                c = SourceContainers.genericOnly(p);
//            }
//            
//            WizardDescriptor.Panel panels[] = new WizardDescriptor.Panel[1];            
//            panels[0] = Templates.createSimpleTargetChooser(p, template, c.getSourceGroups(Sources.TYPE_GENERIC));
//            return new WizardIterators.InstantiatingArrayIterator( panels, template );
//        }
//    }
//
//    }            
