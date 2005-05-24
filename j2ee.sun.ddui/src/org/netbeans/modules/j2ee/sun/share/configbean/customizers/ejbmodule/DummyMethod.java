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
