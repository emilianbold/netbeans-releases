/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
 * File         : IOperationChangeListener.java
 * Version      : 1.0
 * Description  : Listener for operation changes in the Describe model.
 * Author       : Darshan
 */
package org.netbeans.modules.uml.integration.ide.listeners;
import org.netbeans.modules.uml.integration.ide.events.MethodInfo;

/**
 *  Interface for classes that need to be notified of changes to operations
 * in the Describe model.
 *
 * @author  Darshan
 * @version 1.0
 */
public interface IOperationChangeListener {
    public boolean operationAdded(MethodInfo clazz, boolean beforeChange);

    public boolean operationChanged(MethodInfo oldC, MethodInfo newC,
                                    boolean beforeChange);

    public boolean operationDeleted(MethodInfo clazz, boolean beforeChange);
    
    public boolean operationDuplicated(MethodInfo oldM, MethodInfo newM, boolean beforeChange);
}
