/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package com.sun.rave.designtime;

import com.sun.rave.designtime.event.DesignIdeListener;
import java.util.Map;

/**
 * <p>A DesignIde is a top-level container for DesignProjects at design-time.
 * The DesignIde represents the Creator IDE itself.  Not much can be done with
 * DesignIde in the Creator Design-Time API, except for accessing DesignProjects,
 * listening to project-level events, and storing user-level data.</p>
 *
 * <P><B>IMPLEMENTED BY CREATOR</B> - This interface is implemented by Creator
 * for use by the component (bean) author.</P>
 *
 * @since Mako
 */
public interface DesignIde {
    

    // --------------------------------------------------- DesignContext Methods

    /**
     * <p>Returns all the DesignProjects in this IDE.  There will be one
     * DesignProject for each open project in the IDE.</p>
     *
     * @return An array of DesignProject objects
     */
    public DesignProject[] getDesignProjects();


    /**
     * <p>Creates a new DesignProject for this IDE.</p>
     *
     * <p>FIXME - decide on paramete3rs to createDesignContext() method</p>
     *
     * @param projectData A Map of project data to apply to the newly
     *  created project
     * @return The newly created DesignProject, or null if the operation
     *   was unsuccessful
     */
    public DesignProject createDesignProject(Map projectData);


    /**
     * <p>Removes an existing DesignProject from this IDE.</p>
     *
     * @param project The desired DesignProject to remove from the IDE
     * @return <code>true</code> if the operation was successful,
     *  <code>false</code> if not
     */
    public boolean removeDesignProject(DesignProject project);


    // -------------------------------------------------------- Ide Data Methods


    /**
     * <p>Sets a global name-value pair of data.  This name-value pair will be
     * stored in the associated user settings file (as text), so this data is
     * retrievable in a future IDE session.</p>
     *
     * <p>NOTE: The 'data' Object can be a simple String or a complex
     * (non-String) Object.  Either way, it will be stored as text in IDE
     * state and will be associated with this IDE.  When the IDE state is
     * written to disk, any complex (non-String) objects will be converted to
     * String using the 'toString()' method.  If a component author wishes to
     * store a complex (non-String) object, they must be sure to override the
     * 'toString()' method on their object to serialize out enough information
     * to be able to restore the object when a subsequent call to 'getGlobalData'
     * returns a String.  Though a complex object was stored via the
     * 'setIdeData' method, a component author *may* get back a String from
     * 'getIdeData' if the IDE has been closed and reopened since the
     * previous call to 'setIdeData'.  It is the responsibility of the
     * component author to reconstruct the complex object from the String,
     * and if desired, put it back into the context using the 'setIdeData'
     * method passing the newly constructed object in.  This way, all subsequent
     * calls to 'getIdeData' with that key will return the complex object
     * instance - until the IDE is closed and restored.</p>
     *
     * @param key The String key to store the data object under
     * @param data The data object to store - this may be a String or any
     *  complex object, but it will be stored as a string using the
     * 'toString()' method when the user settings are written to disk.
     * @see DesignIde#getIdeData(String)
     */
    public void setIdeData(String key, Object data);


    /**
     * <p>Retrieves the value for a global name-value pair of data.  This
     * name-value pair will be stored in the associated user settings file
     * (as text), so this data is retrievable in any IDE session once it
     * has been set.</p>
     *
     * <p>NOTE: The 'data' Object can be a simple String or a complex
     * (non-String) Object.  Either way, it will be stored as text in IDE
     * state and will be associated with this IDE.  When the IDE state is
     * written to disk, any complex (non-String) objects will be converted
     * to String using the 'toString()' method.  If a component author wishes to
     * store a complex (non-String) object, they must be sure to override the
     * 'toString()' method on their object to serialize out enough information
     * to be able to restore the object when a subsequent call to 'getIdeData'
     * returns a String.  Though a complex object was stored via the
     * 'setIdeData' method, a component author *may* get back a String from
     * 'getIdeData' if the IDE has been closed and reopened since the previous
     * call to 'setIdeData'.  It is the responsibility of the component author
     * to reconstruct the complex object from the String, and if desired, put
     * it back into the context using the 'setIdeData' method passing the newly
     * constructed object in.  This way, all subsequent calls to 'getIdeData'
     * with that key will return the complex object instance - until the
     * IDE is closed and restored.</p>
     *
     * @param key The desired String key to retrieve the data object for
     * @return The data object that is currently stored under this key -
     *  this may be a String or an Object, based on what was stored using
     *  'setIdeData'.  NOTE: This will always be a String after the user
     *  settings are read from disk, even if the stored object was not a
     *  String - it will have been converted using the 'toString()' method.
     * @see DesignIde#setIdeData(String, Object)
     */
    public Object getIdeData(String key);


    // ---------------------------------------------------- IDE Listener Methods

    /**
     * Adds a listener to this DesignIde
     *
     * @param listener The desired listener to add
     */
    public void addDesignIdeListener(DesignIdeListener listener);

    /**
     * Removes a listener from this DesignIde
     *
     * @param listener The desired listener to remove
     */
    public void removeDesignIdeListener(DesignIdeListener listener);

    /**
     * Returns the array of current listeners to this DesignIde
     *
     * @return An array of listeners currently listening to this DesignIde
     */
    public DesignIdeListener[] getDesignIdeListeners();


}
