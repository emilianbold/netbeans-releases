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

package org.netbeans.modules.mobility.e2e.classdata;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author Michal Skvor
 */
public class MethodData {
    
    private final String name, parentClassName;
    private final ClassData returnType;
    private final List<MethodParameter> parameters;
    
    private int requestID;

    public MethodData( String parentClassName, String name, ClassData returnType, List<MethodParameter> parameters ) {
        this.name = name;
        this.parentClassName = parentClassName;
        this.returnType = returnType;
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public String getParentClassName() {
        return parentClassName;
    }
    
    public ClassData getReturnType() {
        return returnType;
    }

    public List<MethodParameter> getParameters() {
        return Collections.unmodifiableList( parameters );
    }
    
    public void setRequestID( int requestID ) {
        this.requestID = requestID;
    }
    
    public int getRequestID() {
        return requestID;
    }
    
    @Override
    public boolean equals( Object o ) {
        if(!( o instanceof MethodData )) return false;
        MethodData md = (MethodData) o;
        if (!md.getParentClassName().equals(parentClassName)) return false;
        if (!md.getName().equals(name)) return false;
        if (!md.getReturnType().equals( returnType )) return false;
        if (md.getParameters().size() != parameters.size()) return false;
        for (int i = 0; i < parameters.size(); i++ ) {
            if (!md.getParameters().get( i ).equals( parameters.get( i ))) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return parentClassName.hashCode() + name.hashCode() + returnType.hashCode() + parameters.hashCode();
    }
}
