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

package org.netbeans.modules.vmd.api.model.presenters.actions;

import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import javax.swing.KeyStroke;
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

            {
                putValue( ACCELERATOR_KEY, KeyStroke.getKeyStroke("control U"));
            }

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

            {
                putValue( ACCELERATOR_KEY, KeyStroke.getKeyStroke("control D"));
            }
            
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

    private WeakReference<DesignComponent> component;
    private boolean isEnabled;
    private String arrayPropertyName;
    private TypeID newArrayTypeID;

    private MoveAction(String arrayPropertyName, String displayName) {
        this.putValue(Action.NAME, displayName);
        this.arrayPropertyName = arrayPropertyName;
    }

    protected DesignComponent getComponent() {
        return component.get();
    }

    @Override
    public boolean isEnabled() {
        component.get().getDocument().getTransactionManager().readAccess(new Runnable() {
            public void run() {
                if (component.get().getDocument().getSelectedComponents().size() > 1)
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
        this.component = new WeakReference<DesignComponent>(component);
    }

    private String getArrayPropertyName() {
        return arrayPropertyName;
    }

    protected void saveToModel(List<PropertyValue> newArray) {
        DesignComponent parentComponent = getComponent().getParentComponent();
        parentComponent.writeProperty(getArrayPropertyName(), PropertyValue.createArray(getNewArrayType(), newArray));
    }
}

