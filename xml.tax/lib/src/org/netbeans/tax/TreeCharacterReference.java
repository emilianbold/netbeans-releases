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

import org.netbeans.tax.spec.DocumentFragment;
import org.netbeans.tax.spec.Element;
import org.netbeans.tax.spec.GeneralEntityReference;
import org.netbeans.tax.spec.Attribute;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeCharacterReference extends TreeChild implements TreeReference, TreeCharacterData, DocumentFragment.Child, Element.Child, GeneralEntityReference.Child, Attribute.Value {
    /** */
    public static final String PROP_NAME = "name"; // NOI18N


    /** */
    private String name;  //literal occuring in document  "#99" // NOI18N
    
    //
    // init
    //
    
    /** Creates new TreeCharacterReference.
     * @throws InvalidArgumentException
     */
    public TreeCharacterReference (String name) throws InvalidArgumentException {
        super ();
        
        checkName (name);
        this.name = name;
    }
    
    /** Creates new TreeCharacterReference -- copy constructor. */
    protected TreeCharacterReference (TreeCharacterReference characterReference) {
        super (characterReference);
        
        this.name = characterReference.name;
    }
    
    
    //
    // from TreeObject
    //
    
    /**
     */
    public Object clone () {
        return new TreeCharacterReference (this);
    }
    
    /**
     */
    public boolean equals (Object object, boolean deep) {
        if (!!! super.equals (object, deep))
            return false;
        
        TreeCharacterReference peer = (TreeCharacterReference) object;
        if (!!! Util.equals (this.getName (), peer.getName ()))
            return false;
        
        return true;
    }
    
    /*
     * Merge name property.
     */
    public void merge (TreeObject treeObject) throws CannotMergeException {
        super.merge (treeObject);
        
        TreeCharacterReference peer = (TreeCharacterReference) treeObject;
        setNameImpl (peer.getName ());
    }
    
    //
    // itself
    //
    
    public final String getName () {
        return name;
    }
    
    /**
     */
    private final void setNameImpl (String newName) {
        String oldName = this.name;
        
        this.name = newName;
        
        firePropertyChange (PROP_NAME, oldName, newName);
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void setName (String newName) throws ReadOnlyException, InvalidArgumentException {
        //
        // check new value
        //
        if ( Util.equals (this.name, newName) )
            return;
        checkReadOnly ();
        checkName (newName);
        
        //
        // set new value
        //
        setNameImpl (newName);
    }
    
    
    /**
     */
    protected final void checkName (String name) throws InvalidArgumentException {
        TreeUtilities.checkCharacterReferenceName (name);
    }
    
    /**
     * @return string representing value (may be a surrogate)
     */
    public final String getData () {
        
        //!!! does not work for surrogates
        
        short val;
        
        if (name.startsWith ("#x")) { // NOI18N
            val = Short.parseShort (name.substring (2), 16);
        } else {
            val = Short.parseShort (name.substring (1));
        }
        return new String (new char[] {(char) val});
    }
    
}
