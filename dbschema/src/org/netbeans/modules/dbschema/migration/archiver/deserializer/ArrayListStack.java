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

package org.netbeans.modules.dbschema.migration.archiver.deserializer;

import java.util.ArrayList;
import java.util.EmptyStackException;

//@olsen+MBO: local class providing an unsynchronized Stack
class ArrayListStack extends ArrayList {
    public ArrayListStack() {
    }

    public Object push(Object item) {
	add(item);
	return item;
    }

    public Object pop() {
	Object	obj;
	int	len = size();

	obj = remove(len - 1);
	return obj;
    }

    public Object peek() {
	int	len = size();

	if (len == 0)
	    throw new EmptyStackException();
	return get(len - 1);
    }

    public boolean empty() {
	return size() == 0;
    }

    public int search(Object o) {
	int i = lastIndexOf(o);

	if (i >= 0) {
	    return size() - i;
	}
	return -1;
    }
}
