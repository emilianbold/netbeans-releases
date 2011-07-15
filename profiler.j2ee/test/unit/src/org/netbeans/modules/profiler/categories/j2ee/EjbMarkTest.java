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
package org.netbeans.modules.profiler.categories.j2ee;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.netbeans.lib.profiler.marker.Mark;
import org.netbeans.lib.profiler.results.cpu.marking.MarkMapping;
import org.netbeans.modules.profiler.categorization.api.Category;


/**
 * @author ads
 *
 */
public class EjbMarkTest extends TestBase {
    
    final static String APP_NAME = "EjbApp";

    public EjbMarkTest( String name ) {
        super(name);
    }
    
    public void testLifecycleCategory(){
        MarkMapping[] mappings = getCategorization().getMappings();
        Category lifecycle = getCategory("Lifecycle");
        Mark lifecycleMark = lifecycle.getAssignedMark();
        boolean messagePassivate = false;
        boolean setEntityContext = false;
        boolean unSetEntityContext = false;
        boolean activate = false;
        boolean sessionBMethod = false;
        boolean sessionMethod = false;
        boolean onMessage = false;
        boolean remove = false;
        boolean sessionContext = false;
        boolean drivenContext = false;
        boolean store = false;
        
        for (MarkMapping markMapping : mappings) {
            if ( markMapping.mark.equals(lifecycleMark)){
                if ( markMapping.markMask.getClassName().equals("pack.NewMessageBean")){
                    if ( markMapping.markMask.getMethodName().equals("onMessage")){
                        onMessage = true;  
                    }
                    else if ( markMapping.markMask.getMethodName().equals("ejbActivate")){
                        activate = true;  
                    }
                    else if ( markMapping.markMask.getMethodName().equals("ejbPassivate")){
                        messagePassivate = true;  
                    }
                    else if ( markMapping.markMask.getMethodName().equals("ejbRemove")){
                        remove = true;  
                    }
                    else if ( markMapping.markMask.getMethodName().equals("setSessionContext")){
                        sessionContext = true;  
                    }
                    else if ( markMapping.markMask.getMethodName().equals("setEntityContext")){
                        setEntityContext = true;  
                    }
                    else if ( markMapping.markMask.getMethodName().equals("unsetEntityContext")){
                        unSetEntityContext = true;  
                    }
                    else if ( markMapping.markMask.getMethodName().equals("setMessageDrivenContext")){
                        drivenContext = true;  
                    }
                    else if ( markMapping.markMask.getMethodName().equals("ejbStore")){
                        store = true;  
                    }
                    else {
                        assertFalse( "Found unexpected mar for method "+
                                markMapping.markMask.toFlattened(),true );
                    }
                }
                else if ( markMapping.markMask.getClassName().equals("pack.NewSessionBean")){
                    if ( markMapping.markMask.getMethodName().equals("businessMethod")){
                        sessionBMethod = true;  
                    }
                    else if ( markMapping.markMask.getMethodName().equals("method")){
                        sessionMethod = true;
                    }
                    else {
                        assertFalse( "Found unexpected mark for method "+
                                markMapping.markMask.toFlattened(),true );
                    }
                }
                else {
                    assertFalse("Found unexpected mark for class "+
                            markMapping.markMask.getClassName(),true);
                }
            }
        }
        assertTrue( "No found pack.NewMessageBean.ejbActivate() method", activate);
        assertTrue( "No found pack.NewMessageBean.onMessage() method", onMessage);
        assertTrue( "No found pack.NewMessageBean.ejbPassivate() method", messagePassivate);
        assertTrue( "No found pack.NewMessageBean.ejbRemove() method", remove);
        assertTrue( "No found pack.NewMessageBean.setSessionContext() method", sessionContext);
        assertTrue( "No found pack.NewMessageBean.setEntityContext() method", setEntityContext);
        assertTrue( "No found pack.NewMessageBean.unsetEntityContext() method", unSetEntityContext);
        assertTrue( "No found pack.NewMessageBean.setMessageDrivenContext() method", 
                drivenContext);
        assertTrue( "No found pack.NewMessageBean.ejbStore() method", store);
        
        assertTrue( "No found pack.NewSessionBean.businessMethod() method", sessionBMethod);
        assertTrue( "No found pack.NewSessionBean.method() method", sessionMethod);
    }
    
    public void testEJBContainerCategory(){
        MarkMapping[] mappings = getCategorization().getMappings();
        Category ejbContainer = getCategory("Enterprise Beans");
        Mark ejbMark = ejbContainer.getAssignedMark();
        
        boolean messageOperation = false;
        boolean sessionOperation = false;
        boolean method = false;
        
        Set<String> objectMethods = getMethods(Object.class); 
        for (MarkMapping markMapping : mappings) {
            if ( markMapping.mark.equals(ejbMark)){
                if ( markMapping.markMask.getClassName().equals("pack.StatefulBean")){
                    if ( markMapping.markMask.getMethodName().equals("method")){
                        method = true;
                    }
                    else {
                        assertTrue( "There is unexpected mark for method "+
                            markMapping.markMask.toFlattened(), 
                            objectMethods.contains( markMapping.markMask.getMethodName()));
                    }
                }
                else if ( markMapping.markMask.getClassName().equals("pack.NewMessageBean")){
                    if ( markMapping.markMask.getMethodName().equals("operation")){
                        messageOperation = true;
                    }
                }
                else if (markMapping.markMask.getClassName().equals("pack.NewSessionBean")){
                    if ( markMapping.markMask.getMethodName().equals("operation")){
                        sessionOperation = true;
                    }
                    else {
                        assertTrue( "There is unexpected mark for method "+
                            markMapping.markMask.toFlattened(), 
                            objectMethods.contains( markMapping.markMask.getMethodName()));
                    }
                }
            }
        }
        assertTrue( "No found pack.StatefulBean.method() method", method);
        assertTrue( "No found pack.NewMessageBean.operation() method", messageOperation);
        assertTrue( "No found pack.NewSessionBean.operation() method", sessionOperation);
    }
    
    public void testPersistenceCategory(){
        MarkMapping[] mappings = getCategorization().getMappings();
        Category ejbContainer = getCategory("Enterprise Beans");
        Category persistence = findCategory( ejbContainer, "Persistence");
        Mark persistenceMark = persistence.getAssignedMark();
        
        boolean load = false;
        boolean store = false;
        boolean messageBeanStore = false;
        for (MarkMapping markMapping : mappings) {
            if ( markMapping.mark.equals(persistenceMark)){
                boolean valid = false;
                if ( markMapping.markMask.getClassName().equals("pack.StatefulBean")){
                    if ( markMapping.markMask.getMethodName().equals("ejbLoad")){
                        valid = true;
                        load = true;
                    }
                    else if ( markMapping.markMask.getMethodName().equals("ejbStore")){
                        valid = true;
                        store = true;
                    }
                }
                if ( markMapping.markMask.getClassName().equals("pack.NewMessageBean")){
                    if ( markMapping.markMask.getMethodName().equals("ejbStore")){
                        valid = true;
                        messageBeanStore = true;
                    }
                }
                if ( !valid ){
                    assertFalse( "There is unexpected mark for method "+
                            markMapping.markMask.toFlattened(), true);
                }
            }
        }
        assertTrue( "No found pack.StatefulBean.ejbLoad() method", load);
        assertTrue( "No found pack.StatefulBean.ejbStore() method", store);
        assertTrue( "No found pack.NewMessageBean.ejbStore() method", messageBeanStore);
    }
    

    private Set<String> getMethods( Class<?> clazz ){
        Method[] methods = clazz.getDeclaredMethods();
        Set<String> allMethods = new HashSet<String>();
        for (Method method : methods) {
            allMethods.add( method.getName());
        }
        return allMethods;
    }
    
    
    @Override
    protected String getProjectName() {
        return APP_NAME;
    }
    
}
