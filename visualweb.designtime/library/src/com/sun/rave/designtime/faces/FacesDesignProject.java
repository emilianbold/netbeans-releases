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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
