/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.tax;

import org.netbeans.tax.spec.Document;
import org.netbeans.tax.spec.DocumentFragment;
import org.netbeans.tax.spec.Element;
import org.netbeans.tax.spec.GeneralEntityReference;
import org.netbeans.tax.spec.DTD;
import org.netbeans.tax.spec.ParameterEntityReference;
import org.netbeans.tax.spec.DocumentType;
import org.netbeans.tax.spec.ConditionalSection;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeComment extends TreeData implements Document.Child, DocumentFragment.Child, Element.Child, GeneralEntityReference.Child, DTD.Child, ParameterEntityReference.Child, DocumentType.Child, ConditionalSection.Child {
    
    //
    // init
    //
    
    /** Creates new TreeComment.
     * @throws InvalidArgumentException
     */
    public TreeComment (String data) throws InvalidArgumentException {
        super (data);
    }
    
    /** Creates new TreeComment -- copy constructor. */
    protected TreeComment (TreeComment comment) {
        super (comment);
    }
    
    
    //
    // from TreeObject
    //
    
    /**
     */
    public Object clone () {
        return new TreeComment (this);
    }
    
    /**
     */
    public boolean equals (Object object, boolean deep) {
        if (!!! super.equals (object, deep))
            return false;
        return true;
    }
    
    /*
     * Checks instance and delegates to superclass.
     */
    public void merge (TreeObject treeObject) throws CannotMergeException {
        super.merge (treeObject);
    }
    
    
    //
    // from TreeData  //??? what is this
    //
    
    /**
     */
    protected final void checkData (String data) throws InvalidArgumentException {
        TreeUtilities.checkCommentData (data);
    }
    
    /**
     * @throws InvalidArgumentException
     */
    protected TreeData createData (String data) throws InvalidArgumentException {
        return new TreeComment (data);
    }
    
}
