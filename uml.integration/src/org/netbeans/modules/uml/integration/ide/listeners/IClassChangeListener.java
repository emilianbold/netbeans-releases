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
 * File         : IClassChangeListener.java
 * Version      : 1.1
 * Description  : Listens to class changes from the Describe model.
 * Author       : Darshan
 */
package org.netbeans.modules.uml.integration.ide.listeners;
import java.util.*;
import org.netbeans.modules.uml.integration.ide.events.ClassInfo;

/**
 * An interface for classes that wish to be notified of class change
 * events in the Describe model. IDE-integrations should implement this
 * class, instead of directly inheriting from Describe's event sink
 * interfaces.
 *
 * @author  Darshan
 * @version 1.0
 */
public interface IClassChangeListener {
    public boolean classAdded(ClassInfo clazz, boolean beforeChange);

    public boolean classChanged(ClassInfo oldC, ClassInfo newC,
                                boolean beforeChange);

    public boolean classDeleted(ClassInfo clazz, boolean beforeChange);

    public boolean classesDeleted(Vector classesToBeDeleted, String packageToBeDeleted);
    public boolean classTransformed(ClassInfo oldC, ClassInfo newC, boolean beforeChange);
}

