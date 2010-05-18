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
 * PackageElem.java
 *
 * Created on January 23, 2007, 3:50 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.umllib.project.elem.impl;

import org.netbeans.test.umllib.project.elem.IPackageElem;

/**
 *
 * @author andromeda
 */

public class PackageElem implements IPackageElem {
    
    private String name;
    private IPackageElem parent;
    
    public static final PackageElem ROOT_PACKAGE_ELEM = new RootPackageElem();
    
    /** Creates a new instance of PackageElem */
    public PackageElem(String name) {
	this(name, ROOT_PACKAGE_ELEM);
    }
    
    public PackageElem(String name, IPackageElem parent) {
	this.name = name;
	this.parent = parent;
    }
    
    public IPackageElem getParent() {
	return parent;
    }
    
    public String getName() {
	return name;
    }
    
    public String getFullName() {
	if (parent == null){
	    return name;
	}else{
	    return  removeFirtsDot(parent.getFullName() + "." + getName());
	}
    }
    
    
    public String toString() {
	String str = "";
	
	str += "Package Name: \"" + getName() + "\"\n";
	str += "Package Full Name: \"" + getFullName() + "\"\n";
	
	return str;
    }
    
    
    public static String getFullName(IPackageElem pack, String name){
	String str = "";
	str += pack.getFullName()  + "." + name;

	return removeFirtsDot(str);
    }
    
    
    private static String removeFirtsDot(String str){
	return ( str.charAt(0) == '.' ) ? str.substring(1) : str;
    }
    
    private static class RootPackageElem extends PackageElem {
	RootPackageElem(){
	    super("", null);
	}

        public String getFullName() {
	    return "";
        }
	
	
    }
    
    
    
}
