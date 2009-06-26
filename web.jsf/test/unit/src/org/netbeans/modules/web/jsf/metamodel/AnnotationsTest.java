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
package org.netbeans.modules.web.jsf.metamodel;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.web.jsf.api.metamodel.Behavior;
import org.netbeans.modules.web.jsf.api.metamodel.Component;
import org.netbeans.modules.web.jsf.api.metamodel.JsfModel;
import org.openide.filesystems.FileUtil;


/**
 * @author ads
 *
 */
public class AnnotationsTest extends CommonTestCase {

    public AnnotationsTest( String testName ) {
        super(testName);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.metamodel.CommonTestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        URL url = FileUtil.getArchiveRoot(javax.faces.component.FacesComponent.class.getProtectionDomain().
                getCodeSource().getLocation());
        addCompileRoots( Collections.singletonList( url ));
    }

    public void testModel() throws IOException, InterruptedException {
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomComponent.java",
                "package foo; " +
                "import javax.faces.component.*; " +
                "@FacesComponent(value=\"a\") " +
                "public class CustomComponent  {" +
                "}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomBehavior.java",
                "package foo; " +
                "import javax.faces.component.behavior .*; " +
                "@FacesBehavior(value=\"b\") " +
                "public class CustomBehavior  {" +
                "}");
        
        createJsfModel().runReadAction(new MetadataModelAction<JsfModel,Void>(){

            public Void run( JsfModel model ) throws Exception {
                List<Component> components = model.getElements(Component.class);
                assertEquals( 1 ,  components.size());
                assertEquals( "foo.CustomComponent", components.get(0 ).getComponentClass() );
                assertEquals( "a", components.get(0 ).getComponentType() );
                
                List<Behavior> behaviors = model.getElements( Behavior.class);
                assertEquals( 1 ,  behaviors.size());
                assertEquals( "foo.CustomBehavior", behaviors.get(0 ).getBehaviorClass() );
                assertEquals( "b", behaviors.get(0 ).getBehaviorId() );
                return null;
            }
        });
    }
}
