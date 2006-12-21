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
package org.netbeans.modules.vmd.midp.flow;

import org.netbeans.modules.vmd.api.flow.FlowPinOrderPresenter;
import org.netbeans.modules.vmd.api.flow.visual.FlowPinDescriptor;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.midp.components.points.IfPointCD;

import java.util.ArrayList;
import java.util.List;

/**
 * @author David Kaspar
 */
public class FlowIfPointPinOrderPresenter extends FlowPinOrderPresenter {

    public static final String CATEGORY_ID = "IfCallPointCase"; // NOI18N

    public String getCategoryID () {
        return CATEGORY_ID;
    }

    public String getCategoryDisplayName () {
        return "Condition Result";
    }

    public List<FlowPinDescriptor> sortCategory (ArrayList<FlowPinDescriptor> descriptors) {
        ArrayList<FlowPinDescriptor> list = new ArrayList<FlowPinDescriptor> ();

        DesignComponent trueCase = getComponent ().readProperty (IfPointCD.PROP_TRUE).getComponent ();
        for (FlowPinDescriptor descriptor : descriptors)
            if (descriptor.getRepresentedComponent () == trueCase) {
                list.add (descriptor);
                break;
            }

        DesignComponent falseCase = getComponent ().readProperty (IfPointCD.PROP_FALSE).getComponent ();
        for (FlowPinDescriptor descriptor : descriptors)
            if (descriptor.getRepresentedComponent () == falseCase) {
                list.add (descriptor);
                break;
            }

        for (FlowPinDescriptor descriptor : descriptors)
            if (! list.contains (descriptor))
                list.add (descriptor);

        return list;
    }

}
