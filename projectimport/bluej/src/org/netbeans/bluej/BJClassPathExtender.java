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

package org.netbeans.bluej;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.bluej.classpath.ClassPathProviderImpl;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author mkleint
 */
public class BJClassPathExtender implements ProjectClassPathExtender{

    private BluejProject project;
    
    /** Creates a new instance of BJClassPathExtender */
    public BJClassPathExtender(BluejProject proj) {
        project = proj;
    }

    public boolean addLibrary(org.netbeans.api.project.libraries.Library library) throws IOException {
        FileObject libs = project.getProjectDirectory().getFileObject("+libs"); //NOI18N
        if (libs == null) {
            libs = project.getProjectDirectory().createFolder("+libs"); //NOI18N
        }
        Iterator it = library.getContent("classpath").iterator(); //NOI18N
        while (it.hasNext()) {
            URL url = (URL) it.next();
            if (FileUtil.getArchiveFile(url) != null) {
                url = FileUtil.getArchiveFile(url);
            }
            FileObject fo = URLMapper.findFileObject(url);
            FileObject newLib = libs.getFileObject(fo.getNameExt());
            if (newLib == null) {
                FileUtil.copyFile(fo, libs, fo.getName());
            }
        }
        ClassPathProviderImpl prov = (ClassPathProviderImpl) project.getLookup().lookup(ClassPathProviderImpl.class);
        prov.getBluejCPImpl().fireChange();
        return true;
    }

    public boolean addArchiveFile(FileObject archiveFile) throws IOException {
        FileObject libs = project.getProjectDirectory().getFileObject("+libs"); //NOI18N
        if (libs == null) {
            libs = project.getProjectDirectory().createFolder("+libs"); //NOI18N
        }
        FileObject newLib = libs.getFileObject(archiveFile.getNameExt());
        if (newLib == null) {
            FileUtil.copyFile(archiveFile, libs, archiveFile.getName());
        }
        ClassPathProviderImpl prov = (ClassPathProviderImpl) project.getLookup().lookup(ClassPathProviderImpl.class);
        prov.getBluejCPImpl().fireChange();
        return true;
    }

    public boolean addAntArtifact(AntArtifact artifact, URI artifactElement) throws IOException {
        throw new IOException("It is not possible to create project dependencies in BlueJ projects. Please convert the project to J2SE Project first.");
    }
    
}
