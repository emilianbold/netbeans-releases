/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.vmd.componentssupport.ui.helpers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.netbeans.api.project.ProjectManager;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author avk
 */
public class XmlHelper extends BaseHelper {

    protected static Document parseXmlDocument(final FileObject xmlFO) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Document>() {

            public Document run() {
                if (xmlFO == null || !xmlFO.isData()) {
                    return null;
                }
                try{
                    return doParseXmlDocument(xmlFO);
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                } catch (SAXException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
                return null;
            }
        });
    }

    protected static void saveXmlDocument(final Document doc, final FileObject xmlFO) {
        ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {

            public Void run() {
                try {
                    doSaveXmlDocument(doc, xmlFO);
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
                return null;
            }
        });
    }
    
    private static void doSaveXmlDocument(Document doc, FileObject xmlFO)
            throws IOException {
        FileLock lock = xmlFO.lock();
        try {
            OutputStream out = xmlFO.getOutputStream(lock);
            try {
                XMLUtil.write(doc, out, UTF_8);
            } finally {
                out.close();
            }
        } finally {
            lock.releaseLock();
        }
    }

    private static Document doParseXmlDocument(final FileObject xmlFO) 
            throws FileNotFoundException, IOException, SAXException 
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream in = xmlFO.getInputStream();
        try {
            FileUtil.copy(in, baos);
        } finally {
            in.close();
        }
        return XMLUtil.parse(new InputSource(
                new ByteArrayInputStream(baos.toByteArray())),
                false, false, null, null);
    }
    
}
