/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.search;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import org.openidex.search.SearchType;

/**
 * <!-- PENDING -->
 *
 * @author  Marian Petras
 */
final class SearchCriterion implements java.io.Serializable {
    
    private static final long serialVersionUID = 1190693501592921043L;
    
    /** */
    String searchTypeClassName;
    /** */
    String name;
    /** <!-- PENDING --> */
    byte[] criterionData;

    /**
     * Creates a new <code>SearchCriterion</code> from an instance
     * of <code>SearchType</code>.
     * The created <code>SearchCriterion</code> is initially not
     * {@linkplain #isDefault default}.
     *
     * @param  searchType  instance of <code>SearchType</code> to create
     *                     a <code>SearchCriterion</code> for
     * @exception  java.io.IOException  if some error occured during creation
     */
    SearchCriterion(SearchType searchType) throws IOException {
        this.name = searchType.getName();
        searchTypeClassName = searchType.getClass().getName();
        
        /* serialize the search type: */
        ObjectOutputStream oos = null;
        try {
            ByteArrayOutputStream bos;
            oos = new ObjectOutputStream(bos = new ByteArrayOutputStream(8192));
            oos.writeObject(searchType);
            criterionData = bos.toByteArray();
        } finally {
            if (oos != null) {
                oos.close();
            }
        }
    }
    
    /** */
    public String toString() {
        return name;
    }
    
}
