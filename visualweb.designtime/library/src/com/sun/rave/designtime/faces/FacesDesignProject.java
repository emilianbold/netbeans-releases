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
