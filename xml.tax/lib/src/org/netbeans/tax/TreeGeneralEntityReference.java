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

import org.netbeans.tax.spec.DocumentFragment;
import org.netbeans.tax.spec.Element;
import org.netbeans.tax.spec.GeneralEntityReference;
import org.netbeans.tax.spec.Attribute;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeGeneralEntityReference extends TreeEntityReference implements DocumentFragment.Child, Element.Child, GeneralEntityReference.Child, Attribute.Value {

    //
    // init
    //

    /** Creates new TreeGeneralEntityReference.
     * @throws InvalidArgumentException
     */
    public TreeGeneralEntityReference (String name) throws InvalidArgumentException {
        super (name);
    }

    /** Creates new TreeGeneralEntityReference -- copy constructor. */
    protected TreeGeneralEntityReference (TreeGeneralEntityReference generalEntityReference, boolean deep) {
	super (generalEntityReference, deep);
    }

    
    //
    // from TreeObject
    //
    
    /**
     */
    public Object clone (boolean deep) {
	return new TreeGeneralEntityReference (this, deep);
    }

    /**
     */
    public boolean equals (Object object, boolean deep) {
        if (!!! super.equals (object, deep))
            return false;
        return true;
    }

    /*
     * Checks instance and delegate to superclass.
     */
    public void merge (TreeObject treeObject) throws CannotMergeException {
	super.merge (treeObject);
    }

    
    //
    // itself
    //

    /**
     */
    protected final void checkName (String name) throws InvalidArgumentException {
	TreeUtilities.checkGeneralEntityReferenceName (name);
    }


    //
    // TreeObjectList.ContentManager
    //

    /**
     */
    protected TreeObjectList.ContentManager createChildListContentManager () {
	return new ChildListContentManager();
    }


    /**
     *
     */
    protected class ChildListContentManager extends TreeEntityReference.ChildListContentManager {

	/**
	 */
	public TreeNode getOwnerNode () {
	    return TreeGeneralEntityReference.this;
	}	

	/**
	 */
	public void checkAssignableObject (Object obj) {
	    super.checkAssignableObject (obj);
	    checkAssignableClass (GeneralEntityReference.Child.class, obj);
	}

    } // end: class ChildListContentManager

}
