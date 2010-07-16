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


package org.netbeans.modules.tbls.editor.palette;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import org.netbeans.spi.palette.DragAndDropHandler;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExTransferable;

/*
 * TcgDragAndDropHandler.java
 *
 * Created on January 4, 2006, 9:13 AM
 *
 * @author Bing Lu
 */
public class TcgDragAndDropHandler extends DragAndDropHandler {
    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(TcgDragAndDropHandler.class.getName());

    public TcgDragAndDropHandler() {
    }

    public void customize(ExTransferable t, Lookup item) {
        String body = (String) item.lookup(String.class);
        mLog.info("body: " + body);
        TcgActiveEditorDrop  drop = new TcgActiveEditorDrop(body);
        PaletteItemTransferable s = new PaletteItemTransferable(drop);
        t.put(s);
    }

    public boolean canDrop(Lookup targetCategory, DataFlavor[] flavors, int dndAction) {
        return false;
    }

    public boolean doDrop(Lookup targetCategory, Transferable item, int dndAction, int dropIndex) {
        return false;
    }
    
    private static class PaletteItemTransferable extends ExTransferable.Single {
        
        private TcgActiveEditorDrop drop;

        PaletteItemTransferable(TcgActiveEditorDrop drop) {
            super(ActiveEditorDrop.FLAVOR);
            this.drop = drop;
        }
               
        public Object getData () {
            return drop;
        }
        
    }
    
}


