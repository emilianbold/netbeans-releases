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
 *
 */

package org.netbeans.modules.vmd.midpnb.producers;

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpJavaSupport;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;
import org.netbeans.modules.vmd.midp.producers.MidpComponentProducer;
import org.netbeans.modules.vmd.midpnb.components.commands.SVGMenuSelectCommandCD;
import org.netbeans.modules.vmd.midpnb.components.handlers.SVGMenuEventHandlerCD;
import org.netbeans.modules.vmd.midpnb.components.sources.SVGMenuSelectCommandEventSourceCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGMenuCD;
import org.netbeans.modules.vmd.midpnb.palette.MidpNbPaletteProvider;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 */
public class SVGMenuProducer extends MidpComponentProducer {
    
    public SVGMenuProducer() {
        super(SVGMenuCD.TYPEID, new PaletteDescriptor(MidpNbPaletteProvider.CATEGORY_SVG, NbBundle.getMessage(SVGMenuProducer.class, "DISP_SVG_Menu"), NbBundle.getMessage(SVGMenuProducer.class, "TTIP_SVG_Menu"), SVGMenuCD.ICON_PATH, SVGMenuCD.ICON_LARGE_PATH)); // NOI18N
    }

    @Override
    public Result postInitialize (DesignDocument document, DesignComponent menu) {
        DesignComponent selectCommand = MidpDocumentSupport.getSingletonCommand(document, SVGMenuSelectCommandCD.TYPEID);
        
        DesignComponent selectEventSource = document.createComponent(SVGMenuSelectCommandEventSourceCD.TYPEID);
        selectEventSource.writeProperty(CommandEventSourceCD.PROP_DISPLAYABLE, PropertyValue.createComponentReference(menu));
        selectEventSource.writeProperty(CommandEventSourceCD.PROP_COMMAND, PropertyValue.createComponentReference(selectCommand));
        MidpDocumentSupport.addEventSource(menu, DisplayableCD.PROP_COMMANDS, selectEventSource);
        
        DesignComponent menuEventHandler = document.createComponent(SVGMenuEventHandlerCD.TYPEID);
        MidpDocumentSupport.updateEventHandlerWithNew(selectEventSource, menuEventHandler);
        
        return new Result(menu, selectCommand, selectEventSource);
    }
    
    @Override
    public boolean checkValidity(DesignDocument document) {
            return MidpJavaSupport.checkValidity(document, "javax.microedition.m2g.SVGImage") && // NOI18N
                   MidpJavaSupport.checkValidity(document, "javax.microedition.lcdui.Canvas"); // NOI18N
    }
}
