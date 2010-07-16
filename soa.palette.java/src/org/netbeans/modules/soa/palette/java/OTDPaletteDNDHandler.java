/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.soa.palette.java;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import org.netbeans.spi.palette.DragAndDropHandler;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExTransferable;

/**
 *
 * @author gpatil
 */
public class OTDPaletteDNDHandler extends DragAndDropHandler {
    
    public void customize(ExTransferable t, Lookup item) {
    }

    @Override
    public boolean canDrop(Lookup targetCategory, DataFlavor[] flavors, int dndAction) {
        return false;
    }

    @Override
    public boolean doDrop(Lookup targetCategory, Transferable item, int dndAction, int dropIndex) {
        return false;
    }

    @Override
    public boolean canReorderCategories(Lookup paletteRoot) {
        return false;
    }

    @Override
    public boolean moveCategory(Lookup category, int moveToIndex) {
        return false;
    }
}
