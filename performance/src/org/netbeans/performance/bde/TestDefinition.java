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
package org.netbeans.performance.bde;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;

/** Describes one test */
public final class TestDefinition {
    
    private String className;
    private List methodPatterns;
    private List arguments;
    
   /** Creates new Interval */
    public TestDefinition(String className, List methodPatterns, List arguments) {
        this.className = className;
        if (methodPatterns == null) {
            this.methodPatterns = new ArrayList(1);
        } else {
            this.methodPatterns = methodPatterns;
        }
        if (arguments == null) {
            this.arguments = new ArrayList(1);
        } else {
            this.arguments = arguments;
        }
    }
    
    /** @return className */
    public String getClassName() {
        return className;
    }
    
    /** @return iteration of method patterns (Strings) */
    public Iterator getMethodPatterns() {
        return Collections.unmodifiableList(methodPatterns).iterator();
    }
    
    /** @return iteration of ArgumentSeries */
    public Iterator getArgumentSeries() {
        return Collections.unmodifiableList(arguments).iterator();
    }
}
