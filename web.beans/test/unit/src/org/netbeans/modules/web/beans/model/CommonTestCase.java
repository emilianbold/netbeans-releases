/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.support.JavaSourceTestCase;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.web.beans.api.model.ModelUnit;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.web.beans.api.model.WebBeansModelFactory;


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
    
    protected void inform( String message ){
        System.out.println(message);
    }
    
    /**
     * This method should be changed to loading jar which injection annotations
     * into classpath.   
     */
    private void initAnnotations() throws IOException{
        TestUtilities.copyStringToFileObject(srcFO, "javax/enterprise/inject/BindingType.java",
                "package javax.enterprise.inject; " +
                "import static java.lang.annotation.ElementType.ANNOTATION_TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({ElementType.ANNOTATION_TYPE}) "+          
                "public @interface BindingType  {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "javax/enterprise/inject/Any.java",
                "package javax.enterprise.inject; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@BindingType " +
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
                "@BindingType " +
                "@Retention(RUNTIME) "+
                "@Target({FIELD, PARAMETER}) "+          
                "public @interface New  { " +
                " Class<?> value() ; "+
                "}");
        
        TestUtilities.copyStringToFileObject(srcFO, "javax/enterprise/inject/Current.java",
                "package javax.enterprise.inject; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "@BindingType " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+          
                "public @interface Current  {} ");
        
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
        
        TestUtilities.copyStringToFileObject(srcFO, "javax/enterprise/inject/NonBinding.java",
                "package javax.enterprise.inject; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({METHOD }) "+          
                "public @interface NonBinding  {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "javax/enterprise/inject/Initializer.java",
                "package javax.enterprise.inject; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.CONSTRUCTOR; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({METHOD,CONSTRUCTOR }) "+          
                "public @interface Initializer  {}");
        
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
        
        TestUtilities.copyStringToFileObject(srcFO, "javax/enterprise/inject/deployment/Specializes.java",
                "package javax.enterprise.inject.deployment; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "import java.lang.annotation.RetentionPolicy; "+
                "@Retention(RUNTIME) "+
                "@Target({TYPE,METHOD }) "+          
                "public @interface Specializes  {}");
    }

}
