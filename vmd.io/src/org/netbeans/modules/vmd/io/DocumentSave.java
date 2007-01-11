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

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.io.DataSerializer;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.providers.IOSupport;
import org.openide.xml.XMLUtil;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileSystem;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Attr;

import java.util.HashSet;
import java.util.Collection;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author David Kaspar
 */
// TODO - should not we save all components including ones outside of the tree
// TODO - use versions
public class DocumentSave {

    static final Lookup.Result<DataSerializer> customDataSerializers = Lookup.getDefault ().lookupResult (DataSerializer.class);

    static final String XML_ROOT_NODE = "VisualDesign"; // NOI18N
    static final String PROP_PROJECT_TYPE = "projectType"; // NOI18N
    static final String DOCUMENT_NODE = "Document"; // NOI18N
    static final String VERSION_ATTR = "version"; // NOI18N
    static final String COMPONENT_NODE = "Component"; // NOI18N
    static final String PROPERTY_NODE = "Property"; // NOI18N
    static final String COMPONENTID_ATTR = "componentID"; // NOI18N
    static final String TYPEID_ATTR = "typeID"; // NOI18N
    static final String NAME_ATTR = "name"; // NOI18N
    static final String VALUE_ATTR = "value"; // NOI18N

    static final String VERSION_VALUE_1 = "1"; // NOI18N

    static Object sync = new Object ();

    public static void save (DataObjectContext context, DesignDocument savingDocument) {
        Document xml = XMLUtil.createDocument (XML_ROOT_NODE, null, null, null);// TODO - NS, DTD
        Node xmlRootNode = xml.getFirstChild ();
        setAttribute (xml, xmlRootNode, VERSION_ATTR, VERSION_VALUE_1);
        setAttribute (xml, xmlRootNode, PROP_PROJECT_TYPE, savingDocument.getDocumentInterface().getProjectType());

        Node node = xml.createElement (DOCUMENT_NODE);
        setAttribute (xml, node, VERSION_ATTR, VERSION_VALUE_1);
        checkDocumentValidity (savingDocument);
        saveComponent (xml, node, savingDocument.getRootComponent ());
        xmlRootNode.appendChild (node);

        for (DataSerializer serializer : customDataSerializers.allInstances ()) {
            Node data = serializer.serializeData (context, savingDocument, xml);
            if (data != null)
                xml.getFirstChild ().appendChild (data);
        }

        try {
            writeDocument (IOSupport.getDesignFile (context), xml);
        } catch (IOException e) {
            throw Debug.error (e);
        }
    }

    private static void checkDocumentValidity (DesignDocument savingDocument) {
        HashSet<DesignComponent> componentsInTree = new HashSet<DesignComponent> ();
        HashSet<DesignComponent> referencedComponents = new HashSet<DesignComponent> ();

        collectComponentsInTree (componentsInTree, savingDocument.getRootComponent ());
        for (DesignComponent component : componentsInTree)
            collectUsedReferences (referencedComponents, component);

        for (DesignComponent component : referencedComponents)
            if (! componentsInTree.contains (component))
                Debug.warning ("Saving", "Referenced component is not in the tree", component);

    }

    private static void saveComponent (Document xml, Node parentNode, DesignComponent component) {
        ComponentDescriptor descriptor = component.getComponentDescriptor ();

        Node node = xml.createElement (COMPONENT_NODE);
        parentNode.appendChild (node);

        setAttribute (xml, node, COMPONENTID_ATTR, Long.toString (component.getComponentID ()));
        setAttribute (xml, node, TYPEID_ATTR, component.getType ().getEncoded ());

        Collection<PropertyDescriptor> propertyDescriptors = descriptor.getPropertyDescriptors ();
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            if (! propertyDescriptor.isUseForSerialization ())
                continue;
            String propertyName = propertyDescriptor.getName ();
            if (component.isDefaultValue (propertyName))
                continue;
            PropertyValue propertyValue = component.readProperty (propertyName);
            String serialized = propertyValue.serialize ();

            Node propertyNode = xml.createElement (PROPERTY_NODE);
            node.appendChild (propertyNode);
            setAttribute (xml, propertyNode, NAME_ATTR, propertyDescriptor.getName ());
            setAttribute (xml, propertyNode, TYPEID_ATTR, propertyDescriptor.getType ().getEncoded ());
            setAttribute (xml, propertyNode, VALUE_ATTR, serialized);
        }

        for (DesignComponent child : component.getComponents ())
            saveComponent (xml, node, child);
    }

    private static void collectComponentsInTree (HashSet<DesignComponent> componentsInTree, DesignComponent component) {
        componentsInTree.add (component);
        for (DesignComponent child : component.getComponents ())
            collectComponentsInTree (componentsInTree, child);
    }

    private static void collectUsedReferences (HashSet<DesignComponent> referencedComponents, DesignComponent component) {
        ComponentDescriptor descriptor = component.getComponentDescriptor ();
        Collection<PropertyDescriptor> propertyDescriptors = descriptor.getPropertyDescriptors ();
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            if (! propertyDescriptor.isUseForSerialization ())
                continue;
            PropertyValue propertyValue = component.readProperty (propertyDescriptor.getName ());
            Debug.collectAllComponentReferences (propertyValue, referencedComponents);
        }
    }

    private static void writeDocument (final FileObject file, final Document doc) throws IOException {
        synchronized (sync) {
            file.getFileSystem ().runAtomicAction (new FileSystem.AtomicAction () {
                public void run () throws IOException {
                    OutputStream os = null;
                    FileLock lock = null;
                    try {
                        lock = file.lock ();
                        os = file.getOutputStream (lock);
                        XMLUtil.write (doc, os, "UTF-8"); // NOI18N
                    } finally {
                        if (os != null)
                            try {
                                os.close ();
                            } catch (IOException e) {
                                ErrorManager.getDefault ().notify (e);
                            }
                        if (lock != null)
                            lock.releaseLock ();
                    }
                }
            });
        }
    }

    private static void setAttribute (Document xml, Node node, String name, String value) {
        NamedNodeMap map = node.getAttributes ();
        Attr attribute = xml.createAttribute (name);
        attribute.setValue (value);
        map.setNamedItem (attribute);
    }

}
