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
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.categories.ResourcesCategoryCD;
import org.netbeans.modules.vmd.midp.components.commands.CommandCD;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;
import org.netbeans.modules.vmd.midp.components.resources.FontCD;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashMap;

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
            final HashMap<String, ConverterItem> components = getConverterComponents (rootNode);

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

    private static HashMap<String, ConverterItem> getConverterComponents (Node rootNode) {
        HashMap<String, ConverterItem> components = new HashMap<String, ConverterItem> ();
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
            components.put (item.getUID (), item);
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

    private static void convert (ArrayList<String> errors, HashMap<String, ConverterItem> components, DesignDocument document) {
        for (ConverterItem item : components.values ()) {
            if (item.isUsed ())
                return;
            if ("javax.microedition.lcdui.Font".equals (item.getTypeID ()))
                convertFont (item, document);
            if ("javax.microedition.lcdui.Command".equals (item.getTypeID ()))
                convertCommand (item, document);
            // TODO - all components
        }
    }

    private static Boolean getBoolean (String value) {
        if (value == null)
            return null;
        return "true".equalsIgnoreCase (value);
    }

    private static Integer getInteger (String value) {
        if (value == null)
            return null;
        try {
            return Integer.parseInt (value);
        } catch (NumberFormatException e) {
            // TODO - invalid value
            return null;
        }
    }

    private static PropertyValue getStringWithUserCode (String value) {
        if (value == null)
            return null;
        if (value.startsWith ("CODE:"))
            return PropertyValue.createUserCode (value.substring (5));
        if (value.startsWith ("STRING:"))
            return MidpTypes.createStringValue (value.substring (7));
        // TODO - invalid value
        return null;
    }

//    private static String getCodeValueClass (String value) {
//        assert value.startsWith ("CODE-");
//        return value.substring (5);
//    }

    private static void convertObject (ConverterItem item) {
        item.setUsed ();
    }

    private static void convertClass (ConverterItem item, DesignComponent component) {
        convertObject (item);
        component.writeProperty (ClassCD.PROP_INSTANCE_NAME, MidpTypes.createStringValue (item.getID ()));
        Boolean lazy = getBoolean (item.getPropertyValue ("lazyInitialized")); // NOI18N
        component.writeProperty (ClassCD.PROP_LAZY_INIT, MidpTypes.createBooleanValue (lazy == null  ||  lazy));
    }

    private static void convertFont (ConverterItem item, DesignDocument document) {
        DesignComponent font = document.createComponent (FontCD.TYPEID);
        convertClass (item, font);
        MidpDocumentSupport.getCategoryComponent (document, ResourcesCategoryCD.TYPEID).addComponent (font);

        // TODO - all properties
    }

    private static void convertCommand (ConverterItem item, DesignDocument document) {
        DesignComponent command = document.createComponent (CommandCD.TYPEID);
        convertClass (item, command);
        MidpDocumentSupport.getCategoryComponent (document, ResourcesCategoryCD.TYPEID).addComponent (command);

        PropertyValue label = getStringWithUserCode (item.getPropertyValue ("label")); // NOI18N
        if (label != null)
            command.writeProperty (CommandCD.PROP_LABEL, label);

        PropertyValue longLabel = getStringWithUserCode (item.getPropertyValue ("longLabel")); // NOI18N
        if (longLabel != null)
            command.writeProperty (CommandCD.PROP_LONG_LABEL, longLabel);

        Integer priority = getInteger (item.getPropertyValue ("priority")); // NOI18N
        if (priority != null)
            command.writeProperty (CommandCD.PROP_PRIORITY, MidpTypes.createIntegerValue (priority));

        String typeValue = item.getPropertyValue ("type"); // NOI18N
        int type;
        if ("SCREEN".equals (typeValue)) // NOI18N
            type = CommandCD.VALUE_SCREEN;
        else if ("BACK".equals (typeValue)) // NOI18N
            type = CommandCD.VALUE_BACK;
        else if ("CANCEL".equals (typeValue)) // NOI18N
            type = CommandCD.VALUE_CANCEL;
        else if ("OK".equals (typeValue)) // NOI18N
            type = CommandCD.VALUE_OK;
        else if ("HELP".equals (typeValue)) // NOI18N
            type = CommandCD.VALUE_HELP;
        else if ("STOP".equals (typeValue)) // NOI18N
            type = CommandCD.VALUE_STOP;
        else if ("EXIT".equals (typeValue)) // NOI18N
            type = CommandCD.VALUE_EXIT;
        else if ("ITEM".equals (typeValue)) // NOI18N
            type = CommandCD.VALUE_ITEM;
        else
            type = CommandCD.VALUE_OK;
        command.writeProperty (CommandCD.PROP_PRIORITY, MidpTypes.createIntegerValue (type));
    }

}
