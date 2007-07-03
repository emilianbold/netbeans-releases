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

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.ComponentProducer.Result;
import org.netbeans.modules.vmd.api.model.common.AcceptSuggestion;
import org.netbeans.modules.vmd.api.model.common.DesignComponentDataFlavorSupport;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpAcceptTrensferableKindPresenter;
import org.netbeans.modules.vmd.midp.components.MidpArraySupport;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.categories.ResourcesCategoryCD;
import org.netbeans.modules.vmd.midp.components.items.ImageItemCD;
import org.netbeans.modules.vmd.midp.components.resources.ImageCD;
import org.netbeans.modules.vmd.midp.general.FileAcceptPresenter;
import org.openide.filesystems.FileObject;

import java.awt.datatransfer.Transferable;

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
    /*
    public static Presenter createItemAtLastIndexAcceptPresenter() {
        return new ItemFormAcceptPresenter();
    }
    */
    private static class FormFileAcceptPresenter extends FileAcceptPresenter {
        
        private FormFileAcceptPresenter(String... fileExtensions) {
            super(FormCD.PROP_ITEMS, ImageItemCD.TYPEID, fileExtensions);
        }
        
        @Override
        public Result accept(Transferable transferable, AcceptSuggestion suggestion) {
            DesignDocument document = getComponent().getDocument();
            Result result = super.accept(transferable, suggestion);
            DesignComponent component = result.getComponents().iterator().next();
            ComponentProducer ip = DocumentSupport.getComponentProducer(getComponent().getDocument(), ImageCD.TYPEID.toString ());
            if (ip == null)
                return super.accept(transferable, suggestion);
            DesignComponent image = ip.createComponent(document).getMainComponent ();
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
        
        @Override
        public Result accept(Transferable transferable, AcceptSuggestion suggestion) {
            DesignDocument document = getComponent().getDocument();
            DesignComponent image = DesignComponentDataFlavorSupport.getTransferableDesignComponent(transferable);
            ComponentProducer ip = DocumentSupport.getComponentProducer(getComponent().getDocument(), ImageItemCD.TYPEID.toString ());
            if (ip == null)
                return super.accept(transferable, suggestion);
            DesignComponent imageItem = ip.createComponent(document).getComponents().iterator().next();
            imageItem.writeProperty(ImageItemCD.PROP_IMAGE,PropertyValue.createComponentReference(image));
            MidpArraySupport.append(getComponent(), FormCD.PROP_ITEMS, imageItem);
            getComponent().addComponent(imageItem);
            return new Result(imageItem);
        }
    }
    /*
    private static class ItemFormAcceptPresenter extends AcceptPresenter {

        private ItemFormAcceptPresenter() {
            super(AcceptPresenter.Kind.TRANSFERABLE);
        }
        
        @Override
        public boolean isAcceptable(Transferable transferable, AcceptSuggestion suggestion) {
            if (getComponent().getDocument().getSelectedComponents().size() > 1)
                return false;
            DesignComponent component = DesignComponentDataFlavorSupport.getTransferableDesignComponent(transferable);
            List<PropertyValue> array = getComponent().readProperty(FormCD.PROP_ITEMS).getArray();
            for (PropertyValue value : array) {
                if (value.getComponent() == component)
                    return true;
            }
            return false;
        }
        
        @Override
        public Result accept(Transferable transferable, AcceptSuggestion suggestion) {
            DesignComponent component = DesignComponentDataFlavorSupport.getTransferableDesignComponent(transferable);
            List<PropertyValue> array = getComponent().readProperty(FormCD.PROP_ITEMS).getArray();
            List<PropertyValue> newArray = new ArrayList<PropertyValue>(array);
            for (PropertyValue value : array) {
                if (value.getComponent() == component)
                    newArray.remove(value);
            }
            newArray.add(PropertyValue.createComponentReference(component));
            getComponent().writeProperty(FormCD.PROP_ITEMS, PropertyValue.createArray(ItemCD.TYPEID , newArray));        
            return super.accept(transferable, suggestion);
        }
    }
    */
}
