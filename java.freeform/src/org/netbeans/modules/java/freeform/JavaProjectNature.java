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

package org.netbeans.modules.java.freeform;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * General hook for registration of the Java nature for freeform projects.
 * @author David Konecny
 */
public class JavaProjectNature implements ProjectNature {

    public static final String NS_JAVA_1 = "http://www.netbeans.org/ns/freeform-project-java/1"; // NOI18N
    public static final String NS_JAVA_2 = "http://www.netbeans.org/ns/freeform-project-java/2"; // NOI18N
    public static final String EL_JAVA = "java-data"; // NOI18N
    private static final String SCHEMA_1 = "nbres:/org/netbeans/modules/java/freeform/resources/freeform-project-java.xsd"; // NOI18N
    private static final String SCHEMA_2 = "nbres:/org/netbeans/modules/java/freeform/resources/freeform-project-java-2.xsd"; // NOI18N
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
        return new HashSet(Arrays.asList(new String[] {SCHEMA_1, SCHEMA_2}));
    }

    public Set/*<String>*/ getSourceFolderViewStyles() {
        return Collections.singleton(STYLE_PACKAGES);
    }
    
    public org.openide.nodes.Node createSourceFolderView(Project project, FileObject folder, String style, String name, String displayName) throws IllegalArgumentException {
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

    public org.openide.nodes.Node findSourceFolderViewPath(Project project, org.openide.nodes.Node root, Object target) {
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
            new TestQuery(projectHelper, projectEvaluator, aux), // MultipleRootsUnitTestForSourceQueryImplementation
            new JavadocQuery(projectHelper, projectEvaluator, aux), // JavadocForBinaryQueryImplementation
            new PrivilegedTemplatesImpl(), // PrivilegedTemplates
            new JavaActions(project, projectHelper, projectEvaluator, aux), // ActionProvider
            new LookupMergerImpl(), // LookupMerger
        });
    }
    
    private static boolean isMyProject(AuxiliaryConfiguration aux) {
        return aux.getConfigurationFragment(EL_JAVA, NS_JAVA_1, true) != null ||
               aux.getConfigurationFragment(EL_JAVA, NS_JAVA_2, true) != null;
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
    
    /**
     * Transparently handles /1 -> /2 schema upgrade (on read only, not write!).
     */
    static final class UpgradingAuxiliaryConfiguration implements AuxiliaryConfiguration {
        
        private final AuxiliaryConfiguration delegate;
        
        public UpgradingAuxiliaryConfiguration(AuxiliaryConfiguration delegate) {
            this.delegate = delegate;
        }

        public Element getConfigurationFragment(String elementName, String namespace, boolean shared) {
            if (elementName.equals(EL_JAVA) && namespace.equals(NS_JAVA_2) && shared) {
                Element nue = delegate.getConfigurationFragment(EL_JAVA, NS_JAVA_2, true);
                if (nue == null) {
                    Element old = delegate.getConfigurationFragment(EL_JAVA, NS_JAVA_1, true);
                    if (old != null) {
                        nue = upgradeSchema(old);
                    }
                }
                return nue;
            } else {
                return delegate.getConfigurationFragment(elementName, namespace, shared);
            }
        }

        public void putConfigurationFragment(Element fragment, boolean shared) throws IllegalArgumentException {
            delegate.putConfigurationFragment(fragment, shared);
        }
        
        public boolean removeConfigurationFragment(String elementName, String namespace, boolean shared) throws IllegalArgumentException {
            return delegate.removeConfigurationFragment(elementName, namespace, shared);
        }
        
    }

    static Element upgradeSchema(Element old) {
        Document doc = old.getOwnerDocument();
        Element nue = doc.createElementNS(NS_JAVA_2, EL_JAVA);
        copyXMLTree(doc, old, nue, NS_JAVA_2);
        return nue;
    }

    // Copied from org.netbeans.modules.java.j2seproject.UpdateHelper with changes; could be an API eventually:
    private static void copyXMLTree(Document doc, Element from, Element to, String newNamespace) {
        NodeList nl = from.getChildNodes();
        int length = nl.getLength();
        for (int i = 0; i < length; i++) {
            org.w3c.dom.Node node = nl.item(i);
            org.w3c.dom.Node newNode;
            switch (node.getNodeType()) {
                case org.w3c.dom.Node.ELEMENT_NODE:
                    Element oldElement = (Element) node;
                    newNode = doc.createElementNS(newNamespace, oldElement.getTagName());
                    NamedNodeMap attrs = oldElement.getAttributes();
                    int alength = attrs.getLength();
                    for (int j = 0; j < alength; j++) {
                        org.w3c.dom.Attr oldAttr = (org.w3c.dom.Attr) attrs.item(j);
                        ((Element)newNode).setAttributeNS(oldAttr.getNamespaceURI(), oldAttr.getName(), oldAttr.getValue());
                    }
                    copyXMLTree(doc, oldElement, (Element) newNode, newNamespace);
                    break;
                case org.w3c.dom.Node.TEXT_NODE:
                    newNode = doc.createTextNode(((Text) node).getData());
                    break;
                case org.w3c.dom.Node.COMMENT_NODE:
                    newNode = doc.createComment(((Comment) node).getData());
                    break;
                default:
                    // Other types (e.g. CDATA) not yet handled.
                    throw new AssertionError(node);
            }
            to.appendChild(newNode);
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
            this.aux = new UpgradingAuxiliaryConfiguration(aux);
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
    
    private static final class PrivilegedTemplatesImpl implements PrivilegedTemplates, RecommendedTemplates {
        
        private static final String[] PRIVILEGED_NAMES = new String[] {
            "Templates/Classes/Class.java", // NOI18N
            "Templates/Classes/Package", // NOI18N
            "Templates/Classes/Interface.java", // NOI18N
        };
        
        // List of primarily supported templates = J2SEProject.LIBRARY_TYPES + J2SEProject.APPLICATION_TYPES
        private static final String[] RECOMENDED_TYPES = new String[] { 
            "java-classes",         // NOI18N
            "java-main-class",      // NOI18N
            "java-forms",           // NOI18N
            "gui-java-application", // NOI18N
            "java-beans",           // NOI18N
            "oasis-XML-catalogs",   // NOI18N
            "XML",                  // NOI18N
            "ant-script",           // NOI18N
            "ant-task",             // NOI18N
            "servlet-types",        // NOI18N
            // "web-types",         // NOI18N
            "junit",                // NOI18N
            // "MIDP",              // NOI18N
            "simple-files"          // NOI18N
        };
        
        public String[] getRecommendedTypes() {            
            return RECOMENDED_TYPES;
        }
        
        public String[] getPrivilegedTemplates() {
            return PRIVILEGED_NAMES;
        }
        
    }
    
}
