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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.vmd.api.model.presenters.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.openide.util.NbBundle;


/**
 *
 * @author Karol Harezlak
 */
public abstract class MoveAction extends AbstractAction implements ActionContext {

    public static final String DISPLAY_NAME_MOVE_UP = NbBundle.getMessage(MoveAction.class, "NAME_MoveUpAction"); //NOI18N
    public static final String DISPLAY_NAME_MOVE_DOWN = NbBundle.getMessage(MoveAction.class, "NAME_MoveDownAction"); //NOI18N
    public static final String PROPERTY_NAME_REFERENCE  = "propertyName"; //NOI18N

    public static final Action createMoveUpAction(String arrayPropertyName) {
        return new MoveAction(arrayPropertyName, DISPLAY_NAME_MOVE_UP) {
            protected void invokeMoveAction(List<PropertyValue> array, List<PropertyValue> newArray, PropertyValue currentValue) {
                int index = array.indexOf(currentValue);
                if (index != 0) {
                    newArray.remove(currentValue);
                    newArray.add(index - 1, currentValue);
                    saveToModel(newArray);
                }
            }
        };
    }

    public static final Action createMoveDownAction(String arrayPropertyName) {
        return new MoveAction( arrayPropertyName, DISPLAY_NAME_MOVE_DOWN) {
            protected void invokeMoveAction(List<PropertyValue> array, List<PropertyValue> newArray, PropertyValue currentValue) {
                int index = array.indexOf(currentValue);
                int minIndex = array.size() - index;
                if (minIndex > 1) {
                    newArray.remove(currentValue);
                    newArray.add(index + 1, currentValue);
                    saveToModel(newArray);
                }
            }
        };
    }

    private DesignComponent component;
    private boolean isEnabled;
    private String arrayPropertyName;
    private TypeID newArrayTypeID;

    private MoveAction(String arrayPropertyName, String displayName) {
        this.putValue(Action.NAME, displayName);
        this.arrayPropertyName = arrayPropertyName;
    }

    protected DesignComponent getComponent() {
        return component;
    }

    public boolean isEnabled() {
        component.getDocument().getTransactionManager().readAccess(new Runnable() {
            public void run() {
                if (component.getDocument().getSelectedComponents().size() > 1)
                    isEnabled =  false;
                else
                    isEnabled = true;
            }
        });

        return isEnabled;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        getComponent().getDocument().getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                PropertyValue arrayPropertyValue = getComponent().getParentComponent().readProperty(getArrayPropertyName());
                List<PropertyValue> array = arrayPropertyValue.getArray();
                newArrayTypeID = arrayPropertyValue.getType().getComponentType();
                if(array.size() < 1)
                    return;
                List<PropertyValue> newArray = new ArrayList(array);
                for (PropertyValue value : array) {
                    if (value.getComponent() == getComponent()) {
                        invokeMoveAction(array, newArray, value);
                    }
                }
            }
        });
    }

    protected abstract void invokeMoveAction(List<PropertyValue> currentArray, List<PropertyValue> newArray, PropertyValue currentValue);

    protected TypeID getNewArrayType() {
        return newArrayTypeID;
    }

    public void setComponent(DesignComponent component) {
        this.component = component;
    }

    private String getArrayPropertyName() {
        return arrayPropertyName;
    }

    protected void saveToModel(List<PropertyValue> newArray) {
        DesignComponent parentComponent = getComponent().getParentComponent();
        parentComponent.writeProperty(getArrayPropertyName(), PropertyValue.createArray(getNewArrayType(), newArray));
    }
}

