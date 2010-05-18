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


package org.netbeans.modules.compapp.test.ui;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.api.diff.Difference;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;


/**
 * Stream source for diffing CVS managed files.
 *
 * @author Bing Lu
 */
public class DiffStreamSource extends StreamSource {
    private static final java.util.logging.Logger mLogger =
            java.util.logging.Logger.getLogger("org.netbeans.modules.compapp.projects.jbi.ui.DiffStreamSource"); // NOI18N
    
    private File mFile;
    private String mName;
    private String mTitle;
    private String mMimeType;
    
    /**
     * Creates a new StreamSource implementation for Diff engine.
     *
     *
     *
     * @param mFile
     * @param revision file revision, may be null if the revision does not exist (ie for new files)
     * @param mTitle mTitle to use in diff panel
     */
    public DiffStreamSource(String name, String title, File file) {
        mFile = file;
        mName = name;
        mTitle = title;
    }
    
    public String getName() {
        return mName;
    }
    
    public String getTitle() {
        return mTitle;
    }
    
    public String getMIMEType() {
        try {
            init();
        } catch (IOException e) {
            return null;
        }
        return mMimeType;
    }
    
    public Reader createReader() throws IOException {
        init();
        if (mFile == null || !mFile.exists()) return null;
        
        // Ideally, both the expected and actual output files are properly 
        // indented, so there is no need to do transformation here. 
        //        return new java.io.FileReader(mFile);
        // However, there are a few scenarios that we still need to do 
        // indentation transformation on the fly when presenting the diff view:
        //   (1) existing projects' expected output file might be in one line.
        //   (2) user might have manually modified the indentation in the 
        //       expected or actual output file
        //   (3) the user changes the indentation setting 
                
        try {
            javax.xml.transform.stream.StreamSource xmlStreamSource =
                    new javax.xml.transform.stream.StreamSource(mFile);

            // Temporary hack to get the encoding
            String encoding = getXMLEncoding(mFile);

            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            Transformer trans = TransformerFactory.newInstance().newTransformer();

            trans.setOutputProperty(OutputKeys.ENCODING, encoding); // NOI18N
            trans.setOutputProperty(OutputKeys.INDENT, "yes"); // NOI18N
            trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); // NOI18N    // FIXME: hard-coded indentation here
            trans.setOutputProperty(OutputKeys.METHOD, "xml"); // NOI18N
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no"); // NOI18N
            trans.transform(xmlStreamSource, new StreamResult(baos));

            return new InputStreamReader(new ByteArrayInputStream(baos.toByteArray()), encoding); // NOI18N
        } catch (Exception e) {
            mLogger.log(Level.SEVERE,
                    NbBundle.getMessage(TestcaseNode.class, "MSG_Fail_to_load_file", mFile.getAbsolutePath()), // NOI18N
                    e);
        }
        return null;
    }
    
    private String getXMLEncoding(File file) throws FileNotFoundException, IOException {
        String encoding = "UTF-8";  // This is the default encoding // NOI18N
        
        BufferedReader reader = new BufferedReader(new FileReader(mFile));
        
        String line = reader.readLine();
        Pattern pattern = Pattern.compile("encoding=[\"'](.*?)[\"']"); // NOI18N
        Matcher matcher = pattern.matcher(line);
        
        boolean matchFound = matcher.find();
        if (matchFound && matcher.groupCount() == 1) {
            encoding = matcher.group(1);
        }
        reader.close();
        
        return encoding;
    }
    
    public Writer createWriter(Difference[] conflicts) throws IOException {
        throw new IOException("MSG_Operation_not_supported"); // NOI18N
    }
    
    /**
     * Loads data over network.
     *
     * @param group combines multiple loads or <code>null</code>
     * Note that this group must not be executed later on.
     */
    synchronized void init() throws IOException {
        if (mFile == null || !mFile.exists()) return;
        FileObject fo = FileUtil.toFileObject(mFile);
        if (fo != null) {
            mMimeType = fo.getMIMEType();
        } else {
            mMimeType = "xml"; // NOI18N
        }
    }
}
