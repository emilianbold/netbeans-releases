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
package org.netbeans.modules.vmd.midp.producers;

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.midp.components.sources.EntryStartEventSourceCD;
import org.netbeans.modules.vmd.midp.components.points.EntryPointCD;
import org.netbeans.modules.vmd.midp.palette.MidpPaletteProvider;
import org.openide.util.NbBundle;

/**
 * @author David Kaspar
 */
public class EntryPointProducer extends ComponentProducer {

    private static final String PRODUCER_ID = "#EntryPointProducer"; // NOI18N

    public EntryPointProducer () {
        super (EntryPointProducer.PRODUCER_ID, EntryPointCD.TYPEID, new PaletteDescriptor (MidpPaletteProvider.CATEGORY_PROCESS_FLOW, NbBundle.getMessage(EntryPointProducer.class, "DISP_EntryPoint"), NbBundle.getMessage(EntryPointProducer.class, "TTIP_EntryPoint"), EntryPointCD.ICON_PATH, EntryPointCD.LARGE_ICON_PATH)); // NOI18N
    }

    public Result postInitialize (DesignDocument document, DesignComponent entryPoint) {
        DesignComponent eventSource = document.createComponent (EntryStartEventSourceCD.TYPEID);
        entryPoint.addComponent (eventSource);
        entryPoint.writeProperty (EntryPointCD.PROP_START, PropertyValue.createComponentReference (eventSource));
        return new Result (entryPoint, eventSource);
    }

    public boolean checkValidity(DesignDocument document) {
        return true;
    }

}
