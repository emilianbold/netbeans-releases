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

package org.netbeans.modules.web.freeform;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.w3c.dom.Element;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleFactory;
import org.netbeans.modules.web.spi.webmodule.WebModuleProvider;
import org.netbeans.modules.web.spi.webmodule.WebModuleImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;

/**
 * Web module implementation on top of freeform project.
 *
 * @author  Pavel Buzek
 */
public class WebModules implements WebModuleProvider, AntProjectListener, ClassPathProvider {
    
    private ArrayList modules = new ArrayList ();
    private HashMap cache = new HashMap ();
    private Project project;
    private AntProjectHelper helper;
    private PropertyEvaluator evaluator;
    
    public WebModules (Project project, AntProjectHelper helper, PropertyEvaluator evaluator) {
        assert project != null;
        this.project = project;
        this.helper = helper;
        this.evaluator = evaluator;
        helper.addAntProjectListener(this);
    }
    
    public synchronized WebModule findWebModule (FileObject file) {
        Project owner = FileOwnerQuery.getOwner (file);
        if (project.equals (owner)) {
            if (modules.isEmpty()) {
                readAuxData ();
            }
            for (Iterator iter = modules.iterator (); iter.hasNext ();) {
                FFWebModule wm = (FFWebModule) iter.next ();
                if (wm.contais (file)) {
                    if (cache.get (wm) == null) {
                        cache.put (wm, WebModuleFactory.createWebModule (wm));
                    }
                    return (WebModule) cache.get (wm);
                }
            }
        }
        return null;
    }

    public ClassPath findClassPath (FileObject file, String type) {
        Project owner = FileOwnerQuery.getOwner (file);
        if (owner != null && owner.equals (project)) {
            if (modules == null) {
                readAuxData ();
            }
            for (Iterator iter = modules.iterator (); iter.hasNext ();) {
                FFWebModule wm = (FFWebModule) iter.next ();
                if (wm.contais (file)) {
                    return wm.findClassPath (file, type);
                }
            }
        }
        return null;
    }
    
    public synchronized void readAuxData () {
        modules.clear();
        cache.clear();
        AuxiliaryConfiguration aux = (AuxiliaryConfiguration)project.getLookup().lookup(AuxiliaryConfiguration.class);
        assert aux != null;
        Element web = aux.getConfigurationFragment("web-data", WebProjectNature.NS_WEB, true);
        if (web == null) {
            return;
        }
        List/*<Element>*/ webModules = Util.findSubElements(web);
        Iterator it = webModules.iterator();
        while (it.hasNext()) {
            Element webModulesEl = (Element)it.next();
            assert webModulesEl.getLocalName().equals("web-module") : webModulesEl;
            FileObject docRootFO = getFile (webModulesEl, "doc-root"); //NOI18N
            Element j2eeSpecEl = Util.findElement (webModulesEl, "j2ee-spec-level", WebProjectNature.NS_WEB);
            String j2eeSpec = j2eeSpecEl == null ? null : evaluator.evaluate (Util.findText (j2eeSpecEl));
            Element contextPathEl = Util.findElement (webModulesEl, "context-path", WebProjectNature.NS_WEB);
            String contextPathText = contextPathEl == null ? null : Util.findText (contextPathEl);
            String contextPath = contextPathText == null ? null : evaluator.evaluate (contextPathText);
            Element classpathEl = Util.findElement (webModulesEl, "classpath", WebProjectNature.NS_WEB);
            ClassPath cp = classpathEl == null ? null : createClasspath (classpathEl);
            FileObject [] sources = getSources ();
            modules.add (new FFWebModule (docRootFO, j2eeSpec, contextPath, sources, cp));
        }
    }
    
    private FileObject getFile (Element parent, String fileElName) {
        Element el = Util.findElement (parent, fileElName, WebProjectNature.NS_WEB);
        String fname = Util.findText (el);
        String locationEval = evaluator.evaluate(fname);
        if (locationEval != null) {
            File locationFile = helper.resolveFile(locationEval);
            return FileUtil.toFileObject(locationFile);
        }
        return null;
    }

    private FileObject [] getSources () {
        SourceGroup sg [] = ProjectUtils.getSources (project).getSourceGroups (JavaProjectConstants.SOURCES_TYPE_JAVA);
        FileObject[] sources = new FileObject[sg.length];
        for (int i = 0; i < sg.length; i++)
            sources[i] = sg [i].getRootFolder ();
        return sources;
    }
    
    /**
     * Create a classpath from a &lt;classpath&gt; element.
     */
    private ClassPath createClasspath(Element classpathEl) {
        String cp = Util.findText(classpathEl);
        if (cp == null) {
            cp = "";
        }
        String cpEval = evaluator.evaluate(cp);
        if (cpEval == null) {
            return null;
        }
        String[] path = PropertyUtils.tokenizePath(cpEval);
        URL[] pathURL = new URL[path.length];
        for (int i = 0; i < path.length; i++) {
            File entryFile = helper.resolveFile(path[i]);
            URL entry;
            try {
                entry = entryFile.toURI().toURL();
                if (FileUtil.isArchiveFile(entry)) {
                    entry = FileUtil.getArchiveRoot(entry);
                } else {
                    String s = entry.toExternalForm();
                    if (!s.endsWith("/")) { // NOI18N
                        // Folder which is not built.
                        entry = new URL(s + '/');
                    }
                }
            } catch (MalformedURLException x) {
                throw new AssertionError(x);
            }
            pathURL[i] = entry;
        }
        return ClassPathSupport.createClassPath(pathURL);
    }
    
    public void configurationXmlChanged(AntProjectEvent ev) {
        readAuxData();
    }
    
    public void propertiesChanged(AntProjectEvent ev) {
        // ignore
    }
    
    private final class FFWebModule implements WebModuleImplementation {
        
        public static final String FOLDER_WEB_INF = "WEB-INF";//NOI18N
        public static final String FILE_DD        = "web.xml";//NOI18N
    
        private FileObject docRootFO;
        private FileObject [] sourcesFOs;
        private ClassPath classPath;
        private String j2eeSpec;
        private String contextPath;
        
        FFWebModule (FileObject docRootFO, String j2eeSpec, String contextPath, FileObject sourcesFOs[], ClassPath classPath) {
            this.docRootFO = docRootFO;
            this.j2eeSpec = j2eeSpec;
            this.contextPath = (contextPath == null ? "" : contextPath);
            this.sourcesFOs = sourcesFOs;
            this.classPath = classPath;
        }
        
        boolean contais (FileObject fo) {
            if (docRootFO == fo || FileUtil.isParentOf (docRootFO , fo))
                return true;
            for (int i = 0; i < sourcesFOs.length; i++) {
                if (sourcesFOs [i] == fo || FileUtil.isParentOf (sourcesFOs [i], fo))
                    return true;
            }
            return false;
        }
        
        public FileObject getDocumentBase () {
            return docRootFO;
        }
        
        public ClassPath findClassPath (FileObject file, String type) {
            return classPath;
        }
        
        public String getJ2eePlatformVersion () {
            return j2eeSpec;
        }
        
        public String getContextPath () {
            return contextPath;
        }
        
        public String toString () {
            StringBuffer sb = new StringBuffer ("web module in freeform project" +
                "\n\tdoc root:" + docRootFO.getPath () + 
                "\n\tcontext path:" + contextPath +
                "\n\tj2ee version:" + j2eeSpec);
            for (int i = 0; i < sourcesFOs.length; i++) {
                sb.append ("\n\tsource root:" + sourcesFOs [i].getPath ());
            }
            return sb.toString ();
        }
        
        public FileObject getDeploymentDescriptor () {
            return getWebInf ().getFileObject (FILE_DD);
        }
        
        public FileObject getWebInf () {
            return getDocumentBase ().getFileObject (FOLDER_WEB_INF);
        }
        
    }
}
