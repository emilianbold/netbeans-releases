package org.netbeans.modules.iep.editor.designer.actions;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import com.nwoods.jgo.JGoObjectSimpleCollection;

public class IEPComponentTransferable implements Transferable {

    public static final DataFlavor NODE_DATA_FLAVOR;
    static {
        try {
            NODE_DATA_FLAVOR = new DataFlavor("text/iep_editor_flavor;class=org.netbeans.modules.iep.editor.designer.actions.IEPComponentTransferable", // NOI18N
                    "Paste Item", // XXX missing I18N!
                    CopyAction.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }
    
    
    private JGoObjectSimpleCollection mCollection;
    
    public IEPComponentTransferable(JGoObjectSimpleCollection collection) {
        this.mCollection = collection;
    }
    
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if(flavor.equals(NODE_DATA_FLAVOR)) {
            return mCollection;
        }
        
        return null;
    }

    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] {NODE_DATA_FLAVOR};
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        if(flavor.equals(NODE_DATA_FLAVOR)) {
            return true;
        }
        return false;
    }

}
