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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import com.sun.rave.designtime.event.DesignProjectListener;

/**
 * <p>A DesignProject is a top-level container for DesignContexts at design-time.  The DesignProject
 * represents the project in the Creator IDE.  Not much can be done with Projects in the Creator
 * Design-Time API, except for accessing other DesignContexts, listening to project-level events,
 * and storing project-level data.</p>
 *
 * <P><B>IMPLEMENTED BY CREATOR</B> - This interface is implemented by Creator for use by the
 * component (bean) author.</P>
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see DesignContext#getProject()
 */
public interface DesignProject extends DisplayItem {

    //------------------------------------------------------------------------ DesignContext Methods

    /**
     * Returns all the DesignContexts in this project.  There will be one DesignContext for each
     * designable file in the project.  Note that for JSF, this means one DesignContext for each
     * combination of "PageX.java" and "PageX.jsp" file, as well as one for each of the non-page
     * beans, like "SessionBeanX.java", "ApplicationBeanX.java", etc.
     *
     * @return An array of DesignContext objects - one for each designable file in the project
     */
    public DesignContext[] getDesignContexts();

    /**
     * Creates a new DesignContext (backing file) in this project.
     *
     * @param className The desired fully-qualified class name for the file
     * @param baseClass The desired base class for the file
     * @param contextData A Map of context data to apply to the newly created context file
     * @return The newly created DesignContext, or null if the operation was unsuccessful
     */
    public DesignContext createDesignContext(String className, Class baseClass, Map contextData);

    /**
     * Removes an existing DesignContext (backing file) from this project.
     *
     * @param context The desired DesignContext to remove from the project
     * @return <code>true</code> if the operation was successful, <code>false</code> if not
     */
    public boolean removeDesignContext(DesignContext context);

    //----------------------------------------------------------------------------- Resource Methods

    /**
     * <p>Returns the set of project root relative resources in this project as an array of local
     * resource identifiers.  The returned URIs will always be paths from the project root,
     * including folder hiearchy within the project.  The specified <code>rootPath</code> is used
     * as a filter, to allow drilling-in to directories as desired.  Use
     * <code>URI.relativize()</code> to make relative URIs when needed.  Use
     * <code>getResourceFile(URI)</code> to retrieve a File object for a particular resource in
     * the project.</p>
     *
     * @param rootPath The root path to fetch resources underneath.  Passing <code>null</code> will
     *        start at the root of the project.
     * @param recurseFolders <code>true</code> to include the sub-resources inside of any folders
     * @return A project root relative array of URIs representing all the resource files under the
     *         specified root path
     */
    public URI[] getResources(URI rootPath, boolean recurseFolders);

    /**
     * Returns a File object containing the specified resource.
     *
     * @param resourceUri The desired project relative resource uri to fetch a file object
     * @return A File object containing the project resource
     */
    public File getResourceFile(URI resourceUri);

    /**
     * Copies a resource into this project, and converts the external URL into a local URI
     * (resource identifier string).
     *
     * @param sourceUrl A URL pointing to the desired external resource
     * @param targetUri The desired resource URI (path) within the project directory
     * @return The resulting project relative resource uri (resourceUri)
     * @throws IOException if the resource cannot be copied
     */
    public URI addResource(URL sourceUrl, URI targetUri) throws IOException;

    /**
     * Removes a resource from the project directory.
     *
     * @param resourceUri The desired resource to remove from the project
     * @return boolean <code>true</code> if the resource was successfully removed,
     *         <code>false</code> if not
     */
    public boolean removeResource(URI resourceUri);

    //------------------------------------------------------------------------- Project Data Methods

    /**
     * <p>Sets a name-value pair of data on this DesignProject.  This name-value pair will be stored
     * in the associated project file, so this data is retrievable in a future IDE session.</p>
     *
     * <p>NOTE: The 'data' Object can be a simple String or a complex (non-String) Object.  Either
     * way, it will be stored as text in the project file and will be associated with this project.
     * When the project file is written to disk, any complex (non-String) objects
     * will be converted to String using the 'toString()' method.  If a component author wishes to
     * store a complex (non-String) object, they must be sure to override the 'toString()' method
     * on their object to serialize out enough information to be able to restore the object when a
     * subsequent call to 'getProjectData' returns a String.  Though a complex object was stored
     * via the 'setProjectData' method, a component author *may* get back a String from
     * 'getProjectData' if the project has been saved and reopened since the previous call to
     * 'setProjectData'.  It is the responsibility of the component author to reconstruct the
     * complex object from the String, and if desired, put it back into the context using the
     * 'setProjectData' method passing the newly constructed object in.  This way, all subsequent
     * calls to 'getProjectData' with that key will return the complex object instance - until the
     * project is closed and restored.</p>
     *
     * @param key The String key to store the data object under
     * @param data The data object to store - this may be a String or any complex object, but it
     *        will be stored as a string using the 'toString()' method when the project file is
     *        written to disk.
     * @see #getProjectData(String)
     */
    public void setProjectData(String key, Object data);

    /**
     * <p>Retrieves the value for a name-value pair of data on this DesignProject.  This name-value
     * pair is stored in the project file, so this data is retrievable in any IDE session once it
     * has been set.  Some custom pre-defined keys can be found in {@link Constants.ProjectData}.
     * </p>
     *
     * <p>NOTE: The 'data' Object can be a simple String or a complex (non-String) Object.  Either
     * way, it will be stored as text in the project file and will be associated with this project.
     * When the project file is written to disk, any complex (non-String) objects
     * will be converted to String using the 'toString()' method.  If a component author wishes to
     * store a complex (non-String) object, they must be sure to override the 'toString()' method
     * on their object to serialize out enough information to be able to restore the object when a
     * subsequent call to 'getProjectData' returns a String.  Though a complex object was stored
     * via the 'setProjectData' method, a component author *may* get back a String from
     * 'getProjectData' if the project has been saved and reopened since the previous call to
     * 'setProjectData'.  It is the responsibility of the component author to reconstruct the
     * complex object from the String, and if desired, put it back into the context using the
     * 'setProjectData' method passing the newly constructed object in.  This way, all subsequent
     * calls to 'getProjectData' with that key will return the complex object instance - until the
     * project is closed and restored.</p>
     *
     * @param key The desired String key to retrieve the data object for
     * @return The data object that is currently stored under this key - this may be a String or
     *         an Object, based on what was stored using 'setProjectData'.  NOTE: This will always
     *         be a String after the project file is read from disk, even if the stored object was
     *         not a String - it will have been converted using the 'toString()' method.
     * @see #setProjectData(String, Object)
     * @see Constants.ProjectData
     */
    public Object getProjectData(String key);

    //-------------------------------------------------------------------------- Global Data Methods

    /**
     * <p>Sets a global name-value pair of data.  This name-value pair will be stored in the
     * associated user settings file (as text), so this data is retrievable in a future IDE
     * session.</p>
     *
     * <p>NOTE: The 'data' Object can be a simple String or a complex (non-String) Object.  Either
     * way, it will be stored as text in IDE state and will be associated with this IDE.
     * When the IDE state is written to disk, any complex (non-String) objects
     * will be converted to String using the 'toString()' method.  If a component author wishes to
     * store a complex (non-String) object, they must be sure to override the 'toString()' method
     * on their object to serialize out enough information to be able to restore the object when a
     * subsequent call to 'getGlobalData' returns a String.  Though a complex object was stored
     * via the 'setGlobalData' method, a component author *may* get back a String from
     * 'getGlobalData' if the IDE has been closed and reopened since the previous call to
     * 'setGlobalData'.  It is the responsibility of the component author to reconstruct the
     * complex object from the String, and if desired, put it back into the context using the
     * 'setGlobalData' method passing the newly constructed object in.  This way, all subsequent
     * calls to 'getGlobalData' with that key will return the complex object instance - until the
     * IDE is closed and restored.</p>
     *
     * @param key The String key to store the data object under
     * @param data The data object to store - this may be a String or any complex object, but it
     *        will be stored as a string using the 'toString()' method when the project file is
     *        written to disk.
     * @see DesignProject#getGlobalData(String)
     *
     * @deprecated (since Mako) Use {@link DesignIde#getIdeData(String)} instead
     */
    public void setGlobalData(String key, Object data);

    /**
     * <p>Retrieves the value for a global name-value pair of data.  This name-value pair will be
     * stored in the associated user settings file (as text), so this data is retrievable in any
     * IDE session once it has been set.</p>
     *
     * <p>NOTE: The 'data' Object can be a simple String or a complex (non-String) Object.  Either
     * way, it will be stored as text in IDE state and will be associated with this IDE.
     * When the IDE state is written to disk, any complex (non-String) objects
     * will be converted to String using the 'toString()' method.  If a component author wishes to
     * store a complex (non-String) object, they must be sure to override the 'toString()' method
     * on their object to serialize out enough information to be able to restore the object when a
     * subsequent call to 'getGlobalData' returns a String.  Though a complex object was stored
     * via the 'setGlobalData' method, a component author *may* get back a String from
     * 'getGlobalData' if the IDE has been closed and reopened since the previous call to
     * 'setGlobalData'.  It is the responsibility of the component author to reconstruct the
     * complex object from the String, and if desired, put it back into the context using the
     * 'setGlobalData' method passing the newly constructed object in.  This way, all subsequent
     * calls to 'getGlobalData' with that key will return the complex object instance - until the
     * IDE is closed and restored.</p>
     *
     * @param key The desired String key to retrieve the data object for
     * @return The data object that is currently stored under this key - this may be a String or
     *         an Object, based on what was stored using 'setGlobalData'.  NOTE: This will always
     *         be a String after the project file is read from disk, even if the stored object was
     *         not a String - it will have been converted using the 'toString()' method.
     * @see DesignProject#setGlobalData(String, Object)
     *
     * @deprecated (since Mako) Use {@link DesignIde#setIdeData(String, Object)} instead
     */
    public Object getGlobalData(String key);


    //---------------------------------------------------------------------------- IDE Access Method


    /**
     * Returns the IDE element, which is the top-level container for all projects.
     *
     * @return The DesignIde associated with this DesignProject
     *
     * @since Mako
     */
//    FIXME - cannot define getIde() until it is implemented in concrete classes
//    public DesignIde getIde();


    //--------------------------------------------------------------------- Project Listener Methods

    /**
     * Adds a listener to this DesignProject
     *
     * @param listener The desired listener to add
     */
    public void addDesignProjectListener(DesignProjectListener listener);

    /**
     * Removes a listener from this DesignProject
     *
     * @param listener The desired listener to remove
     */
    public void removeDesignProjectListener(DesignProjectListener listener);

    /**
     * Returns the array of current listeners to this DesignProject
     *
     * @return An array of listeners currently listening to this DesignProject
     */
    public DesignProjectListener[] getDesignProjectListeners();
}
