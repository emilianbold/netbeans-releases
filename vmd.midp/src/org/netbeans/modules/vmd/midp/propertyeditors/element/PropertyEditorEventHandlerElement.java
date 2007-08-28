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

package org.netbeans.modules.vmd.midp.propertyeditors.element;

import java.util.List;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement;

/**
 *
 * @author Anton Chechel
 */
public interface PropertyEditorEventHandlerElement extends PropertyEditorElement {
    
    int MODEL_TYPE_DISPLAYABLES = 0;
    int MODEL_TYPE_DISPLAYABLES_WITHOUT_ALERTS = 1;
    int MODEL_TYPE_POINTS = 2;
    
    void createEventHandler(DesignComponent eventSource);
    void updateModel(List<DesignComponent> components, int modelType);
    void setElementEnabled(boolean enabled);
}
