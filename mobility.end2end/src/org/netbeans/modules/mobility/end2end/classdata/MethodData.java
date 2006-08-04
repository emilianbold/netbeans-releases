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

/*
 * MethodData.java
 *
 * Created on July 7, 2005, 11:38 AM
 *
 */
package org.netbeans.modules.mobility.end2end.classdata;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Michal Skvor
 */
public class MethodData {
    
    protected String name;
    
    protected String returnType;
    protected List<TypeData> paramTypes;
    
    /** Creates a new instance of MethodData */
    public MethodData( String name ) {
        this.name = name;
        
        paramTypes = new ArrayList<TypeData>();
    }
    
    public String getName() {
        return name;
    }
    
    public void setReturnType( final String returnType ) {
        this.returnType = returnType;
    }
    
    public String getReturnType() {
        return returnType;
    }
    
    public void setParameterTypes( final List<TypeData> paramTypes ) {
        this.paramTypes = paramTypes;
    }
    
    public List<TypeData> getParameterTypes() {
        return paramTypes;
    }
}

