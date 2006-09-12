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
package org.netbeans.modules.openide.explorer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.ExTransferable.Multi;
import org.openide.util.datatransfer.MultiTransferObject;

/**
 * Utilities to handle drag and drop events to/from other applications
 * 
 * @author S. Aubrecht
 */
public class ExternalDragAndDrop {
    
    private ExternalDragAndDrop() {
    }
    
    /**
     * The default Transferable implementation for multi-object drag and drop operations is
     * ExTransferable.Multi. However it uses a custom DataFlavor which prevents drag and drop
     * of multiple files from the IDE to other applications.
     * This method checks whether the given Multi instance contains objects that support
     * DataFlavor.javaFileListFlavor and adds a separate Transferable instance for them.
     * 
     * @param multi Multi transferable
     * 
     * @return The original Multi transferable if none of the inner transferables supports
     * javaFileListFlavor. Otherwise it returns a new ExTransferable with the original Multi
     * transferable and an additional Transferable with javaFileListFlavor that aggregates
     * all file objects from the Multi instance.
     * 
     */
    public static Transferable maybeAddExternalFileDnd( Multi multi ) {
        Transferable res = multi;
        try {
            MultiTransferObject mto = (MultiTransferObject) multi.getTransferData(ExTransferable.multiFlavor);
            final ArrayList fileList = new ArrayList( mto.getCount() );
            for( int i=0; i<mto.getCount(); i++ ) {
                if( mto.isDataFlavorSupported( i, DataFlavor.javaFileListFlavor ) ) {
                    List list = (List)mto.getTransferData( i, DataFlavor.javaFileListFlavor );
                    fileList.addAll( list );
                }
            }
            if( !fileList.isEmpty() ) {
                ExTransferable fixed = ExTransferable.create( multi );
                fixed.put( new ExTransferable.Single( DataFlavor.javaFileListFlavor ) {
                    protected Object getData() throws IOException, UnsupportedFlavorException {
                        return fileList;
                    }
                });
                res = fixed;
            }
        } catch (UnsupportedFlavorException ex) {
            Logger.global.log(Level.INFO, null, ex);
        } catch (IOException ex) {
            Logger.global.log(Level.INFO, null, ex);
        }
        return res;
    }
}
