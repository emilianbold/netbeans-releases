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
package org.netbeans.tax.decl;

import org.netbeans.tax.*;

/** Reference to other declared element.
 * It links itself to context that element.
 */
public class NameType extends LeafType {
    
    /** */
    public static final String PROP_TYPE_NAME = "nt-name"; // NOI18N

    /** */
    private String name;

    
    //
    // init
    //

    public NameType (String name, String mul) {
        super ();

        this.name = name;
        setMultiplicity (mul);
    }

    public NameType (String name) {
        this(name, ""); // NOI18N
    }
    
    public NameType (NameType nameType) {
        super (nameType);

        this.name = nameType.name;
    }


    //
    // from TreeObject
    //

    /**
     */
    public Object clone () {
	return new NameType (this);
    }

    /**
     */
    public boolean equals (Object object, boolean deep) {
        if (!!! super.equals (object, deep))
            return false;

        NameType peer = (NameType) object;
        if (!!! Util.equals (this.getName(), peer.getName()))
            return false;

        return true;
    }

    /*
     * Merges changes from passed object to actual object.
     * @param node merge peer (TreeAttributeDecl)
     * @throws CannotMergeException if can not merge with given node (invalid class)
     */
    public void merge (TreeObject treeObject) throws CannotMergeException {
        super.merge (treeObject);
        
        NameType peer = (NameType) treeObject;

        // just become peer

        setName (peer.getName());
    } 


    //
    // itself
    //

    /**
     */
    public String getName () {
        return name;
    }

    /**
     */
    public void setName (String name) {
	if (Util.equals (this.name, name))
  	    return;
        this.name = name;
        Util.debug ("[NameType] firePropertyChange(PROP_TYPE_NAME, name);"); // NOI18N
    }

    /**
     */
    public String toString () {
        return name + getMultiplicity();
    }


    /**
     */
    public boolean allowElements () {
        return false; //??? should it report TreeElementDecl.forName(name);
    }

    /**
     */
    public boolean allowText() {
        return false; //??? should it report TreeElementDecl.forName(name);
    }

}
