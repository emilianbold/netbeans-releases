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
package org.netbeans.modules.vmd.model;

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.common.DefaultVersionDescriptor;
import org.openide.util.Lookup;
import org.w3c.dom.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Collections;

/**
 * @author David Kaspar
 */
// TODO - whole serialization and deserialization of versionDescriptor, excludePropertyDescriptorNames, required library
public class XMLComponentDescriptor extends ComponentDescriptor {

    private static final Lookup.Result<PresenterSerializer> lookupResult = Lookup.getDefault ().lookupResult (PresenterSerializer.class);

    private static final String COMPONENT_DESCRIPTOR_NODE = "ComponentDescriptor"; // NOI18N
    private static final String VERSION_ATTR = "version";  // NOI18N

    private static final String TYPE_NODE = "TypeDescriptor"; // NOI18N
    private static final String SUPER_TYPEID_ATTR = "superTypeID"; // NOI18N
    private static final String THIS_TYPEID_ATTR = "thisTypeID"; // NOI18N
    private static final String CAN_INSTANTIATE_ATTR = "canInstantiate"; // NOI18N
    private static final String CAN_DERIVE_ATTR = "canDerive"; // NOI18N

    private static final String PALETTE_NODE = "PaletteDescriptor"; // NOI18N
    private static final String DISPLAY_NAME_ATTR = "displayName"; // NOI18N
    private static final String TOOLTIP_ATTR = "toolTip"; // NOI18N
    private static final String SMALL_ICON_ATTR = "smallIcon"; // NOI18N
    private static final String LARGE_ICON_ATTR = "largeIcon"; // NOI18N
    private static final String PREFERRED_CATEGORYID_ATTR = "preferredCategoryID"; // NOI18N

    private static final String PROPERTY_DESCRIPTOR_NODE = "PropertyDescriptor"; // NOI18N
    private static final String NAME_ATTR = "name"; // NOI18N
    private static final String TYPEID_ATTR = "typeID"; // NOI18N
    private static final String DEFAULT_VALUE_ATTR = "defaultValue"; // NOI18N
    private static final String ALLOW_NULL = "allowNull"; // NOI18N
    private static final String ALLOW_USER_CODE = "allowUserCode"; // NOI18N
    private static final String USE_FOR_SERIALIZATION_ATTR = "useForSerialization"; // NOI18N
    private static final String READ_ONLY_ATTR = "readOnly"; // NOI18N

    private static final String PRESENTERS_NODE = "Presenters"; // NOI18N

    private static final String VERSION_VALUE_1 = "1"; // NOI18N

    private TypeDescriptor typeDescriptor;
    private PaletteDescriptor paletteDescriptor;
    private List<PropertyDescriptor> propertyDescriptors;
    private List<PresenterSerializer.PresenterFactory> presenterFactories;

    public TypeDescriptor getTypeDescriptor () {
        return typeDescriptor;
    }

    public VersionDescriptor getVersionDescriptor () {
        return DefaultVersionDescriptor.createForeverCompatibleVersionDescriptor (); // TODO
    }

    public Collection<String> getExcludedPropertyDescriptorNames () {
        return Collections.emptySet (); // TODO
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return propertyDescriptors;
    }

    public PaletteDescriptor getPaletteDescriptor () {
        return paletteDescriptor;
    }

    protected List<? extends Presenter> createPresenters () {
        ArrayList<Presenter> presenters = new ArrayList<Presenter> ();
        for (PresenterSerializer.PresenterFactory factory : presenterFactories) {
            List<Presenter> list = factory.createPresenters (this);
            if (list != null)
                presenters.addAll (list);
        }
        return presenters;
    }

    public boolean deserialize (Document document) {
        Node rootNode = document.getFirstChild ();

        if (! COMPONENT_DESCRIPTOR_NODE.equals (rootNode.getNodeName ())) {
            Debug.warning ("Invalid root node"); // NOI18N
            return false;
        }

        String version = getAttributeValue (rootNode, VERSION_ATTR);
        if (VERSION_VALUE_1.equals (version)) {
            deserializeVersion1 (rootNode);
            return true;
        } else {
            Debug.warning ("Invalid version", version); // NOI18N
            return false;
        }
    }

    private void deserializeVersion1 (Node rootNode) {
        ArrayList<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor> ();
        ArrayList<PresenterSerializer.PresenterFactory> presenters = new ArrayList<PresenterSerializer.PresenterFactory> ();

        for (Node node : getChildNode (rootNode)) {
            if (TYPE_NODE.equals (node.getNodeName ())) {
                String thisTypeID = getAttributeValue (node, THIS_TYPEID_ATTR);
                if (thisTypeID == null) {
                    Debug.warning ("Missing " + THIS_TYPEID_ATTR + " attribute"); // NOI18N
                    continue;
                }
                typeDescriptor = new TypeDescriptor (
                        TypeID.createFrom (getAttributeValue (node, SUPER_TYPEID_ATTR)),
                        TypeID.createFrom (thisTypeID),
                        Boolean.parseBoolean (getAttributeValue (node, CAN_INSTANTIATE_ATTR)),
                        Boolean.parseBoolean (getAttributeValue (node, CAN_DERIVE_ATTR))
                );
            } else if (PALETTE_NODE.equals (node.getNodeName ())) {
                paletteDescriptor = new PaletteDescriptor (
                        getAttributeValue (node, PREFERRED_CATEGORYID_ATTR),
                        getAttributeValue (node, DISPLAY_NAME_ATTR),
                        getAttributeValue (node, TOOLTIP_ATTR),
                        getAttributeValue (node, SMALL_ICON_ATTR),
                        getAttributeValue (node, LARGE_ICON_ATTR)
                );
            } else if (PROPERTY_DESCRIPTOR_NODE.equals (node.getNodeName ())) {
                String name = getAttributeValue (node, NAME_ATTR);
                if (name == null) {
                    Debug.warning ("Missing name attribute"); // NOI18N
                    continue;
                }
                TypeID typeID = TypeID.createFrom (getAttributeValue (node, TYPEID_ATTR));
                if (typeID == null) {
                    Debug.warning ("Missing typeID attribute", name); // NOI18N
                    continue;
                }
                String defaultValue = getAttributeValue (node, DEFAULT_VALUE_ATTR);
                properties.add (new PropertyDescriptor (
                        name,
                        typeID,
                        defaultValue != null ? PropertyValue.deserialize (defaultValue, null, typeID) : PropertyValue.createNull (), // TODO - null document is passed
                        Boolean.parseBoolean (getAttributeValue (node, ALLOW_NULL)),
                        Boolean.parseBoolean (getAttributeValue (node, ALLOW_USER_CODE)),
                        Versionable.FOREVER,
                        Boolean.parseBoolean (getAttributeValue (node, USE_FOR_SERIALIZATION_ATTR)),
                        Boolean.parseBoolean (getAttributeValue (node, READ_ONLY_ATTR))
                ));
            }
        }

        for (Node presentersNode : getChildNode (rootNode))
            if (PRESENTERS_NODE.equals (presentersNode.getNodeName ()))
                for (Node node : getChildNode (presentersNode)) {
                    Collection<? extends PresenterSerializer> presenterSerializers = lookupResult.allInstances ();
                    for (PresenterSerializer serializer : presenterSerializers) {
                        PresenterSerializer.PresenterFactory factory = serializer.deserialize (this, node);
                        if (factory != null)
                            presenters.add (factory);
                    }
                }

        propertyDescriptors = properties;
        presenterFactories = presenters;
    }

    public void serialize () {
        // TODO - serialize
    }

//    private Image loadImage (String imageResource) {
//        return imageResource != null ? Utilities.loadImage (imageResource) : null;
//    }

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

    static void setAttribute (Document xml, Node node, String name, String value) {
        NamedNodeMap map = node.getAttributes ();
        Attr attribute = xml.createAttribute (name);
        attribute.setValue (value);
        map.setNamedItem (attribute);
    }

    static Node[] getChildNode (Node node) {
        NodeList childNodes = node.getChildNodes ();
        Node[] nodes = new Node[childNodes != null ? childNodes.getLength () : 0];
        for (int i = 0; i < nodes.length; i++)
            nodes[i] = childNodes.item (i);
        return nodes;
    }

}
