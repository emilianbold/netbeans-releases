/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.form;

import java.beans.Introspector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.jmi.javamodel.Element;
import org.netbeans.jmi.javamodel.ClassDefinition;
import org.netbeans.jmi.javamodel.Field;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.Type;
import org.netbeans.jmi.javamodel.PrimitiveType;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.PrimitiveTypeKindEnum;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.modules.form.project.ClassPathUtils;
import org.netbeans.jmi.javamodel.ParameterizedType;
import org.netbeans.jmi.javamodel.Array;
import org.netbeans.jmi.javamodel.PrimitiveTypeKind;
import org.netbeans.jmi.javamodel.UnresolvedClass;
import org.netbeans.modules.java.JavaEditor;
import org.netbeans.modules.javacore.api.JavaModel;
import org.openide.filesystems.FileObject;

	
/**
 *
 * Provides information about the forms java source file.
 *
 * @author Tomas Stupka
 */
public class FormJavaSource {
    
    private final FormDataObject formDataObject;	
    private List fields = null;	
    private static final String[] PROPERTY_PREFIXES = new String[] {"get", // NOI18N
								    "is"}; // NOI18N
    
    public FormJavaSource(FormDataObject formDataObject) {
	this.formDataObject = formDataObject;
    }    
    
    public void refresh() {
        JavaModel.getJavaRepository().beginTrans(false);	    
        JavaModel.setClassPath(formDataObject.getPrimaryFile());
	try{	            	    
            fields = getFieldNames();			    
	} finally {	 	    
	    JavaModel.getJavaRepository().endTrans();
	}
    }

    public boolean containsField(String name, boolean refresh) {
	if(refresh) {
	    refresh();
	}	    
	return fields != null && fields.contains(name);
    }	

    /**
     * Returns names for all methods with the specified return type
     */
    public String[] getMethodNames(final Class returnType) {
        ChildrenFilter cf = new MethodChildrenFilter(returnType) {
            protected String getName(Element child) {
                return ((Method)child).getName();
            }	    
        };            
        JavaModel.getJavaRepository().beginTrans(false);	    
        JavaModel.setClassPath(formDataObject.getPrimaryFile());
        try{	        
            return toArray(cf.getNames());				    	        
        } finally {
            JavaModel.getJavaRepository().endTrans();	                 
        }
    }

    /**
     * Returns names for all methods with the specified return type which 
     * start with the prefixes "is" and "get"
     */        
    public String[] getPropertyReadMethodNames(Class returnType) {
        ChildrenFilter cf = new MethodChildrenFilter(returnType) {
	    public boolean accept(Element child) {
		Method method = (Method)child;						
		if(FormJavaSource.extractPropertyName(method.getName()).equals("")) { // NOI18N	
                    // seems to be no property method
		    return false;	
		}		    
		return super.accept(method);
	    }				    
	    protected String getName(Element child) {		
		return ((Method)child).getName();
	    }
	};
        JavaModel.getJavaRepository().beginTrans(false);	    
        JavaModel.setClassPath(formDataObject.getPrimaryFile());
        try{	                
            return toArray(cf.getNames());
        } finally {
            JavaModel.getJavaRepository().endTrans();	                 
        }    
    }
    
    /**
     *
     */
    public static String extractPropertyName(String methodName) {
	for (int i = 0; i < PROPERTY_PREFIXES.length; i++) {
	    if(methodName.startsWith(PROPERTY_PREFIXES[i]) && 
	       methodName.length() > PROPERTY_PREFIXES[i].length()) 
	    {		    
		return Introspector.decapitalize(methodName.substring(PROPERTY_PREFIXES[i].length()));		     			
	    }	
	}
	return "";  // NOI18N	
    }    

    private List getFieldNames() {		
        JavaEditor.SimpleSection variablesSection = 
            formDataObject.getFormEditorSupport().getVariablesSection();	    

        if(variablesSection==null) {
            return null;
        }

        final int genVariablesStartOffset = variablesSection.getPositionBefore().getOffset();
        final int genVariablesEndOffset = variablesSection.getPositionAfter().getOffset();
        
        ChildrenFilter cf = new ChildrenFilter() {		
            protected boolean accept(Element child) {	    
                int startOffset = child.getStartOffset();            
                return startOffset >= genVariablesEndOffset || 
                       startOffset <= genVariablesStartOffset;
            }		
            protected Class getChildType() {
                return Field.class;
            }
            protected String getName(Element child) {
                return ((Field)child).getName();
            }
        };
        return cf.getNames();	    
    }	
    
    private ClassDefinition getClassDefinition() {
	try{	    		
	    FileObject javaFileObject = formDataObject.getPrimaryFile();		
	    ClassPath classPath = ClassPath.getClassPath(javaFileObject, ClassPath.SOURCE);
	    Resource resource = JavaModel.getResource(classPath.findOwnerRoot(javaFileObject),
				     classPath.getResourceName(javaFileObject));

	    java.util.List classifiers = resource.getClassifiers();
	    Iterator classIter = classifiers.iterator();

	    while (classIter.hasNext()) {
		ClassDefinition javaClass = (ClassDefinition)classIter.next();
		String className = javaClass.getName();
		int dotIndex = className.lastIndexOf('.');
		className = (dotIndex == -1) ? className : className.substring(dotIndex+1);
		if( className.equals(javaFileObject.getName()) ) {			
		    return javaClass;
		}
	    }	
	} catch (Exception e) {
	    org.openide.ErrorManager.getDefault().notify(e);	    
	}	    
	return null;
    }

    private boolean isAssignableFrom(String typeName, Class returnType) {	
	Class clazz = getClassByName(typeName);	    
	return clazz!=null ? returnType.isAssignableFrom(clazz) : false;	    
    }
    
    private Class getClassByName(String className) {
	Class clazz = null;
	try {
	    clazz = ClassPathUtils.loadClass(className, formDataObject.getPrimaryFile());
	}
	catch (Exception ex) {
            // could be anything, ignore it...
            ex.printStackTrace();	    
	}
	catch (LinkageError ex) {
	    ex.printStackTrace();	    
	}
	return clazz;
    }	    
    
    private static String[] toArray(List list) {
        return (String[])list.toArray(new String[list.size()]);
    }
    
    private static String getVMName(Type type) {
	if(type instanceof Array) {
	    return getVMName((Array) type);		
	} else if(type instanceof ParameterizedType) {	    
	    return getVMName((ParameterizedType) type);
	} else {
	    return type.getName();
	}		
    }
    
    private static String getVMName(ParameterizedType paramType) {
	String name = paramType.getName();
	if (!paramType.isInner()) {
	    return name;
	}

	ClassDefinition cd = paramType.getDefinition().getDeclaringClass();
	StringBuffer sb = new StringBuffer(name);	
	String pkgName = getPackageName(cd);
			
	int index = sb.lastIndexOf(".");	// NOI18N
	while (index > pkgName.length()) {
	    sb.setCharAt(index, '$');
	    index = sb.lastIndexOf(".");	// NOI18N
	}
	return sb.toString();
    }
    
    private static String getPackageName(ClassDefinition jc) {
        if (jc instanceof UnresolvedClass) {
            String name = jc.getName();
            int index = name.lastIndexOf('.');
            return index < 0 ? "" : name.substring(0, index);	// NOI18N
        }
        if (jc instanceof JavaClass) {
            Resource res = jc.getResource();
            if (res != null) {
                String result = res.getPackageName();
                if (result != null) {
                    return result;
                }
            }
        }
        return "";	// NOI18N
    }
    
    private static String getVMName(Array array) {
	Type type = array.getType();	
	StringBuffer sb = new StringBuffer();	
	sb.append('[');
	if (type instanceof PrimitiveType) {
	    sb.append(getPrimitiveCode(((PrimitiveType)type).getKind()));
	} else {
	    sb.append('L');
            sb.append(getVMName(type));
	    sb.append(';');
	}
	return sb.toString();
    }

    private static String getPrimitiveCode(PrimitiveTypeKind kind) {
	if(PrimitiveTypeKindEnum.BOOLEAN.equals (kind)) {
	    return "Z"; // NOI18N
	} else if(PrimitiveTypeKindEnum.INT.equals (kind)) {
	    return "I"; // NOI18N
	} else if(PrimitiveTypeKindEnum.CHAR.equals (kind)) {
	    return "C"; // NOI18N
	} else if(PrimitiveTypeKindEnum.BYTE.equals (kind)) {
	    return "B"; // NOI18N
	} else if(PrimitiveTypeKindEnum.SHORT.equals (kind)) {
	    return "S"; // NOI18N
	} else if(PrimitiveTypeKindEnum.LONG.equals (kind)) {
	    return "J"; // NOI18N
	} else if(PrimitiveTypeKindEnum.FLOAT.equals (kind)) {
	    return "F"; // NOI18N
	} else if(PrimitiveTypeKindEnum.DOUBLE.equals (kind)) {
	    return "D"; // NOI18N
	} else return "V"; // NOI18N	
    }

    private abstract class ChildrenFilter {		
	public List getNames() {
            List values = new ArrayList();            
            ClassDefinition classDefinition = getClassDefinition();	
            if(classDefinition==null) {
                return values;
            }            
	    List children = classDefinition.getChildren();	    
	    for (Iterator childrenIter = children.iterator(); childrenIter.hasNext();) {
		Element child = (Element)childrenIter.next();                  
		if(getChildType().isAssignableFrom(child.getClass()) && accept(child)) {
                    String name = getName(child);		    
                    values.add(name);
		} 
	    }		                                
	    return values;
	} 	    	
	protected abstract String getName(Element child);	
	protected abstract boolean accept(Element child);
	protected abstract Class getChildType();
    }

    private abstract class MethodChildrenFilter extends ChildrenFilter {		
	private final Class returnType;
	public MethodChildrenFilter(Class returnType) {	    
	    this.returnType = returnType;
	}	
	protected Class getChildType() {
	    return Method.class;
	}			
	public boolean accept(Element child) {			    
	    return acceptReturnType(((Method)child).getType());
	}
	protected boolean acceptReturnType(Type type) {
	    if(returnType.isPrimitive() || type instanceof PrimitiveType) {
		return type instanceof PrimitiveType &&		       
		       acceptPrimitiveType((PrimitiveType)type);
	    }
	    String typeName = getVMName(type);
	    return typeName!= null && !typeName.equals("") && // NOI18N		   		   
		   isAssignableFrom(typeName, returnType); 		
	}
	private boolean acceptPrimitiveType(PrimitiveType type) {            
	    return returnType.isPrimitive() && 
                   !type.getKind().equals(PrimitiveTypeKindEnum.VOID) &&
		   type.getKind().equals(PrimitiveTypeKindEnum.forName(returnType.getName())); 	 
	}	        
    }    
}
