/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
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
