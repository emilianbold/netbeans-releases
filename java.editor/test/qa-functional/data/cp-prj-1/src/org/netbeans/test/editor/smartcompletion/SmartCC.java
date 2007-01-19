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
package org.netbeans.test.editor.smartcompletion;

import java.io.FileReader;


/**
 * @author jp159440
 */
public class SmartCC extends Exception{
    
    /** Creates a new instance of SmartCC */
    public SmartCC() {        
        //super();
    }
    
    class MyClass {};
    class InnerClass extends MyClass {}
    
    public void method(MyClass s) {        
    }
    
    public MyClass action() {
        //method( )
        //return new 
        return null;
        
    }
    
    public void throwTest(int a) {
        try {
            FileReader f = new FileReader("sss");
        } 
        
        
        
        
    }
    
    
    
}
