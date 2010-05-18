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

package org.netbeans.modules.visualweb.websvcmgr.codegen;

import com.sun.tools.ws.processor.model.java.JavaMethod;
import com.sun.tools.ws.processor.model.java.JavaParameter;
import com.sun.tools.ws.processor.model.java.JavaMethod;
/*
import com.sun.xml.rpc.processor.model.java.JavaException;
import com.sun.xml.rpc.processor.model.java.JavaParameter;
import com.sun.xml.rpc.processor.model.java.JavaMethod;
*/

// SD import org.netbeans.modules.visualweb.xml.rpc.processor.model.java.JavaException;
// SD import org.netbeans.modules.visualweb.xml.rpc.processor.model.java.JavaMethod;
// SD import org.netbeans.modules.visualweb.xml.rpc.processor.model.java.JavaParameter;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A simple writer to write the Bean Info Class.
 * @author  Winston Prakash
 */
public class WrapperClientBeanInfoWriter extends java.io.PrintWriter {
    
    private String className;
    private String superClassName;
    private String packageName;
    
    private Set constructorStatements = new HashSet();
    
    public static String WEBSERVICE_ICON_FILENAME = "webservice.png";
    
    int indent = 0;
    
    /** Creates a new instance of JavaWriter */
    public WrapperClientBeanInfoWriter(Writer writer){
        super(writer);
        setSuperClass("SimpleBeanInfo");
    }
    
    /** Set package name */
    public void setPackage(String pkgName){
        packageName = pkgName;
    }
    
    /** Set the name of the class */
    public void setClassName(String name){
        className = name;
    }
    
    /** Set the name of the super class this class would extends */
    public void setSuperClass(String superClass){
        superClassName = superClass;
    }
    
    public void writeBeanInfo(){
        // Write the Package name
        println("package " + packageName + ";");
        println();
        
        println("import java.awt.Image;");
        println("import java.beans.BeanDescriptor;");
        println("import java.beans.PropertyDescriptor;");
        println("import java.beans.SimpleBeanInfo;");
        println();
        
        // Write the class  signature
        print("public class " + className + "BeanInfo");
        if(superClassName != null) print(" extends " + superClassName + " ");
        println(" {");
        println();
        
        
        println("  private Class beanClass = " + className + ".class;");
        println("  private String iconFileName = \"" + this.WEBSERVICE_ICON_FILENAME + "\";");
        println("  private BeanDescriptor beanDescriptor = null;");
        println("  private PropertyDescriptor[] propDescriptors = null;");
        
        println();
        
        println("  public BeanDescriptor getBeanDescriptor() {");
        println("      if (beanDescriptor == null) {");
        println("           beanDescriptor = new BeanDescriptor(beanClass);");
        println("           beanDescriptor.setValue(\"trayComponent\", Boolean.TRUE);");
        println("       }");
        println("      return beanDescriptor;");
        println("  }");
        
        println();
        
        println("  public PropertyDescriptor[] getPropertyDescriptors() {");
        println("      if (propDescriptors == null) {");
        println("         propDescriptors = new PropertyDescriptor[] {");
        println("         ");
        println("          };");
        println("      }");
        println("      return propDescriptors;");
        println("  }");
        
        println();
        
        println("  public Image getIcon(int iconKind) {");
        println("      return loadImage(iconFileName);");
        println("  }");
        
        println("}");
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try{
            WrapperClientBeanInfoWriter beanWriter = new WrapperClientBeanInfoWriter(new OutputStreamWriter(System.out));
            beanWriter.setPackage("untitled");
            beanWriter.setClassName("WebserviceProxyClient");
            beanWriter.writeBeanInfo();
            beanWriter.flush();
            beanWriter.close();
        }catch(Exception exc){
            exc.printStackTrace();
        }
    }
    
}
