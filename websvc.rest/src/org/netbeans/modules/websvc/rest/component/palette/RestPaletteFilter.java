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
