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
 * CasaPaletteNode.java
 *
 * Created on December 8, 2006, 4:17 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.compapp.casaeditor.palette;

import javax.swing.Action;
import org.netbeans.spi.palette.PaletteController;
import org.openide.actions.NewAction;
import org.openide.actions.PasteAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node.Cookie;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author rdara
 */
public class CasaPaletteCategoryNode extends AbstractNode {
   
    /** Creates a new instance of CasaPaletteNode */
    public CasaPaletteCategoryNode( CasaPaletteCategory category ) {
        super( new CasaPaletteItems(category), Lookups.singleton(category) );
        setDisplayName(category.getName());
        setValue(PaletteController.ATTR_IS_EXPANDED, Boolean.TRUE);
    }

    public Cookie getCookie(Class clazz) {
        Children ch = getChildren();
        
        if (clazz.isInstance(ch)) {
            return (Cookie) ch;
        }
        
        return super.getCookie(clazz);
    }

    
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get( NewAction.class ),
            SystemAction.get( PasteAction.class ) };
    }
    
    public boolean canDestroy() {
        return true;
    }
}    
