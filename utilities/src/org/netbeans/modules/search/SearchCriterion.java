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
    String criterionName;
    /** */
    boolean isDefault;
    /** */
    byte[] criterionData;

    /** */
    public String toString() {
        return criterionName;
    }
    
}
