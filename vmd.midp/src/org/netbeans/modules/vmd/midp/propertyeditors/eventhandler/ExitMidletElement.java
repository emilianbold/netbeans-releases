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

package org.netbeans.modules.vmd.midp.propertyeditors.eventhandler;

import java.util.Collection;
import javax.swing.*;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.categories.PointsCategoryCD;
import org.netbeans.modules.vmd.midp.components.handlers.ExitMidletEventHandlerCD;
import org.netbeans.modules.vmd.midp.components.points.MobileDeviceCD;
import org.netbeans.modules.vmd.midp.propertyeditors.CleanUp;
import org.netbeans.modules.vmd.midp.propertyeditors.element.PropertyEditorElementFactory;
import org.netbeans.modules.vmd.midp.propertyeditors.element.PropertyEditorEventHandlerElement;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * 
 */
public class ExitMidletElement implements PropertyEditorEventHandlerElement, CleanUp {

    private JRadioButton radioButton;
    
    public ExitMidletElement() {
        radioButton = new JRadioButton();
        Mnemonics.setLocalizedText(radioButton, NbBundle.getMessage(ExitMidletElement.class, "LBL_EXIT_MIDLET")); // NOI18N
        
        radioButton.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(ExitMidletElement.class, "ACSN_EXIT_MIDLET")); // NOI18N
        radioButton.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(ExitMidletElement.class, "ACSD_EXIT_MIDLET")); // NOI18N
    }
    
    public void createEventHandler(DesignComponent eventSource) {
        if (!radioButton.isSelected()) {
            return;
        }
        DesignComponent pointsCategory = MidpDocumentSupport.getCategoryComponent(eventSource.getDocument(), PointsCategoryCD.TYPEID);
        List<DesignComponent> list = DocumentSupport.gatherSubComponentsOfType(pointsCategory, MobileDeviceCD.TYPEID);
        if (list.size() != 1) {
            Debug.warning("Can not retrieve MobileDevice from document"); // NOI18N
            return;
        }
        DesignComponent mobileDevice = list.get(0);
        MidpDocumentSupport.updateEventHandlerFromTarget(eventSource, mobileDevice);
    }
    
    public void setTextForPropertyValue (String text) {
    }
    
    public JComponent getCustomEditorComponent() {
        return null;
    }
    
    public JRadioButton getRadioButton() {
        return radioButton;
    }
    
    public boolean isInitiallySelected() {
        return false;
    }
    
    public boolean isVerticallyResizable() {
        return false;
    }
    
    public void updateModel(List<DesignComponent> components, int modelType) {
    }
    
    public String getTextForPropertyValue () {
        return ""; // NOI18N
    }
    
    public void updateState(PropertyValue value) {
        if (value != null) {
            DesignComponent eventHandler = value.getComponent();
            if (eventHandler.getType().equals(ExitMidletEventHandlerCD.TYPEID)) {
                radioButton.setSelected(true);
            }
        }
    }
    
    public void setElementEnabled(boolean enabled) {
        radioButton.setEnabled(enabled);
    }
    
    public Collection<TypeID> getTypes() {
        return Collections.emptyList();
    }

    public void clean(DesignComponent component) {
        radioButton = null;
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.vmd.midp.propertyeditors.element.PropertyEditorElementFactory.class)
    public static class ExitMidletElementFactory implements PropertyEditorElementFactory {
        public PropertyEditorEventHandlerElement createElement() {
            return new ExitMidletElement();
        }
    }
}
