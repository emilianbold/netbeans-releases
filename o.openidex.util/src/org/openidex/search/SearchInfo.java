/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openidex.search;

import java.util.Iterator;

/**
 * Defines which <code>DataObject</code>s should be searched.
 * Iterator returned by this interface's method enumerates
 * <code>DataObject</code>s that should be searched.
 *
 * @see  DataObject
 * @see  SimpleSearchInfo
 * @author  Marian Petras
 */
public interface SearchInfo {

    /**
     * Specifies which <code>DataObject</code>s should be searched.
     *
     * @return  iterator which iterates over <code>DataObject</code>s
     */
    public Iterator objectsToSearch();
    
}
