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
public class TreeText extends TreeData implements TreeCharacterData, DocumentFragment.Child, Element.Child, GeneralEntityReference.Child, Attribute.Value {

    //
    // init
    //

    /** Creates new TreeText.
     * @throws InvalidArgumentException
     */
    public TreeText (String data) throws InvalidArgumentException {
        super (data);
    }

    /** Creates new TreeText -- copy costructor. */
    protected TreeText (TreeText text) {
	super (text);
    }


    //
    // from TreeObject
    //
    
    /**
     */
    public Object clone () {
	return new TreeText (this);
    }

    /**
     */
    public boolean equals (Object object, boolean deep) {
        if (!!! super.equals (object, deep))
            return false;
        return true;
    }

    /*
     * Check instance and delegate to superclass.
     */
    public void merge (TreeObject treeObject) throws CannotMergeException {
	super.merge (treeObject);
    }

    //
    // from TreeData
    //
    
    /**
     */
    protected final void checkData (String data) throws InvalidArgumentException {
	TreeUtilities.checkTextData (data);
    }

    /**
     * @throws InvalidArgumentException
     */
    protected final TreeData createData (String data) throws InvalidArgumentException {
        return new TreeText (data);
    }
        
}
