/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.websvc.axis2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.netbeans.api.project.ProjectManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 *
 * @author mkuchtiak
 */
public class TransformerUtils {
    public static final String AXIS2_XML_PATH = "nbproject/axis2.xml"; // NOI18N
    static final String AXIS2_BUILD_XML_PATH = "nbproject/axis2-build.xml"; // NOI18N
    
    private static final String AXIS2_STYLESHEET_RESOURCE="/org/netbeans/modules/websvc/axis2/resources/axis2.xsl"; //NOI18N
    
    private static final String GENFILES_PROPERTIES_PATH = "nbproject/genfiles.properties"; // NOI18N
    private static final String KEY_SUFFIX_AXIS2_BUILD_CRC = ".stylesheet.CRC32"; // NOI18N

    /** xsl transformation utility for generating axis2-build.xml script
    */ 
    public static void transform(FileObject projectDirectory) throws java.io.IOException {
        final FileObject axis2_xml = projectDirectory.getFileObject(AXIS2_XML_PATH);
        final FileObject axis2BuildScriptXml = FileUtil.createData(projectDirectory, AXIS2_BUILD_XML_PATH);
        byte[] projectXmlData;
        
        try {
            projectXmlData = ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<byte[]>() {
                public byte[] run() throws IOException {
                    InputStream is = axis2_xml.getInputStream();
                    try {
                        return load(is);
                    } finally {
                        is.close();
                    }
                }
            });
        } catch (MutexException e) {
            throw (IOException)e.getException();
        }
        
        URL stylesheet = TransformerUtils.class.getResource(AXIS2_STYLESHEET_RESOURCE);
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
            File axis2_xml_F = FileUtil.toFile(axis2_xml);
            assert axis2_xml_F != null;
            StreamSource axis2Source = new StreamSource(
                    new ByteArrayInputStream(projectXmlData), axis2_xml_F.toURI().toString());
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            t.transform(axis2Source, new StreamResult(result));
            resultData = result.toByteArray();
        } catch (TransformerException e) {
            throw (IOException)new IOException(e.toString()).initCause(e);
        }
        
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Boolean>() {
                public Boolean run() throws IOException {
                    FileLock lock1 = axis2BuildScriptXml.lock();
                    OutputStream os = null;
                    try {
                        os = axis2BuildScriptXml.getOutputStream(lock1);
                        os.write(resultData);
                    } finally {
                        lock1.releaseLock();
                        if (os!=null) os.close();
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
    static byte[] load(InputStream is) throws IOException {
        int size = Math.max(1024, is.available()); // #46235
        ByteArrayOutputStream baos = new ByteArrayOutputStream(size);
        byte[] buf = new byte[size];
        int read;
        while ((read = is.read(buf)) != -1) {
            baos.write(buf, 0, read);
        }
        return baos.toByteArray();
    }

    /** Find (maybe cached) CRC for a URL, using a preexisting input stream (not closed by this method). */
    static String getCrc32(InputStream is) throws IOException {
        return  computeCrc32(is);
    }
    
    /**
     * Compute the CRC-32 of the contents of a stream.
     * \r\n and \r are both normalized to \n for purposes of the calculation.
     */
    private static String computeCrc32(InputStream is) throws IOException {
        Checksum crc = new CRC32();
        int last = -1;
        int curr;
        while ((curr = is.read()) != -1) {
            if (curr != '\n' && last == '\r') {
                crc.update('\n');
            }
            if (curr != '\r') {
                crc.update(curr);
            }
            last = curr;
        }
        if (last == '\r') {
            crc.update('\n');
        }
        int val = (int)crc.getValue();
        String hex = Integer.toHexString(val);
        while (hex.length() < 8) {
            hex = "0" + hex; // NOI18N
        }
        return hex;
    }

}
