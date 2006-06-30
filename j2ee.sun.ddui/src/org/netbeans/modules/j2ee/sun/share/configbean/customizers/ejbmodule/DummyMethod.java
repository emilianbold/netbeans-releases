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
 * DummyMethod.java
 *
 * Created on February 5, 2005, 11:07 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;

/**
 *
 * @author admin
 */
public class DummyMethod {

    String[] parameterTypes;
    String name;
    boolean isMethod;

    /** Creates a new instance of DummyMethod */
    public DummyMethod() {
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String[] getParameterTypes(){
        return parameterTypes;
    }
    
    public void setParameterTypes(String[] parameterTypes){
        this.parameterTypes = parameterTypes;
    }

    public void setIsMethod(boolean isMethod){
        this.isMethod = isMethod;
    }

    public boolean isMethod(){
        return isMethod;
    }
    
}
