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


/*
 * ReflectionElementVerifier.java
 *
 * Created on April 17, 2007, 1:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.umllib.project.verifier.elem;


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.List;
import org.netbeans.test.umllib.project.JavaProject;
import org.netbeans.test.umllib.project.elem.IArgumentElem;
import org.netbeans.test.umllib.project.elem.IAttributeElem;
import org.netbeans.test.umllib.project.elem.IClassElem;
import org.netbeans.test.umllib.project.elem.IInterfaceElem;
import org.netbeans.test.umllib.project.elem.IJavaElem;
import org.netbeans.test.umllib.project.elem.IOperationElem;
import org.netbeans.test.umllib.project.elem.VisibilityType;

/**
 *
 * @author <A HREF="mailto:sunflower@netbeans.org">Alexandr Scherbatiy</A>
 */
public class ReflectionElementVerifier extends AElementVerifier{
    
    private Class cls;
    
    public ReflectionElementVerifier(IJavaElem javaElem, String javaProjectName){
        this(javaElem, new JavaProject(javaProjectName));
        
    }
    public ReflectionElementVerifier(IJavaElem javaElem, JavaProject javaProject){
        this(javaProject.getJavaClass(javaElem.getFullName()), javaElem);
    }
    
    
    public ReflectionElementVerifier(Class cls, IJavaElem javaElem){
        super(javaElem);
        if(cls == null){
            appendMessage("!!!---  Byte code was not generated for \"" + javaElem.getFullName() +"\" element---!!!");
            //fail("Byte code was not generated for \"" + javaElem.getFullName() +"\" element");
        }
        this.cls = cls;
        
        
        System.out.println("-------------------------------------------------");
        showClass(cls);
        System.out.println("-------------------------------------------------");
        System.out.println("[Elem]");
        System.out.println(javaElem);
        System.out.println("-------------------------------------------------");
    }
    
    
    protected void verifyName(IJavaElem javaElem) {
        if(!javaElem.getFullName().equals(cls.getName())){
            appendMessage( javaElem.getName() + " != " + cls.getName());
        }
    }
    
    protected void verifySuperClass(IClassElem superClass) {
        System.out.println("Super Class is not verified!!!");
    }
    
    
    
    
    protected void verifySuperInterfaceList(List<IInterfaceElem> superInterfaceList) {
        
        System.out.println("[super interfaces]");
        
        Class<?>[]  interfaces = cls.getInterfaces();
        
        //System.out.println("interfaces: " + interfaces);
        //System.out.println("list      : " + superInterfaceList);
        
        
        if(  interfaces.length != superInterfaceList.size()){
            appendMessage( "super interface number " + interfaces.length +  " != " + superInterfaceList.size());
        }
        
        for(IInterfaceElem interfaceElem : superInterfaceList){
            
            String name = interfaceElem.getFullName();
            
            boolean flag = false;
            
            for (Class c : interfaces){
                //System.out.println("elem: \"" + c.getName() + "\"");
                if(c.getName().equals(name)){
                    flag = true;
                    break;
                }
            }
            
            if(!flag){
                //message += "[ (-) " + "Interface" + ": " + name + " ]";
                appendMessage("SuperInterface" + ": " + name );
            }
        }
        
    }
    
    
    
    protected void verifyAttribute(IAttributeElem attributeElem) {
        System.out.println("[attribute]: " + attributeElem);
        try {
            Field field = cls.getDeclaredField(attributeElem.getName());
            
            
            if( !verifyVisibility(field.getModifiers(), attributeElem.getVisibility())){
                appendMessage("Visibility: " + attributeElem.getVisibility());
            }
            
            //System.out.println("type = \"" + field.getGenericType() + "\"");
            
            String type1 = attributeElem.getType().getFullName();
            String type2 = field.getType().getName();
            
            if(!type1.equals(type2)){
                appendMessage("Type: \"" + type1 + "\" != \"" + type2 + "\"");
            }
            
            
        } catch (Exception e) {
            e.printStackTrace(System.out);
            appendMessage("Attribute: \"" + attributeElem + "\" not found!");
        }
        
    }
    
    
    protected void verifyOperation(IOperationElem operationElem) {
        System.out.println("[operation]  : " + operationElem);
        
        try {
            
            Method[] methods = cls.getDeclaredMethods();
            
            
            boolean flag = false;
            
            for(Method m: methods){
                //System.out.println("[in] flag = " + flag);
                System.out.println("[method]: \"" + m +"\"");
                
                if(m.getName().equals(operationElem.getName())){
                    Class[] paramType = m.getParameterTypes();
                    List<IArgumentElem> argList = operationElem.getArgumentList();
                    
                    if (paramType.length == argList.size()){
                        flag = true;
                        
                        for(int i=0; i < paramType.length; i++){
                            String paramName1 = paramType[i].getName();
                            String paramName2 = argList.get(i).getType().getFullName();
                            
                            System.out.println("    param = \"" + paramName1 + "\"") ;
                            System.out.println("          = \"" + paramName2 + "\"") ;
                            
                            if(!paramName1.equals(paramName2)){
                                flag = false;
                                break;
                            }
                        }
                        
                        System.out.println("[out] flag = " + flag);
                        
                        if(flag){
                            String retType1 =  m.getReturnType().getName();
                            String retType2 =  operationElem.getType().getFullName();
                            
                            System.out.println("    return type = \"" + retType1 + "\"") ;
                            System.out.println("                = \"" + retType2 + "\"") ;
                            
                            
                            // Verify Return Type
                            if(retType1.equals(retType2)){
                                if( verifyVisibility(m.getModifiers(), operationElem.getVisibility())){
                                    return;
                                }else{
                                    appendMessage(operationElem.getName() + " " + "Visibility: " + operationElem.getVisibility());
                                }
                            }else{
                                appendMessage(operationElem.getName() + " " + "Return Type: \"" + retType1 + "\" != \""+ retType2 +"\"");
                            }
                            
                        }
                        
                    }
                }
                
            }
            
            //System.out.println("[end] flag = " + flag);
            if(!flag){
                appendMessage("Operation not found: " + operationElem);
            }
            
        } catch (Exception e) {
            e.printStackTrace(System.out);
            appendMessage("Operation: " + operationElem);
        }
    }
    
    private boolean verifyVisibility(int modifier, VisibilityType visibility){
        System.out.println("[visibility]  " + visibility + " " + modifier);
        
        switch(visibility){
            case PUBLIC   : return Modifier.isPublic(modifier);
            case PROTECTED: return Modifier.isProtected(modifier);
            case PRIVATE  : return Modifier.isPrivate(modifier);
            case PACKAGE  : return !Modifier.isPrivate(modifier) && !Modifier.isProtected(modifier) && !Modifier.isPublic(modifier);
            default:  return false;
            
        }
        
    }
    
    
    
    protected void verifyNestedElem(IJavaElem javaElem) {
        
        //boolean flag = false;
        for(Class c: cls.getClasses()){
            
            String name1 = c.getName();
            String name2 = javaElem.getFullName();
            
            System.out.println("[nested] \"" + name1 +"\"");
            System.out.println("         \"" + name2 + "\"");
            
            if(name1.equals(name2)){
                return;
            }
        }
        
        appendMessage("Nested class not found: " + javaElem.getFullName());
        
    }
    
    
    
    
    private void showClass(Class cls){
        
        System.out.println("---------------------------------------");
        System.out.println("[class]");
        System.out.println("Name: " + cls.getName());
        
        
        
        System.out.println("Super Class: " + cls.getSuperclass());
        
        
        
        System.out.println("Super Interfaces: ");
        for(Class  si : cls.getInterfaces()){
            System.out.println(si);
        }
        
        
        System.out.println("Fields:\n");
        for(Field field: cls.getDeclaredFields()){
            System.out.println(field);
        }
        
        
        System.out.println("Methods:\n");
        for(Method method: cls.getDeclaredMethods()){
            System.out.println(method);
        }
        
        //Class[] nested = cls.getClasses();
        
        for(Class c: cls.getClasses()){
            System.out.println("  nested: " + c.getName());
        }
        
        System.out.println("---------------------------------------");
        
    }
    
    
    
    
}
