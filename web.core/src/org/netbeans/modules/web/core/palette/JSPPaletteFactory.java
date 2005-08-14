/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core.palette;
import java.io.IOException;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;



/**
 *
 * @author Libor Kotouc
 */

public final class JSPPaletteFactory {
    
    public static final String JSP_PALETTE_FOLDER = "JSPPalette";
    
    private static PaletteController palette = null;
    
    public static PaletteController getPalette() throws IOException {
        if (palette == null)
            palette = PaletteFactory.createPalette(JSP_PALETTE_FOLDER, new JSPPaletteActions());//, null, new JSPDragAndDropHandler());
        
        return palette;
    }
    
}

