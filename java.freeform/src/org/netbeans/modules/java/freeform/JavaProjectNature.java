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

package org.netbeans.modules.java.freeform;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import org.netbeans.api.project.Project;
import org.netbeans.modules.ant.freeform.spi.ProjectNature;
import org.netbeans.modules.ant.freeform.spi.ProjectPropertiesPanel;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.modules.java.freeform.ui.ClasspathPanel;
import org.netbeans.modules.java.freeform.ui.OutputPanel;
import org.netbeans.modules.java.freeform.ui.ProjectModel;
import org.netbeans.modules.java.freeform.ui.SourceFoldersPanel;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.GenericSources;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * @author David Konecny
 */
public class JavaProjectNature implements ProjectNature {

    public static final String NS_JAVA = "http://www.netbeans.org/ns/freeform-project-java/1"; // NOI18N
    private static final String SCHEMA = "nbres:/org/netbeans/modules/java/freeform/resources/freeform-project-java.xsd"; // NOI18N
    public static final String STYLE_PACKAGES = "packages"; // NOI18N
    
    private static final WeakHashMap/*<Project,WeakReference<Lookup>>*/ lookupCache = new WeakHashMap();
    
    public JavaProjectNature() {}

    public Lookup getLookup(Project project, AntProjectHelper projectHelper, PropertyEvaluator projectEvaluator, AuxiliaryConfiguration aux) {
        WeakReference wr = (WeakReference)lookupCache.get(project);
        Lookup lookup = wr != null ? (Lookup)wr.get() : null;
        if (lookup == null) {
            lookup = new ProjectLookup(project, projectHelper, projectEvaluator, aux);
            lookupCache.put(project, new WeakReference(lookup));
        }
        return lookup;
    }
    
    public Set getCustomizerPanels(Project project, AntProjectHelper projectHelper, PropertyEvaluator projectEvaluator, AuxiliaryConfiguration aux) {
        HashSet l = new HashSet();
        if (!isMyProject(aux)) {
            return l;
        }
        ProjectModel pm = ProjectModel.createModel(Util.getProjectLocation(projectHelper, projectEvaluator), FileUtil.toFile(project.getProjectDirectory()), projectEvaluator, projectHelper);
        ProjectPropertiesPanel sfp = new SourceFoldersPanel.Panel(pm, projectHelper);
        l.add(sfp);
        ProjectPropertiesPanel cpp = new ClasspathPanel.Panel(pm);
        l.add(cpp);
        ProjectPropertiesPanel op = new OutputPanel.Panel(pm);
        l.add(op);
        return l;
    }
    
    public List getExtraTargets(Project project, AntProjectHelper projectHelper, PropertyEvaluator projectEvaluator, AuxiliaryConfiguration aux) {
        return new ArrayList();
    }

    public Set/*<String>*/ getSchemas() {
        return Collections.singleton(SCHEMA);
    }

    public Set/*<String>*/ getSourceFolderViewStyles() {
        return Collections.singleton(STYLE_PACKAGES);
    }
    
    public Node createSourceFolderView(Project project, FileObject folder, String style, String name, String displayName) throws IllegalArgumentException {
        if (style.equals(STYLE_PACKAGES)) {
            if (displayName == null) {
                // Don't use folder.getNodeDelegate().getDisplayName() since we are not listening to changes anyway.
                displayName = folder.getNameExt();
            }
            return PackageView.createPackageView(GenericSources.group(project, folder, name, displayName, null, null));
        } else {
            throw new IllegalArgumentException();
        }
    }

    public Node findSourceFolderViewPath(Project project, Node root, Object target) {
        // XXX
        return null;
    }

    private static Lookup initLookup(Project project, AntProjectHelper projectHelper, PropertyEvaluator projectEvaluator, AuxiliaryConfiguration aux) {
        Classpaths cp = new Classpaths(projectHelper, projectEvaluator, aux);
        return Lookups.fixed(new Object[] {
            cp, // ClassPathProvider
            new SourceLevelQueryImpl(projectHelper, projectEvaluator, aux), // SourceLevelQueryImplementation
            new SourceForBinaryQueryImpl(projectHelper, projectEvaluator, aux), // SourceForBinaryQueryImplementation
            new OpenHook(cp), // ProjectOpenedHook
            new TestQuery(project), // UnitTestForSourceQueryImplementation
        });
    }
    
    private static boolean isMyProject(AuxiliaryConfiguration aux) {
        return aux.getConfigurationFragment("java-data", NS_JAVA, true) != null;
    }

    private static class OpenHook extends ProjectOpenedHook {
        
        private final Classpaths cp;
        
        public OpenHook(Classpaths cp) {
            this.cp = cp;
        }
        
        protected void projectOpened() {
            cp.opened();
        }
        
        protected void projectClosed() {
            cp.closed();
        }
        
    }

    private static final class ProjectLookup extends ProxyLookup implements AntProjectListener {

        private AntProjectHelper helper;
        private PropertyEvaluator evaluator;
        private Project project;
        private AuxiliaryConfiguration aux;
        private boolean isMyProject;
        
        public ProjectLookup(Project project, AntProjectHelper helper, PropertyEvaluator evaluator, AuxiliaryConfiguration aux) {
            super(new Lookup[0]);
            this.project = project;
            this.helper = helper;
            this.evaluator = evaluator;
            this.aux = aux;
            this.isMyProject = isMyProject(aux);
            updateLookup();
            helper.addAntProjectListener(this);
        }
        
        private void updateLookup() {
            Lookup l = Lookup.EMPTY;
            if (isMyProject) {
                l = initLookup(project, helper, evaluator, aux);
            }
            setLookups(new Lookup[]{l});
        }
        
        public void configurationXmlChanged(AntProjectEvent ev) {
            if (isMyProject(aux) != isMyProject) {
                isMyProject = !isMyProject;
                updateLookup();
            }
        }
        
        public void propertiesChanged(AntProjectEvent ev) {
            // ignore
        }
        
    }
    
}
