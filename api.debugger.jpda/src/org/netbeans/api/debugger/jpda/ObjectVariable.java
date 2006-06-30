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
 * Represents instance of some object in debugged JVM. This interface can
 * be optionally inplemented by a implementation of {@link LocalVariable} or
 * {@link Field} interfaces.
 *
 * <pre style="background-color: rgb(255, 255, 102);">
 * Since JDI interfaces evolve from one version to another, it's strongly recommended
 * not to implement this interface in client code. New methods can be added to
 * this interface at any time to keep up with the JDI functionality.</pre>
 *
 * @see LocalVariable
 * @see Field
 * @see This
 * @see Super
 * @see JPDAThread#getContendedMonitor
 * @see JPDAThread#getOwnedMonitors
 *
 * @author   Jan Jancura
 */
public interface ObjectVariable extends Variable {

    /**
     * Calls {@link java.lang.Object#toString} in debugged JVM and returns
     * its value.
     *
     * @return toString () value of this instance
     */
    public abstract String getToStringValue () throws InvalidExpressionException;
    
    /**
     * Calls given method in debugged JVM on this instance and returns
     * its value.
     *
     * @param methodName a name of method to be called
     * @param signature a signature of method to be called
     * @param arguments a arguments to be used
     *
     * @return value of given method call on this instance
     */
    public abstract Variable invokeMethod (
        String methodName,
        String signature,
        Variable[] arguments
    ) throws NoSuchMethodException, InvalidExpressionException;

    /**
     * Number of fields defined in this object.
     *
     * @return number of fields defined in this object
     */
    public abstract int getFieldsCount ();

    /**
     * Returns field defined in this object.
     *
     * @param name a name of field to be returned
     *
     * @return field defined in this object
     */
    public abstract Field getField (String name);

    /**
     * Returns non static fields defined in this object.
     *
     * @param from the index of first field to be returned
     * @param to the index of last field, exclusive
     *
     * @return fields defined in this object that are greater then or equal to
     * <code>from</code> index and less then <code>to</code> index.
     */
    public abstract Field[] getFields (int from, int to);

    /**
     * Return all static fields.
     *
     * @return all static fields
     */
    public abstract Field[] getAllStaticFields (int from, int to);

    /**
     * Return all inherited fields.
     *
     * @return all inherited fields
     */
    public abstract Field[] getInheritedFields (int from, int to);
    
    /**
     * Returns representation of super class of this object.
     *
     * @return representation of super class of this object
     */
    public abstract Super getSuper ();
}
