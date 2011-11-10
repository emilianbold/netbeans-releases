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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.web.beans.analysis;

import java.io.IOException;

import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.openide.filesystems.FileObject;


/**
 * @author ads
 *
 */
public class WebBeansAnalysisTest extends BaseAnalisysTestCase {
    
    
    public WebBeansAnalysisTest(String testName) {
        super(testName);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.BaseAnalisysTestCase#createTask()
     */
    @Override
    protected WebBeansAnalysisTestTask createTask() {
        return new WebBeansAnalysisTestTask( getUtilities() );
    }
    
    /*
     * ManagedBeansAnalizer.checkCtor
     */
    public void testManagedBeansCtor() throws IOException {
        getUtilities().createQualifier("Qualifier1");
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                " @Qualifier1 "+
                " public class Clazz { "+
                " private Clazz(){} "+
                "}");
        
        FileObject errorFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                " @Qualifier1 "+
                " public class Clazz1 { "+
                " public Clazz1( int i ){} "+
                "}");
        
        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz2.java",
                "package foo; " +
                " @Qualifier1 "+
                " public class Clazz2  { "+
                "}");
        
        FileObject goodFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz3.java",
                "package foo; " +
                "import javax.inject.Inject; "+
                " @Qualifier1 "+
                " public class Clazz3  { "+
                " @Inject public Clazz3( String str ){} "+
                "}");
        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result.getWarings(), "foo.Clazz");
                assertEquals( "Found unexpected errors", 0, result.getErrors().size());
            }
            
        };
        runAnalysis(errorFile , processor);
        
        processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result.getWarings(), "foo.Clazz1");
                assertEquals( "Found unexpected errors", 0, result.getErrors().size());
            }
            
        };
        runAnalysis(errorFile1 , processor);
        
        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
        runAnalysis( goodFile1, NO_ERRORS_PROCESSOR );
    }
    
    /*
     * ManagedBeansAnalizer.checkInner
     */
    public void testManagedBeansInner() throws IOException {
        getUtilities().createQualifier("Qualifier1");
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                " public class Clazz { "+
                " @Qualifier1 "+
                " class Inner{} "+
                "}");
        
        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                " public class Clazz1  { "+
                " @Qualifier1 "+
                " static class Inner{} "+
                "}");
        
        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement( result , "foo.Clazz.Inner");
            }
            
        };
        runAnalysis(errorFile , processor);
        
        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
    }
    
    /*
     * ManagedBeansAnalizer.checkAbstract
     */
    public void testManagedBeansAbstract() throws IOException {
        getUtilities().createQualifier("Qualifier1");
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                " @Qualifier1 "+
                " public abstract class Clazz { "+
                "}");
        
        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import javax.decorator.Decorator; "+
                "import javax.decorator.Delegate; "+
                "import javax.inject.Inject; "+
                " @Qualifier1 "+
                " @Decorator "+
                " public abstract class Clazz1  { "+
                " @Inject public Clazz1( @Qualifier1 @Delegate Clazz arg ){ "+
                "}");
        
        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement( result.getWarings() , "foo.Clazz");
                assertEquals( "Unxepected error found", 0, result.getErrors().size());
            }
            
        };
        runAnalysis(errorFile , processor);
        
        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
    }
    
    /*
     * ManagedBeansAnalizer.checkImplementsExtension
     */
    public void testManagedBeansImplementsExtension() throws IOException {
        getUtilities().createQualifier("Qualifier1");
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import javax.enterprise.inject.spi.Extension "+
                " @Qualifier1 "+
                " public class Clazz implements Extension { "+
                "}");
        
        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                " @Qualifier1 "+
                " public class Clazz1  { "+
                "}");
        
        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement( result.getWarings() , "foo.Clazz");
                assertEquals( "Unxepected error found", 0, result.getErrors().size());
            }
            
        };
        runAnalysis(errorFile , processor);
        
        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
    }
}
