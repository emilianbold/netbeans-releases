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

import org.netbeans.modules.vmd.api.io.providers.DocumentSerializer;
import org.netbeans.modules.vmd.api.io.providers.IOSupport;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.categories.PointsCategoryCD;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;
import org.netbeans.modules.vmd.midp.components.points.MobileDeviceCD;
import static org.netbeans.modules.vmd.midp.converter.wizard.ConverterUtil.getBoolean;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author David Kaspar
 */
public class Converter {

    public static ArrayList<String> convert (final FileObject inputJavaFile, final FileObject inputDesignFile, String outputFileName) {
        final ArrayList<String> errors = new ArrayList<String> ();
        try {
            DataFolder folder = DataFolder.findFolder (inputJavaFile.getParent ());
            final Node rootNode = XMLUtil.getRootNode (inputDesignFile);
            if (! "1.3".equals (XMLUtil.getAttributeValue (rootNode, "version"))) { // NOI18N
                errors.add ("Unsupported version of the design file. The design has to saved in NetBeans 5.5 or newer.");
                return errors;
            }
            final List<ConverterItem> components = getConverterComponents (rootNode);

            DataObject template = DataObject.find (Repository.getDefault ().getDefaultFileSystem ().findResource ("Templates/MIDP/VisualMIDlet.java")); // NOI18N
            DataObject outputDesign = template.createFromTemplate (folder, outputFileName);
            DocumentSerializer serializer = IOSupport.getDocumentSerializer (outputDesign);
            serializer.waitDocumentLoaded ();
            final DesignDocument document = serializer.getDocument ();

            document.getTransactionManager ().writeAccess (new Runnable() {
                public void run () {
                    convert (errors, components, document);
                }
            });

            IOSupport.forceUpdateCode (outputDesign);
            outputDesign.getLookup ().lookup (CloneableEditorSupport.class).saveDocument ();
        } catch (Exception e) {
            Exceptions.printStackTrace (e);
        }
        return errors;
    }

    private static List<ConverterItem> getConverterComponents (Node rootNode) {
        ArrayList<ConverterItem> components = new ArrayList<ConverterItem> ();
        Node documentNode = XMLUtil.getChild (rootNode, "DesignDocument"); // NOI18N
        for (Node componentNode : XMLUtil.getChildren (documentNode, "DesignComponent")) { // NOI18N
            String typeid = XMLUtil.getAttributeValue (componentNode, "typeid");
           typeid = convertTypeIDFromString (typeid);
            ConverterItem item = new ConverterItem (
                    XMLUtil.getAttributeValue (componentNode, "uid"), // NOI18N
                    XMLUtil.getAttributeValue (componentNode, "id"), // NOI18N
                    typeid // NOI18N
            );
            for (Node propertyNode : XMLUtil.getChildren (componentNode, "Property")) { // NOI18N
                item.addProperty (
                        XMLUtil.getAttributeValue (propertyNode, "name"), // NOI18N
                        XMLUtil.getAttributeValue (propertyNode, "value") // NOI18N
                );
            }
            for (Node containerPropertyNode : XMLUtil.getChildren (componentNode, "ContainerProperty")) { // NOI18N
                String name = XMLUtil.getAttributeValue (containerPropertyNode, "name"); // NOI18N
                item.initContainerProperty (name); // NOI18N
                for (Node itemNode : XMLUtil.getChildren (containerPropertyNode, "ContainerPropertyItem")) // NOI18N
                    item.addContainerPropertyItem (name, XMLUtil.getAttributeValue (itemNode, "value")); // NOI18N
            }
            components.add (item);
        }
        return components;
    }

    private static String convertTypeIDFromString (String string) {
        if (string == null)
            return null;
        int dimension = 0;
        if (string.charAt (0) == '#') {
            int pos = 1;
            for (;;) {
                char c;
                if (pos >= string.length ()) {
                    dimension = 0;
                    break;
                }
                c = string.charAt (pos ++);
                if (c == '#')
                    break;
                if (! Character.isDigit (c)) {
                    dimension = 0;
                    break;
                }
                dimension = dimension * 10 + (c - '0');
            }
            if (dimension > 0)
                string = string.substring (pos);
        }
        int i = string.indexOf (':');
        return i >= 0 ? string.substring (i + 1) : string;
    }

    private static void convert (ArrayList<String> errors, List<ConverterItem> components, DesignDocument document) {
        HashMap<String,ConverterItem> id2item = new HashMap<String, ConverterItem> ();
        for (ConverterItem item : components)
            id2item.put (item.getID (), item);
        for (ConverterItem item : components)
            convert (id2item, item, document);
        for (ConverterItem item : components) {
            if (! item.isUsed ())
                Debug.warning ("Unrecognized component: " + item.getTypeID ());
        }
    }

    private static void convert (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
        if (item.isUsed ())
            return;
        String id = item.getID ();
        String typeID = item.getTypeID ();
        if ("javax.microedition.lcdui.Command".equals (typeID))
            ConverterResources.convertCommand (item, document);
        else if ("javax.microedition.lcdui.Font".equals (typeID))
            ConverterResources.convertFont (item, document);
        else if ("javax.microedition.lcdui.Form".equals (typeID))
            ConverterDisplayables.convertForm (id2item, item, document);
        else if ("javax.microedition.lcdui.TextBox".equals (typeID))
            ConverterDisplayables.convertTextBox (id2item, item, document);

        // TODO - all components

        if ("$MobileDevice".equals (id)) {
            DesignComponent pointsCategory = MidpDocumentSupport.getCategoryComponent(document, PointsCategoryCD.TYPEID);
            List<DesignComponent> list = DocumentSupport.gatherSubComponentsOfType(pointsCategory, MobileDeviceCD.TYPEID);
            DesignComponent mobileDevice = list.get (0);
            convertObject (item, mobileDevice);
        } else if ("$StartPoint".equals (id)) {
            DesignComponent pointsCategory = MidpDocumentSupport.getCategoryComponent(document, PointsCategoryCD.TYPEID);
            List<DesignComponent> list = DocumentSupport.gatherSubComponentsOfType(pointsCategory, MobileDeviceCD.TYPEID);
            DesignComponent mobileDevice = list.get (0);
            DesignComponent startEventSource = mobileDevice.readProperty (MobileDeviceCD.PROP_START).getComponent ();
            convertObject (item, startEventSource);
            ConverterActions.convertCommandActionHandler (id2item, item, startEventSource);
        }

        // TODO - all components
    }

    static ConverterItem convertConverterItem (HashMap<String, ConverterItem> id2item, String value, DesignDocument document) {
        ConverterItem item = id2item.get (value);
        if (item != null) {
            convert (id2item, item, document);
            if (item.isUsed ())
                return item;
        }
        return null;
    }

    // Created: NO, Adds: NO
    static void convertObject (ConverterItem item, DesignComponent component) {
        item.setUsed (component);
    }

    // Created: NO, Adds: NO
    static void convertClass (ConverterItem item, DesignComponent component) {
        convertObject (item, component);
        component.writeProperty (ClassCD.PROP_INSTANCE_NAME, MidpTypes.createStringValue (item.getID ()));
        Boolean lazy = getBoolean (item.getPropertyValue ("lazyInitialized")); // NOI18N
        component.writeProperty (ClassCD.PROP_LAZY_INIT, MidpTypes.createBooleanValue (lazy == null  ||  lazy));
    }

}
