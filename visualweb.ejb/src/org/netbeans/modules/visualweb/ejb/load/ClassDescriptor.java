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
 * ClassDescriptor.java
 *
 * Created on May 14, 2004, 12:51 PM
 */

package org.netbeans.modules.visualweb.ejb.load;

/**
 * This class is to describe the information for a java class
 *
 * @author  cao
 */
public class ClassDescriptor 
{
    // Full path file name, e.g. d:/rave/ejb/src/com/sun/rave/load/EjbLoader.java
    private String fullPathFileName;
    
    // File name with package in the path, e.g. org/netbeans/modules/visualweb/ejb/load/EjbLoader.java
    private String packageFileName;
    
    // Name of the class without package, e.g. EjbLoader
    private String className;
    
    // Name with pacakge, e.g. org.netbeans.modules.visualweb.ejb.load
    private String packageName;
    
    // To indicate it is an inner class
    private boolean innerClass;
 
    
    /**
     * Constructor
     */
    public ClassDescriptor( String className, String packageName, String fullPathFileName, String packageFileName, boolean innerClass ) 
    {
        this.className = className;
        this.packageName = packageName;
        this.fullPathFileName = fullPathFileName;
        this.packageFileName = packageFileName;
        this.innerClass = innerClass;
    }
    
    public ClassDescriptor( String className, String packageName, String fullPathFileName, String packageFileName ) 
    {
        this( className, packageName, fullPathFileName, packageFileName, false );
    }
    
    public String getClassName() { return this.className; }
    public String getPackageName() { return this.packageName; }
    public String getFullPathFileName() { return this.fullPathFileName; }
    public String getPackageFileName() { return this.packageFileName; }
    public boolean isInnerClass() { return this.innerClass; }
    
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "fullPathFileName: " + fullPathFileName + "\n" );
        buf.append( "packageFileName: " + packageFileName + "\n" );
        buf.append( "className: " + className + "\n" );
        buf.append( "packageName: " + packageName + "\n" );
        return buf.toString();
    }
    
}
