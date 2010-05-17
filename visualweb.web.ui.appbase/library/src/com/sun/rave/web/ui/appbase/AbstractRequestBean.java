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

package com.sun.rave.web.ui.appbase;

/**
 * <p><strong>AbstractRequestBean</strong> is the abstract base class for
 * data bean(s) that are stored in request scope attributes.  It extends
 * {@link FacesBean}, so it inherits all of the default behavior
 * found there.  In addition, the following lifecycle methods are called
 * automatically when the corresponding events occur:</p>
 * <ul>
 * <li><code>init()</code> - Called when this bean is initially added as a
 *     request scope attribute (typically as the result of
 *     evaluating a value binding or method binding expression).</li>
 * <li><code>destroy()</code> - Called when the bean is removed from the
 *     request attributes (typically as a result of the application
 *     being shut down by the servlet container).</li>
 * </ul>
 */
public abstract class AbstractRequestBean extends FacesBean {
    

    // ------------------------------------------------------------- Constructor


    /**
     * <p>Create a new request scope bean.</p>
     */
    public AbstractRequestBean() {      
    }
    

    // ------------------------------------------------------- Lifecycle Methods


    /**
     * <p>This method is called when this bean is initially added to
     * request scope.  Typically, this occurs as a result of evaluating
     * a value binding or method binding expression, which utilizes the
     * managed bean facility to instantiate this bean and store it into
     * request scope.</p>
     *
     * <p>You may customize this method to allocate resources that are required
     * for the lifetime of the current request.</p>
     */
    public void init() {

        // The default implementation does nothing

    }


    /**
     * <p>This method is called when this bean is removed from
     * request scope.  This occurs automatically when the corresponding
     * HTTP response has been completed and sent to the client.</p>
     *
     * <p>You may customize this method to clean up resources allocated
     * during the execution of the <code>init()</code> method, or
     * at any later time during the lifetime of the request.</p>
     */
    public void destroy() {

        // The default implementation does nothing

    }


}
