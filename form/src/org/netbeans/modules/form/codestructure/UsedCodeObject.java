/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.codestructure;

import java.util.Iterator;

/**
 * @author Tomas Pavek
 */

interface UsedCodeObject {

    // use type constants
    public final int DEFINING = 1; // the used object defines the using object
    public final int USING = 2; // the used object is just used (as a parameter)

    void addUsingObject(UsingCodeObject usingObject,
                        int useType,
                        Object useCategory);

    boolean removeUsingObject(UsingCodeObject usingObject);

    Iterator getUsingObjectsIterator(int useType, Object useCategory);
}
