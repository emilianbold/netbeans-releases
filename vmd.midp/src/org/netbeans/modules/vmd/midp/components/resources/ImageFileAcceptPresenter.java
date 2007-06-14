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
package org.netbeans.modules.vmd.midp.components.resources;

import java.awt.datatransfer.Transferable;
import org.netbeans.modules.vmd.api.model.ComponentProducer.Result;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.common.AcceptSuggestion;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.categories.ResourcesCategoryCD;
import org.netbeans.modules.vmd.midp.general.FileAcceptPresenter;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Karol Harezlak
 */
public class ImageFileAcceptPresenter extends FileAcceptPresenter {
    
    public ImageFileAcceptPresenter(String propertyName, TypeID typeID, String... fileExtensions) {
        super(propertyName, typeID, fileExtensions);
    }
    
    public Result accept (Transferable transferable, AcceptSuggestion suggestion) {
        Result result = super.accept(transferable, suggestion);
        DesignComponent image = result.getComponents().iterator().next();
        FileObject fileObject = super.getNodeFile(transferable);
        if (fileObject == null)
            return result;
        String path = getFileClasspath(fileObject);
        image.writeProperty(ImageCD.PROP_RESOURCE_PATH , MidpTypes.createStringValue(path));
        MidpDocumentSupport.getCategoryComponent(getComponent().getDocument(), ResourcesCategoryCD.TYPEID).addComponent(image);
        return result;
    }
    
}