/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */

package com.sun.jsfcl.app;

import java.io.Serializable;

/**
 * <p><strong>AbstractSessionBean</strong> is the abstract base class for
 * data bean(s) that are stored in session scope attributes.  It extends
 * {@link FacesBean}, so it inherits all of the default behavior
 * found there.</p>
 */
public abstract class AbstractSessionBean
  extends FacesBean implements Serializable {
    

    // ------------------------------------------------------------- Constructor


    /**
     * <p>Create a new session scope bean.</p>
     */
    public AbstractSessionBean() {      
    }
    

}
