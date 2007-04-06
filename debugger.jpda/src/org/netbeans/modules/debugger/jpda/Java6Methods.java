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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda;

import java.util.ArrayList;
import java.util.List;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import org.openide.ErrorManager;

/**
 * Utility calls of methods defined in JDK 1.6 and newer, through reflection.
 * 
 * @author Martin Entlicher
 */
public class Java6Methods {
    
    private static final boolean IS_JDK_16 = !System.getProperty("java.version").startsWith("1.5"); // NOI18N
    
    /** Creates a new instance of Java6Methods */
    private Java6Methods() {
    }
    
    public static boolean isJDK6() {
        return IS_JDK_16;
    }
    
    public static long[] instanceCounts(VirtualMachine vm, List<ReferenceType> refTypes) {
        try {
            java.lang.reflect.Method method = VirtualMachine.class.getMethod("instanceCounts", new Class[] { List.class });
            Object instanceCounts = method.invoke(vm, new Object[] { refTypes });
            return (long[]) instanceCounts;
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return new long[refTypes.size()];
    }
    
    public static List<ObjectReference> instances(ReferenceType refType, long maxInstances) {
        try {
            java.lang.reflect.Method method = ReferenceType.class.getMethod("instances", new Class[] { Long.TYPE });
            Object instances = method.invoke(refType, new Object[] { maxInstances });
            return (List<ObjectReference>) instances;
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return new ArrayList<ObjectReference>();
    }
    
    public static List<ObjectReference> referringObjects(ObjectReference ref, long maxReferrers) {
        
        try {
            java.lang.reflect.Method method = ObjectReference.class.getMethod("referringObjects", new Class[] { Long.TYPE });
            Object referringObjects = method.invoke(ref, new Object[] { maxReferrers });
            return (List<ObjectReference>) referringObjects;
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return new ArrayList<ObjectReference>();
    }
    
}
