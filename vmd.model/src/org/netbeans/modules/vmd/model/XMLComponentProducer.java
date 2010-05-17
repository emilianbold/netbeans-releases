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
 */package org.netbeans.modules.vmd.model;

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.openide.util.Lookup;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author David Kaspar
 */
public final class XMLComponentProducer extends ComponentProducer {

    private static final Lookup.Result<? extends ProducerDeserializer> result = Lookup.getDefault ().lookupResult (ProducerDeserializer.class);
    
    public static final String COMPONENT_PRODUCER_NODE = "ComponentProducer"; // NOI18N
    public static final String VERSION_ATTR = "version"; // NOI18N

    public static final String PRODUCERID_ATTR = "producerID"; // NOI18N
    public static final String MAIN_COMPONENT_TYPEID_ATTR = "mainComponentTypeID"; // NOI18N

    public static final String DISPLAY_NAME_ATTR = "displayName"; // NOI18N
    public static final String TOOLTIP_ATTR = "toolTip"; // NOI18N
    public static final String SMALL_ICON_ATTR = "smallIcon"; // NOI18N
    public static final String LARGE_ICON_ATTR = "largeIcon"; // NOI18N
    public static final String PREFERRED_CATEGORYID_ATTR = "preferredCategoryID"; // NOI18N

    public static final String VERSION_VALUE_1 = "1"; // NOI18N

    protected XMLComponentProducer (String producerID, TypeID typeID, PaletteDescriptor paletteDescriptor) {
        super (producerID, typeID, paletteDescriptor);
    }

    public static XMLComponentProducer deserialize (String projectType, Document document) {
        Node rootNode = document.getFirstChild ();

        if (! COMPONENT_PRODUCER_NODE.equals (rootNode.getNodeName ())) {
            Debug.warning ("Invalid root node"); // NOI18N
            return null;
        }

        String version = XMLComponentDescriptor.getAttributeValue (rootNode, VERSION_ATTR);
        if (! VERSION_VALUE_1.equals (version)) {
            Debug.warning ("Invalid version", version); // NOI18N
            return null;
        }

        String producerID = XMLComponentDescriptor.getAttributeValue (rootNode, PRODUCERID_ATTR);
        if (producerID == null) {
            Debug.warning ("Missing producerID attribute"); // NOI18N
            return null;
        }

        String typeID = XMLComponentDescriptor.getAttributeValue (rootNode, MAIN_COMPONENT_TYPEID_ATTR);
        if (typeID == null) {
            Debug.warning ("Missing mainComponentTypeID attribute"); // NOI18N
            return null;
        }

        PaletteDescriptor paletteDescriptor = new PaletteDescriptor (
                XMLComponentDescriptor.getAttributeValue (rootNode, PREFERRED_CATEGORYID_ATTR),
                XMLComponentDescriptor.getAttributeValue (rootNode, DISPLAY_NAME_ATTR),
                XMLComponentDescriptor.getAttributeValue (rootNode, TOOLTIP_ATTR),
                XMLComponentDescriptor.getAttributeValue (rootNode, SMALL_ICON_ATTR),
                XMLComponentDescriptor.getAttributeValue (rootNode, LARGE_ICON_ATTR)
        );

        return new XMLComponentProducer (producerID, TypeID.createFrom (typeID), paletteDescriptor);
    }

    @Override
    public Result postInitialize (DesignDocument document, DesignComponent mainComponent) {
        ComponentDescriptor descriptor = document.getDescriptorRegistry ().getComponentDescriptor (getMainComponentTypeID ());
        if (descriptor != null) {
            TypeID superTypeID = descriptor.getTypeDescriptor ().getSuperType ();
            if (superTypeID != null) {
                ComponentProducer producer = DocumentSupport.getComponentProducer (document, superTypeID.toString ());
                if (producer != null)
                    producer.postInitialize (document, mainComponent);
            }
        }
        return super.postInitialize (document, mainComponent);
    }

    public Boolean checkValidity (final DesignDocument document, boolean useCachedValue) {
        if (! checkValidityByDeserializers (document))
            return false;

        final ComponentProducer[] producers = new ComponentProducer[1];
        document.getTransactionManager ().readAccess (new Runnable() {
            public void run () {
                ComponentDescriptor descriptor = document.getDescriptorRegistry ().getComponentDescriptor (getMainComponentTypeID ());
                if (descriptor != null) {
                    TypeID superTypeID = descriptor.getTypeDescriptor ().getSuperType ();
                    if (superTypeID != null)
                        producers[0] = DocumentSupport.getComponentProducer (document, superTypeID.toString ());
                }
            }
        });
        
        if (producers[0] == null) {
            return true;
        }
        
        return producers[0].checkValidity (document, useCachedValue);
    }

    private boolean checkValidityByDeserializers (DesignDocument document) {
        String projectType = document.getDocumentInterface ().getProjectType ();
        for (ProducerDeserializer deserializer : result.allInstances ())
            if (projectType.equals (deserializer.getProjectType ()))
                if (! deserializer.checkValidity (document, this))
                    return false;
        return true;
    }

}
