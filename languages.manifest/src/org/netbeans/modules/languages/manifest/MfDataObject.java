/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.languages.manifest;

import java.io.IOException;

import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;


public class MfDataObject extends MultiDataObject {

    public MfDataObject (FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super (pf, loader);
        registerEditor(MfLanguageProvider.MIME_TYPE, true);
    }

    @Override
    protected Node createNodeDelegate () {
        DataNode node = new DataNode (this, Children.LEAF, getLookup ());
        return node;
    }

    @Override
    protected int associateLookup() {
        return 1;
    }
    
    @Messages("Source=&Source")
    @MultiViewElement.Registration(
            displayName="#Source",
            iconBase="org/netbeans/modules/languages/manifest/manifest_file_16.png",
            persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
            mimeType=MfLanguageProvider.MIME_TYPE,
            preferredID="manifest.source",
            position=1
    )
    public static MultiViewEditorElement createMultiViewEditorElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }
}
