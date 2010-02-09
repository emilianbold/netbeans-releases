/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.web.beans.model;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.web.beans.api.model.Result;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.support.JavaSourceTestCase;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.web.beans.api.model.ModelUnit;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.web.beans.api.model.WebBeansModelFactory;
import org.netbeans.modules.web.beans.impl.model.results.ResultImpl;


/**
 * @author ads
 *
 */
public class CommonTestCase extends JavaSourceTestCase {

    public CommonTestCase( String testName ) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        initAnnotations();
        /*URL url = FileUtil.getArchiveRoot(javax.faces.component.FacesComponent.class.getProtectionDomain().
                getCodeSource().getLocation());
        addCompileRoots( Collections.singletonList( url ));*/
    }
    
    public MetadataModel<WebBeansModel> createBeansModel() throws IOException, InterruptedException {
        IndexingManager.getDefault().refreshIndexAndWait(srcFO.getURL(), null);
        ModelUnit modelUnit = ModelUnit.create(
                ClassPath.getClassPath(srcFO, ClassPath.BOOT),
                ClassPath.getClassPath(srcFO, ClassPath.COMPILE),
                ClassPath.getClassPath(srcFO, ClassPath.SOURCE));
        return WebBeansModelFactory.createMetaModel(modelUnit);
    }
    
    public TestWebBeansModelImpl createModelImpl() throws IOException {
        return createModelImpl(false);
    }
    
    public TestWebBeansModelImpl createModelImpl(boolean fullModel) throws IOException {
        IndexingManager.getDefault().refreshIndexAndWait(srcFO.getURL(), null);
        ModelUnit modelUnit = ModelUnit.create(
                ClassPath.getClassPath(srcFO, ClassPath.BOOT),
                ClassPath.getClassPath(srcFO, ClassPath.COMPILE),
                ClassPath.getClassPath(srcFO, ClassPath.SOURCE));
        return new TestWebBeansModelImpl(modelUnit, fullModel);
    }
    
    protected void inform( String message ){
        System.out.println(message);
    }
    
    /**
     * This method should be changed to loading jar which injection annotations
     * into classpath.   
     */
    private void initAnnotations() throws IOException{
        TestUtilities.copyStringToFileObject(srcFO, "javax/inject/Qualifier.java",
                "package javax.inject; " +
                "import static java.lang.annotation.ElementType.ANNOTATION_TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({ElementType.ANNOTATION_TYPE}) "+          
                "public @interface Qualifier  {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "javax/inject/Named.java",
                "package javax.inject; " +
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Qualifier "+
                "public @interface Named  { " +
                " String value(); "+
                "}");
        
        TestUtilities.copyStringToFileObject(srcFO, "javax/inject/Inject.java",
                "package javax.inject; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.CONSTRUCTOR; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, CONSTRUCTOR}) "+
                "public @interface Inject  {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "javax/enterprise/inject/Any.java",
                "package javax.enterprise.inject; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "import javax.inject.Qualifier; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+          
                "public @interface Any  {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "javax/enterprise/inject/New.java",
                "package javax.enterprise.inject; " +
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "import javax.inject.Qualifier; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({FIELD, PARAMETER}) "+          
                "public @interface New  { " +
                " Class<?> value() ; "+
                "}");
        
        TestUtilities.copyStringToFileObject(srcFO, "javax/enterprise/inject/Default.java",
                "package javax.enterprise.inject; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import javax.inject.Qualifier; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+          
                "public @interface Default  {} ");
        
        TestUtilities.copyStringToFileObject(srcFO, "javax/enterprise/inject/Produces.java",
                "package javax.enterprise.inject; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD }) "+          
                "public @interface Produces  {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "javax/enterprise/inject/Nonbinding.java",
                "package javax.enterprise.inject; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({METHOD }) "+          
                "public @interface Nonbinding  {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "javax/enterprise/event/Observes.java",
                "package javax.enterprise.event; " +
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({PARAMETER}) "+          
                "public @interface Observes  {}");
        
        
        TestUtilities.copyStringToFileObject(srcFO, "javax/enterprise/inject/Disposes.java",
                "package javax.enterprise.inject; " +
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({PARAMETER}) "+          
                "public @interface Disposes  {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "javax/enterprise/inject/Specializes.java",
                "package javax.enterprise.inject; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({TYPE,METHOD }) "+          
                "public @interface Specializes  {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "javax/enterprise/inject/Alternative.java",
                "package javax.enterprise.inject; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, TYPE}) "+          
                "public @interface Alternative  {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "javax/enterprise/inject/Stereotype.java",
                "package javax.enterprise.inject; " +
                "import static java.lang.annotation.ElementType.ANNOTATION_TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({ANNOTATION_TYPE}) "+          
                "public @interface Stereotype  {}");
    }

    public final void assertFindParameterResultInjectables(VariableElement element,
            TestWebBeansModelProviderImpl provider,
            String... injectables) {
        Result result = provider.findParameterInjectable(element, null);
        assertResultInjectables(result, injectables);
    }

    public final void assertFindParameterResultProductions(VariableElement element,
            TestWebBeansModelProviderImpl provider,
            String... injectables) {
        Result result = provider.findParameterInjectable(element, null);
        assertResultProductions(result, injectables);
    }

    public final void assertFindParameterResultProductionsVar(VariableElement element,
            TestWebBeansModelProviderImpl provider,
            String... injectables) {
        Result result = provider.findParameterInjectable(element, null);
        assertResultProductions(result, true, injectables);
    }

    public final void assertFindVariableResultInjectables(VariableElement element,
            TestWebBeansModelProviderImpl provider,
            String... injectables) {
        Result result = provider.findVariableInjectable(element, null);
        assertResultInjectables(result, injectables);
    }

    public final void assertFindVariableResultProductions(VariableElement element,
            TestWebBeansModelProviderImpl provider,
            String... injectables) {
        Result result = provider.findVariableInjectable(element, null);
        assertResultProductions(result, injectables);
    }

    public final void assertFindVariableResultProductionsVar(VariableElement element,
            TestWebBeansModelProviderImpl provider,
            String... injectables) {
        Result result = provider.findVariableInjectable(element, null);
        assertResultProductions(result, true, injectables);
    }

    public final void assertFindVariableResultAllProductions(VariableElement element,
            TestWebBeansModelProviderImpl provider,
            String... injectables) {
        Result result = provider.findVariableInjectable(element, null);
        assertResultAllProductions(result, injectables);
    }

    public final void assertResultInjectables(Result result, String... injectables) {
        assertNotNull(result);
        assertTrue("not ResultImpl instance: "+result, result instanceof ResultImpl);

        Set<TypeElement> typeElements = ((ResultImpl) result).getTypeElements();
        if (injectables == null) {
            assertEquals("no injectables expected, but found: "+typeElements, 0, typeElements.size());
        }
        assertTrue("number of injectables does not match: returned="+typeElements+" expected="+Arrays.asList(injectables), injectables.length == typeElements.size());
        Set<String> set = new HashSet<String>();
        for (TypeElement injactable : typeElements) {
            set.add(injactable.getQualifiedName().toString());
        }
        for (String inj : injectables) {
            assertTrue("Result of typesafe resolution should contain " + inj
                    + " class definition in "+set, set.contains(inj));
        }
    }

    public final void assertResultProductions(Result result, String... producers) {
        assertResultProductions(result, false, producers);
    }

    public final void assertResultProductions(Result result, boolean variable, String... producers) {
        assertNotNull(result);
        assertTrue("not ResultImpl instance: "+result, result instanceof ResultImpl);

        Set<Element> productions = ((ResultImpl) result).getProductions();
        if (producers == null) {
            assertEquals("no producers expected, but found: "+productions, 0, productions.size());
        }
        assertTrue("number of productions does not match: returned="+productions+" expected="+Arrays.asList(producers), producers.length == productions.size());
        Set<String> set = new HashSet<String>();
        for (Element injectable : productions) {
            if (variable) {
                assertTrue("injectable should be a production method," + " but found :"
                        + injectable.getKind(), injectable instanceof VariableElement);
            } else {
                assertTrue("injectable should be a production method," + " but found :"
                        + injectable.getKind(), injectable instanceof ExecutableElement);
            }
            set.add(injectable.getSimpleName().toString());
        }
        for (String prod : producers) {
            assertTrue("Result of typesafe resolution should contain " + prod
                    + " producer in "+set, set.contains(prod));
        }
    }

    public final void assertResultAllProductions(Result result, String... injectables) {
        assertNotNull(result);
        assertTrue("not ResultImpl instance: "+result, result instanceof ResultImpl);

        List<DeclaredType> typeElements = ((ResultImpl) result).getAllProductions().values().iterator().next();
        if (injectables == null) {
            assertEquals("no injectables expected, but found: "+typeElements, 0, typeElements.size());
        }
        assertTrue("number of injectables does not match: returned="+typeElements+" expected="+Arrays.asList(injectables), injectables.length == typeElements.size());
        Set<String> set = new HashSet<String>();
        for (DeclaredType injactable : typeElements) {
            set.add(((TypeElement)injactable.asElement()).getQualifiedName().toString());
        }
        for (String inj : injectables) {
            assertTrue("Result of typesafe resolution should contain " + inj
                    + " class definition in "+set, set.contains(inj));
        }
    }


}
