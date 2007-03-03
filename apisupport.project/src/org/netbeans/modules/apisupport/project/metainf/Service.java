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

package org.netbeans.modules.apisupport.project.metainf;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.SuiteProvider;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.layers.LayerUtils;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Representation of a record in META-INF/services.
 *
 * @author pzajac
 */
final class Service {
    
    private final String codebase;
    private final String fileName;
    private final List classes;
    
    static final String META_INF_SERVICES = "META-INF/services";
    
    /** Creates a new instance of Service */
    public Service(String codebase,String fileName,List classes) { 
        this.codebase = codebase;
        this.fileName = fileName;
        this.classes = classes;
    }
    
    static Service createService(String codebase,String fileName, InputStream jarIs) throws IOException {
        List list = new ArrayList();
        BufferedReader reader = new BufferedReader(new InputStreamReader(jarIs));
        String line = null;
        while ((line = reader.readLine())!= null) {
            line = line.trim();
            if (!line.startsWith("#") && line.length() != 0) {
                list.add(line);
            }
        } 
        return new Service(codebase,fileName,list);
    }
    
    static List /*Service*/ readServices(File jarFile) {
        List /*Service */ services = new ArrayList();
        try {
            JarFile jar = new JarFile(jarFile);
            Attributes attrs = jar.getManifest().getMainAttributes();
            String codebase  = (String) attrs.getValue("OpenIDE-Module"); // NOI18N
            Enumeration /*JarEntry*/entries =  jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = (JarEntry) entries.nextElement();
                if (entry.getName().startsWith(META_INF_SERVICES)) {
                    String name = entry.getName().substring(META_INF_SERVICES.length() + 1).trim();
                    if (!name.equals("")) { // NOI18N
                        InputStream is = jar.getInputStream(entry);
                        try {
                            services.add(createService(codebase,name.intern(),is));
                        } finally {
                            is.close();
                        }
                    }
                }
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.ERROR,ioe);
        }
        return services;
    }
    
    static List /*Service*/ getOnlyProjectServices(NbModuleProject project) {
        List /*Service */ services = new ArrayList();
        try {
            FileObject mIServicesFolder = null;
            mIServicesFolder = SUtil.getServicesFolder(project,false);
            // get META-INF.services folder
            if (mIServicesFolder != null) {
                String codebase = project.getCodeNameBase();
                FileObject servicesFOs [] = mIServicesFolder.getChildren();
                for (int foIt = 0 ; foIt < servicesFOs.length ; foIt++ ) {
                    if (servicesFOs[foIt].isData() && VisibilityQuery.getDefault().isVisible(servicesFOs[foIt])) {
                        InputStream is = servicesFOs[foIt].getInputStream();
                        try {
                            services.add(createService(codebase,servicesFOs[foIt].getNameExt(),is));
                        } finally {
                            is.close();
                        }
                    }
                }
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.ERROR,ioe);
        }
        return services;
    }
    
    public String getCodebase() {
        return codebase;
    }

    public String getFileName() {
        return fileName;
    }

    public List getClasses() {
        return classes;
    }

    public boolean containsClass(String name) {
        return classes.indexOf(name)  != -1;
    }
    
    public void removeClass(String name) {
        classes.remove(name);
    }
    
    private  static Set<File> getJars(NbModuleProject p) throws IOException {
        if (p == null) {
            // testing
            return SUtil.getPlatformJars();
        } else {
            NbModuleProvider.NbModuleType type = ((NbModuleProvider) p.getLookup().lookup(NbModuleProvider.class)).getModuleType();
            if (type == NbModuleProvider.STANDALONE) {
                return LayerUtils.getPlatformJarsForStandaloneProject(p);
            } else if (type == NbModuleProvider.SUITE_COMPONENT) {
                SuiteProvider suiteProv = (SuiteProvider) p.getLookup().lookup(SuiteProvider.class);
                assert suiteProv != null : p;
                File suiteDir = suiteProv.getSuiteDirectory();
                if (suiteDir == null || !suiteDir.isDirectory()) {
                    throw new IOException("Could not locate suite for " + p); // NOI18N
                }
                Project suite = ProjectManager.getDefault().findProject(FileUtil.toFileObject(suiteDir));
                if (!(suite instanceof SuiteProject)) {
                    throw new IOException("Could not load suite for " + p + " from " + suiteDir); // NOI18N
                }
                return LayerUtils.getPlatformJarsForSuiteComponentProject(p,(SuiteProject)suite);
            } else if (type == NbModuleProvider.NETBEANS_ORG) {
                /// Is it really correct?
                // [TODO]
                return LayerUtils.getPlatformJarsForStandaloneProject(p);
            } else {
                throw new AssertionError(type);
            }
        }
    }
    
    static List <Service> getPlatfromServices(NbModuleProject p) throws IOException {
        NbModuleProvider.NbModuleType type = Util.getModuleType(p);
        List<Service> services = new ArrayList<Service>();
        if (type == NbModuleProvider.NETBEANS_ORG) {
            // special case fro nborg modules
            Set<NbModuleProject> projects = LayerUtils.getProjectsForNetBeansOrgProject(p);
            Iterator it = projects.iterator();
            while (it.hasNext()) {
                services.addAll(getOnlyProjectServices((NbModuleProject)it.next()));
            }
        } else {
            Set<File> jars = getJars(p);
            for (File jarFile : jars) {
                services.addAll(readServices(jarFile));
            }            
        }
        return services;
    }

    void removeClass(String className,NbModuleProject project) {
        String removedClass = "-" + className;
        removedClass = removedClass.intern();
        if (containsClass(className)) {
            removeClass(className);
        } else if (containsClass(removedClass)) {
            removeClass(removedClass);
        } else {
            classes.add(removedClass);
        }
        write(project);
    }
    
    void write(NbModuleProject project) {
        try {
            FileObject mIServicesFolder = null;
            mIServicesFolder = SUtil.getServicesFolder(project,true);
            FileObject serviceFo = mIServicesFolder.getFileObject(getFileName());
            if (classes.size() > 0) {
                if (serviceFo == null) {
                    serviceFo = mIServicesFolder.createData(getFileName());
                }
                FileLock lock = serviceFo.lock();
                try {
                    PrintStream ps = new PrintStream(serviceFo.getOutputStream(lock));
                    for (Iterator it = classes.iterator() ; it.hasNext() ; ) {
                        Object object = it.next();
                        ps.println(object);
                    }
                    ps.close();
                } finally {
                    lock.releaseLock();
                }
            } else {
                // no service, remove file
                serviceFo.delete();
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
    }
    
}
