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
package org.netbeans.tax.decl;

import java.util.*;

import org.netbeans.tax.*;

public abstract class ChildrenType extends TreeElementDecl.ContentType implements TypeCollection {

    /** */
    protected List context;         // children collection


    //
    // init
    //

    public ChildrenType (Collection col) {
        super ();

        context = new LinkedList ();
        context.addAll (col);
    }

    public ChildrenType () {
        this (new ArrayList ());
    }

    public ChildrenType (ChildrenType childrenType) {
        super (childrenType);
        
        context = new LinkedList ();
        Iterator it = childrenType.context.iterator ();
        while ( it.hasNext () ) {
            context.add (((TreeElementDecl.ContentType)it.next ()).clone ());
        }
    }
    
    
    //
    // from TreeElementDecl.ContentType
    //
    
    /**
     */
    protected void setNodeDecl (TreeNodeDecl nodeDecl) {
        super.setNodeDecl (nodeDecl);
        
        initNodeDecl ();
    }
    
    protected final void initNodeDecl () {
        //  	Iterator it = context.iterator();
        //  	while ( it.hasNext() ) {
        //  	    TreeElementDecl.ContentType ct = (TreeElementDecl.ContentType)it.next();
        //  	    ct.setNodeDecl (getNodeDecl());
        //  	}
    }
    
    
    //
    // itself
    //
    
    /**
     */
    public void addTypes (Collection types) {
        // remove null members
        Iterator it = types.iterator ();
        while (it.hasNext ()) {
            if (it.next () == null) it.remove ();
        }
        context.addAll (types);
        
        initNodeDecl ();
    }
    
    /** Add new type to collection. */
    public void addType (TreeElementDecl.ContentType type) {
        if (type == null)
            return;
        context.add (type);
        
        initNodeDecl ();
    }
    
    /**
     */
    public Collection getTypes () {
        return context;
    }
    
    /**
     */
    public boolean allowElements () {
        return true;
    }
    
    /**
     */
    public boolean allowText () {
        return false;
    }
    
    /**
     */
    public boolean hasChildren () {
        return context.size () > 0;
    }
    
    /**
     */
    public abstract String getSeparator ();
    
}
