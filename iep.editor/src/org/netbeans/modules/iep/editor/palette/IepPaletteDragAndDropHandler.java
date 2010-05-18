package org.netbeans.modules.iep.editor.palette;

import java.awt.datatransfer.DataFlavor;

import org.netbeans.spi.palette.DragAndDropHandler;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExTransferable;

public class IepPaletteDragAndDropHandler extends DragAndDropHandler {

    @Override
    public void customize(ExTransferable t, Lookup item) {
        
    }
    
    @Override
    public boolean canDrop(Lookup targetCategory, DataFlavor[] flavors, int dndAction) {
        return false;
    }

}
