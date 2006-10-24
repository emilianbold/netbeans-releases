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
package org.netbeans.api.java.source.test.support;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.junit.NbTestCase;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jan Lahoda
 */
public class MemoryValidator extends NbTestCase {
    
    private static final boolean ENABLED = Boolean.getBoolean("org.netbeans.api.java.source.test.support.MemoryValidator.enable");
    
    private TestCase delegate;
    
    /** Creates a new instance of MemoryValidator */
    public MemoryValidator(TestCase delegate) {
        super(delegate.getName());
        
        this.delegate = delegate;
    }
    
    public static Test wrap(Test t) {
        if (t instanceof TestCase) {
            return wrap((TestCase) t);
        }
        if (t instanceof TestSuite) {
            return wrap((TestSuite) t);
        }
        
        throw new IllegalArgumentException("Unknown type to wrap");
    }
    
    public static TestCase wrap(TestCase t) {
        return new MemoryValidator(t);
    }
    
    public static TestSuite wrap(TestSuite t) {
        TestSuite result = new TestSuite();
        
        for (int cntr = 0; cntr < t.testCount(); cntr++) {
            result.addTest(wrap(t.testAt(cntr)));
        }
        
        return result;
    }
    
    protected @Override void runTest() throws Throwable {
        delegate.runBare();
        
        if (ENABLED) {
            //if the tests passes, check if all the DataObjects created during the test are reclaimable.
            //the same for all corresponding JavaSources.
            long start = System.currentTimeMillis();
            long end = -1;
            
            try {
                Collection<FileObject> allFileObjects = null;
                
                try {
                    Class poolClass = Class.forName("org.openide.loaders.DataObjectPool");
                    Method getPOOL = poolClass.getDeclaredMethod("getPOOL", new Class[0]);
                    getPOOL.setAccessible(true);
                    Object pool = getPOOL.invoke(null, new Object[0]);
                    Field m = poolClass.getDeclaredField("map");
                    m.setAccessible(true);
                    
                    Map<FileObject, Object> map = (Map) m.get(pool);
                    
                    allFileObjects = new HashSet(map.keySet());
                } catch  (ThreadDeath t) {
                    throw t;
                } catch (Throwable t) {
                    ErrorManager.getDefault().notify(t);
                }
                
                if (allFileObjects != null) {
                    for (Iterator<FileObject> i = allFileObjects.iterator(); i.hasNext(); ){
                        FileObject file = i.next();
                        
                        i.remove();
                        
                        String name = FileUtil.getFileDisplayName(file);
                        DataObject d = DataObject.find(file);
                        JavaSource s = JavaSource.forFileObject(d.getPrimaryFile());
                        
                        if (s != null) {
                            Reference rD = new WeakReference(d);
                            Reference sD = new WeakReference(s);
                            
                            file = null;
                            d = null;
                            s = null;
                            
                            NbTestCase.assertGC(name, rD);
                            NbTestCase.assertGC(name, sD);
                        }
                    }
                }
                
                end = System.currentTimeMillis();
            } finally {
                if (end != (-1)) {
                    log(getName() + ": reference check took: " + (end - start));
                } else {
                    log(getName() + ": reference check failed");
                }
            }
        }
    }

}
