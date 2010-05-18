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
 */package org.netbeans.modules.vmd.midp.converter.wizard;

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
import java.util.ArrayList;
import java.util.List;

/**
 * @author David Kaspar
 */
public class XMLUtil {

    static Node getRootNode (final FileObject fileObject) throws IOException {
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

    static Node[] getChildren (Node node) {
        NodeList childNodes = node.getChildNodes ();
        Node[] nodes = new Node[childNodes != null ? childNodes.getLength () : 0];
        for (int i = 0; i < nodes.length; i++)
            nodes[i] = childNodes.item (i);
        return nodes;
    }

    static List<Node> getChildren (Node node, String nodeName) {
        ArrayList<Node> children = new ArrayList<Node> ();
        NodeList nodes = node.getChildNodes ();
        for (int i = 0; i < nodes.getLength (); i++) {
            Node child = nodes.item (i);
            if (nodeName.equals (child.getNodeName ()))
                children.add (child);
        }
        return children;
    }

    static Node getChild (Node node, String nodeName) {
        NodeList nodes = node.getChildNodes ();
        for (int i = 0; i < nodes.getLength (); i ++) {
            Node child = nodes.item (i);
            if (nodeName.equals (child.getNodeName ()))
                return child;
        }
        return null;
    }

    static String getAttributeValue (Node node, String attr) {
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
