/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.debugger.jpda;


/**
 * Represents instance of some object in debugged JVM. This interface can 
 * be optionally inplemented by a implementation of {@link LocalVariable} or 
 * {@link Field} interfaces.
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
    public abstract String getToStringValue ();
    
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
    ) throws NoSuchMethodException;

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
     * Returns fields defined in this object.
     *
     * @param from a index of first field to be returned
     * @param to a index of last field to be returned
     *
     * @return fields defined in this object
     */
    public abstract Field[] getFields (int from, int to);
    
    /**
     * Returns representation of super class of this object.
     *
     * @return representation of super class of this object
     */
    public abstract Super getSuper ();
}
