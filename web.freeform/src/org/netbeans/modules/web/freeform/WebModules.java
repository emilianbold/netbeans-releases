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
import java.util.Collections;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileStateInvalidException;
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
        Element web = aux.getConfigurationFragment(WebProjectNature.EL_WEB, WebProjectNature.NS_WEB_2, true);
        if (web == null) {
            return;
        }
        List/*<Element>*/ webModules = Util.findSubElements(web);
        Iterator it = webModules.iterator();
        while (it.hasNext()) {
            Element webModulesEl = (Element)it.next();
            assert webModulesEl.getLocalName().equals("web-module") : webModulesEl;
            FileObject docRootFO = getFile (webModulesEl, "doc-root"); //NOI18N
            Element j2eeSpecEl = Util.findElement (webModulesEl, "j2ee-spec-level", WebProjectNature.NS_WEB_2);
            String j2eeSpec = j2eeSpecEl == null ? null : evaluator.evaluate (Util.findText (j2eeSpecEl));
            Element contextPathEl = Util.findElement (webModulesEl, "context-path", WebProjectNature.NS_WEB_2);
            String contextPathText = contextPathEl == null ? null : Util.findText (contextPathEl);
            String contextPath = contextPathText == null ? null : evaluator.evaluate (contextPathText);
            Element classpathEl = Util.findElement (webModulesEl, "classpath", WebProjectNature.NS_WEB_2);
            FileObject [] sources = getSources ();
            ClassPath cp = classpathEl == null ? null : createClasspath (classpathEl, sources);
            Element webInfEl = Util.findElement (webModulesEl, "web-inf", WebProjectNature.NS_WEB_2);
            FileObject webInf = null;
            if (webInfEl != null) {
                webInf = getFile (webModulesEl, "web-inf"); //NOI18N
            }
            modules.add (new FFWebModule (docRootFO, j2eeSpec, contextPath, sources, cp, webInf));
        }
    }
    
    private FileObject getFile (Element parent, String fileElName) {
        Element el = Util.findElement (parent, fileElName, WebProjectNature.NS_WEB_2);
        String fname = Util.findText (el);
        if (fname == null) {
            // empty element => cannot find fileobject
            return null;
        }
        String locationEval = evaluator.evaluate(fname);
        if (locationEval != null) {
            File locationFile = helper.resolveFile(locationEval);
            return FileUtil.toFileObject(locationFile);
        }
        return null;
    }

    private FileObject [] getSources () {
        SourceGroup sg [] = ProjectUtils.getSources (project).getSourceGroups (JavaProjectConstants.SOURCES_TYPE_JAVA);
        Set srcRootSet = new HashSet ();
        for (int i = 0; i < sg.length; i++) {
            URL entry; 
            try {
                entry = sg[i].getRootFolder().getURL();
            } catch (FileStateInvalidException x) {
                throw new AssertionError(x);
            }
            // There is important calling this. Withouth calling this, will not work java cc in Jsp editor correctly.
            SourceForBinaryQuery.Result res = SourceForBinaryQuery.findSourceRoots (entry);
            FileObject srcForBin [] = res.getRoots ();
            for (int j = 0; j < srcForBin.length; j++) {
                srcRootSet.add (srcForBin [j]);
            }
        }
        
        FileObject[] roots = new FileObject [sg.length];
        for (int i = 0; i < sg.length; i++) {
            roots[i] = sg[i].getRootFolder();
        }
        return roots;
    }
    
    /**
     * Create a classpath from a &lt;classpath&gt; element.
     */
    private ClassPath createClasspath(Element classpathEl, FileObject[] sources) {
//        System.out.println("creating classpath for " + classpathEl);
        String cp = Util.findText(classpathEl);
        if (cp == null) {
            cp = "";
        }
        String cpEval = evaluator.evaluate(cp);
        if (cpEval == null) {
            return null;
        }
        String[] path = PropertyUtils.tokenizePath(cpEval);
        Set entries = new HashSet();
        for (int i = 0; i < path.length; i++) {
            entries.add(helper.resolveFile(path[i]));
        }
        if (entries.size() == 0) {
            // if the classpath element was empty then the classpath
            // should contain all source roots
            for (int i = 0; i < sources.length; i++) {
                entries.add(FileUtil.toFile(sources[i]));
            }
        }
        URL[] pathURL = new URL[entries.size()];
        int i = 0;
        for (Iterator it = entries.iterator(); it.hasNext();) {
            File entryFile = (File)it.next();
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
            pathURL[i++] = entry;
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
        private ClassPath webClassPath;
        private ClassPath javaSourcesClassPath;
        private ClassPath composedClassPath = null;
        private String j2eeSpec;
        private String contextPath;
        private FileObject webInf;
        
        FFWebModule (FileObject docRootFO, String j2eeSpec, String contextPath, FileObject sourcesFOs[], ClassPath classPath, FileObject webInf) {
            this.docRootFO = docRootFO;
            this.j2eeSpec = j2eeSpec;
            this.contextPath = (contextPath == null ? "" : contextPath);
            this.sourcesFOs = sourcesFOs;
            this.webClassPath = (classPath ==  null ? ClassPathSupport.createClassPath(Collections.EMPTY_LIST) : classPath);
            this.webInf = webInf;
            javaSourcesClassPath = (sourcesFOs == null ? ClassPathSupport.createClassPath(Collections.EMPTY_LIST): ClassPathSupport.createClassPath(sourcesFOs)); 
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
           int fileType = getType(file);
            
           if (fileType == 0) {
               if (!type.equals(ClassPath.SOURCE))
                   return null;
               else
                   return javaSourcesClassPath;
            } else 
                if (fileType == 1){
                    if (composedClassPath == null) {
                        HashSet all = new HashSet();
                        FileObject[] javaRoots = null;
                        for (int i = 0; i < sourcesFOs.length; i++){
                            javaRoots = ClassPath.getClassPath(sourcesFOs[i], type).getRoots();
                            for (int j = 0; j < javaRoots.length; j++)
                                if (!all.contains(javaRoots[j]))
                                    all.add(javaRoots[j]);
                        }
                                                
                        for (int i = 0; i < webClassPath.getRoots().length; i++)
                            if (!all.contains(webClassPath.getRoots()[i]))
                                all.add(webClassPath.getRoots()[i]);
                        
                        FileObject[] roots = new FileObject[all.size()];
                        int i = 0;
                        for (Iterator it = all.iterator(); it.hasNext();) 
                            roots[i++] = (FileObject)it.next();

                        composedClassPath = ClassPathSupport.createClassPath(roots);
                    }
                    return composedClassPath;
                }
            return webClassPath;
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
            FileObject winf = getWebInf ();
            if (winf == null) {
                return null;
            }
            return winf.getFileObject (FILE_DD);
        }
        
        public FileObject getWebInf () {
            //NetBeans 5.x and older projects (WEB-INF is placed under Web Pages)
            if (webInf == null) {
                webInf = getDocumentBase().getFileObject(FOLDER_WEB_INF);
            }
            return webInf;
        }
        
        public FileObject[] getJavaSources() {
            return sourcesFOs;
        }
        
        public MetadataModel<WebAppMetadata> getMetadataModel() {
            return null;
        }
        
        /**
         * Find what a given file represents.
         * @param file a file in the project
         * @return one of: <dl>
         *         <dt>0</dt> <dd>java source</dd>
         *         <dt>1</dt> <dd>web pages</dd>
         *         <dt>-1</dt> <dd>something else</dd>
         *         </dl>
         */
        private int getType(FileObject file) {
            //test java source roots
            for (int i=0; i < sourcesFOs.length; i++) {
                FileObject root = sourcesFOs[i];
                if (root.equals(file) || FileUtil.isParentOf(root, file)) {
                    return 0;
                }
            } 
            
            //test if the file is under the web root
            FileObject dir = getDocumentBase();
            if (dir != null && (dir.equals(file) || FileUtil.isParentOf(dir,file))) {
                return 1;
            }
            
            return -1;
        }
    }
}
