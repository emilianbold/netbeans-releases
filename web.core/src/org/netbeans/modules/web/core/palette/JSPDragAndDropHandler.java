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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import org.netbeans.spi.palette.DragAndDropHandler;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExTransferable;

/**
 *
 * @author lk155162
 */
public class JSPDragAndDropHandler extends DragAndDropHandler {

    public JSPDragAndDropHandler() {
    }

    public void customize(ExTransferable t, Lookup item) {

        ActiveEditorDrop drop = (ActiveEditorDrop) item.lookup(ActiveEditorDrop.class);
        if (drop == null) {
            String body = (String) item.lookup(String.class);
            drop = new JSPEditorDropDefault(body);
        }
        
        JSPPaletteItemTransferable s = new JSPPaletteItemTransferable(drop);
        t.put(s);
        
    }

    public boolean canDrop(Lookup targetCategory, DataFlavor[] flavors, int dndAction) {
        return false;
    }

    public boolean doDrop(Lookup targetCategory, Transferable item, int dndAction, int dropIndex) {
        return false;
    }
    
    private static class JSPPaletteItemTransferable extends ExTransferable.Single {
        
        private ActiveEditorDrop drop;

        JSPPaletteItemTransferable(ActiveEditorDrop drop) {
            super(ActiveEditorDrop.FLAVOR);
            
            this.drop = drop;
        }
               
        public Object getData () {
            return drop;
        }
        
    }
    
}
