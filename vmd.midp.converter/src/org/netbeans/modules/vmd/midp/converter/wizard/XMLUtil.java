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
 *
 */
package org.netbeans.modules.vmd.midp.converter.wizard;

import org.netbeans.modules.vmd.api.model.Debug;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.w3c.dom.*;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author David Kaspar
 */
public class XMLUtil {

    public static Node getRootNode (final FileObject fileObject) throws IOException {
        final Node[] node = new Node[1];
        fileObject.getFileSystem ().runAtomicAction (new FileSystem.AtomicAction() {
            public void run () throws IOException {
                Document document = null;
                if (fileObject != null) {
                    FileLock lock = null;
                    try {
                        lock = fileObject.lock ();
                        document = getXMLDocument (fileObject.getInputStream ());
                    } finally {
                        if (lock != null)
                            lock.releaseLock ();
                    }
                }
                node[0] = document != null ? document.getFirstChild () : null;
            }
        });
        return node[0];
    }

    private static Document getXMLDocument (InputStream is) throws IOException {
        Document doc = null;
        try {
            doc = org.openide.xml.XMLUtil.parse (new InputSource (is), false, false, new ErrorHandler() {
                public void error (SAXParseException e) throws SAXException {
                    throw new SAXException (e);
                }

                public void fatalError (SAXParseException e) throws SAXException {
                    throw new SAXException (e);
                }

                public void warning (SAXParseException e) {
                    Debug.warning (e);
                }
            }, null);
        } catch (SAXException e) {
            throw Debug.error (e);
        } finally {
            try {
                is.close ();
            } catch (IOException e) {
                throw Debug.error (e);
            }
        }
        return doc;
    }

    public static Node[] getChildren (Node node) {
        NodeList childNodes = node.getChildNodes ();
        Node[] nodes = new Node[childNodes != null ? childNodes.getLength () : 0];
        for (int i = 0; i < nodes.length; i++)
            nodes[i] = childNodes.item (i);
        return nodes;
    }

    public static String getAttributeValue (Node node, String attr) {
        try {
            if (node != null) {
                NamedNodeMap map = node.getAttributes ();
                if (map != null) {
                    node = map.getNamedItem (attr);
                    if (node != null)
                        return node.getNodeValue ();
                }
            }
        } catch (DOMException e) {
            Debug.warning (e);
        }
        return null;
    }

}
