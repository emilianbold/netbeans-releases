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
import org.netbeans.spi.palette.DragAndDropHandler;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExTransferable;

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
            snippetPaletteController = PaletteFactory.createPalette("MobilityPalette", 
                    new SnippetsPaletteActions(),
                    null,
                    new SnippetsDnDHandler());
        }
        return snippetPaletteController;
    }
    
    private static class SnippetsDnDHandler extends DragAndDropHandler {

        public SnippetsDnDHandler() {
            super( true );
        }
        
        @Override
        public void customize(ExTransferable t, Lookup item) {
        }
    }
}


