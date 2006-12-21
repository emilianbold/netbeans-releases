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

import org.netbeans.modules.vmd.api.model.ComponentProducer;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PaletteDescriptor;
import org.netbeans.modules.vmd.midp.components.sources.ListElementEventSourceCD;
import org.netbeans.modules.vmd.midp.palette.MidpPaletteProvider;

/**
 * @author David Kaspar
 */
public class ListElementEventSourceProducer extends ComponentProducer {

    private static final String PRODUCER_ID = "#ListElementEventSourceProducer"; // NOI18N

    public ListElementEventSourceProducer () {
        super (PRODUCER_ID, ListElementEventSourceCD.TYPEID, new PaletteDescriptor (MidpPaletteProvider.CATEGORY_ELEMENTS, "List Element", "List Element", ListElementEventSourceCD.ICON_PATH, null));
    }

    public Result createComponent (DesignDocument document) {
        DesignComponent eventSource = document.createComponent (ListElementEventSourceCD.TYPEID);
        return new Result (eventSource);
    }

    public boolean checkValidity(DesignDocument document) {
        return true;
    }

}
