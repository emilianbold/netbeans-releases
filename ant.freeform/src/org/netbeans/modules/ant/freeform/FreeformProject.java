/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ant.freeform;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
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
import org.w3c.dom.NodeList;

/**
 * One freeform project.
 * @author Jesse Glick
 */
public final class FreeformProject implements Project {
    
    public static final Lookup.Result/*<ProjectNature>*/ PROJECT_NATURES = Lookup.getDefault().lookupResult(ProjectNature.class);
    
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
            new ProjectCustomizerProvider(this), // CustomizerProvider
            aux, // AuxiliaryConfiguration
            helper().createCacheDirectoryProvider(), // CacheDirectoryProvider
            new Subprojects(this), // SubprojectProvider
            new ArtifactProvider(this), // AntArtifactProvider
            new LookupMergerImpl(), // LookupMerger
            new FreeformProjectOperations(this),
	    new FreeformSharabilityQuery(helper()), //SharabilityQueryImplementation
            new ProjectAccessor(this), //Access to AntProjectHelper and PropertyEvaluator
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
    
    /** Store configured project name. */
    public void setName(final String name) {
        ProjectManager.mutex().writeAccess(new Mutex.Action() {
            public Object run() {
                Element data = helper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(FreeformProjectType.NS_GENERAL, "name");
                Element nameEl;
                if (nl.getLength() == 1) {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                } else {
                    nameEl = data.getOwnerDocument().createElementNS(FreeformProjectType.NS_GENERAL, "name");
                    data.insertBefore(nameEl, /* OK if null */data.getChildNodes().item(0));
                }
                nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
                helper.putPrimaryConfigurationData(data, true);
                return null;
            }
        });
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
        
        //#68623: the proxy lookup fires changes only if someone listens on a particular template:
        private List/*<Lookup.Result>*/ results;
        
        public FreeformLookup(Lookup baseLookup, FreeformProject project, AntProjectHelper helper, PropertyEvaluator evaluator, AuxiliaryConfiguration aux) {
            super(new Lookup[0]);
            this.baseLookup = baseLookup;
            this.project = project;
            this.helper = helper;
            this.evaluator = evaluator;
            this.aux = aux;
            this.results = Collections.EMPTY_LIST;
            updateLookup();
            PROJECT_NATURES.addLookupListener((LookupListener) WeakListeners.create(LookupListener.class, this, PROJECT_NATURES));
        }
        
        public void resultChanged (LookupEvent ev) {
            updateLookup();
        }
        
        private void updateLookup() {
            //unregister listeners from the old results:
            for (Iterator i = results.iterator(); i.hasNext(); ) {
                ((Lookup.Result) i.next()).removeLookupListener(this);
            }
            
            results = new ArrayList();
            
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
            mergers = lkp.lookupResult(LookupMerger.class);
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
                    
                    Lookup.Result result = lkp.lookupResult(classes[i]);
                    
                    result.addLookupListener(this);
                    results.add(result);
                }
            }
            lkp = Lookups.exclude(lkp, (Class[])filtredClasses.toArray(new Class[filtredClasses.size()]));
            Lookup fixed = Lookups.fixed(mergedInstances.toArray(new Object[mergedInstances.size()]));
            setLookups(new Lookup[]{fixed, lkp});
        }
        
    }
    
}
