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
