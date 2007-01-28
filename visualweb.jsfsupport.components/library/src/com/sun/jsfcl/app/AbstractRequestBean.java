/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */

package com.sun.jsfcl.app;

/**
 * <p><strong>AbstractRequestBean</strong> is the abstract base class for
 * data bean(s) that are stored in request scope attributes.  It extends
 * {@link FacesBean}, so it inherits all of the default behavior
 * found there.</p>
 */
public abstract class AbstractRequestBean extends FacesBean {
    

    // ------------------------------------------------------------- Constructor


    /**
     * <p>Create a new request scope bean.</p>
     */
    public AbstractRequestBean() {      
    }
    

}
