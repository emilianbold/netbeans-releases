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
 * VoidType.java
 *
 * Created on January 23, 2007, 6:02 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.umllib.project.elem.impl;

import java.util.HashMap;
import org.netbeans.test.umllib.project.elem.IClassElem;
import org.netbeans.test.umllib.project.elem.IJavaElem;
import org.netbeans.test.umllib.project.elem.IPackageElem;

/**
 *
 * @author sunflower@netbeans.org
 */
public class PredefinedType extends HashMap<String, IJavaElem> {

    public static final PredefinedType INSTANCE = new PredefinedType();
    
    public static final String JAVA_LANG_OBJECT = "java.lang.Object";
    public static final String JAVA_LANG_STRING = "java.lang.String";
    public static final String JAVA_LANG_CLASS = "java.lang.Class";

    public static final String JAVA_LANG_INTEGER = "java.lang.Integer";

    private static  boolean initFlag = false;
    
    
    private PredefinedType(){
    }

    
    private static void initTypeMap(){
	initFlag = true;
	/* ===========  java.lang.Object  =================== */
	
	IPackageElem javaPackage = new PackageElem("java");
	IPackageElem langPackage = new PackageElem("lang", javaPackage);
	
	IClassElem objectElem = new ClassElem("Object", langPackage, null);
	INSTANCE.put(JAVA_LANG_OBJECT, objectElem);
	
	//classElem.getSuperInterfaceList().add(new InterfaceElem("Interface1", pack));
	//classElem.getOperationList().add(new OperationElem("run", PrimitiveType.VOID, VisibilityType.PUBLIC));

	/* ===========  java.lang.String  =================== */
	
	
	IClassElem stringElem = new ClassElem("String", langPackage);
	INSTANCE.put(JAVA_LANG_STRING, stringElem);

	/* ===========  java.lang.Class   =================== */
	
	
	IClassElem classElem = new ClassElem("Class", langPackage);
	INSTANCE.put(JAVA_LANG_CLASS, classElem);

	/* ===========  java.lang.Integer   =================== */
	
	
	IClassElem integerElem = new ClassElem("Integer", langPackage);
	INSTANCE.put(JAVA_LANG_INTEGER, integerElem);
        
        
    }
    
    public static IClassElem getClassElem(String fullName){
	if(!initFlag){ initTypeMap(); }
	return (IClassElem) INSTANCE.get(fullName);
    }

    
}
