/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.mobility.svgcore.palette;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.beans.BeanInfo;
import java.io.IOException;
import java.util.ArrayList;
import org.openide.loaders.DataNode;
import org.openide.nodes.Children;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExTransferable;

/**
 *
 * @author Pavel Benes
 */
public final class SVGPaletteItemDataNode extends DataNode {
    private static final String IMAGE_ICON_BASE = "org/netbeans/modules/mobility/svgcore/resources/svg.png"; //NOI18N
    
    SVGPaletteItemDataNode(SVGPaletteItemDataObject obj, Lookup lookup) {
        super(obj, Children.LEAF, lookup);
        setIconBaseWithExtension(IMAGE_ICON_BASE);
        //setName( m_data.getName());
        //setShortDescription( m_data.getFilePath());        
    }
   
    public String getName() {
        return getData().getName();
    }
    
    public String getShortDescription() {
        return getData().getFilePath();
    }
    
    public Image getIcon( int type) {
        if ( type == BeanInfo.ICON_COLOR_32x32 || type == BeanInfo.ICON_MONO_32x32) {
            return getData().getIcon32();
        }
        return super.getIcon(type);
    }
    
    public String getDisplayName() {
        return getData().getDisplayName();
    }
    
    public boolean canRename() {
        return false;
    }
    
    private SVGPaletteItemData getData() {
        return ((SVGPaletteItemDataObject) getDataObject()).getData();
    }
    
    public Transferable clipboardCopy() throws IOException {
        ExTransferable t = ExTransferable.create( super.clipboardCopy() );
        
        Lookup lookup = getLookup();
        ActiveEditorDrop drop = lookup.lookup(ActiveEditorDrop.class);
        ActiveEditorDropTransferable s = new ActiveEditorDropTransferable(drop);
        t.put(s);
        
        //do not allow external DnD flavors otherwise some items may get interpreted
        //as an external file dropped into the editor window
        return new NoExternalDndTransferable( t );
    }

    private static class ActiveEditorDropTransferable extends ExTransferable.Single {
        private ActiveEditorDrop drop;

        ActiveEditorDropTransferable(ActiveEditorDrop drop) {
            super(ActiveEditorDrop.FLAVOR);
            
            this.drop = drop;
        }
               
        public Object getData () {
            return drop;
        }        
    }
    
    /**
     * Transferable wrapper that does not allow DataFlavors for external drag and drop
     * (FileListFlavor and URI list flavors)
     */
    private static class NoExternalDndTransferable implements Transferable {
        private Transferable t;
        private DataFlavor uriListFlavor;
        public NoExternalDndTransferable( Transferable t ) {
            this.t = t;
        }
    
        public DataFlavor[] getTransferDataFlavors() {
            DataFlavor[] flavors = t.getTransferDataFlavors();
            if( t.isDataFlavorSupported( DataFlavor.javaFileListFlavor ) 
                || t.isDataFlavorSupported( getUriListFlavor() ) ) {
                ArrayList<DataFlavor> tmp = new ArrayList<DataFlavor>( flavors.length );
                for( int i=0; i<flavors.length; i++ ) {
                    if( isDataFlavorSupported( flavors[i] ) )
                        tmp.add( flavors[i] );
                }
                flavors = tmp.toArray( new DataFlavor[tmp.size()] );
            }
            return flavors;
        }

        public boolean isDataFlavorSupported( DataFlavor flavor ) {
            if( DataFlavor.javaFileListFlavor.equals( flavor ) || getUriListFlavor().equals( flavor ) )
                return false;
            return t.isDataFlavorSupported(flavor);
        }

        public Object getTransferData( DataFlavor flavor ) throws UnsupportedFlavorException, IOException {
            if( !isDataFlavorSupported(flavor) )
                throw new UnsupportedFlavorException( flavor );
            return t.getTransferData( flavor );
        }
        
        private DataFlavor getUriListFlavor () {
            if( null == uriListFlavor ) {
                try {
                    uriListFlavor = new DataFlavor("text/uri-list;class=java.lang.String"); //NOI18N
                } catch (ClassNotFoundException ex) {
                    //cannot happen
                    throw new AssertionError(ex);
                }
            }
            return uriListFlavor;
        }
    }    
}
