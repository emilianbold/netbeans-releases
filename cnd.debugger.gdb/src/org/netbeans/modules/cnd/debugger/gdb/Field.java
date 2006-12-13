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

package org.netbeans.modules.cnd.debugger.gdb;

/**
 * Represents one field. This interface is extended by {@link ObjectVariable}
 * interface, if the represented field contains not primitive value (object
 * value).
 *
 * @see ObjectVariable
 * @author Nik Molchanov (copied from Jan Jancura's JPDA implementation)
 */
public interface Field extends Variable {

    /**
     * Declared name of field.
     *
     * @return name of this field.
     */
    public abstract String getName ();

    /**
     * Returns name of enclosing class.
     *
     * @return name of enclosing class
     */
    public abstract String getClassName ();

    /**
     * Declared type of this field.
     *
     * @return declared type of this field
     */
    public abstract String getDeclaredType ();

    /**
     * Returns <code>true</code> for static fields.
     *
     * @return <code>true</code> for static fields
     */
    public abstract boolean isStatic ();

    /**
     * Sets value of this field represented as text.
     *
     * @return sets value of this field represented as text
     * @throws InvalidExpressionException if the expression is not correct
     */
    public abstract void setValue (String value) 
    throws InvalidExpressionException;
}
