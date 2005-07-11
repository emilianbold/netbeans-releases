/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ant.freeform;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.ant.freeform.spi.LookupMerger;
import org.netbeans.modules.ant.freeform.spi.ProjectNature;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.modules.ant.freeform.ui.ProjectCustomizerProvider;
import org.netbeans.modules.ant.freeform.ui.View;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.w3c.dom.Element;

/**
 * One freeform project.
 * @author Jesse Glick
 */
public final class FreeformProject implements Project {
    
    public static final Lookup.Result/*<ProjectNature>*/ PROJECT_NATURES = Lookup.getDefault().lookup(new Lookup.Template(ProjectNature.class));
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final Lookup lookup;
    private AuxiliaryConfiguration aux;
    
    public FreeformProject(AntProjectHelper helper) throws IOException {
        this.helper = helper;
        eval = new FreeformEvaluator(this);
        lookup = initLookup();
        new ProjectXmlValidator(helper.resolveFileObject(AntProjectHelper.PROJECT_XML_PATH));
    }
    
    public AntProjectHelper helper() {
        return helper;
    }
    
    private Lookup initLookup() throws IOException {
        aux = helper().createAuxiliaryConfiguration(); // AuxiliaryConfiguration
        Lookup baseLookup = Lookups.fixed(new Object[] {
            new Info(), // ProjectInformation
            new FreeformSources(this), // Sources
            new Actions(this), // ActionProvider
            new View(this), // LogicalViewProvider
            new ProjectCustomizerProvider(this, helper, eval), // CustomizerProvider
            aux, // AuxiliaryConfiguration
            helper().createCacheDirectoryProvider(), // CacheDirectoryProvider
            new Subprojects(this), // SubprojectProvider
            new ArtifactProvider(this), // AntArtifactProvider
            new LookupMergerImpl(), // LookupMerger
            new FreeformProjectOperations(this),
        });
        return new FreeformLookup(baseLookup, this, helper, eval, aux);
    }
    
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }
    
    public Lookup getLookup() {
        return lookup;
    }
    
    public PropertyEvaluator evaluator() {
        return eval;
    }

    public String toString() {
        return "FreeformProject[" + getProjectDirectory() + "]"; // NOI18N
    }
    
    private final class Info implements ProjectInformation {
        
        public Info() {}
        
        public String getName() {
            return PropertyUtils.getUsablePropertyName(getDisplayName());
        }
        
        public String getDisplayName() {
            return (String) ProjectManager.mutex().readAccess(new Mutex.Action() {
                public Object run() {
                    Element genldata = helper.getPrimaryConfigurationData(true);
                    Element nameEl = Util.findElement(genldata, "name", FreeformProjectType.NS_GENERAL); // NOI18N
                    if (nameEl == null) {
                        // Corrupt. Cf. #48267 (cause unknown).
                        return "???"; // NOI18N
                    }
                    return Util.findText(nameEl);
                }
            });
        }
        
        public Icon getIcon() {
            return new ImageIcon(Utilities.loadImage("org/netbeans/modules/ant/freeform/resources/freeform-project.png", true)); // NOI18N
        }
        
        public Project getProject() {
            return FreeformProject.this;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            // XXX
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            // XXX
        }
        
    }
    
    private static final class FreeformLookup extends ProxyLookup implements LookupListener {

        private final Lookup baseLookup;
        private final AntProjectHelper helper;
        private final PropertyEvaluator evaluator;
        private final FreeformProject project;
        private final AuxiliaryConfiguration aux;
        private Lookup.Result/*<LookupMerger>*/ mergers;
        private WeakReference listenerRef;
        
        public FreeformLookup(Lookup baseLookup, FreeformProject project, AntProjectHelper helper, PropertyEvaluator evaluator, AuxiliaryConfiguration aux) {
            super(new Lookup[0]);
            this.baseLookup = baseLookup;
            this.project = project;
            this.helper = helper;
            this.evaluator = evaluator;
            this.aux = aux;
            updateLookup();
            PROJECT_NATURES.addLookupListener((LookupListener) WeakListeners.create(LookupListener.class, this, PROJECT_NATURES));
        }
        
        public void resultChanged (LookupEvent ev) {
            updateLookup();
        }
        
        private void updateLookup() {
            List/*<Lookup>*/ lookups = new ArrayList();
            lookups.add(baseLookup);
            Iterator/*<ProjectNature>*/ it = PROJECT_NATURES.allInstances().iterator();
            while (it.hasNext()) {
                ProjectNature pn  = (ProjectNature) it.next();
                lookups.add(pn.getLookup(project, helper, evaluator, aux));
            }
            Lookup lkp = new ProxyLookup((Lookup[]) lookups.toArray(new Lookup[lookups.size()]));
            
            //merge:
            ArrayList filtredClasses = new ArrayList();
            ArrayList mergedInstances = new ArrayList();
            LookupListener l = listenerRef != null ? (LookupListener)listenerRef.get() : null;
            if (l != null) {
                mergers.removeLookupListener(l);
            }
            mergers = lkp.lookup(new Lookup.Template(LookupMerger.class));
            l = (LookupListener) WeakListeners.create(LookupListener.class, this, mergers);
            listenerRef = new WeakReference(l);
            mergers.addLookupListener(l);
            it = mergers.allInstances().iterator();
            while (it.hasNext()) {
                LookupMerger lm = (LookupMerger)it.next();
                Class[] classes = lm.getMergeableClasses();
                for (int i=0; i<classes.length; i++) {
                    if (filtredClasses.contains(classes[i])) {
                        ErrorManager.getDefault().log(ErrorManager.WARNING, 
                            "Two LookupMerger registered for class "+classes[i]+
                            ". Only first one will be used"); // NOI18N
                        continue;
                    }
                    filtredClasses.add(classes[i]);
                    mergedInstances.add(lm.merge(lkp, classes[i]));
                }
            }
            lkp = Lookups.exclude(lkp, (Class[])filtredClasses.toArray(new Class[filtredClasses.size()]));
            Lookup fixed = Lookups.fixed(mergedInstances.toArray(new Object[mergedInstances.size()]));
            setLookups(new Lookup[]{fixed, lkp});
        }
        
    }
    
}
