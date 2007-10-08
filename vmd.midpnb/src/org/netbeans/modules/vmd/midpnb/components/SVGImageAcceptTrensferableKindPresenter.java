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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.vmd.midpnb.components;

import java.awt.datatransfer.Transferable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.modules.vmd.api.model.ComponentProducer;
import org.netbeans.modules.vmd.api.model.ComponentProducer.Result;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.common.AcceptSuggestion;
import org.netbeans.modules.vmd.api.model.common.DesignComponentDataFlavorSupport;
import org.netbeans.modules.vmd.midp.components.MidpAcceptTrensferableKindPresenter;
import org.netbeans.modules.vmd.midp.components.MidpProjectSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGImageCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGMenuCD;
import org.netbeans.modules.vmd.midpnb.components.svg.util.SVGUtils;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Anton Chechel
 */
public class SVGImageAcceptTrensferableKindPresenter extends MidpAcceptTrensferableKindPresenter {

    @Override
    public Result accept(Transferable transferable, AcceptSuggestion suggestion) {
        DesignComponent component = DesignComponentDataFlavorSupport.getTransferableDesignComponent(transferable);
        String propertyName = typesMap.get(component.getType());
        if (propertyName == null) {
            throw new IllegalStateException();
        }

        DesignComponent svgPlayer = getComponent();
        svgPlayer.writeProperty(propertyName, PropertyValue.createComponentReference(component));

        if (svgPlayer.getDocument().getDescriptorRegistry().isInHierarchy(SVGMenuCD.TYPEID, svgPlayer.getType()) && svgPlayer.readProperty(SVGMenuCD.PROP_ELEMENTS).getArray().size() == 0) {

            PropertyValue propertyValue = component.readProperty(SVGImageCD.PROP_RESOURCE_PATH);
            if (propertyValue.getKind() == PropertyValue.Kind.VALUE) {
                Map<FileObject, FileObject> images = MidpProjectSupport.getFileObjectsForRelativeResourcePath(svgPlayer.getDocument(), MidpTypes.getString(propertyValue));
                Iterator<FileObject> iterator = images.keySet().iterator();
                if (iterator.hasNext()) {
                    FileObject svgImageFileObject = iterator.next();
                    
                    InputStream inputStream = null;
                    try {
                        inputStream = svgImageFileObject.getInputStream();
                        if (inputStream != null) {
                            SVGUtils.parseSVGMenu(inputStream, svgPlayer);
                        }
                    } catch (FileNotFoundException ex) {
                        Debug.warning(ex);
                    } finally {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException ioe) {
                                Debug.warning(ioe);
                            }
                        }
                    }
                }
            }
        }

        return new ComponentProducer.Result(component);
    }
}