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
package org.netbeans.modules.vmd.midpnb.components;

import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.serialization.ComponentElement;
import org.netbeans.modules.vmd.api.io.serialization.DocumentSerializationController;
import org.netbeans.modules.vmd.api.io.serialization.PropertyElement;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGAnimatorWrapperCD;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author David Kaspar
 */
public class MidpCustomDocumentSerializationController extends DocumentSerializationController {

    public void approveComponents (DataObjectContext context, DesignDocument loadingDocument, Collection<ComponentElement> componentElements) {
    }

    public void approveProperties (DataObjectContext context, DesignDocument loadingDocument, DesignComponent component, Collection<PropertyElement> propertyElements) {
        if (! MidpDocumentSupport.PROJECT_TYPE_MIDP.equals (context.getProjectType ()))
            return;
        if (loadingDocument.getDescriptorRegistry ().isInHierarchy (SVGAnimatorWrapperCD.TYPEID, component.getType ())) {
            Iterator<PropertyElement> iterator = propertyElements.iterator ();
            while (iterator.hasNext ()) {
                PropertyElement propertyElement = iterator.next ();
                if (SVGAnimatorWrapperCD.PROP_OLD_START_ANIM_IMMEDIATELY.equals (propertyElement.getPropertyName ())) {
                    iterator.remove ();
                    propertyElements.add (PropertyElement.create (SVGAnimatorWrapperCD.PROP_START_ANIM_IMMEDIATELY, propertyElement.getTypeID (), propertyElement.getSerialized ()));
                    break;
                }
            }
        }
    }

}
