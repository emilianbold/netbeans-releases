/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
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
	Collection attrDefs = new LinkedList();
	Iterator it = getAttlistDeclarations().iterator();
	while (it.hasNext()) {
	    TreeAttlistDecl attlist = (TreeAttlistDecl)it.next();
	    if ( attlist.getElementName().equals (elementName) ) {
		attrDefs.addAll ((Collection)attlist.getAttributeDefs());
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
