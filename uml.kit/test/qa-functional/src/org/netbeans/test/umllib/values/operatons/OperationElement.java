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
 * OperationElement.java
 *
 * Created on December 6, 2005, 4:42 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.umllib.values.operatons;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.test.umllib.values.Argument;
import org.netbeans.test.umllib.values.DefaultType;
import org.netbeans.test.umllib.values.DefaultVisibility;
import org.netbeans.test.umllib.values.ElementType;
import org.netbeans.test.umllib.values.Type;
import org.netbeans.test.umllib.values.Visibility;
import org.netbeans.test.umllib.values.Arg;

/**
 *
 * @author Alexandr Scherbatiy
 */
public class OperationElement {
    
    /** Creates a new instance of OperationElement */
    
    private Visibility visibility;
    private Type type;
    private String name;
    
    private List<Argument> argList;
    
    
    public OperationElement() {
        this("Unnamed");
    }
    
    public OperationElement(String name, List<Argument> argList) {
        this(name, DefaultType.VOID, DefaultVisibility.PUBLIC, argList);
    }

    public OperationElement(String name) {
        this(name, DefaultType.VOID);
    }
    
    public OperationElement(String name, Type type) {
        this(name, type, DefaultVisibility.PUBLIC);
    }
    
    public OperationElement(String name, Type type, Visibility visibility) {
        this(name, type, visibility, new LinkedList<Argument>());
    }
    public OperationElement(String name, Type type, Visibility visibility, List<Argument> argList) {
        this.visibility = visibility;
        this.type = type;
        this.name = name;
        this.argList = ( argList == null ) ? new LinkedList<Argument>() : argList;
    }
    
    
    public Visibility getVisibility(){
        return visibility;
    }
    
    public Type getType(){
        return type;
    }
    
    public String getName(){
        return name;
    }
    
    public List<Argument> getArguments(){
        return argList;
    }
    
    
    public String getText(){
        String text = "";
        text += visibility.getValue();
        text += " " + type.getValue();
        text += " " + name;

        text += "(" ;
    
        int lastIndex = argList.size() - 1;
        
        for(int i = 0; i < lastIndex ; i++){
            text +=  argList.get(i) + ", ";
        }
        
        if ( lastIndex != -1 ){
            text +=  argList.get( lastIndex ) + " ";
        }
        
        text += ")" ;
        
        return text;
    }
    
    public String toString(){
        
        return "Operation: \"" +  getText() + "\"";
    }
    
    
    
    
    public boolean isEqual(OperationElement operation){
        
        boolean result = true;
        
        result =  result && visibility.isEqual(operation.getVisibility()) ;
        result =  result && type.isEqual(operation.getType());
        result =  result && name.equals(operation.getName());

        List<Argument> otherArgList = operation.getArguments();
        int size1 = argList.size();

        int size2 = otherArgList.size();
        
        if( size1 == size2 ){
            for(int i = 0; i <  size1; i++ ){
                result =  result && argList.get(i).isEqual(otherArgList.get(i));
            }
            
        }else{
            return false;
        }
        
        return result;
    }
    
    
    //  =============   Parsed Strings ===========================
    
    // "public MyClass(  )"
    // "public void  myOperation(  )"
    
    
    public static OperationElement parseOperationElement(String parse){
        
        System.out.println("Parse Operation Element = \"" + parse + "\"");
        
        String withoutBlank = parse.replaceAll("\\s+"," ");
        
        
        // parse visibility, type and name
        
        String description = withoutBlank.substring(0, withoutBlank.indexOf('('));
        
        
        String[] split = description.split("\\s");
        
        int len = split.length;
        
        String name = null;
        Type type = null;
        Visibility visibility = null;
        
        if( len == 2 || len == 3){
            visibility = DefaultVisibility.getVisibility(split[0].toUpperCase());
            if (len == 2){
                type = DefaultType.NONE;
                name = split[1];
            }
            
            if (len == 3){
                type = new ElementType(split[1]);
                name = split[2];
            }
            
        }
        
        // parse Argument list
        
        LinkedList<Argument> argList = new LinkedList<Argument>();
        
        int firstIndex = withoutBlank.indexOf('(');
        int lastIndex  = withoutBlank.indexOf(')');
        
        String  argsString = withoutBlank.substring(firstIndex + 1, lastIndex).trim();
        
        
        //System.out.println("  Args String = \"" + argsString + "\"");
        StringTokenizer argToken = new StringTokenizer(argsString, ",");
        
        while(argToken.hasMoreTokens()){
            String str = argToken.nextToken();
            String[] strSplit = str.split(" ");
            Type argType = new ElementType(strSplit[0]);
            String argName = strSplit[1]; 
            
            argList.addLast(new Arg(argType, argName));
        }
        
        return new OperationElement(name, type, visibility, argList);
    }
}

