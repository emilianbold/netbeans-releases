/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.debugger.jpda.heapwalk;

import org.netbeans.lib.profiler.heap.GCRoot;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.HeapSummary;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.JavaClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import java.util.regex.Pattern;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;

/**
 *
 * @author Martin Entlicher
 */
public class HeapImpl implements Heap {
    
    private JPDADebugger debugger;
    
    /** Creates a new instance of HeapImpl */
    public HeapImpl(JPDADebugger debugger) {
        this.debugger = debugger;
    }
    
    public JPDADebugger getDebugger() {
        return debugger;
    }

    public HeapSummary getSummary() {
        return new DebuggerHeapSummary(debugger);
    }

    public List<JavaClass> getAllClasses() {
        List<JPDAClassType> allClasses = debugger.getAllClasses();
        long[] counts = debugger.getInstanceCounts(allClasses);
        List<JavaClass> javaClasses = new ArrayList<JavaClass>(allClasses.size());
        int i = 0;
        for (JPDAClassType clazz : allClasses) {
            javaClasses.add(new JavaClassImpl(this, clazz, counts[i++]));
        }
        return javaClasses;
    }
    
    public List getBiggestObjectsByRetainedSize(int number) {
        return null;
    }
 
    public Instance getInstanceByID(long id) {
         return null;
    }
    
    public JavaClass getJavaClassByID(long id) {
        return null;
    }
    
    public JavaClass getJavaClassByName(String name) {
        List<JPDAClassType> classes = debugger.getClassesByName(name);
        if (classes.size() == 0) {
            return null;
        }
        return new JavaClassImpl(this, classes.get(0), classes.get(0).getInstanceCount());
    }

    public Collection getJavaClassesByRegExp(String regexp) {
        List<JPDAClassType> allClasses = debugger.getAllClasses();
        Collection result = new ArrayList(256);
        Pattern pattern = Pattern.compile(regexp);

        for (JPDAClassType clazz : allClasses) {
            if (pattern.matcher(clazz.getName()).matches()) {
                result.add(new JavaClassImpl(this, clazz, clazz.getInstanceCount()));
            }
        }
        return result;
    }


    public Collection getGCRoots() {
        return Collections.emptyList();
    }

    public GCRoot getGCRoot(Instance instance) {
        return null;
    }
    
    public Properties getSystemProperties() {
        // TODO
        return null;
    }

    private static final class DebuggerHeapSummary implements HeapSummary {
        
        private JPDADebugger debugger;
        
        public DebuggerHeapSummary(JPDADebugger debugger) {
            this.debugger = debugger;
        }
        
        public long getTotalLiveBytes() {
            return -1;
        }

        public long getTotalLiveInstances() {
            long[] counts = debugger.getInstanceCounts(debugger.getAllClasses());
            long sum = 0;
            for (long c : counts) {
                sum += c;
            }
            return sum;
        }

        public long getTotalAllocatedBytes() {
            return -1L;
        }

        public long getTotalAllocatedInstances() {
            return -1L;
        }

        public long getTime() {
            return System.currentTimeMillis();
        }
        
    }
    
}
