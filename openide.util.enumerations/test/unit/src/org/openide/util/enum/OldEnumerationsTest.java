/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.util.enum;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.openide.util.EnumerationsTest;
import org.openide.util.EnumerationsTest.QueueProcess;

/** Implement factory methods from EnumerationsTest, shares the same tests
 * with EnumerationsTest.
 *
 * @author Jaroslav Tulach
 */
public class OldEnumerationsTest extends EnumerationsTest {
    
    /** Creates a new instance of EnumerationsTest */
    public OldEnumerationsTest(String testName) {
        super(testName);
    }
    
    protected Enumeration singleton(Object obj) {
        return new SingletonEnumeration(obj);
    }
    
    protected Enumeration convert(Enumeration en, final Map map) {
        return new AlterEnumeration(en) {
            protected Object alter(Object o) {
                return map.get(o);
            }
        };
    }
    
    protected Enumeration removeDuplicates(Enumeration en) {
        return new RemoveDuplicatesEnumeration(en);
    }
    
    protected Enumeration removeNulls(Enumeration en) {
        return new FilterEnumeration(en);
    }
    
    protected Enumeration concat(Enumeration en1, Enumeration en2) {
        return new SequenceEnumeration(en1, en2);
    }
    
    protected Enumeration array(Object[] arr) {
        return new ArrayEnumeration(arr);
    }
    
    protected Enumeration filter(Enumeration en, final Set filter) {
        return new FilterEnumeration(en) {
            protected boolean accept(Object obj) {
                return filter.contains(obj);
            }
        };
    }
    protected Enumeration filter(Enumeration en, final QueueProcess filter) {
        en = new AlterEnumeration(en) {
            public Object alter(Object alter) {
                return filter.process(alter, null);
            }
        };
        
        return new FilterEnumeration(en);
    }
    
    protected Enumeration concat(Enumeration enumOfEnums) {
        return new SequenceEnumeration(enumOfEnums);
    }
    
    protected Enumeration empty() {
        return new EmptyEnumeration();
    }
    
    protected Enumeration queue(Collection init, final QueueProcess process) {
        final HashMap diff = new HashMap();
        
        class QEAdd extends QueueEnumeration implements Collection {
            protected void process(Object obj) {
                Object different = process.process(obj, this);
                if (different != obj) {
                    diff.put(obj, different);
                }
            }
            
            public boolean add(Object o) {
                put(o);
                return true;
            }
            
            public boolean addAll(Collection c) {
                put(c.toArray());
                return true;
            }
            
            public void clear() {
                throw new IllegalStateException("Unsupported");
            }
            
            public boolean contains(Object o) {
                throw new IllegalStateException("Unsupported");
            }
            
            public boolean containsAll(Collection c) {
                throw new IllegalStateException("Unsupported");
            }
            
            public boolean isEmpty() {
                throw new IllegalStateException("Unsupported");
            }
            
            public Iterator iterator() {
                throw new IllegalStateException("Unsupported");
            }
            
            public boolean remove(Object o) {
                throw new IllegalStateException("Unsupported");
            }
            
            public boolean removeAll(Collection c) {
                throw new IllegalStateException("Unsupported");
            }
            
            public boolean retainAll(Collection c) {
                throw new IllegalStateException("Unsupported");
            }
            
            public int size() {
                throw new IllegalStateException("Unsupported");
            }
            
            public Object[] toArray() {
                throw new IllegalStateException("Unsupported");
            }
            
            public Object[] toArray(Object[] a) {
                throw new IllegalStateException("Unsupported");
            }
        }
        QEAdd qe = new QEAdd();
        qe.put(init.toArray());
        
        class Change extends AlterEnumeration {
            public Change(Enumeration en) {
                super(en);
            }
            
            public Object alter(Object o) {
                if (diff.keySet().contains(o)) {
                    return diff.remove(o);
                }
                return o;
            }
        }
        
        return new Change(qe);
    }
    
}
