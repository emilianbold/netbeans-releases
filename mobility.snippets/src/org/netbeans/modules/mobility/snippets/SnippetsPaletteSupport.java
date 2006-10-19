/*
 * SnippetsPaletteSupport.java
 *
 * Created on August 22, 2006, 4:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.mobility.snippets;

import java.io.IOException;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;

/**
 *
 * @author bohemius
 */
public class SnippetsPaletteSupport {
    
    private static PaletteController snippetPaletteController=null;
    
    /** Creates a new instance of SnippetsPaletteSupport */
    public SnippetsPaletteSupport() {
    }
    
    public static PaletteController getPaletteController() throws IOException {
        if (snippetPaletteController == null) {
            snippetPaletteController = PaletteFactory.createPalette("MobilityPalette", new SnippetsPaletteActions());
        }
        return snippetPaletteController;
    }
    
}


