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
package com.sun.jsfcl.std.reference;

import java.util.List;
import javax.faces.component.UIComponent;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignProperty;

/**
 * @author eric
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ComponentIdsReferenceDataDefiner extends ReferenceDataDefiner {

    public void addBaseItems(List list) {

        super.addBaseItems(list);
        list.add(newItem("", "", null, true, false)); // NOI18N
    }

    public void addDesignPropertyItems(DesignProperty liveProperty, List list) {

        super.addDesignPropertyItems(liveProperty, list);
        DesignBean[] designBeans = liveProperty.getDesignBean().getDesignContext().getBeansOfType(UIComponent.class);
        if (designBeans == null) {
            return;
        }
        for (int i = 0; i < designBeans.length; i++) {
            DesignBean designBean = designBeans[i];
            try {
                String name = designBean.getInstanceName();
                String id = ((UIComponent)designBean.getInstance()).getId();
                if (id != null) {
                    ReferenceDataItem item = newItem(name, id, false, false);
                    list.add(item);
                }
            } catch (NullPointerException e) {
                // Due to the change in timing on when reference data items are being built now, its
                // possible insync is in the middle of building the units, and if it is its the live unit is not
                // in a solid state yet, in this case the unit of the live bean does not have its class yet
            }
        }
    }

    public boolean canAddRemoveItems() {

        return true;
    }

    public boolean definesDesignPropertyItems() {

        return true;
    }

    public boolean isValueAString() {

        return true;
    }

}
