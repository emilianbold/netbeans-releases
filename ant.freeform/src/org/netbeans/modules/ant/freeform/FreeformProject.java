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

package org.netbeans.modules.ant.freeform;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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
import org.netbeans.spi.project.ui.PrivilegedTemplates;
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
            new PrivilegedTemplatesImpl(),           // List of templates in New action popup
            new Subprojects(this), // SubprojectProvider
            new ArtifactProvider(this), // AntArtifactProvider
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
    
    private static final class PrivilegedTemplatesImpl implements PrivilegedTemplates {
        
        private static final String[] PRIVILEGED_NAMES = new String[] {
            "Templates/Classes/Class.java", // NOI18N
            "Templates/Classes/Package", // NOI18N
            "Templates/Classes/Interface.java", // NOI18N
        };
        
        public String[] getPrivilegedTemplates() {
            return PRIVILEGED_NAMES;
        }
        
    }
    
    private static final class FreeformLookup extends ProxyLookup implements LookupListener {

        private final Lookup baseLookup;
        private final AntProjectHelper helper;
        private final PropertyEvaluator evaluator;
        private final FreeformProject project;
        private final AuxiliaryConfiguration aux;
        
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
            setLookups((Lookup[]) lookups.toArray(new Lookup[lookups.size()]));
        }
    }
    
}
