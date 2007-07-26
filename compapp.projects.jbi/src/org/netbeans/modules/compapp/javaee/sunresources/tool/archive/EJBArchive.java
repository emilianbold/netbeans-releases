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

package org.netbeans.modules.compapp.javaee.sunresources.tool.archive;

import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.compapp.javaee.sunresources.ResourceAggregator;
import org.netbeans.modules.compapp.javaee.sunresources.SunResourcesUtil;
import org.netbeans.modules.compapp.javaee.sunresources.tool.annotation.JavaEEAnnotationProcessor;
import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.CMap;

import org.netbeans.modules.compapp.javaee.sunresources.tool.graph.JAXBHandler;
import org.netbeans.modules.compapp.projects.jbi.jeese.actions.ServerResourcesAction;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * @author echou
 *
 */
public class EJBArchive extends Archive {
       
    private Project p;
    private JavaEEAnnotationProcessor annoProcessor;
    private CMap cmap;
    private String name;
    private ResourceAggregator resAggregator;;
    
    private WebservicesDDJaxbHandler webservicesDD;
    private SunEjbDDJaxbHandler sunEjbDD;
    private EjbDDJaxbHandler ejbDD;
    private FileObject resourceDirFO;
    
    /*
     * this constructor is called if it is not stand-alone EJB
     */
    public EJBArchive(Project p, CMap cmap, JavaEEAnnotationProcessor annoProcessor) throws Exception {
        this.p = p;
        this.name = ProjectUtils.getInformation(p).getName();
        this.cmap = cmap;
        this.annoProcessor = annoProcessor;
        this.resAggregator = new ResourceAggregator(p);
        this.distJarPropName = "dist.jar"; // NOI18N
    }

    /*
     * this constructor is called if it is a stand-alone EJB
     */
    public EJBArchive(Project p) throws Exception {
        this.p = p;
        this.name = ProjectUtils.getInformation(p).getName();
        this.cmap = new CMap(name);
        this.resAggregator = new ResourceAggregator(p);
        this.annoProcessor = new JavaEEAnnotationProcessor(cmap, resAggregator);
        this.distJarPropName = "dist.jar"; // NOI18N
    }
    
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void open() throws Exception {
        // aggregate *.sun-resource files into memory here
        resourceDirFO = SunResourcesUtil.getResourceDir(this.p);
        FileObject[] children = resourceDirFO.getChildren();
        for (int i = 0; i < children.length; i++) {
            FileObject fo = children[i];
            if (!fo.isFolder() && fo.getExt().equalsIgnoreCase("sun-resource")) { // NOI18N
                resAggregator.addResource(fo);
            }
        }
        
        // scan annotations
        FileObject ejbJarFO = SunResourcesUtil.getProjectDistJar(this.p, this.distJarPropName);
        if (ejbJarFO == null) {
            throw new ServerResourcesAction.ProjectNotBuiltException(
                    NbBundle.getMessage(EJBArchive.class, "EXC_project_not_build", this.name));
        }
        
        JarFile jf = null;
        try {
            jf = new JarFile(org.openide.filesystems.FileUtil.toFile(ejbJarFO));
            for (Enumeration<JarEntry> e = jf.entries(); e.hasMoreElements(); ) {
                JarEntry jarEntry = e.nextElement();
                if (jarEntry.getName().endsWith(".class")) { // NOI18N
                    ClassFile classfile = new ClassFile(jf.getInputStream(jarEntry));
                    annoProcessor.process(classfile);
                }
            }
        } finally {
            FileUtil.safecloseJar(jf);
        }
        
        annoProcessor.postProcess();
        
        //readDescriptors();
    }
    
    /*
    private void readDescriptors() {
        webservicesDD = readWebservicesDD();
        sunEjbDD = readSunEjbDD();
        ejbDD = readEjbDD();
    }
    
    private EjbDDJaxbHandler readEjbDD() {
        try {
            // find DD in this archive
            URL url = getJarURL(ArchiveConstants.EJB_DESCRIPTOR_PATH);
            
            JAXBContext jc = JAXBContext.newInstance("com.sun.wasilla.jaxb.ejb21",
                    this.getClass().getClassLoader());
            Unmarshaller u = jc.createUnmarshaller();
            JAXBElement<?> root = (JAXBElement<?>) u.unmarshal(url);
            
            EjbDDJaxbHandler ejbDD = 
                new EjbDDJaxbHandler(root.getValue(), sunEjbDD, webservicesDD);
            return ejbDD;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }

    private SunEjbDDJaxbHandler readSunEjbDD() {
        try {
            // find DD in this archive
            URL url = getJarURL(ArchiveConstants.SUN_EJB_DESCRIPTOR_PATH);
            
            JAXBContext jc = JAXBContext.newInstance("com.sun.wasilla.jaxb.sunejb30",
                    this.getClass().getClassLoader());
            Unmarshaller u = jc.createUnmarshaller();
            Object root = u.unmarshal(url);
            
            SunEjbDDJaxbHandler sunEjbDD = new SunEjbDDJaxbHandler(root);
            return sunEjbDD;
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return null;
    }
    
    private WebservicesDDJaxbHandler readWebservicesDD() {
        try {
            // find DD in this archive
            URL url = getJarURL(ArchiveConstants.WEB_SERVICES_DESCRIPTOR_PATH);
            
            JAXBContext jc = JAXBContext.newInstance("com.sun.wasilla.jaxb.webservices11",
                    this.getClass().getClassLoader());
            Unmarshaller u = jc.createUnmarshaller();
            JAXBElement<?> root = (JAXBElement<?>) u.unmarshal(url);
            
            WebservicesDDJaxbHandler webservicesDD = new WebservicesDDJaxbHandler(this, root.getValue());
            return webservicesDD;
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return null;
    }

    public ArrayList<CMapNode> getCMapNodes() {
        if (this.ejbDD == null) {
            return null;
        } else {
            return this.ejbDD.getNodes();
        }
    }
     */


    /* (non-Javadoc)
     * @see com.sun.wasilla.tool.archive.Archive#getJAXBHandler()
     */
    @Override
    public JAXBHandler getJAXBHandler() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.sun.wasilla.tool.archive.Archive#close()
     */
    @Override
    public void close() throws Exception {
        // TODO Auto-generated method stub
        
    }

    public ResourceAggregator getResourceAggregator() {
        return this.resAggregator;
    }

    public CMap getCMap() {
        return this.cmap;
    }

    public FileObject getResourceDir() {
        return this.resourceDirFO;
    }
}
