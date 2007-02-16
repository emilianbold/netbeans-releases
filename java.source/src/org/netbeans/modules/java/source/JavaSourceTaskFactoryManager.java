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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.source;

import org.netbeans.api.java.source.JavaSourceTaskFactory;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Lahoda
 */
public final class JavaSourceTaskFactoryManager {
    
    private static JavaSourceTaskFactoryManager INSTANCE;
    
    public static synchronized void register() {
        INSTANCE = new JavaSourceTaskFactoryManager();
    }
    
    private Lookup.Result<JavaSourceTaskFactory> factories;
    
    /** Creates a new instance of JavaSourceTaskFactoryManager */
    private JavaSourceTaskFactoryManager() {
        final RequestProcessor.Task updateTask = new RequestProcessor("JavaSourceTaskFactoryManager Worker", 1).create(new Runnable() {
            public void run() {
                update();
            }
        });
        
        factories = Lookup.getDefault().lookupResult(JavaSourceTaskFactory.class);
        factories.addLookupListener(new LookupListener() {
            public void resultChanged(LookupEvent ev) {
                updateTask.schedule(0);
            }
        });
        
        update();
    }
    
    private void update() {
        for (JavaSourceTaskFactory f : factories.allInstances()) {
            ACCESSOR.fireChangeEvent(f);
        }
    }
    
    public static interface Accessor {
        public abstract void fireChangeEvent(JavaSourceTaskFactory f);
    }
    
    public static Accessor ACCESSOR;
    
}
