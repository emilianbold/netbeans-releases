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
