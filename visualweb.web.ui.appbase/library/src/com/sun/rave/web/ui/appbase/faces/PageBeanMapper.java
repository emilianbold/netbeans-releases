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


package com.sun.rave.web.ui.appbase.faces;

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
