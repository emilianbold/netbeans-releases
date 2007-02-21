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

/*
 * CasaPaletteItemNode.java
 *
 * Created on December 8, 2006, 4:42 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.compapp.casaeditor.palette;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.Action;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.DeleteAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author rdara
 */
public class CasaPaletteItemNode extends AbstractNode {
    
    private CasaPaletteItem mPaletteItem;

    public CasaPaletteItemNode(CasaPaletteItem key, String icon) {
        super(Children.LEAF, Lookups.fixed( new Object[] {key} ) );
        mPaletteItem = key;
        setDisplayName(key.getTitle());
        setIconBaseWithExtension(icon);
        setName(key.getTitle());
        
    }
    
    public CasaPaletteItemNode(CasaPaletteItem key) {
        super(Children.LEAF, Lookups.fixed( new Object[] {key} ) );
        mPaletteItem = key;
        setDisplayName(key.getTitle());
    }

    public CasaPaletteItem getCasaPaletteItem() {
        return mPaletteItem;
    }
    
    public boolean canCut() {
        
        return true;
    }
    
    public boolean canDestroy() {
        return true;
    }
    
    public Action[] getActions(boolean popup) {
        return new Action[] {
            SystemAction.get( CopyAction.class ),
            SystemAction.get( CutAction.class ),
            null,
            SystemAction.get( DeleteAction.class ) };
        
    }
    
    public Transferable drag() throws IOException {
        ExTransferable retValue = ExTransferable.create( super.drag() );
        //add the 'data' into the Transferable
        retValue.put( new ExTransferable.Single( CasaPalette.CasaPaletteDataFlavor ) {
            protected Object getData() throws IOException, UnsupportedFlavorException {
                return mPaletteItem;
            }
        });
        return retValue;
    }
    
    
}    
