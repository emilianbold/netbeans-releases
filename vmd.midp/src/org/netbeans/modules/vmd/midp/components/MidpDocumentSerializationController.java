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
package org.netbeans.modules.vmd.midp.components;

import org.netbeans.modules.vmd.api.io.serialization.DocumentSerializationController;
import org.netbeans.modules.vmd.api.io.serialization.ComponentElement;
import org.netbeans.modules.vmd.api.io.serialization.PropertyElement;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.midp.components.items.ItemCD;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author David Kaspar
 */
public class MidpDocumentSerializationController extends DocumentSerializationController {

    public static final String VERSION_1 = "1";

    public void approveComponents (DataObjectContext context, DesignDocument loadingDocument, String documentVersion, Collection<ComponentElement> componentElements) {
        if (! MidpDocumentSupport.PROJECT_TYPE_MIDP.equals (context.getProjectType ())  ||  ! VERSION_1.equals (documentVersion))
            return;
    }

    public void approveProperties (DataObjectContext context, DesignDocument loadingDocument, String documentVersion, DesignComponent component, Collection<PropertyElement> propertyElements) {
        if (! MidpDocumentSupport.PROJECT_TYPE_MIDP.equals (context.getProjectType ())  ||  ! VERSION_1.equals (documentVersion))
            return;
        if (loadingDocument.getDescriptorRegistry ().isInHierarchy (ItemCD.TYPEID, component.getType ())) {
            Iterator<PropertyElement> iterator = propertyElements.iterator ();
            while (iterator.hasNext ()) {
                PropertyElement propertyElement = iterator.next ();
                if (ItemCD.PROP_OLD_ITEM_COMMAND_LISTENER.equals (propertyElement.getPropertyName ())) {
                    iterator.remove ();
                    propertyElements.add (PropertyElement.create (ItemCD.PROP_ITEM_COMMAND_LISTENER, propertyElement.getTypeID (), propertyElement.getSerialized ()));
                    break;
                }
            }
        }
    }

}
