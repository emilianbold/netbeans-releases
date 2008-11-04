/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 */package org.netbeans.modules.vmd.midpnb.components;

import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.serialization.ComponentElement;
import org.netbeans.modules.vmd.api.io.serialization.DocumentSerializationController;
import org.netbeans.modules.vmd.api.io.serialization.PropertyElement;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSerializationController;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGAnimatorWrapperCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGPlayerCD;
import org.netbeans.modules.vmd.midpnb.components.displayables.FileBrowserCD;
import org.netbeans.modules.vmd.midpnb.components.displayables.PIMBrowserCD;
import org.netbeans.modules.vmd.midpnb.components.displayables.SMSComposerCD;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.vmd.api.io.serialization.DocumentErrorHandler;

/**
 * @author David Kaspar
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.vmd.api.io.serialization.DocumentSerializationController.class)
public class MidpCustomDocumentSerializationController extends DocumentSerializationController {

    private static TypeID TYPEID_OLD_FILE_BROWSER = new TypeID (TypeID.Kind.COMPONENT, "org.netbeans.microedition.lcdui.FileBrowser"); // NOI18N
    private static TypeID TYPEID_OLD_PIM_BROWSER = new TypeID (TypeID.Kind.COMPONENT, "org.netbeans.microedition.lcdui.PIMBrowser"); // NOI18N
    private static TypeID TYPEID_OLD_SMS_COMPOSER = new TypeID (TypeID.Kind.COMPONENT, "org.netbeans.microedition.lcdui.SMSComposer"); // NOI18N

    public void approveComponents (DataObjectContext context, DesignDocument loadingDocument, String documentVersion, Collection<ComponentElement> componentElements, DocumentErrorHandler errorHandler) {
        if (! MidpDocumentSupport.PROJECT_TYPE_MIDP.equals (context.getProjectType ())  ||  ! MidpDocumentSerializationController.VERSION_1.equals (documentVersion))
            return;
        ArrayList<ComponentElement> elementsToRemove = new ArrayList<ComponentElement> ();
        ArrayList<ComponentElement> elementsToAdd = new ArrayList<ComponentElement> ();
        for (ComponentElement element : componentElements) {
            if (SVGAnimatorWrapperCD.TYPEID.equals (element.getTypeID ())) {
                elementsToRemove.add (element);
                elementsToAdd.add (ComponentElement.create (element.getParentUID (), element.getUID (), SVGPlayerCD.TYPEID, element.getNode ()));
            } else if (TYPEID_OLD_FILE_BROWSER.equals (element.getTypeID ())) {
                elementsToRemove.add (element);
                elementsToAdd.add (ComponentElement.create (element.getParentUID (), element.getUID (), FileBrowserCD.TYPEID, element.getNode ()));
            } else if (TYPEID_OLD_PIM_BROWSER.equals (element.getTypeID ())) {
                elementsToRemove.add (element);
                elementsToAdd.add (ComponentElement.create (element.getParentUID (), element.getUID (), PIMBrowserCD.TYPEID, element.getNode ()));
            } else if (TYPEID_OLD_SMS_COMPOSER.equals (element.getTypeID ())) {
                elementsToRemove.add (element);
                elementsToAdd.add (ComponentElement.create (element.getParentUID (), element.getUID (), SMSComposerCD.TYPEID, element.getNode ()));
            }
        }
        componentElements.removeAll (elementsToRemove);
        componentElements.addAll (elementsToAdd);
    }

    public void approveProperties (DataObjectContext context, DesignDocument loadingDocument, String documentVersion, DesignComponent component, Collection<PropertyElement> propertyElements, DocumentErrorHandler errorHandler) {
        if (! MidpDocumentSupport.PROJECT_TYPE_MIDP.equals (context.getProjectType ())  ||  ! MidpDocumentSerializationController.VERSION_1.equals (documentVersion))
            return;
        if (loadingDocument.getDescriptorRegistry ().isInHierarchy (SVGPlayerCD.TYPEID, component.getType ())) {
            ArrayList<PropertyElement> elementsToRemove = new ArrayList<PropertyElement> ();
            ArrayList<PropertyElement> elementsToAdd = new ArrayList<PropertyElement> ();
            for (PropertyElement propertyElement : propertyElements) {
                if (SVGPlayerCD.PROP_OLD_START_ANIM_IMMEDIATELY.equals (propertyElement.getPropertyName ())) {
                    elementsToRemove.add (propertyElement);
                    elementsToAdd.add (PropertyElement.create (SVGPlayerCD.PROP_START_ANIM_IMMEDIATELY, propertyElement.getTypeID (), propertyElement.getSerialized ()));
                    break;
                }
            }
            propertyElements.removeAll (elementsToRemove);
            propertyElements.addAll (elementsToAdd);
        }
    }

    public void postValidateDocument (DataObjectContext context, DesignDocument loadingDocument, String documentVersion, DocumentErrorHandler errorHandler) {
    }

}
