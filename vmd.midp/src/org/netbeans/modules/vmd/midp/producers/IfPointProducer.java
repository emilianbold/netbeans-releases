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
package org.netbeans.modules.vmd.midp.producers;

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.midp.components.sources.IfFalseEventSourceCD;
import org.netbeans.modules.vmd.midp.components.sources.IfTrueEventSourceCD;
import org.netbeans.modules.vmd.midp.components.points.IfPointCD;
import org.netbeans.modules.vmd.midp.palette.MidpPaletteProvider;
import org.openide.util.NbBundle;

/**
 * @author David Kaspar
 */
public class IfPointProducer extends ComponentProducer {

    private static final String PRODUCER_ID = "#IfCallPointProducer"; // NOI18N

    public IfPointProducer () {
        super (PRODUCER_ID, IfPointCD.TYPEID, new PaletteDescriptor (MidpPaletteProvider.CATEGORY_PROCESS_FLOW, NbBundle.getMessage(IfPointProducer.class, "DISP_IfPoint"), NbBundle.getMessage(IfPointProducer.class, "TTIP_IfPoint"), IfPointCD.ICON_PATH, IfPointCD.LARGE_ICON_PATH)); // NOI18N
    }

    public Result postInitialize (DesignDocument document, DesignComponent ifCallPoint) {
        DesignComponent trueCase = document.createComponent (IfTrueEventSourceCD.TYPEID);
        ifCallPoint.addComponent (trueCase);
        ifCallPoint.writeProperty (IfPointCD.PROP_TRUE, PropertyValue.createComponentReference (trueCase));

        DesignComponent falseCase = document.createComponent (IfFalseEventSourceCD.TYPEID);
        ifCallPoint.addComponent (falseCase);
        ifCallPoint.writeProperty (IfPointCD.PROP_FALSE, PropertyValue.createComponentReference (falseCase));

        return new Result (ifCallPoint, trueCase, falseCase);
    }

    public boolean checkValidity(DesignDocument document) {
        return true;
    }

}
