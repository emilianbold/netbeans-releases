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

import org.netbeans.tax.spec.ParameterEntityReference;
import org.netbeans.tax.spec.DocumentType;
import org.netbeans.tax.spec.DTD;
import org.netbeans.tax.spec.ConditionalSection;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeParameterEntityReference extends TreeEntityReference implements DocumentType.Child, DTD.Child, ParameterEntityReference.Child, ConditionalSection.Child {

    //
    // init
    //

    /** Creates new TreeParameterEntityReference.
     * @throws InvalidArgumentException
     */
    public TreeParameterEntityReference (String name) throws InvalidArgumentException {
        super (name);
    }
    
    
    /** Creates new TreeParameterEntityReference -- copy constructor. */
    protected TreeParameterEntityReference (TreeParameterEntityReference parameterEntityReference, boolean deep) {
        super (parameterEntityReference, deep);
    }
    
    
    //
    // from TreeObject
    //
    
    /**
     */
    public Object clone (boolean deep) {
        return new TreeParameterEntityReference (this, deep);
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
    // itself
    //
    
    /**
     */
    protected final void checkName (String name) throws InvalidArgumentException {
        TreeUtilities.checkParameterEntityReferenceName (name);
    }
    
    
    //
    // TreeObjectList.ContentManager
    //
    
    /**
     */
    protected TreeObjectList.ContentManager createChildListContentManager () {
        return new ChildListContentManager ();
    }
    
    
    /**
     *
     */
    protected class ChildListContentManager extends TreeEntityReference.ChildListContentManager {
        
        /**
         */
        public TreeNode getOwnerNode () {
            return TreeParameterEntityReference.this;
        }
        
        /**
         */
        public void checkAssignableObject (Object obj) {
            super.checkAssignableObject (obj);
            checkAssignableClass (ParameterEntityReference.Child.class, obj);
        }
        
    } // end: class ChildListContentManager
    
}
