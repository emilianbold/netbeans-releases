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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ant.freeform;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.ant.freeform.spi.ProjectNature;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.modules.ant.freeform.ui.ProjectCustomizerProvider;
import org.netbeans.modules.ant.freeform.ui.View;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
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
    
    public static final Lookup.Result<ProjectNature> PROJECT_NATURES = Lookup.getDefault().lookupResult(ProjectNature.class);
    
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
        Lookup baseLookup = Lookups.fixed(
            new Info(), // ProjectInformation
            new FreeformSources(this), // Sources
            new Actions(this), // ActionProvider
            new View(this), // LogicalViewProvider
            new ProjectCustomizerProvider(this), // CustomizerProvider
            aux, // AuxiliaryConfiguration
            helper().createCacheDirectoryProvider(), // CacheDirectoryProvider
            new Subprojects(this), // SubprojectProvider
            new ArtifactProvider(this), // AntArtifactProvider
            new LookupMergerImpl(), // LookupMerger or ActionProvider
            UILookupMergerSupport.createPrivilegedTemplatesMerger(), 
            new FreeformProjectOperations(this),
	    new FreeformSharabilityQuery(helper()), //SharabilityQueryImplementation
            new ProjectAccessor(this) //Access to AntProjectHelper and PropertyEvaluator
        );
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
        ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
            public Void run() {
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
            return ProjectManager.mutex().readAccess(new Mutex.Action<String>() {
                public String run() {
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
            if (usesAntScripting()) {
                return new ImageIcon(Utilities.loadImage("org/netbeans/modules/ant/freeform/resources/freeform-project.png", true)); // NOI18N
            } else {
                return new ImageIcon(Utilities.loadImage("org/netbeans/modules/project/ui/resources/projectTab.gif", true)); // NOI18N
            }
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
        private Lookup.Result<org.netbeans.spi.project.LookupMerger> mergers2;
        private Reference<LookupListener> listenerRef2;
        
        //#68623: the proxy lookup fires changes only if someone listens on a particular template:
        private List<Lookup.Result<?>> results;
        
        public FreeformLookup(Lookup baseLookup, FreeformProject project, AntProjectHelper helper, PropertyEvaluator evaluator, AuxiliaryConfiguration aux) {
            super();
            this.baseLookup = baseLookup;
            this.project = project;
            this.helper = helper;
            this.evaluator = evaluator;
            this.aux = aux;
            this.results = Collections.emptyList();
            updateLookup();
            PROJECT_NATURES.addLookupListener(WeakListeners.create(LookupListener.class, this, PROJECT_NATURES));
        }
        
        public void resultChanged (LookupEvent ev) {
            updateLookup();
        }
        
        private void updateLookup() {
            //unregister listeners from the old results:
            for (Lookup.Result<?> r : results) {
                r.removeLookupListener(this);
            }
            
            results = new ArrayList<Lookup.Result<?>>();
            
            List<Lookup> lookups = new ArrayList<Lookup>();
            lookups.add(baseLookup);
            for (ProjectNature pn : PROJECT_NATURES.allInstances()) {
                lookups.add(pn.getLookup(project, helper, evaluator, aux));
            }
            Lookup lkp = new ProxyLookup(lookups.toArray(new Lookup[lookups.size()]));
            
            //merge:
            List<Class<?>> filteredClasses = new ArrayList<Class<?>>();
            List<Object> mergedInstances = new ArrayList<Object>();
            //first comes the new project API's LookupMerger
            //merge:
            LookupListener l = listenerRef2 != null ? listenerRef2.get() : null;
            if (l != null) {
                mergers2.removeLookupListener(l);
            }
            mergers2 = lkp.lookupResult(org.netbeans.spi.project.LookupMerger.class);
            l = WeakListeners.create(LookupListener.class, this, mergers2);
            listenerRef2 = new WeakReference<LookupListener>(l);
            mergers2.addLookupListener(l);
            for (org.netbeans.spi.project.LookupMerger lm : mergers2.allInstances()) {
                Class<?> c = lm.getMergeableClass();
                if (filteredClasses.contains(c)) {
                    ErrorManager.getDefault().log(ErrorManager.WARNING,
                            "Two LookupMerger registered for class " + c +
                            ". Only first one will be used"); // NOI18N
                    continue;
                }
                filteredClasses.add(c);
                mergedInstances.add(lm.merge(lkp));
                
                Lookup.Result<?> result = lkp.lookupResult(c);
                
                result.addLookupListener(this);
                results.add(result);
            }
            
            lkp = Lookups.exclude(lkp, filteredClasses.toArray(new Class<?>[filteredClasses.size()]));
            Lookup fixed = Lookups.fixed(mergedInstances.toArray(new Object[mergedInstances.size()]));
            setLookups(fixed, lkp);
        }
        
    }
    
    /**
     * Utility method to decide if the project actually uses Ant scripting.
     * It does if at least one of these hold:
     * <ol>
     * <li>There is a <code>build.xml</code> at top level.
     * <li>The property <code>ant.script</code> is defined.
     * <li>There are any <code>&lt;action&gt;</code>s bound.
     * </ol>
     */
    public boolean usesAntScripting() {
        return getProjectDirectory().getFileObject("build.xml") != null || // NOI18N
                evaluator().getProperty("ant.script") != null || // NOI18N
                helper.getPrimaryConfigurationData(true).getElementsByTagName("action").getLength() > 0; // NOI18N
    }

}
