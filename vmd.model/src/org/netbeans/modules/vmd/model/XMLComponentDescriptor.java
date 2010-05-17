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
package org.netbeans.modules.vmd.model;

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.common.DefaultVersionDescriptor;
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

    public static final String COMPONENT_DESCRIPTOR_NODE = "ComponentDescriptor"; // NOI18N
    public static final String VERSION_ATTR = "version";  // NOI18N

    public static final String TYPE_NODE = "TypeDescriptor"; // NOI18N
    public static final String SUPER_TYPEID_ATTR = "superTypeID"; // NOI18N
    public static final String THIS_TYPEID_ATTR = "thisTypeID"; // NOI18N
    public static final String CAN_INSTANTIATE_ATTR = "canInstantiate"; // NOI18N
    public static final String CAN_DERIVE_ATTR = "canDerive"; // NOI18N

    public static final String PALETTE_NODE = "PaletteDescriptor"; // NOI18N
    public static final String DISPLAY_NAME_ATTR = "displayName"; // NOI18N
    public static final String TOOLTIP_ATTR = "toolTip"; // NOI18N
    public static final String SMALL_ICON_ATTR = "smallIcon"; // NOI18N
    public static final String LARGE_ICON_ATTR = "largeIcon"; // NOI18N
    public static final String PREFERRED_CATEGORYID_ATTR = "preferredCategoryID"; // NOI18N

    public static final String PROPERTY_DESCRIPTOR_NODE = "PropertyDescriptor"; // NOI18N
    public static final String NAME_ATTR = "name"; // NOI18N
    public static final String TYPEID_ATTR = "typeID"; // NOI18N
    public static final String DEFAULT_VALUE_ATTR = "defaultValue"; // NOI18N
    public static final String ALLOW_NULL = "allowNull"; // NOI18N
    public static final String ALLOW_USER_CODE = "allowUserCode"; // NOI18N
    public static final String USE_FOR_SERIALIZATION_ATTR = "useForSerialization"; // NOI18N
    public static final String READ_ONLY_ATTR = "readOnly"; // NOI18N

    public static final String PRESENTERS_NODE = "Presenters"; // NOI18N

    public static final String VERSION_VALUE_1 = "1"; // NOI18N

    private TypeDescriptor typeDescriptor;
    private PaletteDescriptor paletteDescriptor;
    private List<PropertyDescriptor> propertyDescriptors;
    private List<PresenterDeserializer.PresenterFactory> presenterDescriptors;

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
        for (PresenterDeserializer.PresenterFactory factory : presenterDescriptors) {
            List<Presenter> list = factory.createPresenters (this);
            if (list != null)
                presenters.addAll (list);
        }
        return presenters;
    }

    public boolean deserialize (String projectType, Document document) {
        Node rootNode = document.getFirstChild ();

        if (! COMPONENT_DESCRIPTOR_NODE.equals (rootNode.getNodeName ())) {
            Debug.warning ("Invalid root node"); // NOI18N
            return false;
        }

        String version = getAttributeValue (rootNode, VERSION_ATTR);
        if (VERSION_VALUE_1.equals (version)) {
            deserializeVersion1 (projectType, rootNode);
            return true;
        } else {
            Debug.warning ("Invalid version", version); // NOI18N
            return false;
        }
    }

    private void deserializeVersion1 (String projectType, Node rootNode) {
        ArrayList<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor> ();
        ArrayList<PresenterDeserializer.PresenterFactory> presenters = new ArrayList<PresenterDeserializer.PresenterFactory> ();

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
                        defaultValue != null ? PropertyValue.createUserCode (defaultValue) : PropertyValue.createNull (),
                        Boolean.parseBoolean (getAttributeValue (node, ALLOW_NULL)),
                        Boolean.parseBoolean (getAttributeValue (node, ALLOW_USER_CODE)),
                        Versionable.FOREVER,
                        Boolean.parseBoolean (getAttributeValue (node, USE_FOR_SERIALIZATION_ATTR)),
                        Boolean.parseBoolean (getAttributeValue (node, READ_ONLY_ATTR))
                ));
            }
        }

        deserializePresenters (rootNode, projectType, presenters);

        propertyDescriptors = properties;
        presenterDescriptors = presenters;
    }

    private void deserializePresenters (Node rootNode, String projectType, ArrayList<PresenterDeserializer.PresenterFactory> presenters) {
        for (Node presentersNode : getChildNode (rootNode))
            if (PRESENTERS_NODE.equals (presentersNode.getNodeName ()))
                for (Node node : getChildNode (presentersNode)) {
                    PresenterDeserializer.PresenterFactory presenterFactory = PresenterDeserializerSupport.deserialize (projectType, node);
                    if (presenterFactory != null)
                        presenters.add (presenterFactory);
                }
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

    static Node[] getChildNode (Node node) {
        NodeList childNodes = node.getChildNodes ();
        Node[] nodes = new Node[childNodes != null ? childNodes.getLength () : 0];
        for (int i = 0; i < nodes.length; i++)
            nodes[i] = childNodes.item (i);
        return nodes;
    }

}
