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

package org.netbeans.modules.java.hints.infrastructure;
import org.netbeans.modules.java.hints.infrastructure.CreatorBasedLazyFixList;
import org.netbeans.modules.java.hints.*;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.support.EditorAwareJavaSourceTaskFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Jan Lahoda
 */
public class LazyHintComputationFactory extends EditorAwareJavaSourceTaskFactory {
    
    private static Map<FileObject, List<Reference<CreatorBasedLazyFixList>>> file2Creators = new WeakHashMap<FileObject, List<Reference<CreatorBasedLazyFixList>>>();
    
    /** Creates a new instance of LazyHintComputationFactory */
    public LazyHintComputationFactory() {
        super(Phase.RESOLVED, Priority.LOW);
    }

    public CancellableTask<CompilationInfo> createTask(FileObject file) {
        return new LazyHintComputation(file);
    }
 
    private static void rescheduleImpl(FileObject file) {
        LazyHintComputationFactory f = Lookup.getDefault().lookup(LazyHintComputationFactory.class);
        
        if (f != null) {
            f.reschedule(file);
        }
    }
    
    public static void addToCompute(FileObject file, CreatorBasedLazyFixList list) {
        synchronized (LazyHintComputationFactory.class) {
            List<Reference<CreatorBasedLazyFixList>> references = file2Creators.get(file);
            
            if (references == null) {
                file2Creators.put(file, references = new ArrayList<Reference<CreatorBasedLazyFixList>>());
            }
            
            references.add(new WeakReference(list));
        }
        
        rescheduleImpl(file);
    }
    
    public static synchronized List<CreatorBasedLazyFixList> getAndClearToCompute(FileObject file) {
        List<Reference<CreatorBasedLazyFixList>> references = file2Creators.get(file);
        
        if (references == null) {
            return Collections.emptyList();
        }
        
        List<CreatorBasedLazyFixList> result = new ArrayList<CreatorBasedLazyFixList>();
        
        for (Reference<CreatorBasedLazyFixList> r : references) {
            CreatorBasedLazyFixList c = r.get();
            
            if (c != null) {
                result.add(c);
            }
        }
        
        references.clear();
        
        return result;
    }
}
