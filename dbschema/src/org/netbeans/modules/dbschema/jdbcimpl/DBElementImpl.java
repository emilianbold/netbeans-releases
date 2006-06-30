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

package org.netbeans.modules.dbschema.jdbcimpl;

import java.beans.*;

import org.netbeans.modules.dbschema.*;

abstract class DBElementImpl implements DBElement.Impl, DBElementProperties {

	/** Element */
	DBElement element;

    protected DBIdentifier _name;

	/** Property change support */
	transient private PropertyChangeSupport support;

    /** Creates new DBElementImpl */
	public DBElementImpl () {
	}

	/** Creates new DBElementImpl with the specified name */
    public DBElementImpl (String name) {
		if (name != null)
	        _name = DBIdentifier.create(name);
	}

    /** Called to attach the implementation to a specific
    * element. Will be called in the element's constructor.
    * Allows implementors of this interface to store a reference to the
    * holder class, useful for implementing the property change listeners.
    *
    * @param element the element to attach to
    */
    public void attachToElement(DBElement el) {
        element = el;
    }
  
    /** Get the name of this element.
    * @return the name
    */
    public DBIdentifier getName() {
        return _name;
    }

    /** Set the name of this element.
    * @param name the name
    * @throws DBException if impossible
    */
    public void setName(DBIdentifier name) throws DBException {
        _name = name;
    }
    
    protected boolean comp(Object obj1, Object obj2) {
        if (obj1 == null || obj2 == null) {
            if (obj1 == obj2)
                return true;
        } else
            if (obj1.equals(obj2))
                return true;
            
        return false;
    }
  
	/** Fires property change event.
	 * @param name property name
	 * @param o old value
	 * @param n new value
	 */
	protected final void firePropertyChange (String name, Object o, Object n)	{
		if (support != null)
			support.firePropertyChange(name, o, n);
	}
  
    /** Add a property change listener.
    * @param l the listener to add
    */
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
		if (support == null)
			synchronized (this)  {
				// new test under synchronized block
				if (support == null)
					support = new PropertyChangeSupport(element);
			}

		support.addPropertyChangeListener(l);
    }
  
    /** Remove a property change listener.
    * @param l the listener to remove
    */
    public void removePropertyChangeListener(PropertyChangeListener l) {
		if (support != null)
			support.removePropertyChangeListener(l);
    }
}
