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
package org.netbeans.tax;

import java.util.Iterator;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Introduces utility methods for quering DTD content.
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public abstract class AbstractTreeDTD extends TreeParentNode {

    //
    // init
    //

    /** Creates new AbstractTreeDTD. */
    protected AbstractTreeDTD () {
        super ();
    }

    /** Creates new AbstractTreeDTD -- copy constructor. */
    protected AbstractTreeDTD (AbstractTreeDTD abstractDTD, boolean deep) {
        super (abstractDTD, deep);
    }
    
    
    //
    // itself
    //
    
    /**
     */
    public final Collection getElementDeclarations () {
        return getChildNodes (TreeElementDecl.class, true);
    }
    
    /**
     */
    public final Collection getAttlistDeclarations () {
        return getChildNodes (TreeAttlistDecl.class, true);
    }
    
    /**
     */
    public final Collection getAttributeDeclarations (String elementName) {
        Collection attrDefs = new LinkedList ();
        Iterator it = getAttlistDeclarations ().iterator ();
        while (it.hasNext ()) {
            TreeAttlistDecl attlist = (TreeAttlistDecl)it.next ();
            if ( attlist.getElementName ().equals (elementName) ) {
                attrDefs.addAll ((Collection)attlist.getAttributeDefs ());
            }
        }
        return attrDefs;
    }
    
    /**
     */
    public final Collection getEntityDeclarations () {
        return getChildNodes (TreeEntityDecl.class, true);
    }
    
    /**
     */
    public final Collection getNotationDeclarations () {
        return getChildNodes (TreeNotationDecl.class, true);
    }
    
    
    //
    // TreeObjectList.ContentManager
    //
    
    /**
     *
     */
    protected abstract class ChildListContentManager extends TreeParentNode.ChildListContentManager {
    } // end: class ChildListContentManager
    
}
