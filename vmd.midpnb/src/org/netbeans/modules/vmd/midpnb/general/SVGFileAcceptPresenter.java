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

package org.netbeans.modules.vmd.midpnb.general;

import org.netbeans.modules.vmd.api.model.ComponentProducer.Result;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.common.AcceptSuggestion;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.categories.ResourcesCategoryCD;
import org.netbeans.modules.vmd.midp.general.FileAcceptPresenter;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGImageCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGMenuCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGPlayerCD;
import org.netbeans.modules.vmd.midpnb.components.svg.util.SVGUtils;
import org.openide.filesystems.FileObject;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.io.InputStream;
import org.netbeans.modules.vmd.api.model.Debug;

/**
 *
 * @author Anton Chechel
 */
public class SVGFileAcceptPresenter extends FileAcceptPresenter {

    public SVGFileAcceptPresenter() {
        super(SVGPlayerCD.PROP_SVG_IMAGE, SVGImageCD.TYPEID, "svg"); // NOI18N
    }

    @Override
    public Result accept(Transferable transferable, AcceptSuggestion suggestion) {
        Result result = super.accept(transferable, suggestion);
        DesignComponent svgImage = result.getComponents().iterator().next();
        DesignComponent svgComponent = getComponent();
        FileObject fileObject = getNodeFileObject(transferable);
        if (fileObject == null) {
            return result;
        }

        String path = getFileClasspath(fileObject);
        svgImage.writeProperty(SVGImageCD.PROP_RESOURCE_PATH, MidpTypes.createStringValue(path));
        MidpDocumentSupport.getCategoryComponent(svgComponent.getDocument(), ResourcesCategoryCD.TYPEID).addComponent(svgImage);

        if (svgComponent.getDocument().getDescriptorRegistry().isInHierarchy(SVGMenuCD.TYPEID, svgComponent.getType()) &&
                        svgComponent.readProperty (SVGMenuCD.PROP_ELEMENTS).getArray().size() == 0) {
            parseSVGMenuItems(transferable, svgComponent);
        }

        return result;
    }

    private void parseSVGMenuItems(Transferable transferable, final DesignComponent svgMenuComponent) {
        InputStream inputStream = null;
        try {
            inputStream = getInputStream(transferable);
            if (inputStream != null) {
                SVGUtils.parseSVGMenu(inputStream, svgMenuComponent);
            }
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