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
 * MethodReturn.java
 *
 * Created on February 8, 2005, 4:17 PM
 */

package org.netbeans.modules.visualweb.ejb.datamodel;

/**
 *
 * @author  cao
 */
public class MethodReturn implements java.lang.Cloneable {
    private static final String[] PRIMITIVE_WRAPPER_CLASSES = 
    { "java.lang.Boolean", 
      "java.lang.Byte", 
      "java.lang.Double", 
      "java.lang.Float", 
      "java.lang.Integer", 
      "java.lang.Long", 
      "java.lang.Short", 
      "java.lang.Character", 
      "java.lang.String" };
    
    private String className;
    private boolean isCollection;
    private String elemClassName;
    
    public MethodReturn( String className, boolean isCollection, String elemClassName ) {
        this.className = className;
        this.isCollection = isCollection;
        this.elemClassName = elemClassName;
    }
    
    public MethodReturn() {
        this( null, false, null );
    }
    
    public void setClassName( String className )
    {
        this.className = className;
    }
    
    public void setIsCollection( boolean col )
    {
        this.isCollection = col;
    }
    
    public void setElemClassName( String elemClassName )
    {
        this.elemClassName = elemClassName;
    }
    
    public String getClassName() { return this.className; }
    public boolean isCollection() { return this.isCollection; }
    public String getElemClassName() { return this.elemClassName; }
    
    public boolean isPrimitive() {
        for (int i = 0; i < PRIMITIVE_WRAPPER_CLASSES.length; i++) {
            if (className.equals(PRIMITIVE_WRAPPER_CLASSES[i])) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean isVoid()
    {
        if( className.equals( "void" ) )
            return true;
        else
            return false;
    }
    
    public Object clone()
    {
        try {
            return super.clone();
        }
        catch( java.lang.CloneNotSupportedException e )
        {
            return null;
        }
    }
}
