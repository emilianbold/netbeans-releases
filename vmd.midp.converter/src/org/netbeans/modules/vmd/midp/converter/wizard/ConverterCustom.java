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

import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.netbeans.modules.vmd.api.model.DescriptorRegistry;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.palette.wizard.ComponentInstaller;
import org.openide.util.Utilities;

import java.util.*;

/**
 * @author David Kaspar
 */
public class ConverterCustom {

    static void loadItemsToRegistry (final List<ConverterItem> items, DesignDocument document) {
        final DescriptorRegistry registry = document.getDescriptorRegistry ();
        final Collection<String> unresolved = new HashSet<String> ();
        registry.readAccess (new Runnable() {
            public void run () {
                for (ConverterItem item : items) {
                    if (! isClassComponent (item))
                        continue;
                    String string = item.getTypeID ();
                    if (registry.getComponentDescriptor (new TypeID (TypeID.Kind.COMPONENT, string)) == null)
                        unresolved.add (string);
                }
            }
        });
        if (! unresolved.isEmpty ()) {
            Map<String, ComponentInstaller.Item> found = ComponentInstaller.search (ProjectUtils.getProject (document));
            ArrayList<ComponentInstaller.Item> install = new ArrayList<ComponentInstaller.Item> ();
            for (String s : unresolved) {
                ComponentInstaller.Item item = found.get (s);
                if (item != null)
                    install.add (item);
            }
            ComponentInstaller.install (found, install);
        }
    }

    static boolean isClassComponent (ConverterItem item) {
        return Utilities.isJavaIdentifier (item.getID ())  &&  MidpTypes.isValidFQNClassName (item.getTypeID ());
    }


    static void convertCustom (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
//        TypeID typeID = new TypeID (TypeID.Kind.COMPONENT, item.getTypeID ());
//        document.createComponent (typeID);
//
//        DescriptorRegistry registry = document.getDescriptorRegistry ();
//        ComponentDescriptor descriptor = registry.getComponentDescriptor (typeID);
//        ComponentDescriptor desc = descriptor;
//        while (desc != null) {
//            if (convertCustomParent (desc.getTypeDescriptor ().getThisType (), id2item, item, document)) {
//                convertCustomProperties (id2item, item, document);
////                 TODO
//            }
//            desc = desc.getSuperDescriptor ();
//        }
    }

//    private static boolean convertCustomParent (TypeID parentTypeID, HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
//        if (DisplayableCD.TYPEID.equals (parentTypeID)) {
//            ConverterDisplayables.convertDisplayable ();
//        }
//        return false; // TODO
//    }

}
