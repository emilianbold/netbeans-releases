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
 * DevelopmentModel.java
 *
 * Created on March 10, 2000, 11:05 AM
 */

package org.netbeans.lib.j2ee.sun.persistence.utility.openide;

import java.io.*;
import java.util.*;

import org.openide.filesystems.*;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import com.sun.jdo.api.persistence.model.*;
import com.sun.jdo.spi.persistence.utility.*;
import com.sun.jdo.spi.persistence.utility.logging.Logger;
import com.sun.jdo.api.persistence.model.mapping.MappingClassElement;
import com.sun.jdo.api.persistence.model.util.LogHelperModel;
import java.lang.ref.WeakReference;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.TypeMirrorHandle;

// TODO - Model.DEVELOPMENT is null because it depends on 
// existence of old java src model classes, but probably okay since
// we no longer want a singleton of it - but double check

/** 
 *
 * @author raccah
 * @version %I%
 */
public class DevelopmentModel extends Model
{
	// cache of DevelopmentModel instances: key Project, value Model
	private static final Map<Project, Model> _modelCache = new WeakHashMap<Project, Model>();

	/** Map of file locks which have been loaded.  Keys are class names.  
	 * Note that this is a hack until the model classes are rewritten.
	 */
	private HashMap<String, FileLock> _locks;

	/** A Project representing the source roots for this 
	 * model.  Classes and files will only be found under this project.
	 */
	private WeakReference<Project> _projectRef;

	/**
	 *
	 */
	private HashMap<String, TypeKind> primitiveTypes;
        
        public static Model getModel (FileObject fileObject)
	{
            return getModel(((fileObject != null) ? 
                FileOwnerQuery.getOwner(fileObject) : (Project)null));
        }

        public static synchronized Model getModel (Project project)
	{
		Model value = _modelCache.get(project);

		if (value == null)
		{
			value = new DevelopmentModel(project);
			_modelCache.put(project, value);
		}

                // TODO: when close a project, remove from cache - 
                // how to tell when a project is closed?
		return value;
	}

        private DevelopmentModel()
        {
            this(null);
        }

	/** Creates a new DevelopmentModel instance which includes the specified
	 * source roots.
	 * @param project a Project representing the source roots
	 * for this model
	 */
	protected DevelopmentModel (Project project)
	{
		super();

		_projectRef = new WeakReference<Project>(project);

		// initialize primitiveTypes;
		primitiveTypes = new HashMap<String, TypeKind>();
		primitiveTypes.put("boolean", TypeKind.BOOLEAN); // NOI18N
		primitiveTypes.put("byte", TypeKind.BYTE); // NOI18N
		primitiveTypes.put("short", TypeKind.SHORT); // NOI18N
		primitiveTypes.put("char", TypeKind.CHAR); // NOI18N
		primitiveTypes.put("int", TypeKind.INT); // NOI18N
		primitiveTypes.put("long", TypeKind.LONG); // NOI18N
		primitiveTypes.put("float", TypeKind.FLOAT); // NOI18N
		primitiveTypes.put("double", TypeKind.DOUBLE); // NOI18N
		primitiveTypes.put("void", TypeKind.VOID); // NOI18N
	}

	/** Get an array of FileObjects representing the source roots for this 
	 * model.  Classes and files will only be found under these roots.
	 * @return an array of FileObjects representing the source roots
	 * for this model
	 */
	protected FileObject[] getSourceRoots ()
	{
            FileObject [] srcDirs = null;
            Project project = _projectRef.get();
            if(project != null) {
                SourceGroup[] groups = ProjectUtils.getSources(project).
                    getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                srcDirs = new FileObject[groups.length];
                
                for (int i = 0; i < groups.length; i++) {
                    srcDirs[i] = groups[i].getRootFolder();
                }
            } else {
                // Should never happen (would mean project was GC'd), but...
                srcDirs = new FileObject[0];
            }
 
            return srcDirs;
	}

    protected void forAllSourceRoots(final SourceRootTask task) {
        final FileObject[] sourceRoots = getSourceRoots();
        final List<Boolean> result = new ArrayList<Boolean>();

        if (sourceRoots != null)
        {
            for (int i = 0; i < sourceRoots.length && result.isEmpty(); i++)
            {
                JavaSource js = JavaSource.forFileObject(sourceRoots[i]);

                try {
                    js.runUserActionTask(new Task<CompilationController>() {
                        public void run(CompilationController controller) throws Exception{
                            controller.toPhase(Phase.ELEMENTS_RESOLVED);
                            if (task.run(controller)) {
                                result.add(true);
                            }
                        }
                    }, true);                   
                } catch (Exception e) {
                    LogHelperModel.getLogger().log(Logger.WARNING, 
                            e.getMessage());
                }
            }
        }

} 
	/** Determines if the specified className represents an interface type.
	 * @param className the fully qualified name of the class to be checked 
	 * @return <code>true</code> if this class name represents an interface;
	 * <code>false</code> otherwise.
	 */
	public boolean isInterface (String className)
	{
                NamedElementHandleWrapper wrapper = (NamedElementHandleWrapper)getClass(className);
                final List<Boolean> result = new ArrayList<Boolean>();

		if (wrapper != null)
		{
                    final ElementHandle classElement = wrapper.getHandle();

                    forAllSourceRoots(new SourceRootTask() {
                        public boolean run(CompilationController controller) {
                            TypeElement clazz = (TypeElement)classElement.resolve(controller);

                            if (clazz != null) {  // found the correct javac context
                                if (ElementKind.INTERFACE == clazz.getKind()) {
                                    result.add(true);
                                }
                            }
                            return (!result.isEmpty());
                        }
                    });
                }
                return (result.isEmpty() ? false : result.get(0));
	}

	/** Returns the input stream with the supplied resource name found with 
	 * the supplied class name.
	 * @param className the fully qualified name of the class which will 
	 * be used as a base to find the resource
	 * @param classLoader the class loader (ignored by this implementation)
	 * @param resourceName the name of the resource to be found
	 * @return the input stream for the specified resource, <code>null</code> 
	 * if an error occurs or none exists
	 */
	protected BufferedInputStream getInputStreamForResource (String className, 
		ClassLoader classLoader, String resourceName)
	{
		try
		{
			FileObject file = getFile(className, resourceName);

                        return ((file != null) ? 
				new BufferedInputStream(file.getInputStream()) : null);
		}
		catch (Exception e)
		{
			return null;
		}
	}

        // must happen in a javac context
        private TypeElement getSuperclass(TypeElement type) {
            TypeMirror supertype = type.getSuperclass();
            if (TypeKind.DECLARED.equals(supertype.getKind())) {
                Element element = ((DeclaredType)supertype).asElement();
                if (ElementKind.CLASS.equals(element.getKind())) {
                    TypeElement superclass = (TypeElement)element;
                    if (!superclass.getQualifiedName().contentEquals("java.lang.Object")) { // NOI18N
                        return superclass;
                    }
                }
            }
            return null;
        }

	/** Returns the name of the second to top (top excluding java.lang.Object) 
	 * superclass for the given class name.
	 * @param className the fully qualified name of the class to be checked
	 * @return the top non-Object superclass for className, 
	 * <code>className</code> if an error occurs or none exists
	 */
	protected String findPenultimateSuperclass (String className)
	{
                NamedElementHandleWrapper wrapper = (NamedElementHandleWrapper)getClass(className);
                final List<String> result = new ArrayList<String>();

		if (wrapper != null)
		{
                    final ElementHandle classElement = wrapper.getHandle();

                    if (classElement == null)
                            result.add(className);
                    else
                    {
                        forAllSourceRoots(new SourceRootTask() {
                            public boolean run(CompilationController controller) {
                                TypeElement clazz = (TypeElement)classElement.resolve(controller);

                                if (clazz != null) {  // found the correct javac context
                                    TypeElement superclass = null;

                                    while ((superclass = getSuperclass(clazz)) != null)
                                    {
                                        clazz = superclass;// TODO: can I do this instead of : (TypeElement)getClass(testName);
                                            // reached the top of the hierachy NetBeans recognizes, need
                                            // to use reflection from here up
                           // TODO uncomment this later after packages and factories/caching is set
                    //			if (classElement == null)
                    //				return Model.RUNTIME.findPenultimateSuperclass(testName);
                                    }
                                    result.add(clazz.getQualifiedName().toString());
                                }
                                return (!result.isEmpty());
                            }
                        });
                    }
                }

                return (result.isEmpty() ? className : result.get(0));
	}

	/** Returns the name of the superclass for the given class name.
	 * @param className the fully qualified name of the class to be checked
	 * @return the superclass for className, <code>null</code> if an error 
	 * occurs or none exists
	 */
	protected String getSuperclass (String className)
	{
                NamedElementHandleWrapper wrapper = (NamedElementHandleWrapper)getClass(className);
                final List<String> result = new ArrayList<String>();

		if (wrapper != null)
		{
                    final ElementHandle classElement = wrapper.getHandle();

                    if (classElement != null)
                    {
                        forAllSourceRoots(new SourceRootTask() {
                            public boolean run(CompilationController controller) {
                                TypeElement clazz = (TypeElement)classElement.resolve(controller);

                                if (clazz != null) {  // found the correct javac context
                                    TypeElement superclass = getSuperclass(clazz);

                                    result.add((superclass != null) ? 
                                        superclass.getQualifiedName().toString() : null);
                                }
                                return (!result.isEmpty());
                            }
                        });
                    }
                }

                return (result.isEmpty() ? null : result.get(0));
	}

	private synchronized Map<String, FileLock> getLocks ()
	{
		if (_locks == null)
			_locks = new HashMap<String, FileLock>();

		return _locks;
	}
	
	private synchronized FileLock getLock (String className)
	{
		return getLocks().get(className);
	}

	private synchronized void putLock (String className, FileLock lock)
	{
		getLocks().put(className, lock);
	}

	private synchronized void removeLock (String className)
	{
		getLocks().remove(className);
	}

	/** Creates a file with the given base file name and extension 
	 * parallel to the supplied class (if it does not yet exist).
	 * @param className the fully qualified name of the class
	 * @param baseFileName the name of the base file
	 * @param extension the file extension
	 * @return the output stream for the specified resource, <code>null</code> 
	 * if an error occurs or none exists
	 * @exception IOException if there is some error creating the file
	 */
	protected BufferedOutputStream createFile (String className,
		String baseFileName, String extension) throws IOException
	{
		char extensionCharacter = '.';
		FileObject file = getFile(className, 
			baseFileName + extensionCharacter + extension);

		if (file == null)
		{
			Repository repository = Repository.getDefault();
			String packageName = JavaTypeHelper.getPackageName(className);
			String shortClassName = JavaTypeHelper.getShortClassName(className);
			FileObject javaFile = repository.find(packageName, 
				shortClassName, "java");	// NOI18N

			if (javaFile != null)
			{
				file = javaFile.getParent().createData(
					shortClassName, extension);
			}
			else
				throw new FileNotFoundException();
		}

		if (file != null)
			return new BufferedOutputStream(
              file.getOutputStream(lockFile(className, file)));

		return null;
	}

        @Override
	public void lockFile (String className) throws IOException
	{
		lockFile(className, getFile(className, 
			getFileNameWithExtension(className)));
	}

	private FileLock lockFile (String className, FileObject file)
		throws IOException
	{
		FileLock lock = null;

		if (file != null)
		{
			lock = getLock(className);	// use as is if already locked

			if (lock == null)
			{
				lock = file.lock();
				putLock(className, lock);
			}
		}

		return lock;
	}

        @Override
	public void unlockFile (String className)
	{
		if (className != null)
		{
			FileLock lock = getLock(className);
			
			if (lock != null)
			{
				lock.releaseLock();
				removeLock(className);
			}
		}

		super.unlockFile(className);
	}

	/** Updates the key in the cache for the supplied MappingClassElement.
	 * @param mappingClass the mapping class to be put in the cache
	 * (the new name is extracted from this element).  The corresponding 
	 * handling of the files is automatically handled by the data object. 
	 * (use <code>null</code> to remove the old key but not replace it)
	 * @param oldName the fully qualified name of the old key for the mapping 
	 * class (use <code>null</code> to add the new key but not replace it)
	 */
        @Override
	public void updateKeyForClass (MappingClassElement mappingClass, 
		String oldName)
	{
		super.updateKeyForClass(mappingClass, oldName);

		if (oldName != null)
		{
			FileLock lock = getLock(oldName);

			if (lock != null)
			{
				unlockFile(oldName);
				if (mappingClass != null)
				{
					try
					{
						lockFile(mappingClass.getName());
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}

	/** Deletes the file with the given file name which is parallel 
	 * to the supplied class.
	 * @param className the fully qualified name of the class
	 * @param fileName the name of the file
	 * @exception IOException if there is some error deleting the file
	 */
	protected void deleteFile (String className, String fileName)
		throws IOException
	{
		FileObject file = getFile(className, fileName);
		FileLock lock = lockFile(className, file);

		try
		{
			file.delete(lock);
		}
		finally
		{
			unlockFile(className);
		}
	}

	/** Returns the first file with the given file name which is parallel 
	 * to the supplied class which is not in a readonly (jar) filesystem.
	 * @param className the fully qualified name of the class
	 * @param fileName the name of the file
	 * @return the file object for the specified resource, <code>null</code> 
	 * if an error occurs
	 * @exception IOException if there is some error getting the file
	 */
	protected FileObject getFile (String className, String fileName)
		throws IOException
	{
		FileObject[] sourceRoots = getSourceRoots();

		if ((fileName != null) && (sourceRoots != null))
		{
			for (int i = 0; i < sourceRoots.length; i++)
			{
				ClassPath cp = ClassPath.getClassPath(
					sourceRoots[i], ClassPath.SOURCE);
// TODO - check if changing this affects EJB support in NB 4.1
 //                                  sourceRoots[i], ClassPath.COMPILE);
				List matches = cp.findAllResources(
					fileName.replace(File.separatorChar, '/'));
				Iterator iterator = matches.iterator();

				while (iterator.hasNext())
				{
					FileObject nextMatch = (FileObject)iterator.next();

					if (!nextMatch.getFileSystem().isReadOnly())
						return nextMatch;
				}
			}
		}

		return null;
	}

	/** Returns the class element with the specified className.
	 * @param className the fully qualified name of the class to be checked 
	 * @param classLoader the class loader (ignored by this implementation)
	 * @return the class element for the specified className
	 */
	public Object getClass (final String className, ClassLoader classLoader)
	{
                final List<NamedElementHandleWrapper> result = new ArrayList<NamedElementHandleWrapper>();

                if (className != null)
                {
                    forAllSourceRoots(new SourceRootTask() {
                        public boolean run(CompilationController controller) {
                            TypeElement returnObject = controller.getElements().getTypeElement(className);

                            if (returnObject != null) {
                                ElementHandle classHandle = ElementHandle.create(returnObject);

                                result.add(new NamedElementHandleWrapper(
                                    classHandle, classHandle.getQualifiedName()));
                            }
                            return (!result.isEmpty());
                        }
                    });
                }

                return (result.isEmpty() ? null : result.get(0));
	}

	/** Determines if the specified class implements the specified interface. 
	 * Note, class element is a model specific class representation as returned 
	 * by a getClass call executed on the same model instance. This 
	 * implementation expects the class element being an openide source element 
	 * instance.
	 *
	 * Note, this method does not provide an complete implementation. It only 
	 * checks whether this class or one of its superclasses directly implements 
	 * the specified interface. But the method does not check whether this 
	 * class implements an interface that inherits from interface to be checked.
	 * Following the Java language the method should return true in this case, 
	 * but the actual implmentation returns false.
	 *
	 * @param classElement the class element to be checked
	 * @param interfaceName the fully qualified name of the interface to 
	 * be checked
	 * @return <code>true</code> if the class implements the interface; 
	 * <code>false</code> otherwise.
	 * @see #getClass
	 */
	public boolean implementsInterface (Object classElement, 
	   final String interfaceName)
	{
		if ((classElement == null) || !(classElement instanceof NamedElementHandleWrapper) ||
			(interfaceName == null))
			return false;
                final ElementHandle thisClass = ((NamedElementHandleWrapper)classElement).getHandle();
                final List<Boolean> result = new ArrayList<Boolean>();

		if (thisClass != null)
		{
                        forAllSourceRoots(new SourceRootTask() {
                            public boolean run(CompilationController controller) {
                                TypeElement clazz = (TypeElement)thisClass.resolve(controller);

                                if (clazz != null) {  // found the correct javac context
                                    // check interfaces which this class implements directly
                                    List<? extends TypeMirror> interfaces = clazz.getInterfaces();
                                    for (TypeMirror typeMirror : interfaces)
                                    {
                                        if (TypeKind.DECLARED.equals(typeMirror))
                                        {
                                            Element element = ((DeclaredType)typeMirror).asElement();
                                            if (ElementKind.CLASS.equals(element.getKind()))
                                            {
                                                TypeElement typeElement = (TypeElement)element;

                                                if (typeElement.getQualifiedName().contentEquals(interfaceName)) 
                                                {
                                                    result.add(true);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                                // this class does not directly implement the interface => 
                                // check superclass
                                TypeElement superclass = getSuperclass(clazz);
                                if (result.isEmpty() && ((superclass == null) || superclass.getQualifiedName().contentEquals("java.lang.Object"))) {
                                        // reached top of hierachy => return false
                                        result.add(false);
                                } else {
// TODO - is this the correct way to make this call given context issues?
                                    ElementHandle superHandle = ElementHandle.create(superclass);
                                    boolean superResult = implementsInterface(
                                            new NamedElementHandleWrapper(superHandle, superHandle.getQualifiedName()), 
                                            interfaceName);
                                    if (superResult) {
                                        result.add(superResult);
                                    }
                                }
                                return (!result.isEmpty());
                            }
                        });
                }

                return (result.isEmpty() ? false : result.get(0));
	}
	
	/** Determines if the class with the specified name declares a constructor.
	 * @param className the name of the class to be checked
	 * @return <code>true</code> if the class declares a constructor; 
	 * <code>false</code> otherwise.
	 * @see #getClass
	 */
	public boolean hasConstructor (String className)
        {
                NamedElementHandleWrapper wrapper = (NamedElementHandleWrapper)getClass(className);
                final List<Boolean> result = new ArrayList<Boolean>();

		if (wrapper != null)
		{
                    final ElementHandle classElement = wrapper.getHandle();

                    forAllSourceRoots(new SourceRootTask() {
                        public boolean run(CompilationController controller) {
                            TypeElement clazz = (TypeElement)classElement.resolve(controller);

                            if (clazz != null) {  // found the correct javac context
                                List<? extends Element> allElements = clazz.getEnclosedElements();

                                for (Element element : allElements)
                                {
                                    if (ElementKind.CONSTRUCTOR.equals(element.getKind())) 
                                    {
                                        result.add(true);
                                        break;
                                    }
                                }
                            }
                            return (!result.isEmpty());
                        }
                    });
                }
                return (result.isEmpty() ? false : result.get(0));
	}

	/** Returns the constructor element for the specified argument types 
	 * in the class with the specified name. Types are specified as type 
	 * names for primitive type such as int, float or as fully qualified 
	 * class names.
	 * @param className the name of the class which contains the constructor 
	 * to be checked
	 * @param argTypeNames the fully qualified names of the argument types
	 * @return the constructor element
	 * @see #getClass
	 */
	public Object getConstructor (String className, String[] argTypeNames)
	{
            return getExecutableObject(ElementKind.CONSTRUCTOR, null, className, argTypeNames);
        }

        private Object getExecutableObject (final ElementKind kind, final String elementName, 
                final String className, final String[] argTypeNames)
	{
                NamedElementHandleWrapper wrapper = (NamedElementHandleWrapper)getClass(className);
                final List<NamedElementHandleWrapper> result = new ArrayList<NamedElementHandleWrapper>();

		if (wrapper != null)
		{
                    final ElementHandle classElement = wrapper.getHandle();

                    forAllSourceRoots(new SourceRootTask() {
                        public boolean run(CompilationController controller) {
                            TypeElement clazz = (TypeElement)classElement.resolve(controller);

                            if (clazz != null) {  // found the correct javac context
                                List<? extends Element> allElements = clazz.getEnclosedElements();

                                for (Element element : allElements)
                                {
                                    if (kind.equals(element.getKind())) 
                                    {
                                        if ((elementName == null) || element.getSimpleName().contentEquals(elementName))
                                        {
                                            ExecutableType executableType = (ExecutableType)element.asType();
                                            if (isArgTypeMatch(argTypeNames, executableType.getParameterTypes()))
                                            {
                                                result.add(new NamedElementHandleWrapper(
                                                    ElementHandle.create(element), elementName));
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                            return (!result.isEmpty());
                        }
                    });
                }

                return (result.isEmpty() ? null : result.get(0));
	}

        // must be called in a javac context
        private boolean isArgTypeMatch (String[] argTypeNames, 
                List<? extends TypeMirror> testParams)
        {
            if ((argTypeNames == null) && (testParams == null)) 
            {
                return true;
            }
            if ((argTypeNames != null) && (testParams != null))
            {
                int argTypeLength = argTypeNames.length;
                if (argTypeLength == testParams.size())
                {
                    List<String> testParamNames = new ArrayList<String>(argTypeLength);
                    for (TypeMirror param : testParams)
                    {
                        testParamNames.add(getNameForType(param));
                    }
                    testParamNames.removeAll(Arrays.asList(argTypeNames));
                    return (testParamNames.size() == 0);
                }
            }
            return false;
        }

	/** Returns the method element for the specified method name and argument 
	 * types in the class with the specified name. Types are specified as 
	 * type names for primitive type such as int, float or as fully qualified 
	 * class names.  Note, the method does not return inherited methods.
	 * @param className the name of the class which contains the method 
	 * to be checked
	 * @param methodName the name of the method to be checked
	 * @param argTypeNames the fully qualified names of the argument types
	 * @return the method element
	 * @see #getClass
	 */
	public Object getMethod (String className, String methodName, 
		String[] argTypeNames)
	{
                return getExecutableObject(ElementKind.METHOD, methodName, 
                            className, argTypeNames);
	}

        protected TypeMirrorHandle getWrapperType (NamedElementHandleWrapper element)
	{
                final List<TypeMirrorHandle> result = new ArrayList<TypeMirrorHandle>();

                if (element != null)
                {
                    final ElementHandle thisElement = element.getHandle();

                    forAllSourceRoots(new SourceRootTask() {
                        public boolean run(CompilationController controller) {
                            Element resolvedElement = thisElement.resolve(controller);

                            if (resolvedElement != null) {  // found the correct javac context
                                result.add(TypeMirrorHandle.create(getTypeObject(resolvedElement)));
                            }
                            return (!result.isEmpty());
                        }
                    });
                }

                return (result.isEmpty() ? null : result.get(0));
	}

	/** Returns the string representation of type of the specified element. 
	 * If element denotes a field, it returns the type of the field. 
	 * If element denotes a method, it returns the return type of the method. 
	 * Note, element is either a field element as returned by getField, or a 
	 * method element as returned by getMethod executed on the same model 
	 * instance. This implementation expects the element being an openide source
	 * element instance.
	 * @param element the element to be checked
	 * @return the string representation of the type of the element
	 * @see #getField
	 * @see #getMethod
	 */
	public String getType (Object element)
	{
            final List<String> result = new ArrayList<String>();

            if ((element != null) && (element instanceof NamedElementHandleWrapper)) {
                final NamedElementHandleWrapper wrapper = (NamedElementHandleWrapper)element;
                final ElementHandle classElement = wrapper.getHandle();

                if (classElement != null)
                {
                    forAllSourceRoots(new SourceRootTask() {
                        public boolean run(CompilationController controller) {
                            TypeElement clazz = (TypeElement)classElement.resolve(controller);

                            if (clazz != null) {  // found the correct javac context
                                result.add(getNameForType(
                                    getWrapperType(wrapper).resolve(controller)));
                            }
                            return (!result.isEmpty());
                        }
                    });
                }
            }

            return (result.isEmpty() ? null : result.get(0));
	}

	/** Returns a list of names of all the declared field elements in the 
	 * class with the specified name.
	 * @param className the fully qualified name of the class to be checked 
	 * @return the names of the field elements for the specified class
	 */
	public List getFields (String className)
	{
                NamedElementHandleWrapper wrapper = (NamedElementHandleWrapper)getClass(className);
		final List<String> result = new ArrayList<String>();

		if (wrapper != null)
		{
                    final ElementHandle classElement = wrapper.getHandle();

                    forAllSourceRoots(new SourceRootTask() {
                        public boolean run(CompilationController controller) {
                            TypeElement clazz = (TypeElement)classElement.resolve(controller);

                            if (clazz != null) {  // found the correct javac context
                                List<? extends Element> allElements = clazz.getEnclosedElements();
                                for (Element element : allElements)
                                {
                                    if (ElementKind.FIELD.equals(element.getKind())) 
                                    {
                                        result.add(element.getSimpleName().toString());
                                    }
                                }
                            }
                            return false;
                        }
                    });
                }
                return result;
	}

	/** Returns the field element for the specified fieldName in the class
	 * with the specified className.
	 * @param className the fully qualified name of the class which contains 
	 * the field to be checked 
	 * @param fieldName the name of the field to be checked 
	 * @return the field element for the specified fieldName
	 */
	public Object getField (final String className, final String fieldName)
	{
                NamedElementHandleWrapper wrapper = (NamedElementHandleWrapper)getClass(className);
                final List<NamedElementHandleWrapper> result = new ArrayList<NamedElementHandleWrapper>();

		if (wrapper != null)
		{
                    final ElementHandle classElement = wrapper.getHandle();

                    forAllSourceRoots(new SourceRootTask() {
                        public boolean run(CompilationController controller) {
                            TypeElement clazz = (TypeElement)classElement.resolve(controller);

                            if (clazz != null) {  // found the correct javac context
                                List<? extends Element> allElements = clazz.getEnclosedElements();

                                for (Element element : allElements)
                                {
                                    if (ElementKind.FIELD.equals(element.getKind()) && 
                                            element.getSimpleName().contentEquals(fieldName))
                                    {
                                        result.add(new NamedElementHandleWrapper(
                                            ElementHandle.create(element), fieldName));
                                        break;
                                    }
                                }
                            }
                            return (!result.isEmpty());
                        }
                    });
                }

                return (result.isEmpty() ? null : result.get(0));
	}

	/** Determines if the specified field element has a serializable type. 
	 * A type is serializable if it is a primitive type, a class that implements 
	 * java.io.Serializable or an interface that inherits from 
	 * java.io.Serializable.
	 * Note, the field element is a model specific field representation as 
	 * returned by a getField call executed on the same model instance. This 
	 * implementation expects the filed element being an openide source element 
	 * instance.
	 * @param fieldElement the field element to be checked
	 * @return <code>true</code> if the field element has a serializable type;
	 * <code>false</code> otherwise.
	 * @see #getField
	 */
	public boolean isSerializable (final Object fieldElement)
	{
                final List<Boolean> result = new ArrayList<Boolean>();

                if (!(fieldElement instanceof VariableElement))
			return false;
                final ElementHandle thisElement = ((NamedElementHandleWrapper)fieldElement).getHandle();

                forAllSourceRoots(new SourceRootTask() {
                    public boolean run(CompilationController controller) {
                        Element resolvedElement = thisElement.resolve(controller);

                        if (resolvedElement != null) {  // found the correct javac context
                            TypeMirror type = getTypeObject(fieldElement);
                            if (type == null) {
                                    result.add(false);
                                    return true;
                            }
                            // check if the topmost element type is serializable
                            while (TypeKind.ARRAY.equals(type.getKind()))
                                    type = ((ArrayType)type).getComponentType();

                            if (type.getKind().isPrimitive()) {
                                    result.add(true);
                                    return true;
                            }
                            Object classElement = DevelopmentModel.this.getClass(getNameForType(type));
                            result.add((classElement == null) ? false :
                                    implementsInterface(classElement, "java.io.Serializable")); //NOI18N
                            return true;
                        }
                        return false;
                    }
                });

                return (result.isEmpty() ? false : result.get(0));
	}

	/** Determines if a field with the specified fieldName in the class
	 * with the specified className is an array.
	 * @param className the fully qualified name of the class which contains 
	 * the field to be checked 
	 * @param fieldName the name of the field to be checked 
	 * @return <code>true</code> if this field name represents a java array
	 * field; <code>false</code> otherwise.
	 * @see #getFieldType
	 */
	public boolean isArray (String className, String fieldName)
	{
                final List<Boolean> result = new ArrayList<Boolean>();
		Object fieldElement = getField(className, fieldName);

                if (fieldElement != null) {
                    final ElementHandle thisElement = ((NamedElementHandleWrapper)fieldElement).getHandle();

                    forAllSourceRoots(new SourceRootTask() {
                        public boolean run(CompilationController controller) {
                            Element resolvedElement = thisElement.resolve(controller);

                            if (resolvedElement != null) {  // found the correct javac context
                                TypeMirror type = getTypeObject(resolvedElement);
                            
                                if ((type != null) && TypeKind.ARRAY.equals(type)) {
                                    result.add(true);
                                }
                            }
                            return (!result.isEmpty());
                        }
                    });
                }

                return (result.isEmpty() ? false : result.get(0));
	}

	protected NamedElementHandleWrapper getWrapperDeclaringClass (NamedElementHandleWrapper memberElement)
	{
                final List<NamedElementHandleWrapper> result = new ArrayList<NamedElementHandleWrapper>();

                if (memberElement != null)
                {
                    final ElementHandle thisElement = memberElement.getHandle();

                    if (thisElement != null)
                    {
                        forAllSourceRoots(new SourceRootTask() {
                            public boolean run(CompilationController controller) {
                                Element resolvedElement = thisElement.resolve(controller);

                                if (resolvedElement != null) // found the correct javac context
                                {
                                    Element enclosingElement = resolvedElement.getEnclosingElement();
                                    TypeElement classElement = null;

                                    if (TypeKind.DECLARED.equals(enclosingElement.getKind()))
                                    {
                                        classElement = (TypeElement)enclosingElement;
                                        ElementHandle classHandle = ElementHandle.create(classElement);

                                        result.add((classElement != null) ? 
                                            new NamedElementHandleWrapper(
                                                classHandle, classHandle.getQualifiedName()) : null);
                                    }
                                }
                                return (!result.isEmpty());
                            }
                        });
                    }
                }

                return (result.isEmpty() ? null : result.get(0));
	}

	/** Returns the string representation of declaring class of 
	 * the specified member element.  Note, the member element is 
	 * either a class element as returned by getClass, a field element 
	 * as returned by getField, a constructor element as returned by 
	 * getConstructor, or a method element as returned by getMethod 
	 * executed on the same model instance. This implementation 
	 * expects the member element to be an openide source element instance.
	 * @param memberElement the member element to be checked
	 * @return the string representation of the declaring class of 
	 * the specified memberElement
	 * @see #getClass
	 * @see #getField
	 * @see #getConstructor
	 * @see #getMethod
	 */
	public String getDeclaringClass (Object memberElement)
	{
                return (((memberElement != null) && (memberElement instanceof NamedElementHandleWrapper)) ?
                    getWrapperDeclaringClass((NamedElementHandleWrapper)memberElement).toString() :
                    null);
	}

	/** Returns the modifier mask for the specified member element.
	 * Note, the member element is either a class element as returned by 
	 * getClass, a field element as returned by getField, a constructor element 
	 * as returned by getConstructor, or a method element as returned by 
	 * getMethod executed on the same model instance. This implementation 
	 * expects the member element to be an openide source element instance.
	 * @param memberElement the member element to be checked
	 * @return the modifier mask for the specified memberElement
	 * @see java.lang.reflect.Modifier
	 * @see #getClass
	 * @see #getField
	 * @see #getConstructor
	 * @see #getMethod
	 */
	public int getModifiers (Object memberElement)
	{
                final List<Integer> result = new ArrayList<Integer>();

                if ((memberElement != null) && (memberElement instanceof NamedElementHandleWrapper))
                {
                    final ElementHandle thisElement = ((NamedElementHandleWrapper)memberElement).getHandle();

                    if (thisElement != null)
                    {
                        forAllSourceRoots(new SourceRootTask() {
                            public boolean run(CompilationController controller) {
                                Element resolvedElement = thisElement.resolve(controller);
                                if (resolvedElement != null) // found the correct javac context
                                {
                                    result.add(getModifiers(resolvedElement.getModifiers()));
                                }
                                return (!result.isEmpty());
                            }
                        });
                    }
                }

                return (result.isEmpty() ? 0 : result.get(0));
	}

        static protected int getModifiers (Set<Modifier> modifierObjects)
        {
// TODO is it impt that reflection modifiers have interface and element doesn't?
            int modifiers = 0;
            for (Modifier modObject : modifierObjects)
            {
                if (Modifier.ABSTRACT.equals(modObject))
                        modifiers |= java.lang.reflect.Modifier.ABSTRACT;
                else if (Modifier.FINAL.equals(modObject))
                        modifiers |= java.lang.reflect.Modifier.FINAL;
                else if (Modifier.NATIVE.equals(modObject))
                        modifiers |= java.lang.reflect.Modifier.NATIVE;
                else if (Modifier.PRIVATE.equals(modObject))
                        modifiers |= java.lang.reflect.Modifier.PRIVATE;
                else if (Modifier.PROTECTED.equals(modObject))
                        modifiers |= java.lang.reflect.Modifier.PROTECTED;
                else if (Modifier.PUBLIC.equals(modObject))
                        modifiers |= java.lang.reflect.Modifier.PUBLIC;
                else if (Modifier.STATIC.equals(modObject))
                        modifiers |= java.lang.reflect.Modifier.STATIC;
                else if (Modifier.STRICTFP.equals(modObject))
                        modifiers |= java.lang.reflect.Modifier.STRICT;
                else if (Modifier.SYNCHRONIZED.equals(modObject))
                        modifiers |= java.lang.reflect.Modifier.SYNCHRONIZED;
                else if (Modifier.TRANSIENT.equals(modObject))
                        modifiers |= java.lang.reflect.Modifier.TRANSIENT;
                else if (Modifier.VOLATILE.equals(modObject))
                        modifiers |= java.lang.reflect.Modifier.VOLATILE;
            }
            return modifiers;
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
	 * @see #getMethod
	 */
	protected TypeMirror getTypeObject (Object element)
	{
		TypeMirror type = null;
		
		if ((element != null) && (element instanceof Element))
		{
                    ElementKind kind = ((Element)element).getKind();
// TODO - is this really the right way to get the field type?
                    if (ElementKind.FIELD.equals(kind))
                            type = ((VariableElement)element).asType();
                    else if (ElementKind.METHOD.equals(kind))
                            type = ((ExecutableElement)element).getReturnType();
		}

		return type;
	}

        // This method must be called in a javac context
	// note that a version of this method is also in FieldFilterNode and 
	// can't be factored right now, so if this changes, change that one too
	private String getNameForType (TypeMirror type)
	{
		String typeName = null;

		if (type != null)
		{
                        TypeKind kind = type.getKind();
			if (TypeKind.ARRAY.equals(kind))
			{
				typeName = 
					getNameForType(((ArrayType)type).getComponentType()) + "[]";	// NOI18N
			}
			else if (kind.isPrimitive())
                        {
                           // TODO check usage of toString here
                                typeName = type.toString();
                        }
// TODO is all this necessary?  and, check usage of toString here
                        else if (TypeKind.DECLARED.equals(type))
                       {
                            Element element = ((DeclaredType)type).asElement();

                            if (ElementKind.CLASS.equals(element.getKind()))
                            {
                                TypeElement typeElement = (TypeElement)element;
				typeName = typeElement.getQualifiedName().toString();
                            }
			}
		}

		return typeName;
	}

	/** Converts the specified type name into an openide type instance.
	 */
	protected TypeMirrorHandle getTypeForName (final String fullTypeName, NamedElementHandleWrapper element)
	{
                final List<TypeMirrorHandle> result = new ArrayList<TypeMirrorHandle>();

                if (element != null)
                {
                    final ElementHandle thisElement = element.getHandle();
                    final boolean isArray = fullTypeName.endsWith("[]"); // NOI18N
                    final String typeName = (isArray ? 
                        fullTypeName.substring(0, fullTypeName.length() - 2) : fullTypeName);

                    forAllSourceRoots(new SourceRootTask() {
                        public boolean run(CompilationController controller) {
                            Element resolvedElement = thisElement.resolve(controller);

                            if (resolvedElement != null) {  // found the correct javac context
                                TypeKind type = primitiveTypes.get(typeName);
                                Types types = controller.getTypes();
                                TypeMirror typeMirror = null;

                                if (type != null) {
                                    if (type.isPrimitive()) {
                                        typeMirror = types.getPrimitiveType(type);
                                    } else {
                                        typeMirror = types.getNoType(type);
                                    }
                                }
                                if (typeMirror == null) {
                                    // TODO - check if this will handle arrays, 
                                    // primitive and void too and can get rid of
                                    // primitiveTypes map
                                    if (ElementKind.CLASS.equals(resolvedElement.getKind()))
                                    {
                                        TreeUtilities treeUtils = controller.getTreeUtilities();
                                        typeMirror = treeUtils.parseType(typeName, (TypeElement)resolvedElement);
                                    }
                                }
                                if (typeMirror != null) {
                                    if (isArray) {
                                        typeMirror = types.getArrayType(typeMirror);
                                    }
                                    result.add(TypeMirrorHandle.create(typeMirror));
                                }
                            }
                            return (!result.isEmpty());
                        }
                    });
                }

                return (result.isEmpty() ? null : result.get(0));
	}

        protected class NamedElementHandleWrapper {
		private ElementHandle _elementHandle;
                private String _name;

		private NamedElementHandleWrapper (ElementHandle handle, String name) {
                    _elementHandle = handle;
                    _name = name;
                }

                public ElementHandle getHandle() { return _elementHandle; }

                @Override
                public String toString () { return _name; }
        }

        protected interface SourceRootTask {
            boolean run(CompilationController c) throws Exception;
        }
}
