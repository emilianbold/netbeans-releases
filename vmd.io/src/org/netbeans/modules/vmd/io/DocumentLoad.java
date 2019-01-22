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
 */
package org.netbeans.modules.vmd.io;

import org.netbeans.modules.vmd.api.io.serialization.DocumentErrorHandlerSupport;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.DataSerializer;
import org.netbeans.modules.vmd.api.io.providers.IOSupport;
import org.netbeans.modules.vmd.api.io.serialization.ComponentElement;
import org.netbeans.modules.vmd.api.io.serialization.PropertyElement;
import org.netbeans.modules.vmd.api.io.serialization.DocumentSerializationController;
import org.netbeans.modules.vmd.api.model.*;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.xml.XMLUtil;
import org.openide.util.Lookup;
import org.w3c.dom.*;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import org.netbeans.modules.vmd.api.io.serialization.DocumentErrorHandler;
import org.openide.util.NbBundle;

/**
 * 
 */
// TODO - check for design and document version
public class DocumentLoad {

    private static final String XML_ERROR = NbBundle.getMessage(DocumentLoad.class, "MSG_BrokenXML_Error"); //NOI18N
    private static final String WRONG_VERSION_ERROR = NbBundle.getMessage(DocumentLoad.class, "MSG_Wrong_VMD_Version_Error"); //NOI18N
    private static final String DESERIALIZATION_ERROR = NbBundle.getMessage(DocumentLoad.class, "MSG_Deserialization_Error"); //NOI18N
    private static final String DESCRIPTOR_MISSING_ERROR = NbBundle.getMessage(DocumentLoad.class, "MSG_MissingDescriptor_Error"); //NOI18N

    private static Collection<? extends DocumentSerializationController> getDocumentSerializationControllers() {
        return Lookup.getDefault().lookupAll(DocumentSerializationController.class);
    }

    public static boolean load(DataObjectContext context, DesignDocument loadingDocument, DocumentErrorHandler errorHandler) {
        final Node rootNode;
        try {
            rootNode = getRootNode(IOSupport.getDesignFile(context));
        } catch (IOException e) {
            throw Debug.error(e);
        }
        if (rootNode == null) {
            Debug.warning("Mobility: Root not not fould"); // NOI18N
            return false;
        }

        if (!DocumentSave.VERSION_VALUE_1.equals(getAttributeValue(rootNode, DocumentSave.VERSION_ATTR))) {
            Debug.warning("Invalid version of VisualDesign"); // NOI18N
            errorHandler.addWarning(WRONG_VERSION_ERROR);
            return false;
        }

        return loadVersion1(context, loadingDocument, rootNode, errorHandler);
    }

    private static boolean loadVersion1(DataObjectContext context, DesignDocument loadingDocument, Node rootNode, DocumentErrorHandler errorHandler) {
        final Node documentNode = findDocumentNode(rootNode);

        loadDocumentVersion1(context, loadingDocument, documentNode, errorHandler);
        if (!errorHandler.getErrors().isEmpty()) {
            return false;
        }
        Collection<? extends DataSerializer> serializers = DocumentSave.customDataSerializers.allInstances();
        for (Node node : getChildNode(rootNode)) {
            if (isDocumentNode(node)) {
                continue;
            }
            for (DataSerializer serializer : serializers) {
                if (serializer.deserializeData(context, loadingDocument, node)) {
                    break;
                }
            }
        }
        return true;
    }

    private static void loadDocumentVersion1(final DataObjectContext context, final DesignDocument loadingDocument, Node documentNode, final DocumentErrorHandler errorHandler) {
        final String documentVersion = getAttributeValue(documentNode, DocumentSave.VERSION_ATTR);

        ArrayList<ComponentElement> componentElements = new ArrayList<ComponentElement>();
        if (documentNode != null) {
            for (Node child : getChildNode(documentNode)) {
                if (isComponentNode(child)) {
                    collectStructure(componentElements, child, Long.MIN_VALUE, errorHandler);
                }
            }
        }
        for (DocumentSerializationController controller : getDocumentSerializationControllers()) {
            controller.approveComponents(context, loadingDocument, documentVersion, componentElements, errorHandler);
            if (!errorHandler.getErrors().isEmpty()) {
                return;
            }
        }
        final HashMap<Long, ComponentElement> hierarchy = new HashMap<Long, ComponentElement>();
        HashSet<TypeID> typeids = new HashSet<TypeID>();
        for (ComponentElement element : componentElements) {
            hierarchy.put(element.getUID(), element);
            typeids.add(element.getTypeID());
        }

//        loadingDocument.getDescriptorRegistry().assertComponentDescriptors(typeids);

        loadingDocument.getTransactionManager().writeAccess(new Runnable() {

            public void run() {
                loadDocumentCore(context, loadingDocument, documentVersion, hierarchy, errorHandler);
                for (DocumentSerializationController controller : getDocumentSerializationControllers()) {
                    controller.postValidateDocument(context, loadingDocument, documentVersion, errorHandler);
                    if (!errorHandler.getErrors().isEmpty()) {
                        return;
                    }
                }
            }
        });
    }

    private static void loadDocumentCore(DataObjectContext context, DesignDocument loadingDocument, String documentVersion, HashMap<Long, ComponentElement> hierarchy, DocumentErrorHandler errorHandler) {
        ArrayList<ComponentElement> list = new ArrayList<ComponentElement>(hierarchy.values());
        Collections.sort(list, new Comparator<ComponentElement>() {

            public int compare(ComponentElement o1, ComponentElement o2) {
                return (int) (o1.getUID() - o2.getUID());
            }
        });

        for (ComponentElement element : list) {
            long componentid = element.getUID();
            loadingDocument.setPreferredComponentID(componentid);
            if (loadingDocument.getDescriptorRegistry().getComponentDescriptor(element.getTypeID()) == null) {
                if (element.getUID() == 0) {
                    Debug.warning("Missing ComponentDescriptor in registry ", element.getTypeID()); // NOI18N
                    errorHandler.addError("{0} {1}", DESCRIPTOR_MISSING_ERROR, element.getTypeID().toString()); //NOI18N
                    continue;
                } else {
                    Debug.warning("Missing ComponentDescriptor in registry ", element.getTypeID()); // NOI18N
                    errorHandler.addWarning("{0} {1}", DESCRIPTOR_MISSING_ERROR, element.getTypeID().toString()); //NOI18N
                    continue;
                }
            }
            if (!errorHandler.getErrors().isEmpty()) {
                return;
            }
            DesignComponent component = loadingDocument.createRawComponent(element.getTypeID());
            assert component.getComponentID() == componentid;
            assert component.getComponentDescriptor() != null;
        }

        for (ComponentElement element : hierarchy.values()) {
            long parentuid = element.getParentUID();
            DesignComponent parent = loadingDocument.getComponentByUID(parentuid);
            DesignComponent component = loadingDocument.getComponentByUID(element.getUID());
            if (component == null) {
                continue;
            }
            if (parentuid != Long.MIN_VALUE) {
                if (parent == null) {
                    continue;
                }
                parent.addComponent(component);
            }
        }

        for (ComponentElement element : hierarchy.values()) {
            DesignComponent component = loadingDocument.getComponentByUID(element.getUID());
            if (component == null) {
                continue;
            }
            ComponentDescriptor descriptor = component.getComponentDescriptor();
            if (descriptor == null) {
                continue;
            }
            Node[] propertyNodes = getChildNode(element.getNode());
            ArrayList<PropertyElement> propertyElements = new ArrayList<PropertyElement>();
            for (Node propertyNode : propertyNodes) {
                if (!isPropertyNode(propertyNode)) {
                    continue;
                }
                String propertyName = getAttributeValue(propertyNode, DocumentSave.NAME_ATTR);
                TypeID typeid = TypeID.createFrom(getAttributeValue(propertyNode, DocumentSave.TYPEID_ATTR));
                String serialized = getAttributeValue(propertyNode, DocumentSave.VALUE_ATTR);
                propertyElements.add(PropertyElement.create(propertyName, typeid, serialized));
            }

            for (DocumentSerializationController controller : getDocumentSerializationControllers()) {
                controller.approveProperties(context, loadingDocument, documentVersion, component, propertyElements, errorHandler);
            }
            for (PropertyElement propertyElement : propertyElements) {
                String propertyName = propertyElement.getPropertyName();
                if (propertyName == null || descriptor.getPropertyDescriptor(propertyName) == null) {
                    Debug.warning("Missing property descriptor", component, propertyName); // NOI18N
                    errorHandler.addWarning("{0} {1} - {2}", NbBundle.getMessage(DocumentLoad.class, "MSG_MissingProperty_Error"), component, propertyName); // NOI18N
                    continue;
                }
                PropertyValue value;
                try {
                    value = PropertyValue.deserialize(propertyElement.getSerialized(), loadingDocument, propertyElement.getTypeID());
                } catch (Exception e) {
                    Debug.warning("Error while deserializing property value", component, propertyName); // NOI18N
                    errorHandler.addWarning("{0} {1} {2} {3}", DESERIALIZATION_ERROR, component, propertyName, propertyElement.getSerialized()); //NOI18N
                    value = PropertyValue.createNull();
                }
                component.writeProperty(propertyName, value);
            }
        }

        DesignComponent componentByUID = loadingDocument.getComponentByUID(0);
        if (componentByUID != null) {
            loadingDocument.setRootComponent(componentByUID);
        }
    }

    private static void collectStructure(Collection<ComponentElement> componentElements, Node node, long parent, DocumentErrorHandler errorHandler) {
        String id = getAttributeValue(node, DocumentSave.COMPONENTID_ATTR);
        if (id == null) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < node.getAttributes().getLength(); i++) {
                sb.append(" "); //NOI18N
                sb.append(node.getAttributes().item(i));
            }
            errorHandler.addError(NbBundle.getMessage(DocumentLoad.class, "MSG_Wrong_argument") + sb.toString()); //NOI18N
            return;
        }
        long componentid = -1;
        try {
            componentid = Long.parseLong(id);
        } catch (NumberFormatException ex) {
            errorHandler.addError("{0} {1}", NbBundle.getMessage(DocumentLoad.class, "MSG_Wrong_argument_value"), id); //NOI18N
            return;
        }
        TypeID typeid = TypeID.createFrom(getAttributeValue(node, DocumentSave.TYPEID_ATTR));
        componentElements.add(ComponentElement.create(parent, componentid, typeid, node));

        Node[] children = getChildNode(node);
        for (Node child : children) {
            if (isComponentNode(child)) {
                collectStructure(componentElements, child, componentid, errorHandler);
            }
        }
    }

    private static boolean isDocumentNode(Node child) {
        return DocumentSave.DOCUMENT_NODE.equals(child.getNodeName());
    }

    private static boolean isComponentNode(Node child) {
        return DocumentSave.COMPONENT_NODE.equals(child.getNodeName());
    }

    private static boolean isPropertyNode(Node child) {
        return DocumentSave.PROPERTY_NODE.equals(child.getNodeName());
    }

    private static Node[] getChildNode(Node node) {
        NodeList childNodes = node.getChildNodes();
        Node[] nodes = new Node[childNodes != null ? childNodes.getLength() : 0];
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = childNodes.item(i);
        }
        return nodes;
    }

    private static Node findDocumentNode(Node rootNode) {
        for (Node node : getChildNode(rootNode)) {
            if (isDocumentNode(node)) {
                return node;
            }
        }
        return null;
    }
//    static void createEmpty (DataObjectContext context, DesignDocument loadingDocument) {
//        final TypeID type = ProjectTypeInfo.getProjectTypeInfoFor (context.getProjectType ()).getRootCDTypeID ();
//        final DescriptorRegistry descriptorRegistry = loadingDocument.getDescriptorRegistry ();
//        final boolean[] ret = new boolean[1];
//        descriptorRegistry.readAccess (new Runnable() {
//            public void run () {
//                ret[0] = descriptorRegistry.getComponentDescriptor (type) != null;
//            }
//        });
//        if (ret[0]) {
//            DesignComponent root = loadingDocument.createComponent (type);
//            loadingDocument.setRootComponent (root);
//        }
//    }

    private static Node getRootNode(final FileObject fileObject) throws IOException {
        synchronized (DocumentSave.sync) {
            final Node[] node = new Node[1];
            if (fileObject != null) {
                fileObject.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {

                    public void run() throws IOException {
                        Document document = null;
                        if (fileObject != null) {
                            FileLock lock = null;
                            try {
                                lock = fileObject.lock();
                                document = getXMLDocument(fileObject);
                            } finally {
                                if (lock != null) {
                                    lock.releaseLock();
                                }
                            }
                        }
                        node[0] = document != null ? document.getFirstChild() : null;
                    }
                });
            }
            return node[0];
        }
    }

    private static Document getXMLDocument(FileObject fileObject) throws IOException {
        Document doc = null;
        final InputStream is = fileObject.getInputStream();
        final DocumentErrorHandler errorHandler = new DocumentErrorHandler();
        try {
            doc = XMLUtil.parse(new InputSource(is), false, false, new ErrorHandler() {

                public void error(SAXParseException e) throws SAXException {
                    errorHandler.addError(XML_ERROR + e.getMessage());
                }

                public void fatalError(SAXParseException e) throws SAXException {
                    errorHandler.addError(XML_ERROR + e.getMessage());
                }

                public void warning(SAXParseException e) {
                    errorHandler.addWarning(XML_ERROR + e.getMessage());
                }
            }, null);
        } catch (SAXException e) {
            errorHandler.addError(XML_ERROR + e.getMessage());
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                errorHandler.addError(XML_ERROR + e.getMessage());
            }
        }
        DocumentErrorHandlerSupport.showDocumentErrorHandlerDialog(errorHandler, fileObject);
        return doc;
    }

    private static String getAttributeValue(Node node, String attr) {
        try {
            if (node != null) {
                NamedNodeMap map = node.getAttributes();
                if (map != null) {
                    node = map.getNamedItem(attr);
                    if (node != null) {
                        return node.getNodeValue();
                    }
                }
            }
        } catch (DOMException e) {
            Debug.warning(e);
        }
        return null;
    }

    public static String loadProjectType(DataObjectContext context) {
        final Node rootNode;
        try {
            rootNode = getRootNode(IOSupport.getDesignFile(context));
            if (rootNode == null) {
                Debug.warning("Mobility: Root not not fould"); // NOI18N
                return ""; //NOI18N
            }
        } catch (IOException e) {
            throw Debug.error(e);
        }
        return getAttributeValue(rootNode, DocumentSave.PROP_PROJECT_TYPE);
    }
}
