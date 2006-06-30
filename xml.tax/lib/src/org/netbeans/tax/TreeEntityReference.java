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

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public abstract class TreeEntityReference extends TreeParentNode implements TreeReference {
    /** */
    public static final String PROP_NAME = "name"; // NOI18N


    /** */
    private String name;

    /** -- can be null. */
    private TreeEntityDecl entityDecl;


    //
    // init
    //

    /** Creates new TreeEntityReference.
     * @throws InvalidArgumentException
     */
    protected TreeEntityReference (String name) throws InvalidArgumentException {
        super ();
        
        checkName (name);
        this.name = name;
    }
    
    /** Creates new TreeEntityReference -- copy constructor. */
    protected TreeEntityReference (TreeEntityReference entityReference, boolean deep) {
        super (entityReference, deep);
        
        this.name = entityReference.name;
        //  	this.entityDecl = entityReference.entityDecl;
    }
    
    
    //
    // from TreeObject
    //
    
    /**
     */
    public boolean equals (Object object, boolean deep) {
        if (!!! super.equals (object, deep))
            return false;
        
        TreeEntityReference peer = (TreeEntityReference) object;
        if (!!! Util.equals (this.getName (), peer.getName ()))
            return false;
        
        return true;
    }
    
    /*
     * Checks instance and delegate to superclass.
     */
    public void merge (TreeObject treeObject) throws CannotMergeException {
        super.merge (treeObject);
        
        TreeEntityReference peer = (TreeEntityReference) treeObject;
        setNameImpl (peer.getName ());
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
    protected abstract void checkName (String name) throws InvalidArgumentException;
    
    
    /**
     */
    public TreeEntityDecl getEntityDecl () {
        return entityDecl;
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
