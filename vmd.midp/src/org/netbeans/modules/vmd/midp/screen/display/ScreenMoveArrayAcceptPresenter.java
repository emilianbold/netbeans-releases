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
package org.netbeans.modules.vmd.midp.screen.display;

import org.netbeans.modules.vmd.midp.screen.*;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.vmd.api.model.ComponentProducer.Result;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.common.AcceptPresenter;
import org.netbeans.modules.vmd.api.model.common.DesignComponentDataFlavorSupport;
import org.netbeans.modules.vmd.api.model.common.AcceptSuggestion;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;

/**
 *
 * @author Karol Harezlak
 */
public class ScreenMoveArrayAcceptPresenter extends AcceptPresenter {
    
    private String arrayPropertyName;
    private TypeID arrayType;
    
    public ScreenMoveArrayAcceptPresenter(String arrayPropertyName, TypeID arrayType) {
        super(AcceptPresenter.Kind.TRANSFERABLE);
        this.arrayPropertyName = arrayPropertyName;
        this.arrayType = arrayType;
    }
    
    @Override
    public boolean isAcceptable(Transferable transferable, AcceptSuggestion suggestion) {
        if (!(transferable.isDataFlavorSupported(DesignComponentDataFlavorSupport.DESIGN_COMPONENT_DATA_FLAVOR))) 
            return false;
        
        if (getComponent().getDocument().getSelectedComponents().size() >1)
            return false;
        
        DesignComponent componentTrans = DesignComponentDataFlavorSupport.getTransferableDesignComponent(transferable);
        if (!(suggestion instanceof ScreenMoveArrayAcceptSuggestion)) 
            return false;
        
        if (componentTrans.getParentComponent() == getComponent().getParentComponent()) 
            return true;
        return false;
    }
    
    @Override
    public Result accept(Transferable transferable, AcceptSuggestion suggestion) {
        DesignComponent componentTrans = DesignComponentDataFlavorSupport.getTransferableDesignComponent(transferable);
        DesignComponent parentComponent = getComponent().getParentComponent();
        
        List<PropertyValue> array = parentComponent.readProperty(arrayPropertyName).getArray();
        if (array == null || array.size() < 1)
            return super.accept(transferable, suggestion);
        
        List<PropertyValue> newArray = new ArrayList<PropertyValue>(array);
        PropertyValue movedValue = null;
        int componentTransIndex = -1;
        int componentIndex = -1;
        for (PropertyValue value : array) {
            if (value.getComponent().equals(componentTrans)) {
                movedValue = value;
                componentTransIndex = array.indexOf(value);
            }
            if (value.getComponent().equals(getComponent()))
                componentIndex = array.indexOf(value);
        }
        
        ScreenDeviceInfo.Edge verticalPosition = ((ScreenMoveArrayAcceptSuggestion) suggestion ).getVerticalPosition();
        newArray.remove(componentTransIndex);
        if (verticalPosition == ScreenDeviceInfo.Edge.TOP && (componentTransIndex +1) != componentIndex)
            newArray.add(componentIndex, movedValue);
        else if (verticalPosition == ScreenDeviceInfo.Edge.BOTTOM) {
            if (componentTransIndex > componentIndex)
                newArray.add(componentIndex + 1 , movedValue);
            else
                newArray.add(componentIndex, movedValue);
        } else
            return super.accept(transferable, suggestion);
        
        DesignComponent dragComponent = DesignComponentDataFlavorSupport.getTransferableDesignComponent(transferable);
        if (dragComponent == null)
            return super.accept(transferable, suggestion);
        parentComponent.writeProperty(arrayPropertyName, PropertyValue.createArray(arrayType, newArray));
        return new Result(dragComponent);
    }
    
}
