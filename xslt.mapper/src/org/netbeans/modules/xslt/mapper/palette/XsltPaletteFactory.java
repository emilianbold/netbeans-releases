/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
import org.openide.filesystems.Repository;
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
                            FileObject metainfoFo = Repository.getDefault().
                                    getDefaultFileSystem().findResource(metainfoRef);
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
