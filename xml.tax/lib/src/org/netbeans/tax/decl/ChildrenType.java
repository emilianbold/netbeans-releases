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
