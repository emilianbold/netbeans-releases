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

/*
 * CasaPalette.java
 *
 * Created on December 8, 2006, 11:12 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.compapp.casaeditor.palette;

import java.awt.datatransfer.DataFlavor;
import javax.swing.Action;
import org.netbeans.spi.palette.PaletteActions;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;
import org.openide.util.Lookup;

/**
 *Singleton class which provides casa specific palette controller
 * @author rdara
 */
public class CasaPalette {
    
    /** Creates a new instance of CasaPalette */
    private static PaletteController msPaletteController = null;
    
    private static String CASA_PALETTE_ROOT = "CasaPalette";
  
    public static enum CASA_CATEGORY_TYPE {
        WSDL_BINDINGS,
        SERVICE_UNITS,
        END_POINTS
    };
    
    public static enum CASA_PALETTE_ITEM_TYPE {
        CONSUME,
        PROVIDE,
        INT_SU,
        EXT_SU,
        UN_KNOWN
    }
    
    public static final DataFlavor CasaPaletteDataFlavor = new DataFlavor( Object.class, "CasaPaletteData" ) {
    };
           
    private CasaPalette() {
    }
    
    private static PaletteController palette = null;
    private static CasaPaletteRootNode paletteRoot;
    
    public static PaletteController getPalette() {
        if( null == palette ) {
            paletteRoot = new CasaPaletteRootNode(new CasaPaletteCategoryChildren());
            paletteRoot.setName( CASA_PALETTE_ROOT);
            palette = PaletteFactory.createPalette( paletteRoot, new MyPaletteActions());
        }
        return palette;
    }
    
     private static class MyPaletteActions extends PaletteActions {
        public Action[] getImportActions() {
            return null;
        }

        public Action[] getCustomPaletteActions() {
            return null;
        }

        public Action[] getCustomCategoryActions(Lookup lookup) {
            return null;
        }

        public Action[] getCustomItemActions(Lookup lookup) {
            return null;
        }

        public Action getPreferredAction(Lookup lookup) {
            return null;
        }
     }
}
