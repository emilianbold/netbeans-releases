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
package org.netbeans.modules.web.beans.xdm.model;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.beans.xml.Beans;
import org.netbeans.modules.web.beans.xml.BeansElement;
import org.netbeans.modules.web.beans.xml.Deploy;
import org.netbeans.modules.web.beans.xml.Type;
import org.netbeans.modules.web.beans.xml.WebBeansModel;


/**
 * @author ads
 *
 */
public class ModelComponentsTest extends NbTestCase {

    public ModelComponentsTest( String name ) {
        super(name);
    }
    
    @Override
    protected Level logLevel() {
        return Level.INFO;
    }
    
    protected void setUp() throws Exception {
        Logger.getLogger(WebBeansModel.class.getName()).setLevel(Level.FINEST);
    }
    
    public void testEmpty() throws Exception{
        WebBeansModel model = Util.loadRegistryModel("empty-beans.xml");
        model.sync();
        Beans beans = model.getBeans();
        
        List<BeansElement> elements = beans.getElements();
        assertEquals( 0 , elements.size());
        assertEquals( 0 ,  beans.getChildren().size());
    }
    
    public void testDeploy() throws Exception{
        WebBeansModel model = Util.loadRegistryModel("deploy-beans.xml");
        model.sync();
        Beans beans = model.getBeans();
        
        List<BeansElement>  elements= beans.getElements();
        assertEquals( 2,  elements.size());
        
        for (BeansElement beansElement : elements) {
            assertTrue( "all children of Beans should be Deploy", 
                    beansElement instanceof Deploy );
        }
    }
    
    public void testType() throws Exception{
        WebBeansModel model = Util.loadRegistryModel("type-beans.xml");
        model.sync();
        Beans beans = model.getBeans();
        
        List<BeansElement>  elements= beans.getElements();
        assertEquals( 1,  elements.size());
        
        Deploy deploy = beans.getChildren( Deploy.class ).get(0 );
        List<Type> types = deploy.getTypes();
        assertEquals( 2,  types.size());
        
        boolean type1Found = false;
        boolean type2Found = false;
        for (Type type : types) {
            String text = type.getText();
            if ( text.equals("type1")){
                type1Found = true;
            }
            else if ( text.equals( "type2")){
                type2Found = true;
            }
        }
        
        assertTrue( "Type with 'type1' value is not found",  type1Found );
        assertTrue( "Type with 'type2' value is not found",  type2Found );
    }
}
