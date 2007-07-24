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
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PaletteDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpJavaSupport;
import org.netbeans.modules.vmd.midp.components.elements.ChoiceElementCD;
import org.netbeans.modules.vmd.midp.components.sources.ListElementEventSourceCD;
import org.netbeans.modules.vmd.midp.palette.MidpPaletteProvider;
import org.openide.util.NbBundle;

/**
 * @author David Kaspar
 */
public class ListElementEventSourceProducer extends ComponentProducer {

    private static final String PRODUCER_ID = "#ListElementEventSourceProducer"; // NOI18N

    public ListElementEventSourceProducer () {
        super (PRODUCER_ID, ListElementEventSourceCD.TYPEID, new PaletteDescriptor (MidpPaletteProvider.CATEGORY_ELEMENTS, NbBundle.getMessage(ListElementEventSourceProducer.class, "DISP_ListElement"), NbBundle.getMessage(ListElementEventSourceProducer.class, "TTIP_ListElement"), ChoiceElementCD.ICON_PATH, ChoiceElementCD.LARGE_ICON_PATH)); // NOI18N
    }

    public boolean checkValidity(DesignDocument document) {
        return MidpJavaSupport.checkValidity(document, "javax.microedition.lcdui.List"); // NOI18N
    }

}
