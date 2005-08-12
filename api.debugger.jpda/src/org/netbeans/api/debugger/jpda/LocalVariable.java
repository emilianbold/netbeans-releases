/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.debugger.jpda;


/**
 * Represents one local. This interface is extended by {@link ObjectVariable}
 * interface, if the represented local contains not primitive value (object
 * value).
 *
 * <pre style="background-color: rgb(255, 255, 102);">
 * Since JDI interfaces evolve from one version to another, it's strongly recommended
 * not to implement this interface in client code. New methods can be added to
 * this interface at any time to keep up with the JDI functionality.</pre>
 *
 * @see ObjectVariable
 * @author   Jan Jancura
 */
public interface LocalVariable extends Variable {

    /**
     * Declared name of local.
     *
     * @return name of this local.
     */
    public abstract String getName ();

    /**
     * Returns name of enclosing class.
     *
     * @return name of enclosing class
     */
    public abstract String getClassName ();

    /**
     * Declared type of this local.
     *
     * @return declared type of this local
     */
    public abstract String getDeclaredType ();

    /**
     * Sets value of this local represented as text.
     *
     * @param value a new value of this local represented as text
     * @throws InvalidExpressionException if the expression is not correct
     */
    public abstract void setValue (String value) 
    throws InvalidExpressionException;
}
