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

import com.sun.jdi.AbsentInformationException;

import java.util.List;

/**
 * Represents type of an object (class, interface, array) in the debugged process.
 * 
 * <pre style="background-color: rgb(255, 255, 102);">
 * Since JDI interfaces evolve from one version to another, it's strongly recommended
 * not to implement this interface in client code. New methods can be added to
 * this interface at any time to keep up with the JDI functionality.</pre>
 *
 * @since 2.7
 *
 * @author Martin Entlicher
 */
public interface JPDAClassType {
    
    /*
    List<ObjectVariable> instances(long maxInstances);
    
    long instanceCount();
     */
    
    /**
     * Get the name of this type.
     * @return fully qualified name of this type.
     */
    String getName();
    
    /**
     * Get the source name of this type.
     * @return the source file name of this type.
     */
    String getSourceName() throws AbsentInformationException;
    
    /**
     * Returns the class object variable, that corresponds to this type in the target VM.
     * @return the class object variable.
     */
    ClassVariable classObject();
    
    /**
     * Provide a list of static fields declared in this type.
     * @return the list of {@link org.netbeans.api.debugger.jpda.Field} objects
     *         representing static fields.
     */
    List<Field> staticFields();
    
}
