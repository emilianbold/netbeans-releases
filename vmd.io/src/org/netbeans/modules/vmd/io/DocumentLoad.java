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
 */
package org.netbeans.modules.vmd.io;

import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.DataSerializer;
import org.netbeans.modules.vmd.api.io.providers.IOSupport;
import org.netbeans.modules.vmd.api.model.*;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.xml.XMLUtil;
import org.w3c.dom.*;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author David Kaspar
 */
// TODO - check for design and document version
public class DocumentLoad {

    public static boolean load (DataObjectContext context, DesignDocument loadingDocument) {
        final Node rootNode;
        try {
            rootNode = getRootNode (IOSupport.getDesignFile (context));
        } catch (IOException e) {
            throw Debug.error (e);
        }

        if (! DocumentSave.VERSION_VALUE_1.equals (getAttributeValue (rootNode, DocumentSave.VERSION_ATTR))) {
            Debug.warning ("Invalid version of VisualDesign");
            return false;
        }

        return loadVersion1 (context, loadingDocument, rootNode);
    }

    private static boolean loadVersion1 (DataObjectContext context, DesignDocument loadingDocument, Node rootNode) {
        final Node documentNode = findDocumentNode (rootNode);

        if (! DocumentSave.VERSION_VALUE_1.equals (getAttributeValue (documentNode, DocumentSave.VERSION_ATTR))) {
            Debug.warning ("Invalid version of VisualDesign");
            return false;
        }

        loadDocumentVersion1 (loadingDocument, documentNode);

        Collection<? extends DataSerializer> serializers = DocumentSave.customDataSerializers.allInstances ();
        for (Node node : getChildNode (rootNode)) {
            if (isDocumentNode (node))
                continue;
            for (DataSerializer serializer : serializers) {
                if (serializer.deserializeData (context, loadingDocument, node))
                    break;
            }
        }

        return true;
    }

    private static void loadDocumentVersion1 (final DesignDocument loadingDocument, Node documentNode) {
        final HashMap<Long, HierarchyElement> hierarchy = new HashMap<Long, HierarchyElement> ();
        if (documentNode != null)
            for (Node child : getChildNode (documentNode))
                if (isComponentNode (child))
                    collectStructure (hierarchy, child, Long.MIN_VALUE);

        HashSet<TypeID> typeids = new HashSet<TypeID> ();
        for (HierarchyElement element : hierarchy.values ())
            typeids.add (element.getTypeID ());

        loadingDocument.getDescriptorRegistry ().assertComponentDescriptors (typeids);

        loadingDocument.getTransactionManager ().writeAccess (new Runnable() {
            public void run () {
                loadDocumentCore (loadingDocument, hierarchy);
            }
        });
    }

    private static void loadDocumentCore (DesignDocument loadingDocument, HashMap<Long, HierarchyElement> hierarchy) {
        ArrayList<HierarchyElement> list = new ArrayList<HierarchyElement> (hierarchy.values ());
        Collections.sort (list, new Comparator<HierarchyElement>() {
            public int compare (HierarchyElement o1, HierarchyElement o2) {
                return (int) (o1.getUID () - o2.getUID ());
            }
        });

        for (HierarchyElement element : list) {
            long componentid = element.getUID ();
            loadingDocument.setPreferredComponentID (componentid);
            if (loadingDocument.getDescriptorRegistry ().getComponentDescriptor (element.getTypeID ()) == null) {
                Debug.warning ("Missing ComponentDescriptor in registry", element.getTypeID ()); // NOI18N
                continue;
            }
            DesignComponent component = loadingDocument.createRawComponent (element.getTypeID ());
            assert component.getComponentID () == componentid;
            assert component.getComponentDescriptor () != null;
        }

        for (HierarchyElement element : hierarchy.values ()) {
            long parentuid = element.getParentuid ();
            DesignComponent parent = loadingDocument.getComponentByUID (parentuid);
            DesignComponent component = loadingDocument.getComponentByUID (element.getUID ());
            if (component == null)
                continue;
            if (parentuid != Long.MIN_VALUE) {
                if (parent == null)
                    continue;
                parent.addComponent (component);
            }

            ComponentDescriptor descriptor = component.getComponentDescriptor ();
            if (descriptor == null)
                continue;

            Node[] propertyNodes = getChildNode (element.getNode ());
            for (Node propertyNode : propertyNodes) {
                if (! isPropertyNode (propertyNode))
                    continue;
                String propertyName = getAttributeValue (propertyNode, DocumentSave.NAME_ATTR);
                if (descriptor.getPropertyDescriptor (propertyName) == null) {
                    Debug.warning  ("Missing property descriptor", component, propertyName);
                }
                TypeID typeid = TypeID.createFrom (getAttributeValue (propertyNode, DocumentSave.TYPEID_ATTR));
                String serialized = getAttributeValue (propertyNode, DocumentSave.VALUE_ATTR);
                PropertyValue value;
                try {
                    value = PropertyValue.deserialize (serialized, loadingDocument, typeid);
                } catch (Exception e) {
                    Debug.warning ("Error while deserializing property value", component, propertyName); // NOI18N
                    throw Debug.error (e);
                }
                component.writeProperty (propertyName, value);
            }
        }

        DesignComponent componentByUID = loadingDocument.getComponentByUID (0);
        if (componentByUID != null)
            loadingDocument.setRootComponent (componentByUID);
    }

    private static void collectStructure (HashMap<Long, HierarchyElement> hierarchy, Node node, long parent) {
        long componentid = Long.parseLong (getAttributeValue (node, DocumentSave.COMPONENTID_ATTR));
        TypeID typeid = TypeID.createFrom (getAttributeValue (node, DocumentSave.TYPEID_ATTR));
        hierarchy.put (componentid, new HierarchyElement (parent, componentid, typeid, node));

        Node[] children = getChildNode (node);
        for (Node child : children)
            if (isComponentNode (child))
                collectStructure (hierarchy, child, componentid);
    }

    private static boolean isDocumentNode (Node child) {
        return DocumentSave.DOCUMENT_NODE.equals (child.getNodeName ());
    }

    private static boolean isComponentNode (Node child) {
        return DocumentSave.COMPONENT_NODE.equals (child.getNodeName ());
    }

    private static boolean isPropertyNode (Node child) {
        return DocumentSave.PROPERTY_NODE.equals (child.getNodeName ());
    }

    private static Node[] getChildNode (Node node) {
        NodeList childNodes = node.getChildNodes ();
        Node[] nodes = new Node[childNodes != null ? childNodes.getLength () : 0];
        for (int i = 0; i < nodes.length; i++)
            nodes[i] = childNodes.item (i);
        return nodes;
    }

    private static Node findDocumentNode (Node rootNode) {
        for (Node node : getChildNode (rootNode))
            if (isDocumentNode (node))
                return node;
        return null;
    }

    static void createEmpty (DesignDocument loadingDocument) {
        final TypeID type = new TypeID (TypeID.Kind.COMPONENT, "#Root");
        final DescriptorRegistry descriptorRegistry = loadingDocument.getDescriptorRegistry ();
        final boolean[] ret = new boolean[1];
        descriptorRegistry.readAccess (new Runnable() {
            public void run () {
                ret[0] = descriptorRegistry.getComponentDescriptor (type) != null;
            }
        });
        if (ret[0]) {
            DesignComponent root = loadingDocument.createComponent (type);
            loadingDocument.setRootComponent (root);
        }
    }

    private static Node getRootNode (final FileObject fileObject) throws IOException {
        synchronized (DocumentSave.sync) {
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
    }

    private static Document getXMLDocument (InputStream is) throws IOException {
        Document doc = null;
        try {
            doc = XMLUtil.parse (new InputSource (is), false, false, new ErrorHandler () {
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

    private static String getAttributeValue (Node node, String attr) {
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

    private static class HierarchyElement {

        private long parentuid;
        private long uid;
        private TypeID typeid;
        private Node node;

        public HierarchyElement (long parentuid, long uid, TypeID typeid, Node node) {
            this.parentuid = parentuid;
            this.uid = uid;
            this.typeid = typeid;
            this.node = node;
        }

        public long getParentuid () {
            return parentuid;
        }

        public long getUID () {
            return uid;
        }

        public TypeID getTypeID () {
            return typeid;
        }

        public Node getNode () {
            return node;
        }

    }

    public static String loadProjectType (DataObjectContext context) {
        final Node rootNode;
        try {
            rootNode = getRootNode (IOSupport.getDesignFile (context));
        } catch (IOException e) {
            throw Debug.error (e);
        }
        return getAttributeValue (rootNode, DocumentSave.PROP_PROJECT_TYPE);
    }

}
