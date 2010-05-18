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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */


/*
 * EJBDevelopmentModel.java
 *
 * Created on October 15, 2004, 3:43 PM
 */

package org.netbeans.modules.j2ee.sun.persistence.mapping.ejb;

import java.io.*;
import java.util.*;
import java.lang.reflect.Modifier;

import org.openide.filesystems.FileObject;

import org.netbeans.modules.j2ee.dd.api.ejb.Entity;

import com.sun.jdo.api.persistence.mapping.ejb.AbstractNameMapper;
import com.sun.jdo.api.persistence.model.jdo.RelationshipElement;
import com.sun.jdo.api.persistence.model.jdo.PersistenceFieldElement;
import com.sun.jdo.spi.persistence.utility.StringHelper;
import com.sun.jdo.spi.persistence.utility.JavaTypeHelper;
import org.netbeans.lib.j2ee.sun.persistence.utility.openide.DevelopmentModel;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.project.FileOwnerQuery;

/** This is a subclass of DevelopmentModel which uses the deployment descriptor 
 * to augment the java metadata for a non-existent persistence-capable
 * java/class file.  It is primarily used at NB mapping time, though it could 
 * be used at any time as long as sufficient mapping and deployment descriptor 
 * information is available.  See the javadoc for individual methods below 
 * for differences and translations between persistence-capable and ejb 
 * names and behavior.  There are different answers to methods depending 
 * on whether they are called on the persistence-capable or the ejb.  These 
 * are primarily for the handling of serializable, non-primitive, non-wrapper 
 * type fields: isByteArray, getFieldType (returning byte array), 
 * isPersistentTypeAllowed and isPersistentAllowed (returning true) which 
 * return answers about the byte array when called on the persistence-capable 
 * and return answers about the serializable type when called on the ejb.
 *
 * @author Rochelle Raccah
 */
public class EJBDevelopmentModel extends DevelopmentModel {
	// TODO: what should class loader be? do we need it here (constructor, var,
	// extra getClassLoader method and calls to it, etc.)
	private ClassLoader classLoader;
	private DevelopmentNameMapper nameMapper;
        
	/** Creates a new instance of DeploymentDescriptorModel
	 * @param nameMapper the name mapper to be used as a helper for 
	 * translation between persistence class and ejb names.
	 * @param classLoader the class loader object which is used for the
	 * application.
	 */
	public EJBDevelopmentModel(DevelopmentNameMapper nameMapper, 
			ClassLoader classLoader, FileObject[] sourceRoots) {
            // TODO - if we translate from sourceRoots to project here,
            // is the dynamically created list of roots the same?
            // also, make sure sourceRoots is not empty or null
		//super(sourceRoots);
                super(FileOwnerQuery.getOwner(sourceRoots[0]));
		this.classLoader = classLoader;
		this.nameMapper = nameMapper;
	}

	private ClassLoader getClassLoader() {
		return classLoader;
	}

	private DevelopmentNameMapper getNameMapper() {
		return nameMapper;
	}

	/** Returns the input stream with the supplied resource name found with 
	 * the supplied class name.  This method overrides the one in 
	 * RuntimeModel to enforce using the class loader provided at construction 
	 * time instead of the one specified in this method.
	 * @param className the fully qualified name of the class which will be
	 * used as a base to find the resource
	 * @param classLoader the class loader used to find mapping information
	 * This parameter is ignored in this implementation - instead the 
	 * class loader supplied at construction time is used.
	 * @param resourceName the name of the resource to be found
	 * @return the input stream for the specified resource, <code>null</code>
	 * if an error occurs or none exists
	 */
	@Override
        protected BufferedInputStream getInputStreamForResource(String className, 
			ClassLoader classLoader, String resourceName) {
		return super.getInputStreamForResource(
			className, getClassLoader(), resourceName);
	}

	/** Returns the name of the second to top (top excluding java.lang.Object)
	 * superclass for the given class name.  This method overrides the one in 
	 * RuntimeModel in order to return the supplied className if it  
	 * represents a persistence-capable class.  This is because the 
	 * persistence-capable classes don't mirror the inheritance 
	 * hierarchy of the ejbs.
	 * @param className the fully qualified name of the class to be checked
	 * @return the top non-Object superclass for className,
	 * <code>className</code> if an error occurs or none exists
	 */
        @Override
	protected String findPenultimateSuperclass(String className) {
		return (isPCClassName(className) ? className : 
			super.findPenultimateSuperclass(className));
	}

	/** Returns the name of the superclass for the given class name.  This 
	 * method overrides the one in RuntimeModel in order to return 
	 * java.lang.Object if the supplied className represents a 
	 * persistence-capable class.  This is because the persistence-capable 
	 * classes don't mirror the inheritance hierarchy of the ejbs.
	 * @param className the fully qualified name of the class to be checked
	 * @return the superclass for className, <code>null</code> if an error
	 * occurs or none exists
	 */
        @Override
	protected String getSuperclass(String className) {
		return (isPCClassName(className) ? "java.lang.Object" :	// NOI18N
			super.getSuperclass(className));
	}

	/** Creates a file with the given base file name and extension
	 * parallel to the supplied class (if it does not yet exist).  This 
	 * method overrides the one in RuntimeModel and throws 
	 * UnsupportedOperationException in the case where the file 
	 * doesn't yet exist.  If the file exists (even if it has 
	 * just been "touched"), this method will return that output stream, 
	 * but it is not capable of using the supplied class as a sibling location.
	 * @param className the fully qualified name of the class
	 * @param baseFileName the name of the base file
	 * @param extension the file extension
	 * @return the output stream for the specified resource, <code>null</code>
	 * if an error occurs or none exists
	 * @exception IOException if there is some error creating the file
	 */
        @Override
	protected BufferedOutputStream createFile(String className, 
			String baseFileName, String extension) throws IOException {
		BufferedOutputStream outputStream = 
			super.createFile(className, baseFileName, extension);

		if (outputStream != null)
			return outputStream;

		throw new UnsupportedOperationException();
	}

	/** Deletes the file with the given file name which is parallel
	 * to the supplied class.  This method overrides the one in RuntimeModel 
	 * and throws UnsupportedOperationException.
	 * @param className the fully qualified name of the class
	 * @param fileName the name of the file
	 * @exception IOException if there is some error deleting the file
	 */
        @Override
	protected void deleteFile(String className, String fileName)
			throws IOException {
		throw new UnsupportedOperationException();
	}

	/** Returns the class element with the specified className.  If the 
	 * specified className represents a persistence-capable class, the 
	 * abstract bean class for the corresponding ejb is always returned 
	 * (even if there is a Class object available for the 
	 * persistence-capable).  If there is an ejb name and an abstract bean
	 * class with the same name, the abstract bean class which is associated
	 * with the ejb will be returned, not the abstract bean class which  
	 * corresponds to the supplied name (directly).  If the specified 
	 * className represents a persistence-capable key class name, the 
	 * corresponding bean's key class is returned.
	 * @param className the fully qualified name of the class to be checked
	 * @param classLoader the class loader used to find mapping information
	 * @return the class element for the specified className
	 */
        @Override
	public Object getClass(final String className, 
			final ClassLoader classLoader) {
		String testClass = className;

		// translate the class name to corresponding ejb name's abstract 
		// bean or key class if necessary
		if (className != null) {
			AbstractNameMapper mapper = getNameMapper();
			String ejbName = 
				(isPCClassName(className) ? getEjbName(className) : className);

			if (mapper.isEjbName(ejbName))
				testClass = mapper.getAbstractBeanClassForEjbName(ejbName);
			else {
				String keyClass = 
					mapper.getKeyClassForPersistenceKeyClass(className);

				if (keyClass != null) {
					// if it's a pk field of type primitive, byte[], 
					// or other array, return the primitive class or a 
					// dummy class
					if (AbstractNameMapper.PRIMARY_KEY_FIELD == 
							getPersistenceKeyClassType(className)) {
						if (isPrimitive(keyClass))
							return JavaTypeHelper.getPrimitiveClass(keyClass);
						if (isByteArray(keyClass) || keyClass.endsWith("[]"))
							return byte[].class;
					}

					testClass = keyClass;
				}
			}
		}

		return super.getClass(testClass, getClassLoader());
	}

	/** Returns a wrapped constructor element for the specified argument types 
	 * in the class with the specified name.  If the specified class name is 
	 * a persistence-capable key class name which corresponds to a bean 
	 * with an unknown primary key class a dummy constructor will also be 
	 * returned.  Types are specified as type names for primitive type 
	 * such as int, float or as fully qualified class names.
	 * @param className the name of the class which contains the constructor 
	 * to be checked
	 * @param argTypeNames the fully qualified names of the argument types
	 * @return the constructor element
	 * @see #getClass
	 */
	@Override
        public Object getConstructor(final String className, 
			String[] argTypeNames) {
		Object returnObject = null;

		if ((AbstractNameMapper.PRIMARY_KEY_FIELD == 
				getPersistenceKeyClassType(className)) && 
				argTypeNames.equals(NO_ARGS)) {
			returnObject = new MemberWrapper(className, null, Modifier.PUBLIC, 
				(NamedElementHandleWrapper)getClass(className));
		}

		if (returnObject == null) {
			returnObject = super.getConstructor(className, argTypeNames);

			if (returnObject instanceof NamedElementHandleWrapper)	{	// wrap it
				returnObject = new MemberWrapper(
					(NamedElementHandleWrapper)returnObject);
			}
		}

		return returnObject;
	}

	/** Returns a wrapped method element for the specified method name and 
	 * argument types in the class with the specified name.  If the 
	 * specified className represents a persistence-capable class and 
	 * the requested methodName is readObject or writeObject, a dummy 
	 * method will be returned.  Similarly, if the specified class name is 
	 * a persistence-capable key class name which corresponds to a bean 
	 * with an unknown primary key class or a primary key field (in both 
	 * cases there is no user defined primary key class) and the requested 
	 * method is equals or hashCode, a dummy method will also be returned.  
	 * Types are specified as type names for primitive type such as int, 
	 * float or as fully qualified class names.  Note, the method does not 
	 * return inherited methods.
	 * @param className the name of the class which contains the method 
	 * to be checked
	 * @param methodName the name of the method to be checked
	 * @param argTypeNames the fully qualified names of the argument types
	 * @return the method element
	 * @see #getClass
	 */
        @Override
	public Object getMethod(final String className, final String methodName,
			String[] argTypeNames) {
		int keyClassType = getPersistenceKeyClassType(className);
		Object returnObject = null;

		if (isPCClassName(className)) {
			if ((methodName.equals("readObject") && 	// NOI18N
					argTypeNames.equals(READ_OBJECT_ARGS)) || 
					(methodName.equals("writeObject") && 	// NOI18N
					argTypeNames.equals(WRITE_OBJECT_ARGS))) {
				NamedElementHandleWrapper wrapper = 
                                        (NamedElementHandleWrapper)getClass(className);
				returnObject = new MemberWrapper(methodName, 
					getTypeForName(TypeKind.VOID.toString(), wrapper), Modifier.PRIVATE, 
					wrapper);
			}
		}
		if ((AbstractNameMapper.UNKNOWN_KEY_CLASS == keyClassType) || 
				(AbstractNameMapper.PRIMARY_KEY_FIELD == keyClassType)) {
                        NamedElementHandleWrapper wrapper = 
                                (NamedElementHandleWrapper)getClass(className);
			if (methodName.equals("equals") && 	// NOI18N
					argTypeNames.equals(EQUALS_ARGS)) {
				returnObject = new MemberWrapper(methodName, 
					getTypeForName(TypeKind.BOOLEAN.toString(), wrapper), Modifier.PUBLIC, 
					wrapper);
			} else if (methodName.equals("hashCode") && 	// NOI18N
					argTypeNames.equals(NO_ARGS)) {
				returnObject = new MemberWrapper(methodName, 
					getTypeForName(TypeKind.INT.toString(), wrapper), Modifier.PUBLIC, 
					wrapper);
			}
		}

		if (returnObject == null) {
			returnObject = super.getMethod(className, methodName, argTypeNames);

			if (returnObject instanceof NamedElementHandleWrapper)		// wrap it
				returnObject = new MemberWrapper((NamedElementHandleWrapper)returnObject);
		}

		return returnObject;
	}

	/** Returns the inherited method element for the specified method 
	 * name and argument types in the class with the specified name.  
	 * Types are specified as type names for primitive type such as 
	 * int, float or as fully qualified class names.  Note that the class 
	 * with the specified className is not checked for this method, only
	 * superclasses are checked.  This method overrides the one in 
	 * Model in order to do special handling for a persistence-capable key
	 * class name which corresponds to a bean with a primary key 
	 * field.  In that case, we don't want to climb the inheritance 
	 * of the primary key field type.
	 * @param className the name of the class which contains the method 
	 * to be checked
	 * @param methodName the name of the method to be checked
	 * @param argTypeNames the fully qualified names of the argument types
	 * @return the method element
	 * @see #getClass
	 */
        @Override
	public Object getInheritedMethod(String className, String methodName, 
			String[] argTypeNames) {
		// If the class name corresponds to a pk field (which means that 
		// there is no user defined primary key class, we don't want to 
		// climb the inheritance hierarchy, we only process this class.
		return ((AbstractNameMapper.PRIMARY_KEY_FIELD == 
			getPersistenceKeyClassType(className)) ? 
			getMethod(className, methodName, argTypeNames) : 
			super.getInheritedMethod(className, methodName, argTypeNames));
	}

	/** Returns a list of names of all the declared field elements in the 
	 * class with the specified name.  If the specified className represents 
	 * a persistence-capable class, the list of field names from the  
	 * corresponding ejb is returned (even if there is a Class object 
	 * available for the persistence-capable).
	 * @param className the fully qualified name of the class to be checked 
	 * @return the names of the field elements for the specified class
	 */
        @Override
	public List getFields(final String className) {
		DevelopmentNameMapper mapper = getNameMapper();
		String testClass = className;

		// need to get names of ejb fields
		if (getCMPDescriptor(className) != null) {
			String ejbName = 
				(isPCClassName(className) ? getEjbName(className) : className);

			return mapper.getFieldsForEjb(ejbName);
		} else {
			String ejbName = mapper.getEjbNameForPersistenceKeyClass(className);

			switch (getPersistenceKeyClassType(className)) {
				// find the field names we need in the corresponding 
				// ejb key class
				case AbstractNameMapper.USER_DEFINED_KEY_CLASS:
					testClass = mapper.getKeyClassForEjbName(ejbName);
					break;
				// find the field name we need in the abstract bean 
				case AbstractNameMapper.PRIMARY_KEY_FIELD:
					return Arrays.asList(new String[]{
						getCMPDescriptor(ejbName).getPrimkeyField()});
				// find the field name we need in the persistence capable 
				case AbstractNameMapper.UNKNOWN_KEY_CLASS:
					String pcClassName = 
						mapper.getPersistenceClassForEjbName(ejbName);
					PersistenceFieldElement[] fields = 
						getPersistenceClass(pcClassName).getFields();
					int i, count = ((fields != null) ? fields.length : 0);

					for (i = 0; i < count; i++) {
						PersistenceFieldElement pfe = fields[i];

						if (pfe.isKey())
							return Arrays.asList(new String[]{pfe.getName()});
					}
					break;
			}
		}

		return super.getFields(testClass);
	}

	/** Returns a list of names of all the field elements in the
	 * class with the specified name.  This list includes the inherited 
	 * fields.  If the specified className represents a 
	 * persistence-capable class, the list of field names from the  
	 * corresponding ejb is returned (even if there is a Class object 
	 * available for the persistence-capable).  This method overrides 
	 * the one in Model in order to do special handling for a 
	 * persistence-capable key class name which corresponds to a bean 
	 * with a primary key field.  In that case, we don't want to
	 * climb the inheritance hierarchy of the primary key field type.
	 * @param className the fully qualified name of the class to be checked
	 * @return the names of the field elements for the specified class
	 */
        @Override
	public List getAllFields(String className) {
		// If the class name corresponds to a pk field (which means that 
		// there is no user defined primary key class, we don't want to 
		// climb the inheritance hierarchy, we only process this class.
		return ((AbstractNameMapper.PRIMARY_KEY_FIELD == 
			getPersistenceKeyClassType(className)) ? getFields(className) : 
			super.getAllFields(className));
	}

	/** Returns a wrapped field element for the specified fieldName in the 
	 * class with the specified className.  If the specified className 
	 * represents a persistence-capable class, a field representing the 
	 * field in the abstract bean class for the corresponding ejb is always 
	 * returned (even if there is a Field object available for the 
	 * persistence-capable).  If there is an ejb name and an abstract bean
	 * class with the same name, the abstract bean class which is associated
	 * with the ejb will be used, not the abstract bean class which  
	 * corresponds to the supplied name (directly).
	 * @param className the fully qualified name of the class which contains
	 * the field to be checked
	 * @param fieldName the name of the field to be checked
	 * @return the wrapped field element for the specified fieldName
	 */
        @Override
	public Object getField(final String className, String fieldName) {
		String testClass = className;
		Object returnObject = null;

		if (className != null) {
			AbstractNameMapper mapper = getNameMapper();
			boolean isPCClass = isPCClassName(className);
			boolean isPKClassName = false;
			String searchClassName = className;
			String searchFieldName = fieldName;

			// translate the class name & field names to corresponding 
			// ejb name's abstract bean equivalents if necessary
			if (isPCClass) {
				searchFieldName = 
					mapper.getEjbFieldForPersistenceField(className, fieldName);
				searchClassName = getEjbName(className);
			} else {	// check if it's a pk class without a user defined key class
				String ejbName = 
					mapper.getEjbNameForPersistenceKeyClass(className);

				switch (getPersistenceKeyClassType(className)) {
					// find the field we need in the corresponding 
					// abstract bean (translated below from ejbName)
					case AbstractNameMapper.PRIMARY_KEY_FIELD:
						testClass = ejbName;
						searchClassName = ejbName;
						isPKClassName = true;
						break;
					// find the field we need by called updateFieldWrapper
					// below which handles the generated field for the 
					// unknown key class - need to use the
					// persistence-capable class name and flag to call that
					// code, so we configure it here
					case AbstractNameMapper.UNKNOWN_KEY_CLASS:
						testClass = 
							mapper.getPersistenceClassForEjbName(ejbName);
						isPCClass = true;
						isPKClassName = true;
						break;
				}
			}

			if (mapper.isEjbName(searchClassName)) {
				searchClassName = 
					mapper.getAbstractBeanClassForEjbName(searchClassName);
			}

			returnObject = super.getField(searchClassName, searchFieldName);

			if (returnObject == null)	// try getting it from the descriptor
				returnObject = getFieldWrapper(testClass, searchFieldName);
			else if (returnObject instanceof NamedElementHandleWrapper)		// wrap it
				returnObject = new MemberWrapper((NamedElementHandleWrapper)returnObject);

			if (isPCClass) {
				returnObject = updateFieldWrapper(
					(MemberWrapper)returnObject, testClass, fieldName);
			}
			// when asking for these fields as part of the 
			// persistence-capable's key class, we need to represent the 
			// public modifier which will be generated in the inner class
			if (isPKClassName && (returnObject instanceof MemberWrapper))
				((MemberWrapper)returnObject)._modifiers = Modifier.PUBLIC;
			
		}

		return returnObject;
	}

	/** Returns the field type for the specified fieldName in the class
	 * with the specified className.  This method is overrides the one in 
	 * Model in order to do special handling for non-collection relationship 
	 * fields.  If it's a generated relationship that case, the returned 
	 * MemberWrapper from getField contains a type of the abstract bean and 
	 * it's impossible to convert that into the persistence capable class name,
	 * so here  that case is detected, and if found, the ejb name is extracted 
	 * and used to find the corresponding persistence capable class.  For a 
	 * relationship which is of type of the local interface, we do the 
	 * conversion from local interface to persistence-capable class.  In the 
	 * case of a collection relationship (generated or not), the superclass' 
	 * implementation which provides the java type is sufficient.
	 * @param className the fully qualified name of the class which contains
	 * the field to be checked
	 * @param fieldName the name of the field to be checked
	 * @return the field type for the specified fieldName
	 */
        @Override
	public String getFieldType(String className, String fieldName) {
		String returnType = super.getFieldType(className, fieldName);

		if (!isCollection(returnType) && isPCClassName(className)) {
			AbstractNameMapper mapper = getNameMapper();
			String ejbName = mapper.getEjbNameForPersistenceClass(className);
			String ejbField = 
				mapper.getEjbFieldForPersistenceField(className, fieldName);

			if (mapper.isGeneratedEjbRelationship(ejbName, ejbField)) {
				String[] inverse = 
					mapper.getEjbFieldForGeneratedField(ejbName, ejbField);
            
				returnType = mapper.getPersistenceClassForEjbName(inverse[0]);
			}

			if (mapper.isLocalInterface(returnType)) {
				returnType = mapper.getPersistenceClassForLocalInterface(
					className, fieldName, returnType);
			}
		}

		return returnType;
	}

	/** Returns the string representation of declaring class of 
	 * the specified member element.  Note, the member element is 
	 * either a class element as returned by getClass, a field element 
	 * as returned by getField, a constructor element as returned by 
	 * getConstructor, or a method element as returned by getMethod 
	 * executed on the same model instance.  This implementation expects 
	 * the member element to be a reflection instance or a wrapped member 
	 * instance.
	 * @param memberElement the member element to be checked
	 * @return the string representation of the declaring class of 
	 * the specified memberElement
	 * @see #getClass
	 * @see #getField
	 * @see #getConstructor
	 * @see #getMethod
	 */
        @Override
	public String getDeclaringClass(Object memberElement) {
		if ((memberElement != null) && 
				(memberElement instanceof MemberWrapper)) {
			NamedElementHandleWrapper classElement = 
				((MemberWrapper)memberElement).getDeclaringClass();

                        return ((classElement != null) ? 
				classElement.toString() : null);
		}

		return super.getDeclaringClass(memberElement);
	}

	/** Returns the modifier mask for the specified member element.
	 * Note, the member element is either a class element as returned by 
	 * getClass, a member wrapper element as returned by getField or 
	 * getMethod, a constructor element as returned by getConstructor 
	 * executed on the same model instance. 
	 * This implementation expects the member element being a reflection 
	 * instance or a wrapped member instance.
	 * @param memberElement the member element to be checked
	 * @return the modifier mask for the specified memberElement
	 * @see java.lang.reflect.Modifier
	 * @see #getClass
	 * @see #getField
	 * @see #getConstructor
	 * @see #getMethod
	 */
        @Override
	public int getModifiers(Object memberElement) {
		if ((memberElement != null) && (memberElement instanceof MemberWrapper))
			return ((MemberWrapper)memberElement).getModifiers();

		return super.getModifiers(memberElement);
	}

	/** Returns the modifier mask for the specified className.  This method 
	 * overrides the one in Model to strip out the abstract modifier when 
	 * the persistence capable class is represented by the abstract bean.
	 * It also adds the static modifier when the specified class represents
	 * the persistence capable key class which will be generated.
	 * @param className the fully qualified name of the class to be checked
	 * @return the modifier mask for the specified class
	 * @see java.lang.reflect.Modifier
	 */
        @Override
	public int getModifiersForClass(String className) {
		int modifiers = super.getModifiersForClass(className);

		if (isPCClassName(className))
			modifiers &= ~Modifier.ABSTRACT;
		else if (getNameMapper().
				getKeyClassForPersistenceKeyClass(className) != null) {
			modifiers |= Modifier.STATIC;
		}

		return modifiers;
	}

	/** Determines if the specified className and fieldName pair represent a
	 * field which has a type which is valid for key fields.  Valid key 
	 * field types include non-collection SCOs (wrappers, Date, Time, etc.) 
	 * and primitives for user defined key classes.  This method overrides 
	 * the one in Model in order to do special handling for a 
	 * persistence-capable key class name which corresponds to a bean 
	 * with a primary key field.  In that case, we want to restrict the 
	 * list of allowable types not to include primitives.
	 * @param className the fully qualified name of the class which contains
	 * the field to be checked
	 * @param fieldName the name of the field to be checked
	 * @return <code>true</code> if this field name represents a field 
	 * with a valid type for a key field; <code>false</code> otherwise.
	 */
        @Override
	public boolean isValidKeyType(String className, String fieldName) {
		return (((AbstractNameMapper.PRIMARY_KEY_FIELD == 
			getPersistenceKeyClassType(className)) && 
			isPrimitive(className, fieldName)) ? false : 
			super.isValidKeyType(className, fieldName));
	}

	/** Returns the Type type of the specified element. 
	 * If element denotes a field, it returns the type of the field. 
	 * If element denotes a method, it returns the return type of the method. 
	 * Note, element is either a field element as returned by getField, or a 
	 * method element as returned by getMethod executed on the same model 
	 * instance.  This method must be called in a javac context.
	 * @param element the element to be checked
	 * @return the Type type of the element
	 * @see #getField
	 * @see DevelopmentModel#getMethod
	 */
        @Override
	protected TypeMirror getTypeObject(Object element) {
                TypeMirror type = super.getTypeObject(element);

                if ((element != null) && (element instanceof MemberWrapper)) {
                    final List<TypeMirror> result = new ArrayList<TypeMirror>();
                    final MemberWrapper wrapper = (MemberWrapper)element;
                    final NamedElementHandleWrapper namedWrapper = wrapper.getDeclaringClass();
                    final ElementHandle classElement = namedWrapper.getHandle();

                    if (classElement != null)
                    {
                        forAllSourceRoots(new SourceRootTask() {
                            public boolean run(CompilationController controller) {
                                TypeElement clazz = (TypeElement)classElement.resolve(controller);

                                if (clazz != null) {  // found the correct javac context
                                    result.add(getWrapperType(namedWrapper).resolve(controller));
                                }
                                return (!result.isEmpty());
                            }
                        });
                    }
                    return (result.isEmpty() ? null : result.get(0));
                }

		return type;
	}

	// return true if a conversion to a ejb class is needed for 
	// org.openide.src metadata
	private boolean isPCClassName(String className) {
		return (getEjbName(className) != null);
	}

	private String getEjbName(String className) {
		return getNameMapper().getEjbNameForPersistenceClass(className);
	}

	private Entity getCMPDescriptor(String className) {
		String descriptorName = (isPCClassName(className) ? 
			getEjbName(className) : className);

		return getNameMapper().getDescriptorForEjbName(descriptorName);
	}

	private int getPersistenceKeyClassType(String className) {
		int returnValue = -1;

		if (getCMPDescriptor(className) == null) {
			AbstractNameMapper mapper = getNameMapper();
			String ejbName = mapper.getEjbNameForPersistenceKeyClass(className);

			if (ejbName != null)
				returnValue = mapper.getKeyClassTypeForEjbName(ejbName);
		}

		return returnValue;
	}

	private MemberWrapper getFieldWrapper(String className, String fieldName) {
		Entity descriptor = getCMPDescriptor(className);
                final List<TypeMirrorHandle> result = new ArrayList<TypeMirrorHandle>();
		MemberWrapper returnObject = null;

		if (descriptor != null) {
                    String getter = "get" + 	// NOI18N
                        StringHelper.getCapitalizedString(fieldName);

                    try {
                        final Object method = getMethod(className, getter, NO_ARGS);

                        if (method != null) {
                            NamedElementHandleWrapper element = (NamedElementHandleWrapper)getClass(className);

                            if (element != null)
                            {
                                final ElementHandle thisElement = element.getHandle();

                                forAllSourceRoots(new SourceRootTask() {
                                    public boolean run(CompilationController controller) {
                                        Element resolvedElement = thisElement.resolve(controller);

                                        if (resolvedElement != null) {  // found the correct javac context
                                            result.add(TypeMirrorHandle.create(
                                                    getTypeObject(method)));
                                        }
                                        return (!result.isEmpty());
                                    }
                                });
                            }
                        }
                    }
                    catch (Exception ex) {
                        // will be null - there is no such field
                    }

                    returnObject = (result.isEmpty() ? null :
                        new MemberWrapper(fieldName, result.get(0), 
                            Modifier.PRIVATE, (NamedElementHandleWrapper)getClass(className)));
		}

		return returnObject;
	}

	private MemberWrapper updateFieldWrapper(MemberWrapper returnObject, 
			String className, String fieldName) {
		AbstractNameMapper mapper = getNameMapper();

		if (returnObject == null) {
			// can't call isPersistent or isKey because that calls 
			// hasField which calls getField and that would end up
			// in an endless loop
			PersistenceFieldElement field =
				getPersistenceFieldInternal(className, fieldName);

			if (field != null) {
				String ejbName = getEjbName(className);
				String ejbFieldName = 
					mapper.getEjbFieldForPersistenceField(className, fieldName);

				// Check if this is the auto-added field for unknown pk 
				// support.  If so, return a private field of type Long.
				if (field.isKey() && (ejbName != null) && 
						(mapper.getKeyClassTypeForEjbName(ejbName) == 
						AbstractNameMapper.UNKNOWN_KEY_CLASS)) {
                                        NamedElementHandleWrapper wrapper = 
                                                (NamedElementHandleWrapper)getClass(className);
					returnObject = new MemberWrapper(ejbFieldName, 
						getTypeForName(TypeKind.LONG.toString(), wrapper), Modifier.PRIVATE, 
						wrapper);
				}

				// Check if this is the auto-added field for 2 way managed rels 
				// support.  If so, return a private field of type according to 
				// cardinality of the relationship.
				else if ((field instanceof RelationshipElement) && 
						mapper.isGeneratedEjbRelationship(ejbName, 
						ejbFieldName)) {
					RelationshipElement rel = (RelationshipElement)field;
					TypeMirrorHandle classType = null;

					// figure out the type
					if (rel.getUpperBound() > 1) {
                                                NamedElementHandleWrapper wrapper = 
                                                        (NamedElementHandleWrapper)getClass(className);

                                                classType = getTypeForName(
							"java.util.HashSet", wrapper); // NOI18N
					} else {
						String[] inverse = mapper.
							getEjbFieldForGeneratedField(ejbName, ejbFieldName);

						classType = getWrapperType(
							((NamedElementHandleWrapper)getClass(inverse[0])));
					}

					if (classType != null) {
						returnObject = new MemberWrapper(ejbFieldName, 
							classType, Modifier.PRIVATE, 
							(NamedElementHandleWrapper)getClass(className));
					}
				}
				// Check if this is the auto-added version field.
				// If so, return a private field of type long.
				else if (ejbFieldName.startsWith(
						AbstractNameMapper.GENERATED_VERSION_FIELD_PREFIX) && 
						mapper.isGeneratedField(ejbName, ejbFieldName)) {
                                        NamedElementHandleWrapper wrapper = 
                                                (NamedElementHandleWrapper)getClass(className);
					returnObject = new MemberWrapper(ejbFieldName, 
						getTypeForName(TypeKind.LONG.toString(), wrapper), Modifier.PRIVATE, 
						wrapper);
				}
			}
		}

		// if the field in the corresponding ejb is a serializable, 
		// non-primitive, non-wrapper type, convert it to byte[] here
		if (!isPersistentTypeAllowed(getType(returnObject), 
				getClassLoader()) && isSerializable(returnObject)) {
                    // TODO - is this how to get TypeMirror for byte[]?
			returnObject.setType(getTypeForName(TypeKind.BYTE.toString(), 
                                returnObject.getDeclaringClass()));
		}

		return returnObject;
	}

	private class MemberWrapper {
		private String _name;
		private TypeMirrorHandle _type;
		private int _modifiers;
		private NamedElementHandleWrapper _declaringClass;

		private MemberWrapper (NamedElementHandleWrapper member) {
			this(member.toString(), getWrapperType(member), 
				EJBDevelopmentModel.this.getModifiers(member),
                                getWrapperDeclaringClass(member));
		}

		private MemberWrapper (String name, TypeMirrorHandle type, int modifiers, 
				NamedElementHandleWrapper declaringClass) {
			_name = name;
			_type = type;
			_modifiers = modifiers;
			_declaringClass = declaringClass;
		}

		private TypeMirrorHandle getType () { return _type; }
		private void setType (TypeMirrorHandle type) { _type = type; }
		private String getName () { return _name; }
		private int getModifiers () { return _modifiers; }
		private NamedElementHandleWrapper getDeclaringClass () { return _declaringClass; }

		/** Returns a string representation of this object.
		 * @return a string reprentation of the member wrapper object.
		 */		
                @Override
		public String toString () { return getName(); }
	}
}
