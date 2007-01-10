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
        FileObject obj = FileUtil.toFileObject(file);
        if(!list.contains(file)) {
            list.add(file);
            obj.addFileChangeListener(new FileChangeAdapter() {
                public void fileChanged(FileEvent event) {
                    syncDescriptors();
                }
            });
        }
    }
}
