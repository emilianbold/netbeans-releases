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


package org.netbeans.modules.palette;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.EventListener;
import javax.swing.Action;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteActions;
import org.netbeans.spi.palette.DragAndDropHandler;

import org.openide.*;
import org.openide.actions.*;
import org.openide.filesystems.FileObject;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.NewType;
import org.openide.util.WeakListeners;
import org.openide.util.datatransfer.PasteType;

/**
 * A Node for palette item.
 *
 * @author S. Aubrecht
 */
class ItemNode extends FilterNode {
    

    private Action[] actions;

    public ItemNode( Node originalNode ) {
        super( originalNode, Children.LEAF );
    }

    public Action[] getActions(boolean context) {
        if (actions == null) {
            actions = new Action[] {
                new Utils.CutItemAction( this ),
                new Utils.CopyItemAction( this ),
                new Utils.PasteItemAction( getParentNode() ),
                null,
                new Utils.RemoveItemAction( this ),
                null,
                new Utils.SortItemsAction( getParentNode() ),
                null,
                new Utils.RefreshPaletteAction()
            };
        }
        PaletteActions customActions = getCustomActions();
        if( null != customActions ) {
            return Utils.mergeActions( actions, customActions.getCustomItemActions( getLookup() ) );
        }
        return actions;
    }

    public Transferable clipboardCut() throws java.io.IOException {
        ExTransferable t = ExTransferable.create( super.clipboardCut() );
        
        customizeTransferable( t );
        t.put( createTransferable() );
        
        return t;
    }

    public Transferable clipboardCopy() throws IOException {
        ExTransferable t = ExTransferable.create( super.clipboardCopy() );
        
        customizeTransferable( t );
        t.put( createTransferable() );
        
        return t;
    }

    public PasteType getDropType( Transferable t, int action, int index ) {
        return null;
    }

    public Transferable drag() throws IOException {
        ExTransferable t = ExTransferable.create( super.drag() );//NodeTransfer.transferable(this, NodeTransfer.DND_MOVE) );
        
        customizeTransferable( t );
        t.put( createTransferable() );
        
        return t;
    }
    
    private ExTransferable.Single createTransferable() {
        final Lookup lkp = getLookup();
        return new ExTransferable.Single( PaletteController.ITEM_DATA_FLAVOR ) {
           public Object getData () {
               return lkp;
           }
       };
    }
    
    private void customizeTransferable( ExTransferable t ) {
        DragAndDropHandler tp = getTransferableProvider();
        if( null != tp ) {
            tp.customize( t, getLookup() );
        }
    }

    private PaletteActions getCustomActions() {
        Node category = getParentNode();
        assert null != category;
        Node root = category.getParentNode();
        assert null != root;
        return (PaletteActions)root.getLookup().lookup( PaletteActions.class );
    }

    private DragAndDropHandler getTransferableProvider() {
        Node category = getParentNode();
        assert null != category;
        Node root = category.getParentNode();
        assert null != root;
        return (DragAndDropHandler)root.getLookup().lookup( DragAndDropHandler.class );
    }
    
    public Action getPreferredAction() {

        PaletteActions customActions = getCustomActions();
        
        if( null == customActions )
            return null;;
        
        return customActions.getPreferredAction( getLookup() );
    }

    public boolean canDestroy() {

        return !Utils.isReadonly( getOriginal() );
    }
    
    Node getOriginalNode() {
        return getOriginal();
    }
}
