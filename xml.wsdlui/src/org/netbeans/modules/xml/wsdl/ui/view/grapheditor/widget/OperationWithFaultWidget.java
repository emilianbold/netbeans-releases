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

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.openide.util.Lookup;

/**
 *
 * @author rico
 */
public abstract class OperationWithFaultWidget<T extends Operation> extends OperationWidget<T> {
    private List<Fault> currentFaults;
    private List<Fault> deletedFaults;
    private List<Fault> newFaults;
    private Widget dummyWidget;
    
    /** Creates a new instance of OperationWithFaultWidget */
    public OperationWithFaultWidget(Scene scene, T operation, Lookup lookup) {
        super(scene, operation, lookup);
        deletedFaults = new ArrayList();
        currentFaults = new ArrayList(getWSDLComponent().getFaults());
        dummyWidget = new Widget(scene);
        dummyWidget.setMinimumSize(new Dimension(5, 10));
    }
    
    @Override
    public void setRightSided(boolean rightSided) {
        super.setRightSided(rightSided);
        init();
        populateFaults(getVerticalWidget());
    }

    protected abstract Widget getVerticalWidget();
    protected abstract void init();
    
    @Override
    public void updateContent() {
        super.updateContent();
        refreshFaults(getVerticalWidget());
    }
    
    private void populateFaults(Widget verticalWidget){
        if(currentFaults.size() > 0 ){
            for(Fault fault : currentFaults){
                Widget faultWidget = WidgetFactory.getInstance().createWidget(getScene(), fault, getLookup(), true);
                if (faultWidget.getParentWidget() != null && faultWidget.getParentWidget() != this) {
                    faultWidget = WidgetFactory.getInstance().createWidget(getScene(), fault, getLookup());
                }
                verticalWidget.addChild(faultWidget);
            }
        }
        verticalWidget.addChild(dummyWidget);
    }
    
    private void refreshFaults(Widget verticalWidget){
        deletedFaults.clear();
        deletedFaults.addAll(currentFaults);
        newFaults = new ArrayList(getWSDLComponent().getFaults());
        
        //remove deleted faults
        currentFaults.retainAll(newFaults);
        //get new faults
        newFaults.removeAll(currentFaults);
        //add new faults
        currentFaults.addAll(newFaults);
        //get deleted faults
        deletedFaults.removeAll(currentFaults);
        if(newFaults.size() > 0){
            verticalWidget.removeChild(dummyWidget);
            for(Fault fault : newFaults){
                Widget faultWidget = WidgetFactory.getInstance().createWidget(getScene(), fault, getLookup(), true);
                if (faultWidget.getParentWidget() != null && faultWidget.getParentWidget() != this) {
                    faultWidget = WidgetFactory.getInstance().createWidget(getScene(), fault, getLookup());
                }
                 verticalWidget.addChild(faultWidget); //adjust for dummy widget. add the fault before dummy widget.
            }
            verticalWidget.addChild(dummyWidget);
            getScene().validate();
        }
        if(deletedFaults.size() > 0){
            List<Widget> children = new ArrayList(verticalWidget.getChildren());
            for(Fault fault : deletedFaults){
                for(Widget child : children){
                    if(child instanceof FaultWidget){
                        FaultWidget fw = (FaultWidget)child;
                        if(fw.getWSDLComponent() == fault){
                            verticalWidget.removeChild(child);
                        }
                    }
                }
            }
            getScene().validate();
        }
    }
}
