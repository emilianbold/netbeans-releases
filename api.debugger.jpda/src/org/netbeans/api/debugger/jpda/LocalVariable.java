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
