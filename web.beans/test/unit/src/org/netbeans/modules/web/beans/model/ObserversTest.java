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
package org.netbeans.modules.web.beans.model;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;

/**
 * @author ads
 *
 */
public class ObserversTest extends CommonTestCase {

    public ObserversTest(String testName){
        super( testName);
    }
    
    public void testSimple () throws MetadataModelException, IOException,
        InterruptedException 
    {
        createQualifier("Binding");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/EventObject.java",
                "package foo; " +
                "public class EventObject { " +
                "} ");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/TestClass.java",
                "package foo; " +
                "import javax.inject.*; "+
                "import javax.enterprise.event.Event; "+
                "public class TestClass {" +
                " @Inject @foo.Binding Event<EventObject> event; " +
                "} ");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import javax.enterprise.event.Observes; "+
                "public class Clazz {" +
                " public void eventObserver( @Observes @foo.Binding EventObject event ) {}" +
                "} ");
        
        inform("start simple observer test");
        
        MetadataModel<WebBeansModel> metaModel = createBeansModel() ;
        metaModel.runReadAction(new MetadataModelAction<WebBeansModel,Void>(){
            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType( "foo.TestClass" );
                Element clazz = ((DeclaredType)mirror).asElement();
                List<? extends Element> children = clazz.getEnclosedElements();
                List<VariableElement> injectionPoints = 
                    new ArrayList<VariableElement>( children.size());
                for (Element element : children) {
                    if ( element instanceof VariableElement ){
                        injectionPoints.add( ( VariableElement) element);
                    }
                }
                
                assertEquals(  1, injectionPoints.size());
                VariableElement var  = injectionPoints.get(0);
                assertEquals( var.getSimpleName().toString(), "event");
                List<ExecutableElement> observers = model.getObservers( var, 
                        (DeclaredType)mirror );
                assertEquals( "Should be exactly one observer method , but found " +
                        observers.size()  +" methods",  1, observers.size());
                ExecutableElement executableElement = observers.get(0);
                assertNotNull( executableElement );
                String name = executableElement.getSimpleName().toString();
                assertEquals(  "eventObserver" , name );
                return  null;
            }
        });
    }
    
    public void testCommon () throws MetadataModelException, IOException,
        InterruptedException 
    {
        createQualifier("Binding");
        createQualifier("Binding1");
        createQualifier("Binding2");

        TestUtilities.copyStringToFileObject(srcFO, "foo/SuperObject.java",
                "package foo; " + 
                "public class SuperObject { " + 
                "} ");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Iface.java",
                "package foo; " + 
                "public interface Iface { " + 
                "} ");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/EventObject.java",
                "package foo; " + 
                "public class EventObject extends SuperObject implements Iface { " + 
                "} ");

        TestUtilities.copyStringToFileObject(srcFO, "foo/TestClass.java",
                "package foo; " + 
                "import javax.inject.*; "
                + "import javax.enterprise.event.Event; "
                + "public class TestClass {"
                + " @Inject @foo.Binding  @foo.Binding2 Event<EventObject> event; "
                + "} ");

        TestUtilities.copyStringToFileObject(srcFO,"foo/Clazz1.java",
                "package foo; "
                + "import javax.enterprise.event.Observes; "
                + "public class Clazz1 {"
                + " public void eventObserver( @Observes @foo.Binding @foo.Binding2 SuperObject event ) {}"
                + " public void method( @foo.Binding @foo.Binding2 EventObject event ) {}"
                + "} ");
        
        TestUtilities.copyStringToFileObject(srcFO,"foo/Clazz2.java",
                "package foo; "
                + "import javax.enterprise.event.Observes; "
                + "public class Clazz2 {"
                + " public void eventObserver( @Observes @foo.Binding @foo.Binding2 Iface event ) {}"
                + " public void notEventObserver( @Observes @foo.Binding @foo.Binding1 EventObject event ) {}"
                + "} ");

        inform("start common observer test");

        MetadataModel<WebBeansModel> metaModel = createBeansModel();
        metaModel.runReadAction(new MetadataModelAction<WebBeansModel, Void>() {

            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType("foo.TestClass");
                Element clazz = ((DeclaredType) mirror).asElement();
                List<? extends Element> children = clazz.getEnclosedElements();
                List<VariableElement> injectionPoints = new ArrayList<VariableElement>(
                        children.size());
                for (Element element : children) {
                    if (element instanceof VariableElement) {
                        injectionPoints.add((VariableElement) element);
                    }
                }

                assertEquals( 1, injectionPoints.size());
                VariableElement var = injectionPoints.get(0);
                assertEquals("event", var.getSimpleName().toString() );
                List<ExecutableElement> observers = model.getObservers(var,
                        (DeclaredType) mirror);
                assertEquals(
                        "Should be exactly two observer methods , but found "
                                + observers.size() + " methods",
                        2, observers.size());
                boolean foundSuper = false;
                boolean foundIface = false;
                for (ExecutableElement executableElement : observers) {
                    String name = executableElement.getSimpleName().toString();
                    assertEquals( "Found unexpected event observer method :"+
                            name ,name , "eventObserver");
                    TypeElement typeElement = model.getCompilationController().
                        getElementUtilities().enclosingTypeElement( executableElement );
                    String fqnType = typeElement.getQualifiedName().toString();
                    if ( "foo.Clazz1".equals(fqnType)){
                        foundSuper = true;
                    }
                    else if( "foo.Clazz2".equals( fqnType)){
                        foundIface = true;
                    }
                }
                assertTrue( "Observer method inside Clazz1 is not found", foundSuper );
                assertTrue( "Observer method inside Clazz2 is not found" , foundIface);
                return null;
            }
        });
    }
}
