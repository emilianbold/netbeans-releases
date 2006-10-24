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
package org.netbeans.test.codegen;

/**
 * Source to rename, golden file
 *
 * @author  Pavel Flaska
 */
public class RenameTestClass {

    private RenameTestClass.ClassToRename renamedCl;

    /** Creates a new instance of RenameTestClass */
    public RenameTestClass() {
        renamedCl = new ClassToRename(5);
    }

    private final RenameTestClass.ClassToRename containsClassToRename(int x) {
        ClassToRename confusingVar;
        confusingVar = renamedCl;
        ClassToRename result = (ClassToRename) confusingVar;
        int y = result.getData();
        return new ClassToRename(y);
    }
    
    // class to rename
    static class ClassToRename {
        int a;
        
        public ClassToRename(int a) {
            this.a = a;
        }
        
        public int getData() {
            return a;
        }
    }
}
