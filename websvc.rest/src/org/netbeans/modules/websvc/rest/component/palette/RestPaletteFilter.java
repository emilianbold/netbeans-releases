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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.rest.component.palette;

import org.netbeans.spi.palette.PaletteFilter;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Owner
 */
public class RestPaletteFilter extends PaletteFilter {
    
    public boolean isValidItem(Lookup lookup) {
        Node itemNode = (Node)lookup.lookup( Node.class );
        return isItemVisibleInCurrentEditorContext( itemNode );
    }
    
    public boolean isValidCategory(Lookup lookup) {
        Node categoryNode = (Node)lookup.lookup( Node.class );
        return isCategoryVisibleInCurrentEditorContext( categoryNode );
    }
    
    private boolean isItemVisibleInCurrentEditorContext( Node item ) {
        return true;
        //return RestPaletteUtils.ready(TopComponent.getRegistry().getActivatedNodes());
    }
    
    private boolean isCategoryVisibleInCurrentEditorContext( Node item ) {
        return true;
        //return RestPaletteUtils.ready(TopComponent.getRegistry().getActivatedNodes());
    }
}
