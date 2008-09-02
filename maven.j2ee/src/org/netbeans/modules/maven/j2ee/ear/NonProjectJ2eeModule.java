/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.j2ee.ear;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.apache.maven.artifact.Artifact;
import org.netbeans.modules.j2ee.dd.api.common.RootInterface;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * a j2eemodule implementation that is not tied to a particular project but
 *  works only on top of ear's modules' artifacts.. will this work?
 * @author mkleint
 */
public class NonProjectJ2eeModule implements J2eeModuleImplementation {

    private static final String WAR = "war"; //NOI18N
    private static final String EAR = "ear"; //NOI18N
    private static final String EJB = "ejb"; //NOI18N
    private String moduleVersion;
    private Artifact artifact;
    private String url;
    private EarModuleProviderImpl provider;

    private MetadataModel<EjbJarMetadata> ejbJarMetadataModel;
    private MetadataModel<WebAppMetadata> webAppAnnMetadataModel;
    
    /** Creates a new instance of NonProjectJ2eeModule */
    public NonProjectJ2eeModule(Artifact art, String modVer, EarModuleProviderImpl prov) {
        artifact = art;
        moduleVersion = modVer;
        provider = prov;
    }
    
    public String getModuleVersion() {
//        System.out.println("NPJM: get Version=" + moduleVersion);
        return moduleVersion;
    }
    
    public Object getModuleType() {
        String type = artifact.getType();
//        System.out.println("NPJM: get type=" + type);
        if (WAR.equals(type)) {
            return J2eeModule.WAR;
        }
        if (EJB.equals(type)) {
            return J2eeModule.EJB;
        }
        if (EAR.equals(type)) {
            return J2eeModule.EAR;
        }
        //TODO what to do here?
        return J2eeModule.CLIENT;
    }
    
    public String getUrl() {
        //TODO url should be probably based on application.xml??
        String ret = url == null ? artifact.getFile().getName() : url;
//        System.out.println("NPJM: get url=" + ret);
        return ret;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public FileObject getArchive() throws IOException {
//        System.out.println("NPJM: get archive=" + artifact.getFile());
        return FileUtil.toFileObject(FileUtil.normalizeFile(artifact.getFile()));
    }
    
    public Iterator getArchiveContents() throws IOException {
//        System.out.println("NPJM: get archive content..");
        FileObject fo = getArchive();
        return new ContentIterator(FileUtil.getArchiveRoot(fo));
    }
    
    public FileObject getContentDirectory() throws IOException {
        return null;
    }
    
    public RootInterface getDeploymentDescriptor(String location) {
//        System.out.println("NPJM: get DD =" + location);
        try {
            JarFile fil = new JarFile(artifact.getFile());
            ZipEntry entry = fil.getEntry(location);
            if (entry != null) {
                InputStream str = fil.getInputStream(entry);
                return readBaseBean(str);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    
    private RootInterface readBaseBean(InputStream str) {
//        System.out.println("NPJM:   read base bean");
        String type = artifact.getType();
        if (WAR.equals(type)) {
            try {
                FileObject root = FileUtil.getArchiveRoot(getArchive());
//                System.out.println("NPJM:root=" + root);
                return org.netbeans.modules.j2ee.dd.api.web.DDProvider.getDefault().getDDRoot(root.getFileObject(J2eeModule.WEB_XML));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else if (EJB.equals(type)) {
                try {
                    return org.netbeans.modules.j2ee.dd.api.ejb.DDProvider.getDefault().getDDRoot(new InputSource(str));
                } catch (SAXException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
        } else if (EAR.equals(type)) {
            try {
                return org.netbeans.modules.j2ee.dd.api.application.DDProvider.getDefault().getDDRoot(new InputSource(str));
            } catch (SAXException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            
        }
        return null;
    }

    public File getResourceDirectory() {
        return  null;
    }

    public File getDeploymentConfigurationFile(String name) {
//       if (name == null) {
//            return null;
//        }
//        String path = provider.getConfigSupport().getContentRelativePath(name);
//        if (path == null) {
//            path = name;
//        }
        // here we don't really have access to the source deployment configs, as we operate on top of 
        // maven local repository binaries only..
        return null;
    }

    public void addPropertyChangeListener(PropertyChangeListener arg0) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removePropertyChangeListener(PropertyChangeListener arg0) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public synchronized MetadataModel<EjbJarMetadata> getMetadataModel() {
//TODO        if (ejbJarMetadataModel == null) {
//            FileObject ddFO = getDeploymentDescriptor();
//            File ddFile = ddFO != null ? FileUtil.toFile(ddFO) : null;
//            ClassPathProviderImpl cpProvider = project.getLookup().lookup(ClassPathProviderImpl.class);
//            MetadataUnit metadataUnit = MetadataUnit.create(
//                cpProvider.getProjectSourcesClassPath(ClassPath.BOOT),
//                cpProvider.getProjectSourcesClassPath(ClassPath.COMPILE),
//                cpProvider.getProjectSourcesClassPath(ClassPath.SOURCE),
//                // XXX: add listening on deplymentDescriptor
//                ddFile);
//            ejbJarMetadataModel = EjbJarMetadataModelFactory.createMetadataModel(metadataUnit);
//        }
        return ejbJarMetadataModel;
    }

    public <T> MetadataModel<T> getMetadataModel(Class<T> type) {
        if (type == EjbJarMetadata.class) {
            @SuppressWarnings("unchecked") // NOI18N
            MetadataModel<T> model = (MetadataModel<T>)getMetadataModel();
            return model;
        }
        if (type == WebAppMetadata.class) {
            @SuppressWarnings("unchecked") // NOI18N
            MetadataModel<T> model = (MetadataModel<T>)getAnnotationMetadataModel();
            return model;
//        } else if (type == WebservicesMetadata.class) {
//            @SuppressWarnings("unchecked") // NOI18N
//            MetadataModel<T> model = (MetadataModel<T>)getWebservicesMetadataModel();
//            return model;
        }
        return null;
    }
        
    /**
     * The server plugin needs all models to be either merged on annotation-based. 
     * Currently only the web model does a bit of merging, other models don't. So
     * for web we actually need two models (one for the server plugins and another
     * for everyone else). Temporary solution until merging is implemented
     * in all models.
     */
    public synchronized MetadataModel<WebAppMetadata> getAnnotationMetadataModel() {
        if (webAppAnnMetadataModel == null) {
//TODO            FileObject ddFO = getDeploymentDescriptor();
//            File ddFile = ddFO != null ? FileUtil.toFile(ddFO) : null;
//            ClassPathProviderImpl cpProvider = project.getLookup().lookup(ClassPathProviderImpl.class);
//            
//            MetadataUnit metadataUnit = MetadataUnit.create(
//                cpProvider.getProjectSourcesClassPath(ClassPath.BOOT),
//                cpProvider.getProjectSourcesClassPath(ClassPath.COMPILE),
//                cpProvider.getProjectSourcesClassPath(ClassPath.SOURCE),
//                // XXX: add listening on deplymentDescriptor
//                ddFile);
//            webAppAnnMetadataModel = WebAppMetadataModelFactory.createMetadataModel(metadataUnit, false);
        }
        return webAppAnnMetadataModel;
    }
    
    
    // inspired by netbeans' webmodule codebase, not really sure what is the point
    // of the iterator..
    private static final class ContentIterator implements Iterator {
        private ArrayList ch;
        private FileObject root;
        
        private ContentIterator(FileObject f) {
            this.ch = new ArrayList();
            ch.add(f);
            this.root = f;
        }
        
        public boolean hasNext() {
            return ! ch.isEmpty();
        }
        
        public Object next() {
            FileObject f = (FileObject) ch.get(0);
            ch.remove(0);
            if (f.isFolder()) {
                f.refresh();
                FileObject[] chArr = f.getChildren();
                for (int i = 0; i < chArr.length; i++) {
                    ch.add(chArr [i]);
                }
            }
            return new FSRootRE(root, f);
        }
        
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
        
    private static final class FSRootRE implements J2eeModule.RootedEntry {
        private FileObject f;
        private FileObject root;
        
        FSRootRE(FileObject rt, FileObject fo) {
            f = fo;
            root = rt;
        }
        
        public FileObject getFileObject() {
            return f;
        }
        
        public String getRelativePath() {
            return FileUtil.getRelativePath(root, f);
        }
    }
    
}
