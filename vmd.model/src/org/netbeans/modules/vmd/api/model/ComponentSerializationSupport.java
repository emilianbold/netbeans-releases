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

package org.netbeans.modules.vmd.api.model;

import org.netbeans.modules.vmd.model.XMLComponentDescriptor;
import org.openide.loaders.DataFolder;
import org.openide.xml.XMLUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileLock;
import org.w3c.dom.*;

import java.util.List;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class contains a support for a custom component serialization into a xml file which is automatically stored into the global registry.
 *
 * @author David Kaspar
 */
public class ComponentSerializationSupport {

    /**
     * Invokes refreshing of descriptor registry of a specified project type.
     * @param projectType the project type
     */
    public static void refreshDescriptorRegistry (String projectType) {
        GlobalDescriptorRegistry registry = GlobalDescriptorRegistry.getGlobalDescriptorRegistry (projectType);
        DataFolder registryFolder = registry.getRegistryFolder ();
        registryFolder.getPrimaryFile ().refresh (true);
        registry.reload ();
    }

    /**
     * Creates a new component descriptor and stores it into xml file and add it into global descriptor registry.
     * After you add all custom component descriptors, then call <code>ComponentSerializationSupport.refresh</code> to refresh the registry.
     * @param projectType the project type
     * @param typeDescriptor the type descriptor
     * @param paletteDescriptor the palette descriptor
     * @param properties the list of declared properties
     * @param presenters the list of presenter serializers
     */
    public static void serialize (String projectType, TypeDescriptor typeDescriptor, PaletteDescriptor paletteDescriptor, List<PropertyDescriptor> properties, List<PresenterSerializer> presenters) {
        assert projectType != null  &&  typeDescriptor != null  &&  properties != null  &&  presenters != null;

        Document document = XMLUtil.createDocument (XMLComponentDescriptor.COMPONENT_DESCRIPTOR_NODE, null, null, null);
        Node rootNode = document.getFirstChild ();
        setAttribute (document, rootNode, XMLComponentDescriptor.VERSION_ATTR, XMLComponentDescriptor.VERSION_VALUE_1);

        Element typeNode = document.createElement (XMLComponentDescriptor.TYPE_NODE);
        if (typeDescriptor.getSuperType () != null)
            setAttribute (document, typeNode, XMLComponentDescriptor.SUPER_TYPEID_ATTR, typeDescriptor.getSuperType ().toString ());
        setAttribute (document, typeNode, XMLComponentDescriptor.THIS_TYPEID_ATTR, typeDescriptor.getThisType ().toString ());
        setAttribute (document, typeNode, XMLComponentDescriptor.CAN_DERIVE_ATTR, Boolean.toString (typeDescriptor.isCanDerive ()));
        setAttribute (document, typeNode, XMLComponentDescriptor.CAN_INSTANTIATE_ATTR, Boolean.toString (typeDescriptor.isCanInstantiate ()));
        rootNode.appendChild (typeNode);

        Element paletteNode = document.createElement (XMLComponentDescriptor.PALETTE_NODE);
        setAttribute (document, paletteNode, XMLComponentDescriptor.DISPLAY_NAME_ATTR, paletteDescriptor.getDisplayName ());
        if (paletteDescriptor.getToolTip () != null)
            setAttribute (document, paletteNode, XMLComponentDescriptor.TOOLTIP_ATTR, paletteDescriptor.getToolTip ());
        if (paletteDescriptor.getCategoryID () != null)
            setAttribute (document, paletteNode, XMLComponentDescriptor.PREFERRED_CATEGORYID_ATTR, paletteDescriptor.getCategoryID ());
        rootNode.appendChild (paletteNode);

        for (PropertyDescriptor propertyDescriptor : properties) {
            assert propertyDescriptor != null;
            Element propertyNode = document.createElement (XMLComponentDescriptor.PROPERTY_DESCRIPTOR_NODE);
            setAttribute (document, propertyNode, XMLComponentDescriptor.NAME_ATTR, propertyDescriptor.getName ());
            setAttribute (document, propertyNode, XMLComponentDescriptor.TYPEID_ATTR, propertyDescriptor.getType ().toString ());
            String userCode = propertyDescriptor.getDefaultValue ().getUserCode ();
            if (userCode != null)
                setAttribute (document, propertyNode, XMLComponentDescriptor.DEFAULT_VALUE_ATTR, userCode);
            setAttribute (document, propertyNode, XMLComponentDescriptor.ALLOW_NULL, Boolean.toString (propertyDescriptor.isAllowNull ()));
            setAttribute (document, propertyNode, XMLComponentDescriptor.ALLOW_USER_CODE, Boolean.toString (propertyDescriptor.isAllowUserCode ()));
            setAttribute (document, propertyNode, XMLComponentDescriptor.USE_FOR_SERIALIZATION_ATTR, Boolean.toString (propertyDescriptor.isUseForSerialization ()));
            setAttribute (document, propertyNode, XMLComponentDescriptor.READ_ONLY_ATTR, Boolean.toString (propertyDescriptor.isReadOnly ()));
            rootNode.appendChild (propertyNode);
        }

        Element presentersNode = document.createElement (XMLComponentDescriptor.PRESENTERS_NODE);
        for (PresenterSerializer serializer : presenters) {
            List<Element> nodes = serializer.serialize (document);
            if (nodes != null)
                for (Node node : nodes) {
                    if (node != null)
                        presentersNode.appendChild (node);
                }
        }
        rootNode.appendChild (presentersNode);

        GlobalDescriptorRegistry registry = GlobalDescriptorRegistry.getGlobalDescriptorRegistry (projectType);
        DataFolder registryFolder = registry.getRegistryFolder ();
        if (! writeDocument (registryFolder.getPrimaryFile (), typeDescriptor.getThisType ().toString (), "xml", document))
            Debug.warning ("Error while serializing a component descriptor", typeDescriptor.getThisType ().toString ());
    }

    private static void setAttribute (Document xml, Node node, String name, String value) {
        NamedNodeMap map = node.getAttributes ();
        Attr attribute = xml.createAttribute (name);
        attribute.setValue (value);
        map.setNamedItem (attribute);
    }

    public static boolean writeDocument (final FileObject folder, final String filename, final String ext, final Document doc) {
        try {
            folder.getFileSystem ().runAtomicAction (new FileSystem.AtomicAction() {
                public void run () throws IOException {
                    FileObject file = folder.getFileObject (filename, ext);
                    if (file == null)
                        file = folder.createData (filename, ext);
                    writeDocument (file, doc);
                }
            });
            return true;
        } catch (IOException e) {
            e.printStackTrace ();
            return false;
        }
    }

    public static void writeDocument (FileObject file, Document doc) throws IOException {
        if (file == null)
            throw new IOException ("Null file to write document"); // NOI18N
        if (doc == null)
            throw new IOException ("Empty document is about to save"); // NOI18N
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
                }
            if (lock != null)
                lock.releaseLock ();
        }
    }


}
