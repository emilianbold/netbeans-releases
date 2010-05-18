/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
 * $Id$
 */

package org.netbeans.modules.j2ee.websphere6.config.sync;

import java.io.*;
import java.util.LinkedList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Dmitry Lipin
 */
public abstract class Synchronizer {
    
    protected static final String BINDING_SEPARATOR = "__Bnd__";
    
    protected void saveDocument(Document document, File file) throws TransformerException, FileNotFoundException, IOException {
        OutputStream outputStream = null;
        
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            outputStream = new FileOutputStream(file);
            DOMSource ds = new DOMSource(document);
            StreamResult sr = new StreamResult(outputStream);
            transformer.transform(ds, sr);
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }
    protected Document loadDocument(File file) {
        Document res = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            //  factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            res = builder.parse(file);
        } catch (SAXException ex) {
        } catch (IOException ex) {
        } catch (ParserConfigurationException ex) {
        }
        return res;
    }
    public abstract void syncDescriptors();
    
    private LinkedList <File> list = new LinkedList <File> ();
    
    public void addSyncFile(File file) {
        FileObject obj = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        if(!list.contains(file)) {
            list.add(file);
            obj.addFileChangeListener(new FileChangeAdapter() {
                @Override
                public void fileChanged(FileEvent event) {
                    syncDescriptors();
                }
            });
        }
    }
}
