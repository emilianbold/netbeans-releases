/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */

package com.sun.jsfcl.app;

/**
 * <p>Interface describing a service used to map from a JavaServer Faces
 * <em>view identifier</em> (typically the context relative path to a JSP
 * page) to the managed bean name of the corresponding page bean.</p>
 */
public interface PageBeanMapper {
    

    /**
     * <p>Return the managed bean name for the page bean (must extend
     * {@link AbstractPageBean}) that corresponds to the specified
     * view identifier (typically the context-relative path to a
     * JSP page).</p>
     *
     * @param viewId View identifier to be mapped
     */
    public String mapViewId(String viewId);


}
