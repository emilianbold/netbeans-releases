/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.dbschema;

import org.openide.nodes.Node;

public class DBElementProvider implements Node.Cookie {

    private DBElement element;
    
    /** Default constructor.
     */
    public DBElementProvider() {
    }
    
    /** Creates a new element provider.
     * @param element the element which will be represented by this element provider.
     */
    public DBElementProvider(DBElement element) {
        this.element = element;
    }
    
	/** Gets a database element from this element provider.
	 * @param element the element
	 * @return the element or <code>null</code>
	 */
	public DBElement getDBElement() {
		return element;
	}
}
