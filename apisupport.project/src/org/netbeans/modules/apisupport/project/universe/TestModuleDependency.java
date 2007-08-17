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
 * Represents one test dependency. I.e. <em>&lt;test-dependency&gt;</em>
 * element in module's <em>project.xml</em>.
 * 
 * @author pzajac
 */
public class TestModuleDependency implements Comparable {
    
    public static final String UNIT = "unit"; // NOI18N
    public static final String QA_FUNCTIONAL = "qa-functional"; // NOI18N 
    
    private final ModuleEntry module;
    // depends also on tests of modules
    private boolean test;
    // depends on execution classpath of the modules
    private boolean recursive;
    // compilation dependency 
    private boolean compile;
    
    public static final Comparator<TestModuleDependency> CNB_COMPARATOR = new Comparator<TestModuleDependency>() {
        public int compare(TestModuleDependency tmd1, TestModuleDependency tmd2) {
            return (tmd1).module.getCodeNameBase().compareTo((tmd2).module.getCodeNameBase());
        }
    };
    
    /**
     * Creates a new instance of TestModuleDependency
     */
    public TestModuleDependency(ModuleEntry me,boolean test,boolean recursive,boolean compile) {
        this.module = me;
        this.test = test;
        this.recursive = recursive;
        this.compile = compile;
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
    
    // td1 equals td2 iff cnb and all three boolean fileds of td are the same
    public boolean equals(Object o){
        if(o instanceof TestModuleDependency) {
            TestModuleDependency tmd = (TestModuleDependency) o;
            return tmd.isCompile() == this.isCompile()
                    && tmd.isRecursive() == this.isRecursive()
                    && tmd.isTest() == this.isTest()
                    && tmd.getModule().getCodeNameBase().equals(this.getModule().getCodeNameBase());
        } else {
            return false;
}
    }
    
    //compare only on cnb. ATTENTION, compareTo is not consistent with equals method!
    //i.e. two instances of TestModuleDependency can be nonequal, and tmd1.compareTo(tmd2)
    // can return 0
    public int compareTo(Object o) {
        TestModuleDependency tmd = (TestModuleDependency) o;
        return this.module.getCodeNameBase().compareTo(tmd.module.getCodeNameBase());
    }
    
    //hash from CNB only
    public int hashCode(){
        int hash = module.getCodeNameBase().hashCode();
//        if(test)  hash*=5;
//        if(recursive)  hash*=7;
//        if(compile) hash*=11;
        return hash;
    }
}
