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

package org.netbeans.modules.junit;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.modules.junit.plugin.JUnitPlugin.CreateTestParam;

/**
 *
 * @author  Marian Petras
 */
public final class TestCreator implements TestabilityJudge {
    
    /**
     * bitmap combining modifiers PUBLIC, PROTECTED and PRIVATE
     *
     * @see  java.lang.reflect.Modifier
     */
    static final Set<Modifier> ACCESS_MODIFIERS
            = EnumSet.of(Modifier.PUBLIC,
                         Modifier.PROTECTED,
                         Modifier.PRIVATE);
    
    /** */
    private final TestGeneratorSetup setup;
    /** */
    private final JUnitVersion junitVersion;
    
    /** Creates a new instance of TestCreator */
    TestCreator(boolean loadDefaults,
                JUnitVersion junitVersion) {
        setup = new TestGeneratorSetup(loadDefaults);
        this.junitVersion = junitVersion;
    }
    
    /** Creates a new instance of TestCreator */
    TestCreator(Map<CreateTestParam, Object> params,
                JUnitVersion junitVersion) {
        setup = new TestGeneratorSetup(params);
        this.junitVersion = junitVersion;
    }
    
    /**
     */
    public void createEmptyTest(final JavaSource tstSource) throws IOException {
        AbstractTestGenerator testGenerator;
        switch (junitVersion) {
            case JUNIT3:
                testGenerator = new JUnit3TestGenerator(setup);
                break;
            case JUNIT4:
                testGenerator = new JUnit4TestGenerator(setup);
                break;
            default:
                throw new IllegalStateException("junit version not set");//NOI18N
        }
        ModificationResult result = tstSource.runModificationTask(testGenerator);
        result.commit();
    }
    
    /**
     * 
     * @return  list of names of created classes
     */
    public void createSimpleTest(ElementHandle<TypeElement> topClassToTest,
                                 JavaSource tstSource,
                                 boolean isNewTestClass) throws IOException {
        AbstractTestGenerator testGenerator;
        switch (junitVersion) {
            case JUNIT3:
                testGenerator = new JUnit3TestGenerator(
                                          setup,
                                          Collections.singletonList(topClassToTest),
                                          null,
                                          isNewTestClass);
                break;
            case JUNIT4:
                testGenerator = new JUnit4TestGenerator(
                                          setup,
                                          Collections.singletonList(topClassToTest),
                                          null,
                                          isNewTestClass);
                break;
            default:
                throw new IllegalStateException("junit version not set");//NOI18N
        }
        ModificationResult result = tstSource.runModificationTask(testGenerator);
        result.commit();
    }
    
    /**
     */
    public List<String> createTestSuite(List<String> suiteMembers,
                                        JavaSource tstSource,
                                        boolean isNewTestClass) throws IOException {
        AbstractTestGenerator testGenerator;
        switch (junitVersion) {
            case JUNIT3:
                testGenerator = new JUnit3TestGenerator(
                                          setup,
                                          null,
                                          suiteMembers,
                                          isNewTestClass);
                break;
            case JUNIT4:
                testGenerator = new JUnit4TestGenerator(
                                          setup,
                                          null,
                                          suiteMembers,
                                          isNewTestClass);
                break;
            default:
                throw new IllegalStateException("junit version not set");//NOI18N
        }
        ModificationResult result = tstSource.runModificationTask(testGenerator);
        result.commit();
        
        return testGenerator.getProcessedClassNames();
    }
    
    public TestabilityResult isClassTestable(CompilationInfo compInfo,
                                             TypeElement classElem) {
        return setup.isClassTestable(compInfo, classElem);
    }

    public boolean isMethodTestable(ExecutableElement method) {
        return setup.isMethodTestable(method);
    }

}
