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

package org.netbeans.modules.apisupport.project.universe;

import java.util.Comparator;

/**
 * Test dependedency entry from project.xml
 * @author pzajac
 */
public class TestModuleDependency {
    public static final String UNIT = "unit"; // NOI18N
    public static final String QA_FUNCTIONAL = "qa-functional"; // NOI18N 
    private final ModuleEntry module;
    // dependendends also on tests of modules
    private boolean test;
    // depends on execution classpath of the modules
    private boolean recursive;
    // compilation dependency 
    private boolean compile;

    public static final Comparator CNB_COMPARATOR = new CndsComparator();
    
    private static final class CndsComparator implements Comparator {
        public int compare(Object tmd1,Object tmd2) {
            return ((TestModuleDependency)tmd1).module.getCodeNameBase().compareTo(((TestModuleDependency)tmd2).module.getCodeNameBase());
        }
    }
    /**
     * Creates a new instance of TestModuleDependency
     */
    public TestModuleDependency(ModuleEntry me,boolean test,boolean recursive,boolean compile) {
        this.module = me;
        this.setTest(test);
        this.setRecursive(recursive);
        this.setCompile(compile);
    }

    public ModuleEntry getModule() {
        return module;
    }

    public boolean isTest() {
        return test;
    }

    public void setTest(boolean test) {
        this.test = test;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public boolean isCompile() {
        return compile;
    }

    public void setCompile(boolean compile) {
        this.compile = compile;
    }
    
}
