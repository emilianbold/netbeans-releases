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