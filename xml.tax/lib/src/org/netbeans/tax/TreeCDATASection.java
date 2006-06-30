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

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeCDATASection extends TreeData implements TreeCharacterData, DocumentFragment.Child, Element.Child, GeneralEntityReference.Child {

    //
    // init
    //

    /** Creates new TreeCDATASection.
     * @throws InvalidArgumentException
     */
    public TreeCDATASection (String data) throws InvalidArgumentException {
        super (data);
    }
    
    /** Creates new TreeCDATASection -- copy constructor. */
    protected TreeCDATASection (TreeCDATASection cdataSection) {
        super (cdataSection);
    }
    
    
    //
    // from TreeObject
    //
    
    /**
     */
    public Object clone () {
        return new TreeCDATASection (this);
    }
    
    /**
     */
    public boolean equals (Object object, boolean deep) {
        if (!!! super.equals (object, deep))
            return false;
        return true;
    }
    
    /*
     * Check instance and propagate to superclass.
     */
    public void merge (TreeObject treeObject) throws CannotMergeException {
        super.merge (treeObject);
    }
    
    //
    // from TreeData  //??? what is it?
    //
    
    /**
     */
    protected final void checkData (String data) throws InvalidArgumentException {
        TreeUtilities.checkCDATASectionData (data);
    }
    
    /**
     * @throws InvalidArgumentException
     */
    protected TreeData createData (String data) throws InvalidArgumentException {
        return new TreeCDATASection (data);
    }
    
}
