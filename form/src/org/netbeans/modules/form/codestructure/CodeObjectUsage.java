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

import java.util.*;

/**
 * @author Tomas Pavek
 */

final class CodeObjectUsage {

    private UsedCodeObject usedObject;
    private java.util.List usageList;

    public CodeObjectUsage(UsedCodeObject usedObject) {
        this.usedObject = usedObject;
    }

    public void addUsingObject(UsingCodeObject usingObject,
                               int useType,
                               Object useCategory)
    {
        if (useCategory == null)
            throw new IllegalArgumentException();

        if (usageList == null)
            usageList = new LinkedList();

        // [check if the object is not already registered??]
        usageList.add(new ObjectUse(usingObject, useType, useCategory));

        usingObject.usageRegistered(usedObject);
    }

    public void removeUsingObject(UsingCodeObject usingObject) {
        if (usageList == null)
            return;

        boolean removed = false;
        Iterator it = usageList.iterator();
        while (it.hasNext()) {
            ObjectUse use = (ObjectUse) it.next();
            if (usingObject == use.usingObject)
                it.remove();
        }

        if (removed)
            usingObject.usedObjectRemoved(usedObject);
    }

    public Iterator getUsingObjectsIterator(int useType, Object useCategory) {
        Iterator it = usageList != null ? usageList.iterator() : null;
        return new UsageIterator(it, useType, useCategory);
    }

    public boolean isEmpty() {
        return usageList == null || usageList.size() == 0;
    }

    // -------

    private static class ObjectUse {
        Object usingObject;
        int type;
        Object category;

        ObjectUse(Object usingObject, int useType, Object useCategory) {
            this.usingObject = usingObject;
            this.type = useType;
            this.category = useCategory;
        }

        boolean matches(int type, Object category) {
            if (type != 0 && type != this.type)
                return false;
            if (category == null)
                return true;
            return category.equals(this.category);
        }
    }

    // --------

    private static class UsageIterator implements Iterator {
        private int useType;
        private Object useCategory;

        private Iterator iterator;
        private Object next;

        public UsageIterator(Iterator iterator,
                             int useType, Object useCategory)
        {
            this.iterator = iterator;
            this.useType = useType;
            this.useCategory = useCategory;
        }

        public boolean hasNext() {
            if (iterator == null)
                return false;
            if (next != null)
                return true;

            while (iterator.hasNext()) {
                ObjectUse use = (ObjectUse) iterator.next();
                if (use.matches(useType, useCategory)) {
                    next = use.usingObject;
                    return true;
                }
            }
            return false;
        }

        public Object next() {
            if (!hasNext())
                throw new NoSuchElementException();

            Object nextObject = next;
            next = null;
            return nextObject;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
