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

package org.netbeans.modules.websvc.jaxwsmodel.project;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.websvc.api.jaxws.project.JAXWSVersionProvider;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 *
 * @author mkuchtiak
 */
public class TransformerUtils {
    /** jax-ws.xml: configuration file for web services (JAX-WS)
     */
    public static final String JAX_WS_XML_PATH = "nbproject/jax-ws.xml"; // NOI18N
    /** jaxws-build.xml: build script containing wsimport/wsgen tasks, that is included to build-impl.xml
     */    
    public static final String JAXWS_BUILD_XML_PATH = "nbproject/jaxws-build.xml"; // NOI18N

    static final String JAXWS_20_LIB = "jaxws20lib";
    static final String JAXWS_VERSION = "jaxwsversion";
    
    /** xsl transformation utility for generating jaxws-build.xml script
    */ 
    public static void transformClients(final FileObject projectDirectory,
                                        final String jaxws_stylesheet_resource) throws java.io.IOException {
        transformClients(projectDirectory,jaxws_stylesheet_resource,false);
    }
    /** xsl transformation utility for generating jaxws-build.xml script
    */ 
    public static void transformClients(final FileObject projectDirectory,
                                        final String jaxws_stylesheet_resource,
                                        boolean setJaxWsVersion) throws java.io.IOException {
        final FileObject jaxws_xml = projectDirectory.getFileObject(JAX_WS_XML_PATH);
        final FileObject jaxWsBuildScriptXml = FileUtil.createData(projectDirectory, JAXWS_BUILD_XML_PATH);
        byte[] projectXmlData;
        
        try {
            projectXmlData = ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<byte[]>() {
                public byte[] run() throws IOException {
                    FileLock lock1 = jaxWsBuildScriptXml.lock();
                    try {
                        InputStream is = jaxws_xml.getInputStream();
                        try {
                            return load(is);
                        } finally {
                            is.close();
                        }
                    } finally {
                        lock1.releaseLock();
                    }
                }
            });
        } catch (MutexException e) {
            throw (IOException)e.getException();
        }
        
        URL stylesheet = TransformerUtils.class.getResource(jaxws_stylesheet_resource);
        byte[] stylesheetData;
        InputStream is = stylesheet.openStream();
        try {
            stylesheetData = load(is);
        } finally {
            is.close();
        }
        
        final byte[] resultData;

        TransformerFactory tf = TransformerFactory.newInstance();
        try {
            StreamSource stylesheetSource = new StreamSource(
                    new ByteArrayInputStream(stylesheetData), stylesheet.toExternalForm());
            Transformer t = tf.newTransformer(stylesheetSource);
            if (setJaxWsVersion) {
                if(!isJAXWS21(projectDirectory)) {
                    t.setParameter(JAXWS_VERSION, JAXWS_20_LIB );
                }                
            }
            File jaxws_xml_F = FileUtil.toFile(jaxws_xml);
            assert jaxws_xml_F != null;
            StreamSource jaxWsSource = new StreamSource(
                    new ByteArrayInputStream(projectXmlData), jaxws_xml_F.toURI().toString());
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            t.transform(jaxWsSource, new StreamResult(result));
            resultData = result.toByteArray();
        } catch (TransformerException e) {
            throw (IOException)new IOException(e.toString()).initCause(e);
        }
        
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Boolean>() {
                public Boolean run() throws IOException {
                    FileLock lock1 = jaxWsBuildScriptXml.lock();
                    try {
                        OutputStream os = jaxWsBuildScriptXml.getOutputStream(lock1);
                        os.write(resultData);
                    } finally {
                        lock1.releaseLock();
                    }
                    return Boolean.TRUE;
                }
            });
        } catch (MutexException e) {
            throw (IOException)e.getException();
        }        
    }
            
    /**
     * Load data from a stream into a buffer.
     */
    private static byte[] load(InputStream is) throws IOException {
        int size = Math.max(1024, is.available()); // #46235
        ByteArrayOutputStream baos = new ByteArrayOutputStream(size);
        byte[] buf = new byte[size];
        int read;
        while ((read = is.read(buf)) != -1) {
            baos.write(buf, 0, read);
        }
        return baos.toByteArray();
    }
    
    private static boolean isJAXWS21(FileObject projectDirectory){
        Project project = FileOwnerQuery.getOwner(projectDirectory);
        if(project != null){
            JAXWSVersionProvider jvp = project.getLookup().lookup(JAXWSVersionProvider.class);
            if(jvp != null &&
                    jvp.getJAXWSVersion().equals(JAXWSVersionProvider.JAXWS20)){
                return false;
            }
        }
        // Defaultly return true
        return true;
    }
}
