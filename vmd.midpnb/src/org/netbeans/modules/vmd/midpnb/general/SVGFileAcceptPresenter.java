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

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.vmd.api.model.ComponentProducer.Result;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.common.AcceptSuggestion;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.categories.ResourcesCategoryCD;
import org.netbeans.modules.vmd.midp.general.FileAcceptPresenter;
import org.netbeans.modules.vmd.midpnb.components.sources.SVGMenuElementEventSourceCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGAnimatorWrapperCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGImageCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGMenuCD;
import org.netbeans.modules.vmd.midpnb.components.svg.util.SVGUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Anton Chechel
 */
public class SVGFileAcceptPresenter extends FileAcceptPresenter {
    
    public SVGFileAcceptPresenter() {
        super(SVGAnimatorWrapperCD.PROP_SVG_IMAGE, SVGImageCD.TYPEID, "svg");
    }
    
    public Result accept (Transferable transferable, AcceptSuggestion suggestion) {
        Result result = super.accept(transferable, suggestion);
        DesignComponent svgImage = result.getComponents().iterator().next();
        final DesignComponent animator = getComponent();
        FileObject fileObject = getNodeFile(transferable);
        svgImage.writeProperty(SVGImageCD.PROP_RESOURCE_PATH , MidpTypes.createStringValue(fileObject.getPath()));
        MidpDocumentSupport.getCategoryComponent(animator.getDocument(), ResourcesCategoryCD.TYPEID).addComponent(svgImage);
        
        if (animator.getType() == SVGMenuCD.TYPEID) {
            InputStream inputStream = null;
            try {
                inputStream = getInputStream(transferable);
                if (inputStream != null) {
                    final String[] menuItems = SVGUtils.getMenuItems(inputStream);
                    if (menuItems != null) {
                        animator.getDocument().getTransactionManager().writeAccess( new Runnable() {
                            public void run() {
                                List<PropertyValue> list = new ArrayList<PropertyValue>(menuItems.length);
                                
                                for (String str : menuItems) {
                                    DesignComponent es = animator.getDocument().createComponent(SVGMenuElementEventSourceCD.TYPEID);
                                    es.writeProperty(SVGMenuElementEventSourceCD.PROP_STRING, MidpTypes.createStringValue(str));
                                    list.add(PropertyValue.createComponentReference(es));
                                    animator.addComponent(es);
                                }
                                animator.writeProperty(SVGMenuCD.PROP_ELEMENTS, PropertyValue.createArray(SVGMenuElementEventSourceCD.TYPEID, list));
                            }
                        });
                    }
                }
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                }
            }
        }
        
        return result;
    }
}
