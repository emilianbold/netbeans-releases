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
package org.netbeans.modules.xslt.mapper.palette;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoid;
import org.netbeans.modules.soa.ui.StubPaletteActions;
import org.netbeans.modules.xslt.mapper.methoid.Constants;
import org.netbeans.modules.xslt.mapper.methoid.MethoidLoader;
import org.netbeans.spi.palette.DragAndDropHandler;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExTransferable;

/**
 *
 * @author nk160297
 */
public final class XsltPaletteFactory {
    
    private XsltPaletteFactory() {}
    
    /**
     * Creates a new xslt palette.
     * @return a new xslt palette
     */
    public static PaletteController getPalette() {
        if (ourPalette == null) {
            try {
                ourPalette = PaletteFactory.createPalette(
                        Constants.XSLT_PALETTE_FOLDER,
                        new StubPaletteActions(),
                        null,
                        new MyDnDHandler()
                        );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ourPalette;
    }
    
    public static class MyDnDHandler extends DragAndDropHandler {
        public MyDnDHandler() {
            super( true );
        }
        
        public void customize(ExTransferable t, Lookup item) {
            try {
                DataFlavor mapperFlavor =
                        new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
                t.put(new MapperPaletteTransferable(mapperFlavor, item));
            } catch (ClassNotFoundException ex) {
                ErrorManager.getDefault().notify(ex);
            }
            //
//            t.addTransferListener(new TransferListener() {
//                public void accepted(int action) {
//                }
//                public void ownershipLost() {
//                }
//                public void rejected() {
//                }
//            });
        }
    }
    
    public static class MapperPaletteTransferable extends ExTransferable.Single {
        
        private Lookup myPaletteItemLookup;
        
        public MapperPaletteTransferable(DataFlavor flavor, Lookup item) {
            super(flavor);
            myPaletteItemLookup = item;
        }
        
        protected Object getData() throws IOException, UnsupportedFlavorException {
            Node paletteItemNode = (Node)myPaletteItemLookup.lookup(Node.class);
            if (paletteItemNode != null) {
                DataObject dataObject = (DataObject)paletteItemNode.
                        getCookie(DataObject.class);
                if (dataObject != null) {
                    FileObject fo = dataObject.getPrimaryFile();
                    if (fo != null) {
                        String metainfoRef = (String)fo.getAttribute(
                                Constants.METAINFO_REF);
                        if (metainfoRef != null && metainfoRef.length() != 0) {
                            FileObject metainfoFo = FileUtil.getConfigFile(metainfoRef);
                            if (metainfoFo != null) {
                                IMethoid methoid = MethoidLoader.loadMethoid(metainfoFo);
                                return methoid;
                            }
                        }
                    }
                }
            }
            return null;
        }
    }
    
    private static PaletteController ourPalette;
}
