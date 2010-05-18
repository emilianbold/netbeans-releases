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
package org.netbeans.modules.visualweb.insync.beans;

import java.util.List;
import org.netbeans.modules.visualweb.insync.UndoEvent;
import org.netbeans.modules.visualweb.insync.java.EventMethod;
import org.netbeans.modules.visualweb.insync.java.JavaClass;
import org.netbeans.modules.visualweb.insync.java.JavaUnit;
import java.beans.MethodDescriptor;
import java.io.File;
import org.netbeans.modules.visualweb.insync.java.Method;
import org.netbeans.modules.visualweb.insync.java.Statement;

import org.openide.util.NbBundle;

import org.netbeans.modules.visualweb.extension.openide.util.Trace;
import java.lang.reflect.Modifier;

/**
 * Manage the methods, fields and such that should be defined for the bean described by my model.
 *
 * @author eric
 *
 */
public class BeanStructureScanner {
    public static String CTOR = "<init>";
    public static String ENSURE_INITBLOCK = "ensureInitBlock";
    // TODO: add code to read the user preference over explicit and
    // implicit imports.
    protected boolean explicitImport = true;

    protected BeansUnit beansUnit;
    protected JavaUnit javaUnit;

    //Method info contains the nature of methods need to be in a managed bean.
    //Sub-classes may alter the method infos according to requirement
    protected MethodInfo ctorInfo = new MethodInfo(CTOR, Modifier.PUBLIC, Void.TYPE, "", ENSURE_INITBLOCK);
    protected MethodInfo propertiesInitInfo = ctorInfo;
    protected MethodInfo destroyInfo;

    protected Object propertyRegionInsertPosition;

    public BeanStructureScanner(BeansUnit unit) {
        super();
        this.beansUnit = unit;
        this.javaUnit = unit.getJavaUnit();
    }

    //Methods that need to be in a managed bean
    protected MethodInfo[] getMethodInfos(){
        return new MethodInfo[]{ctorInfo};
    }

   /**
    *
    */
   protected void ensureInitBlock(MethodInfo mi) {
       UndoEvent event = null;
       try {
           String eventName = NbBundle.getMessage(BeanStructureScanner.class, "EnsureInitBlock"); //NOI18N
           event = beansUnit.getModel().writeLock(eventName);
           if(mi.getMethod().hasInitBlock()) {
               return;
           }
           String bodyText = "// "+ NbBundle.getMessage(BeansUnit.class, "COMMENT_InitMethodToDoMarker") + "\n" + //NOI18N
                   "// <editor-fold defaultstate=\"collapsed\" desc=\"" +
                   NbBundle.getMessage(BeansUnit.class, "COMMENT_InitDescription") + "\">" + //NOI18N
                   "try {\n" + //NOI18N
                   "} catch (Exception e) {\n" +
                   "log(\"Page1 Initialization Failure\", e);\n" + //NOI18N
                   "throw e instanceof FacesException ? (FacesException) e: new FacesException(e);\n" + //NOI18N
                   "}\n" + //NOI18N
                   "// </editor-fold>\n" + //NOI18N
                   "// " + NbBundle.getMessage(BeanStructureScanner.class, "COMMENT_AdditionalCode"); //NOI18N
           mi.getMethod().replaceBody(bodyText);
       }finally {
           if(event != null) {
               beansUnit.getModel().writeUnlock(event);
           }
       }
   }   

    /**
     * Add as needed an event method with a given name and event type, and return type. Do nothing
     * if the method is already present.
     *
     * @param md  The MethodDescriptor that identifies the method signature+return.
     * @param name  The name of the metod to find or create.
     * @param defaultBody The default body to be inserted for the event if one does not
     *     already exist, or null to get a generic comment body
     * @param parameterNames An array of names to be used for the parameters, or null
     *     to use a default algorithm which will derive names from the types
     * @param requiredImports An array of classes to be imported, or null to import nothing
     * @return The existing or newly created method.
     */
    public  EventMethod ensureEventMethod(MethodDescriptor md, String name, 
                                    String defaultBody, String[] parameterNames,
                                    String[] requiredImports) {
        Class[] pts = md.getMethod().getParameterTypes();
        EventMethod method = beansUnit.getThisClass().getEventMethod(name, pts);
        if( method != null) {
            return method;
        }
        Class retType = md.getMethod().getReturnType();
        String body = defaultBody;
        if(defaultBody == null) {
            body = "// " + NbBundle.getMessage(BeanStructureScanner.class, "COMMENT_EventMethodBody"); //NOI18N
        }
        body += "\n\n";
        if (retType != Void.TYPE) {  
            body = body + "return null;"; //NOI18N
        }
        
        String[] pns = parameterNames;
        if (pns == null) {
            pns = Naming.paramNames(pts, md.getParameterDescriptors());
        }
        
        UndoEvent event = null;
        try {
            String eventName = NbBundle.getMessage(BeanStructureScanner.class, "EnsureEventMethod"); //NOI18N
            event = beansUnit.getModel().writeLock(eventName);
            org.netbeans.modules.visualweb.insync.java.MethodInfo info =
                    new org.netbeans.modules.visualweb.insync.java.MethodInfo(name, retType, Modifier.PUBLIC,
                    pns, pts, body, null);
            
            return beansUnit.getThisClass().addEventMethod(info);

        }finally {
            if(event != null) {
                beansUnit.getModel().writeUnlock(event);
            }
        }
    }

    /**
     * Use the junit method to enure that we have an import for this type so that 
     * the identifier can use its short form. By default explicit imports are ensured
     * 
     * @param type  fully-qualified type name 
     */
    public void ensureImportForType(String type) {
        if(explicitImport) {
            javaUnit.ensureImport(type);
        }else {
            int dot = type.lastIndexOf('.');
            if (dot > 0)
                javaUnit.ensureImport(type.substring(0, dot+1) + "*");
        }
    }

   protected Method ensureMethod(Object location, MethodInfo mi) {
       Method method = beansUnit.getThisClass().getMethod(mi.getName(), null);
       if(method != null) {
           return method;
       }
       org.netbeans.modules.visualweb.insync.java.MethodInfo info =
               new org.netbeans.modules.visualweb.insync.java.MethodInfo(mi.getName(),
               mi.getReturnType(), mi.getModifiers(), null, null,
               null, mi.getComment());
       UndoEvent event = null;
       try {
           String eventName = NbBundle.getMessage(BeanStructureScanner.class, "EnsureMethod"); //NOI18N
           event = beansUnit.getModel().writeLock(eventName);
           method = beansUnit.getThisClass().addMethod(info);
       }finally {
           if(event != null) {
               beansUnit.getModel().writeUnlock(event);
           }
       }
       return method;
    }

   /**
    * Return the last method added.
    * 
    * @return
    */
    protected void ensureMethods() {
        Method m = null;
        MethodInfo[] methodInfos = getMethodInfos();
        for(int i = 0;i < methodInfos.length; i++) {
            m = ensureMethod(null, methodInfos[i]);
            methodInfos[i].setMethod(m);
            try {
                String ensureMethodName = methodInfos[i].getEnsureMethodName();
                if(ensureMethodName != null) {
                    java.lang.reflect.Method m1 = BeanStructureScanner.class.getDeclaredMethod(
                            ensureMethodName, new Class[]{MethodInfo.class});
                    m1.invoke(this, new Object[]{methodInfos[i]});
                }
            }catch(Exception e){
                //This should not happen
               assert Trace.trace("insync.beans", e.getMessage()); //NOI18N
            }
        }
    }
    
    /**
     * TODO: We need to change how this region is created.
     */
    protected Object/*VariableElement*/ ensurePropertyRegion() {
/*//NB6.0
        JavaClassAdapter javaClass = beansUnit.getThisClass();
        UndoEvent event = null;
        try {
            String eventName = NbBundle.getMessage(BeanStructureScanner.class, "EnsurePropertyRegion"); //NOI18N
            event = beansUnit.getModel().writeLock(eventName);
            boolean rollback = true;
            try {
                JMIUtils.beginTrans(true);
                Field ph = javaClass.getField("__placeholder"); //NOI18N
                
                // Insert a placeholder field at the top of the class and put a region fold comment above it
                if (ph == null) {
                    ph = javaClass.addField("__placeholder", Integer.TYPE, null, false); //NOI18N
                    ph.setModifiers(Modifier.PRIVATE);
                    ph.setJavadocText("<editor-fold defaultstate=\"collapsed\" desc=\"" + //NOI18N
                            NbBundle.getMessage(BeanStructureScanner.class, "COMMENT_DefDescription") + "\">"); //NOI18N
                }
                rollback = false;
                return ph;
            }finally {
                JMIUtils.endTrans(rollback);
            }
        }finally {
            if(event != null) {
                beansUnit.getModel().writeUnlock(event);
            }
        }
 //*/
        return null;
    }
    

    /**
    *
    */
   protected JavaClass ensureThisClass() {

       /*TODO - Deva
       // get the expected classname from the filename
       String cname = thisClassName(getJavaUnit().getName());

       // scan for class by name. if it is there return it, possibly tweaking it first
       Clazz[] classes = getJavaUnit().getClazzes();
       for (int i = 0; i < classes.length; i++) {
           Clazz cls = classes[i];
           assert Trace.trace("insync.beans", "BU.findThisClass name:" + cls.getName() + " mods:"   //NOI18N
                   + cls.getModifiers());

           // grab the one with the matching name, making it the only public one if necessary
           if (cls.getName().equals(cname)) {
               if (cls.getAccessModifiers() != Modifiers.PUBLIC)
                   cls.setAccessModifiers(Modifiers.PUBLIC);
               for (int j = 0; j < classes.length; j++)
                   if (j != i && classes[j].getAccessModifiers() == Modifiers.PUBLIC)
                       classes[j].setAccessModifiers(0);
               return cls;
           }
       }

       // no match--grab the public one & rename it
       for (int i = 0; i < classes.length; i++) {
           Clazz cls = classes[i];
           assert Trace.trace("insync.beans", "BU.findThisClass name:" + cls.getName() + " mods:"   //NOI18N
                       + cls.getModifiers());

           if (cls.getAccessModifiers() == Modifiers.PUBLIC) {
               cls.setName(cname);
               return cls;
           }
       }

       // no match and no public class--
       // create the class definition & add a comment
       Clazz clazz = getJavaUnit().addClass(null, cname);
       clazz.setModifiers(Modifiers.PUBLIC);
       String suggestedSuperclass = getSuggestedThisClassSuperclass();
       if (suggestedSuperclass != null) {
           clazz.setSuperclass(suggestedSuperclass);
           ensureImportForType(suggestedSuperclass);
       }
       Comment c = clazz.addComment(Comment.STYLE_DOC);
       c.setBody(getThisClassComment());
       c.setPrewhite(LineColumn.make(2, 0));
       clazz.setPrewhite(LineColumn.make(1, 0));

       return clazz;
        **/
       return null;
   }

   /**
    * Ensures that a cross-reference accessor to a sibling bean is in place. Accessor method is of
    * the form:
    *      public <type> get<Mname>() {
    *          return (<type>) getBean("<bname>");
    *      }
    *
    * @param bname
    * @param type
    */
   public void addXRefAccessor(String bname, String type) {
       String mname = bname.replaceAll("/", "_");
       // Identify whether type has a package name, if not then skip creating an accessor for it
       int index = type.lastIndexOf('.');
       if (index == -1)
           return;
       
       UndoEvent event = null;
       try {
           String eventName = NbBundle.getMessage(BeanStructureScanner.class, "EnsureXrefAccessor"); //NOI18N
           event = beansUnit.getModel().writeLock(eventName);
           String simpleName = type;
           if(beansUnit.getJavaUnit().ensureImport(type)) {
               simpleName = type.substring(index+1);
           }
           String body = "return (" + simpleName + ")getBean(\"" + bname + "\");"; //NOI18N
           String comment = NbBundle.getMessage(BeanStructureScanner.class, "COMMENT_GetScopedBeanComment"); //NOI18N
           org.netbeans.modules.visualweb.insync.java.MethodInfo info =
                   new org.netbeans.modules.visualweb.insync.java.MethodInfo("get" + mname, //NOI18N
                   null, Modifier.PROTECTED, null, null, body, comment);
           info.setReturnTypeName(type);
           beansUnit.getThisClass().addMethod(info);
       }finally {
           if(event != null) {
               beansUnit.getModel().writeUnlock(event);
           }
       }
   }

   /**
    * Finds a possibly existing cross-reference accessor to a sibling bean
    */
   protected Method  findXRefAccessor(String name) {
       return beansUnit.getThisClass().getMethod("get" + name, new Class[] {}); //NOI18N
   }

   public String getComment(String id) {
       return NbBundle.getMessage(BeanStructureScanner.class, id);
   }
   
   public Method getConstructorMethod() {
       return ctorInfo.getMethod();
   }
   
   public Method getPropertiesInitMethod() {
       return propertiesInitInfo.getMethod();
   }
   
   /**
    * @return
    */
   public Method getDestroyMethod() {
       return beansUnit.getThisClass().getMethod(destroyInfo.getName(), new Class[]{});
   }


   public List<Statement> getPropertiesInitStatements() {
        return propertiesInitInfo.getMethod().getPropertySetStatements();
   }    

    public JavaUnit getJavaUnit() {
        return javaUnit;
    }
    
    public String getSuggestedThisClassSuperclass() {
        return null;
    }
    
    public String getThisClassComment() {
        return NbBundle.getMessage(BeanStructureScanner.class, "COMMENT_BeanClassComment"); //NOI18N
    }
    
    /**
     * Removes a possibly existing cross-reference accessor to a sibling bean
     */
    /**
     * @param name
     */
    public void removeXRefAccessor(String name) {
        Method m = findXRefAccessor(name);
        if (m != null)
           m.remove();
    }

    public void scan() {
        //clazz = ensureThisClass();
        ensureMethods();
        ensurePropertyRegion();
    }

    /**
    *
    */
   protected String thisClassName(String filename) {
       int suffix = filename.lastIndexOf('.');
       int dir = filename.lastIndexOf(File.separatorChar);
       int start = (dir >= 0) ? dir+1 : 0;
       if (suffix > 0)
           return filename.substring(start, suffix);
       return filename.substring(start);
   }

  
   public class MethodInfo {
       String name, comment, ensureMethodName, exception;
       Class retType;
       int modifiers;
       Method method;
       
       public MethodInfo(String name, int modifiers, Class retType, String comment, String ensureMethodName, String exception) {
           this.retType = retType;
           this.modifiers = modifiers;
           this.name = name;
           this.comment = comment;
           this.ensureMethodName = ensureMethodName;
           this.exception = exception;
       }
       
       public MethodInfo(String name, int modifiers, Class retType, String comment, String ensureMethodName) {
           this(name, modifiers, retType, comment, ensureMethodName, null);
       }
       
       public MethodInfo(String name, int modifiers, Class retType, String comment) {
           this(name, modifiers, retType, comment, null, null);
       }       

       public MethodInfo(String name, String comment) {
           this(name, Modifier.PUBLIC, Void.TYPE, comment, null, null); //NOI18N
       }
       
       public MethodInfo(String name) {
           this(name, Modifier.PUBLIC, Void.TYPE, "", null, null); //NOI18N
       }
       
       public String getName() {
           return name;
       }
       
       public int getModifiers() {
           return modifiers;
       }
       
       public Class getReturnType() {
           return retType;
       }
       
       public String getExceptionName() {
           return exception;
       }

       public Method getMethod() {
           return method;
       }
       
       public void setMethod(Method method) {
           this.method = method;
       }

       public void setEnsureMethodName(String ensureMethodName) {
           this.ensureMethodName = ensureMethodName;
       }
       
       public String getEnsureMethodName() {
           return ensureMethodName;
       }
       
       public String getComment() {
           return comment;
       }
       
       public void setComment(String comment) {
           this.comment = comment;
       }
   }
}
