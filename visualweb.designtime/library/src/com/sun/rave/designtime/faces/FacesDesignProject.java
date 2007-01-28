/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package com.sun.rave.designtime.faces;

import com.sun.rave.designtime.DesignProject;
import com.sun.rave.designtime.DesignContext;
import java.beans.PropertyChangeListener;

/**
 * <p>A FacesDesignProject is a top-level container for DesignContexts
 * at design-time for projects that support JSF.  The DesignProject
 * represents the project in the Creator IDE. This interface extends
 * the the DesignProject by providing APIs to find the DesignContexts
 * by name and find DesignContexts by scope.</p>
 *
 * <P><B>IMPLEMENTED BY CREATOR</B> - This interface is implemented by Creator for use by the
 * component (bean) author.</P>
 *
 * @version 1.0
 * @see DesignProject
 * @see DesignContext#getProject()
 */
public interface FacesDesignProject extends DesignProject {

    /**
     * Constant for property (for firing change event) Context Class Loader
     */
    public static final String CONTEXT_CLASS_LOADER = "context_class_loader";
    
    /**
     * Finds <code>DesignContext</code> of specified name.
     */
    public DesignContext findDesignContext(String variableName);

    /**
     * <p>Finds <code>DesignContext</code>s of specified scopes. The
     * scopes can have value of "request", "session", "application"
     * and "none".</p>
     */
    public DesignContext[] findDesignContexts(String[] scopes);
    
    /**
     * Get the project common Context Class loader so that it could use used by
     * component authors who create the design time for component.
     */
    
    public ClassLoader getContextClassLoader();
    
    /**
     * Add a property change listener to the design project so that it could fire
     * events such as classloader changed.
     */
    public void addPropertyChangeListener(PropertyChangeListener propChangeListener);
    
    /**
     * Remove property change listener.
     */
    public void removePropertyChangeListener(PropertyChangeListener propChangeListener);
    
}
