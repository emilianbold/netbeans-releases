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

package com.sun.rave.designtime;

import java.io.IOException;
import java.net.URL;
import java.awt.datatransfer.Transferable;
import com.sun.rave.designtime.event.DesignContextListener;

/**
 * <p>A DesignContext is a 'host' for DesignBean instances at design-time.  The DesignContext
 * represents the 'source file' or 'persistence model' for a design-time session.  A DesignContext
 * is the container (instance host) for a set of DesignBeans.  For example, in a JSF application,
 * the DesignContext represents the logical backing file which is the combination of the 'Page1.jsp'
 * and the 'Page1.java' files.  In a Swing application, the DesignContext represents the
 * 'JFrame1.java' file.</p>
 *
 * <P><B>IMPLEMENTED BY CREATOR</B> - This interface is implemented by Creator for use by the
 * component (bean) author.</P>
 *
 * @author Joe Nuxoll
 * @version 1.0
 */
public interface DesignContext extends DisplayItem {

    //-------------------------------------------------------------------- DesignBean Access Methods

    /**
     * Returns the root container DesignBean for this DesignContext.  This is typically the "this"
     * component being designed.  For example, this would be the view root in a JSF application.
     * The children of the root container are the items you see on the page.  To get all of the
     * DesignBeans within the scope of this context (ignoring the containership hierarchy), use
     * the getBeans() method.
     *
     * @return The root container DesignBean for this DesignContext
     * @see getBeans()
     */
    public DesignBean getRootContainer();

    /**
     * Returns a DesignBean (design-time proxy) to represent the specified JavaBean instance.  This
     * must be an instance that lives within the scope of this DesignContext, or the method will
     * return null.
     *
     * @param beanInstance A live instance of a JavaBean
     * @return A DesignBean (design-time proxy) representing the specified bean instance, or null if
     *         the specified Object does not represent a JavaBean within the scope of this
     *         DesignContext
     */
    public DesignBean getBeanForInstance(Object beanInstance);

    /**
     * Returns a DesignBean (design-time proxy) to represent the JavaBean with the specified
     * instance name.  This must be an instance that lives within the scope of this DesignContext,
     * or the method will return null.
     *
     * @param instanceName The String instance name of the desired JavaBean
     * @return A DesignBean (design-time proxy) representing the specified bean, or null if the
     *         specified instance name does not represent a JavaBean within the scope of this
     *         DesignContext
     */
    public DesignBean getBeanByName(String instanceName);

    /**
     * Returns a DesignBean array (design-time proxies) representing the JavaBeans within the scope
     * of this DesignContext that are assignable from the specified class type.  This uses
     * Class.isAssignableFrom(...) to determine if a JavaBean satisfies the specified criteria, so
     * subtypes of the specified type will be included.
     *
     * @param beanClass The desired class type
     * @return An array of DesignBean representing the JavaBeans within the scope of this
     *         DesignContext that are assignable from the specified class type
     * @see Class#isAssignableFrom(Class)
     */
    public DesignBean[] getBeansOfType(Class beanClass);

    /**
     * Returns an array of all the DesignBeans within the scope of this DesignContext.  This is a
     * flat list of instances, ignoring the containership hierarchy.  To navigate the containership
     * hierarchy, use the getRootContainer() method.
     *
     * @return An array of DesignBean representing the JavaBeans within the scope of this
     *         DesignContext
     * @see getRootContainer()
     */
    public DesignBean[] getBeans();

    //-------------------------------------------------------------- DesignBean Manipulation Methods

    /**
     * Returns <code>true</code> if the specified type (classname) of JavaBean can be created as a
     * child of the specified parent DesignBean at the specified position.  This is a test call that
     * should be performed before calling the createBean method.
     *
     * @param classname The fully qualified class name of the JavaBean to be created
     * @param parent The DesignBean parent for the JavaBean to be created
     * @param position The desired position for the JavaBean to be created
     * @return <code>true</code> if a matching call to 'createBean' would succeed, or
     *         <code>false</code> if not
     * @see DesignContext#createBean(String, DesignBean, Position)
     */
    public boolean canCreateBean(String classname, DesignBean parent, Position position);

    /**
     * Creates an instance of a JavaBean of the specified type (classname), as a child of the
     * specified parent DesignBean at the specified position.  If successful, a DesignBean
     * representing the newly created bean is returned.  Before this method is called, a test call
     * should be performed to the canCreateBean method.
     *
     * @param classname The fully qualified class name of the JavaBean to be created
     * @param parent The DesignBean parent for the JavaBean to be created
     * @param position The desired position for the JavaBean to be created
     * @return A DesignBean representing the JavaBean that was created, or null if the operation
     *         failed
     * @see DesignContext#canCreateBean(String, DesignBean, Position)
     */
    public DesignBean createBean(String classname, DesignBean parent, Position position);

    /**
     * Returns <code>true</code> if the specified DesignBean can be can be moved to be a child of
     * the specified parent DesignBean at the specified position.  This is a test call that should
     * be performed before calling the moveBean method.
     *
     * @param designBean The DesignBean to be moved
     * @param newParent The new DesignBean parent for the DesignBean
     * @param position The desired position for the DesignBean to be moved
     * @return <code>true</code> if a matching call to 'moveBean' would succeed, or
     *         <code>false</code> if not
     * @see moveBean(DesignBean, DesignBean, Position)
     */
    public boolean canMoveBean(DesignBean designBean, DesignBean newParent, Position position);

    /**
     * Moves a DesignBean, to become a child of the specified parent DesignBean at the specified
     * position.  Returns <code>true</code> if successful, <code>false</code> if not.  Before this
     * method is called, a test call should be performed to the canMoveBean method.
     *
     * @param designBean The DesignBean to move
     * @param newParent The new DesignBean parent for the DesignBean
     * @param position The desired position for the DesignBean to be moved
     * @return <code>true</code> if move was succeessful, or <code>false</code> if not
     * @see canMoveBean(DesignBean, DesignBean, Position)
     */
    public boolean moveBean(DesignBean designBean, DesignBean newParent, Position position);

    /**
     * Copies a set of DesignBean instances into a clipboard-like format.  This returns a
     * Transferable object that stores all the necessary data for the pasteBeans method.
     *
     * @param designBeans An array of desired DesignBean instances
     * @return the resulting Transferable object representing the copied beans
     * @see pasteBeans(java.awt.datatransfer.Transferable, DesignBean, Position)
     */
    public Transferable copyBeans(DesignBean[] designBeans);

    /**
     * Pastes a set of DesignBean instances (acquired via copyBeans) into the specified parent
     * DesignBean at the specified position.  This returns an array of DesignBean(s), representing
     * the newly pasted children.
     *
     * @param persistData The Transferable object acquired via 'copyBeans' that contains the data
     *        representing the DesignBean(s) to be pasted
     * @param newParent The desired new parent DesignBean to paste the DesignBean(s) into
     * @param position The desired new position for the pasted DesignBean(s)
     * @return The newly created DesignBean instances
     * @see copyBeans(DesignBean[])
     */
    public DesignBean[] pasteBeans(Transferable persistData, DesignBean newParent, Position position);

    /**
     * Deletes a DesignBean object (and removes all persistence).  Returns <code>true</code> if the
     * delete was successful, <code>false</code> if not.
     *
     * @param designBean The desired DesignBean to delete
     * @return <code>true</code> if the delete operation was successful, <code>false</code> if not
     */
    public boolean deleteBean(DesignBean designBean);

    //------------------------------------------------------------------------- Context Data Methods

    /**
     * <p>Sets a name-value pair of data on this DesignContext.  This name-value pair will be stored
     * in the associated project file (as text) that contains this DesignContext, so this data is
     * retrievable in a future IDE session.</p>
     *
     * <p>NOTE: The 'data' Object can be a simple String or a complex (non-String) Object.  Either
     * way, it will be stored as text in the project file and will be associated with this
     * DesignContext.  When the project file is written to disk, any complex (non-String) objects
     * will be converted to String using the 'toString()' method.  If a component author wishes to
     * store a complex (non-String) object, they must be sure to override the 'toString()' method
     * on their object to serialize out enough information to be able to restore the object when a
     * subsequent call to 'getContextData' returns a String.  Though a complex object was stored
     * via the 'setContextData' method, a component author *may* get back a String from
     * 'getContextData' if the project has been saved and reopened since the previous call to
     * 'setContextData'.  It is the responsibility of the component author to reconstruct the
     * complex object from the String, and if desired, put it back into the context using the
     * 'setContextData' method passing the newly constructed object in.  This way, all subsequent
     * calls to 'getContextData' with that key will return the complex object instance - until the
     * project is closed and restored.</p>
     *
     * @param key The String key to store the data object under
     * @param data The data object to store - this may be a String or any complex object, but it
     *        will be stored as a string using the 'toString()' method when the project file is
     *        written to disk.
     * @see getContextData(String)
     */
    public void setContextData(String key, Object data);

    /**
     * <p>Retrieves the value for a name-value pair of data on this DesignContext.  This name-value
     * pair is stored in the associated project file (as text) that contains this DesignContext, so
     * this data is retrievable in any IDE session once it has been set.</p>
     *
     * <p>NOTE: The 'data' Object can be a simple String or a complex (non-String) Object.  Either
     * way, it will be stored as text in the project file and will be associated with this
     * DesignContext.  When the project file is written to disk, any complex (non-String) objects
     * will be converted to String using the 'toString()' method.  If a component author wishes to
     * store a complex (non-String) object, they must be sure to override the 'toString()' method
     * on their object to serialize out enough information to be able to restore the object when a
     * subsequent call to 'getContextData' returns a String.  Though a complex object was stored
     * via the 'setContextData' method, a component author *may* get back a String from
     * 'getContextData' if the project has been saved and reopened since the previous call to
     * 'setContextData'.  It is the responsibility of the component author to reconstruct the
     * complex object from the String, and if desired, put it back into the context using the
     * 'setContextData' method passing the newly constructed object in.  This way, all subsequent
     * calls to 'getContextData' with that key will return the complex object instance - until the
     * project is closed and restored.</p>
     *
     * @param key The desired String key to retrieve the data object for
     * @return The data object that is currently stored under this key - this may be a String or
     *         an Object, based on what was stored using 'setContextData'.  NOTE: This will always
     *         be a String after the project file is read from disk, even if the stored object was
     *         not a String - it will have been converted using the 'toString()' method.
     * @see #setContextData(String, Object)
     * @see Constants.ContextData
     */
    public Object getContextData(String key);

    //----------------------------------------------------------------------------- Resource Methods

    /**
     * Adds a resource reference to this DesignContext, and converts the external URL into a local
     * resource identifyer String.  This may also copy (if specified) an external resource into the
     * project.
     *
     * @param resource A URL pointing to the desired external resource
     * @param copy <code>true</code> if the resource should be copied into the project,
     *        <code>false</code> if not
     * @throws IOException if the resource cannot be copied
     * @return The resulting relative resource identifyer String.  This will be a local relative
     *         resource if the external resource was copied into the project.
     */
    public String addResource(URL resource, boolean copy) throws IOException;

    /**
     * Resolves a local resource identifyer String into a fully-qualified URL.
     *
     * @param localResource A local resource identifier string
     * @return A fully-qualified URL
     */
    public URL resolveResource(String localResource);

    //----------------------------------------------------------------------- Context Method Methods

    /**
     * Returns a set of {@link ContextMethod} objects describing the methods declared on this
     * DesignContext (source file).
     *
     * @return An array of {@link ContextMethod} objects, describing the methods declared on this
     * DesignContext (source file)
     */
    public ContextMethod[] getContextMethods();

    /**
     * Returns a {@link ContextMethod} object describing the method with the specified name and
     * parameter types.  Returns <code>null</code> if no method exists on this DesignContext with
     * the specified name and parameter types.
     *
     * @param methodName The method name of the desired context method
     * @param parameterTypes The parameter types of the desired context method
     * @return A ContextMethod object describing the requested method, or <code>null</code> if no
     *         method exists with the specified name and parameter types
     */
    public ContextMethod getContextMethod(String methodName, Class[] parameterTypes);

    /**
     * <p>Creates a new method in the source code for this DesignContext.  The passed ContextMethod
     * <strong>must</strong> specify at least the designContext and name, and <strong>must
     * not</strong> describe a method that already exists in the DesignContext source.  To update
     * an existing method, use the <code>updateContextMethod()</code> method.  These methods are
     * separated to help prevent accidental method overwriting.  The following table
     * details how the specified ContextMethod is used for this method:</p>
     *
     * <p><table border="1">
     * <tr><th>designContext <td><strong>REQUIRED.</strong> Must match the DesignContext that is
     *         being called.  This is essentially a safety precaution to help prevent accidental
     *         method overwriting.
     * <tr><th>name <td><strong>REQUIRED.</strong> Defines the method name.
     * <tr><th>modifiers <td>Defines the method modifiers.  Use {@link java.lang.reflect.Modifier}
     *         to define the modifier bits.  If <code>0</code> is specified (no modifier bits), then
     *         a public method is created.
     * <tr><th>returnType <td>Defines the return type.  If <code>null</code> is specified, the
     *         created method will have a <code>void</code> return type.
     * <tr><th>parameterTypes <td>Defines the parameter types.  If <code>null</code> or an empty
     *         array is specified, the created method will have no arguments.
     * <tr><th>parameterNames <td>Defines the parameter names.  If <code>null</code> or an empty
     *         array is specified (or an array shorter than the parameterTypes array), default
     *         argument names will be used.
     * <tr><th>exceptionTypes <td>Defines the throws clause exception types.  If <code>null</code>
     *         is specified, the created method will have no throws clause.
     * <tr><th>methodBodyText <td>Defines the method body Java source code.  If <code>null</code> is
     *         specified, the method will have an empty body.  If the value is non-null, this must
     *         represent valid (compilable) Java source code.
     * <tr><th>commentText <td>Defines the comment text above the newly created method.  If
     *         <code>null</code> is specified, no comment text will be included.
     * </table></p>
     *
     * @param method A ContextMethod object representing the desired method.
     * @return <code>true</code> if the method was created successfully, or <code>false</code> if
     *         it was not.
     * @throws IllegalArgumentException If there was a syntax error in any of the ContextMethod
     *         settings, or if the ContextMethod represents a method that already exists on this
     *         DesignContext (<code>updateContextMethod()</code> must be used in this case to avoid
     *         accidental method overwriting)
     */
    public boolean createContextMethod(ContextMethod method) throws IllegalArgumentException;

    /**
     * <p>Updates an existing method in the source code for this DesignContext.  The passed
     * ContextMethod will be used to locate the desired method to update using the designContext,
     * name, and parameterTypes.  This method may only be used to update the modifiers, returnType,
     * parameterNames, exceptionTypes, methodBodyText, or commentText.  Any other changes
     * actually constitute the creation of a new method, as they alter the method signature.  To
     * create a new method, the <code>createContextMethod()</code> method should be used.  These
     * operations are separated to help prevent accidental method overwriting.  The following table
     * details how the specified ContextMethod is used for this method:</p>
     *
     * <p><table border="1">
     * <tr><th>designContext <td><strong>REQUIRED.</strong> Must match the DesignContext that is
     *         being called.  This is essentially a safety precaution to help prevent accidental
     *         method overwriting.
     * <tr><th>name <td><strong>REQUIRED.</strong> Specifies the desired method name.
     * <tr><th>modifiers <td>Defines the method modifiers.  Use {@link java.lang.reflect.Modifier}
     *         to define the modifier bits.
     * <tr><th>returnType <td>Defines the method's return type.  If <code>null</code> is specified,
     *         the method is assumed to have a <code>void</code> return type.
     * <tr><th>parameterTypes <td><strong>REQUIRED.</strong> Specifies the desired method's
     *         parameter types (if it has any).  If <code>null</code> or an empty array is
     *         specified, the desired method is assumed to have zero arguments.
     * <tr><th>parameterNames <td>Defines the parameter names.  If <code>null</code> or an empty
     *         array is specified (or an array shorter than the parameterTypes array), default
     *         argument names will be used.
     * <tr><th>exceptionTypes <td>Defines the throws clause exception types.  If <code>null</code>
     *         is specified, the resulting method will have no throws clause.
     * <tr><th>methodBodyText <td>Defines the method body Java source code.  If <code>null</code> is
     *         specified, the resulting method body will be empty.  If the value is non-null, this
     *         must represent valid (compilable) Java source code.  Note that a method with a
     *         non-void return type <strong>must</strong> return a value.
     * <tr><th>commentText <td>Defines the comment text above the newly created method.  If
     *         <code>null</code> is specified, no comment text will be included.
     * </table></p>
     *
     * @param method The desired ContextMethod representing the method to be updated
     * @return The resulting ContextMethod object (including any updates from the process)
     * @throws IllegalArgumentException If there was a syntax error in any of the ContextMethod
     *         settings, or if the ContextMethod does not exist in this DesignContext.
     */
    public ContextMethod updateContextMethod(ContextMethod method) throws IllegalArgumentException;

    /**
     * <p>Removes an existing method from the source code for this DesignContext.  The passed
     * ContextMethod will be used to locate the desired method to remove using the designContext,
     * name, and parameterTypes.  No other portions of the ContextMethod are used.  The
     * following table details how the specified ContextMethod is used for this method:</p>
     *
     * <p><table border="1">
     * <tr><th>designContext <td><strong>REQUIRED.</strong> Must match the DesignContext that is
     *         being called.  This is essentially a safety precaution to help prevent accidental
     *         method removal.
     * <tr><th>name <td><strong>REQUIRED.</strong> Specifies the desired method name.
     * <tr><tr>modifiers <id>Ignored.
     * <tr><th>returnType <td>Ignored.
     * <tr><th>parameterTypes <td><strong>REQUIRED.</strong> Specifies the desired method's
     *         parameter types (if it has any).  If <code>null</code> or an empty array is
     *         specified, the desired method is assumed to have zero arguments.
     * <tr><th>parameterNames <td>Ignored.
     * <tr><th>exceptionTypes <td>Ignored.
     * <tr><th>methodBodyText <td>Ignored.
     * <tr><th>commentText <td>Ignored.
     * </table></p>
     *
     * @param method A ContextMethod object defining the method to be removed
     * @return <code>true</code> if the method was successfully removed
     * @exception IllegalArgumentException if the specified ContextMethod does not exist on this
     *            DesignContext
     */
    public boolean removeContextMethod(ContextMethod method);

    //------------------------------------------------------------------------ Project Access Method

    /**
     * Returns the project, which is the top-level container for all contexts.
     *
     * @return The DesignProject associated with this DesignContext
     */
    public DesignProject getProject();

    //------------------------------------------------------------------- Event Registration Methods

    /**
     * Adds a listener to this DesignContext
     *
     * @param listener The desired listener to add
     */
    public void addDesignContextListener(DesignContextListener listener);

    /**
     * Removes a listener from this DesignContext
     *
     * @param listener The desired listener to remove
     */
    public void removeDesignContextListener(DesignContextListener listener);

    /**
     * Returns the array of current listeners to this DesignContext
     *
     * @return An array of listeners currently listening to this DesignContext
     */
    public DesignContextListener[] getDesignContextListeners();
}
