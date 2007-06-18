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
package org.netbeans.modules.vmd.midp.components.displayables;

import java.awt.datatransfer.Transferable;
import java.util.Collection;
import org.netbeans.modules.vmd.api.model.ComponentProducer;
import org.netbeans.modules.vmd.api.model.ComponentProducer.Result;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.model.common.AcceptSuggestion;
import org.netbeans.modules.vmd.api.model.common.DesignComponentDataFlavorSupport;
import org.netbeans.modules.vmd.midp.components.MidpAcceptTrensferableKindPresenter;
import org.netbeans.modules.vmd.midp.components.MidpArraySupport;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.categories.ResourcesCategoryCD;
import org.netbeans.modules.vmd.midp.components.items.ImageItemCD;
import org.netbeans.modules.vmd.midp.components.resources.ImageCD;
import org.netbeans.modules.vmd.midp.general.FileAcceptPresenter;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Karol Harezlak
 */

public final class FormAcceptPresenterSupport {
    
    public static Presenter createFileAcceptPresenter(String... fileExtensions) {
        return new FormFileAcceptPresenter(fileExtensions);
    }
    
    public static Presenter createImageAcceptPresenter() {
        return new FormTypeAccepter().addType(ImageCD.TYPEID, ImageItemCD.PROP_IMAGE);
    }
    
    private static class FormFileAcceptPresenter extends FileAcceptPresenter {
        
        private FormFileAcceptPresenter(String... fileExtensions) {
            super(FormCD.PROP_ITEMS, ImageItemCD.TYPEID, fileExtensions);
        }
        
        public Result accept(Transferable transferable, AcceptSuggestion suggestion) {
            DesignDocument document = getComponent().getDocument();
            Result result = super.accept(transferable, suggestion);
            DesignComponent component = result.getComponents().iterator().next();
            Collection<ComponentProducer> ips = DocumentSupport.getComponentProducers(getComponent().getDocument(), ImageCD.TYPEID);
            if (ips.isEmpty())
                return super.accept(transferable, suggestion);
            DesignComponent image = ips.iterator().next().createComponent(document).getComponents().iterator().next();
            component.writeProperty(ImageItemCD.PROP_IMAGE, PropertyValue.createComponentReference(image));
            FileObject fileObject = getNodeFileObject(transferable);
            String path = getFileClasspath(fileObject);
            image.writeProperty(ImageCD.PROP_RESOURCE_PATH, MidpTypes.createStringValue(path));
            MidpDocumentSupport.getCategoryComponent(document, ResourcesCategoryCD.TYPEID).addComponent(image);
            getComponent().addComponent(component);
            return new Result(component);
        }
    }
    
    private static class FormTypeAccepter extends MidpAcceptTrensferableKindPresenter {
        
        public Result accept(Transferable transferable, AcceptSuggestion suggestion) {
            DesignDocument document = getComponent().getDocument();
            DesignComponent image = DesignComponentDataFlavorSupport.getTransferableDesignComponent(transferable);
            Collection<ComponentProducer> ips = DocumentSupport.getComponentProducers(getComponent().getDocument(), ImageItemCD.TYPEID);
            if (ips.isEmpty())
                return super.accept(transferable, suggestion);
            DesignComponent imageItem = ips.iterator().next().createComponent(document).getComponents().iterator().next();
            imageItem.writeProperty(ImageItemCD.PROP_IMAGE,PropertyValue.createComponentReference(image));
            MidpArraySupport.append(getComponent(), FormCD.PROP_ITEMS, imageItem);
            getComponent().addComponent(imageItem);
            return new Result(imageItem);
        }
    }
    
}
