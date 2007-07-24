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

package org.netbeans.modules.vmd.midpnb.producers;

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpJavaSupport;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;
import org.netbeans.modules.vmd.midp.producers.MidpComponentProducer;
import org.netbeans.modules.vmd.midpnb.components.commands.SVGSplashScreenDismissCommandCD;
import org.netbeans.modules.vmd.midpnb.components.sources.SVGSplashScreenDismissCommandEventSourceCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGSplashScreenCD;
import org.netbeans.modules.vmd.midpnb.palette.MidpNbPaletteProvider;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 */
public class SVGSplashScreenProducer extends MidpComponentProducer {
    
    public SVGSplashScreenProducer() {
        super(SVGSplashScreenCD.TYPEID, new PaletteDescriptor(MidpNbPaletteProvider.CATEGORY_SVG, NbBundle.getMessage(SVGSplashScreenProducer.class, "DISP_SVG_Splash_Screen"), NbBundle.getMessage(SVGSplashScreenProducer.class, "TTIP_SVG_Splash_Screen"), SVGSplashScreenCD.ICON_PATH, SVGSplashScreenCD.ICON_LARGE_PATH)); // NOI18N
    }

    public Result postInitialize (DesignDocument document, DesignComponent splashScreen) {
        DesignComponent dismissCommand = MidpDocumentSupport.getSingletonCommand(document, SVGSplashScreenDismissCommandCD.TYPEID);
        
        DesignComponent dismissEventSource = document.createComponent(SVGSplashScreenDismissCommandEventSourceCD.TYPEID);
        
        dismissEventSource.writeProperty(CommandEventSourceCD.PROP_DISPLAYABLE, PropertyValue.createComponentReference(splashScreen));
        dismissEventSource.writeProperty(CommandEventSourceCD.PROP_COMMAND, PropertyValue.createComponentReference(dismissCommand));
        
        MidpDocumentSupport.addEventSource(splashScreen, DisplayableCD.PROP_COMMANDS, dismissEventSource);
        
        return new Result(splashScreen, dismissCommand, dismissEventSource);
    }
    
    public boolean checkValidity(DesignDocument document) {
            return MidpJavaSupport.checkValidity(document, "javax.microedition.m2g.SVGImage") && // NOI18N
                   MidpJavaSupport.checkValidity(document, "javax.microedition.lcdui.Canvas"); // NOI18N
    }
}
