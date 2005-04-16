/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.registry.jaxrpc;

import com.sun.xml.rpc.processor.model.java.JavaException;
import com.sun.xml.rpc.processor.model.java.JavaMethod;
import com.sun.xml.rpc.processor.model.java.JavaParameter;
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
    public void setName(String name){
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
            beanWriter.setName("WebserviceProxyClient");
            beanWriter.writeBeanInfo();
            beanWriter.flush();
            beanWriter.close();
        }catch(Exception exc){
            exc.printStackTrace();
        }
    }
    
}
