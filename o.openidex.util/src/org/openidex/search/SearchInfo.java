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
 * List returned by this interface may contain also other
 * <code>SearchInfo</code> objects &ndash; it has the same effect as if
 * <code>DataObject</code>s returned by the nested <code>SearchInfo</code>
 * object were included in the containing <code>SeachInfo</code> object.
 *
 * @see  SimpleSearchInfo
 * @author  Marian Petras
 */
public interface SearchInfo {

    /**
     * Specifies which <code>DataObject</code>s should be searched.
     *
     * @return  list of <code>DataObject</code>s and other
     *          <code>SearchInfo</code> objects
     * @see  DataObject#Container
     */
    public Iterator objectsToSearch();
    
}
