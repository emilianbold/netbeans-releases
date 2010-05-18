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
 * <p><strong>AbstractFragmentBean</strong> is the abstract base class for every
 * page bean associated with a JSP page fragment containing JavaServer Faces
 * components.  It extends {@link FacesBean}, so it inherits all of the
 * default integration behavior found there.</p>
 *
 * <p>In addition to event handler methods that you create while building
 * your application, the runtime environment will also call the following
 * <em>lifecycle</em> related methods at appropriate points during the execution
 * of your application:</p>
 * <ul>
 * <li><strong>init()</strong> - Called whenever you navigate to a page
 *     containing this page fragment, either directly (via a URL) or indirectly
 *     via page navigation from a different page.  You can override this
 *     method to acquire any resources that will always be needed by this
 *     page fragment.</li>
 * <li><strong>destroy()</strong> - Called unconditionally if
 *     <code>init()</code> was called, after completion of rendering by
 *     whichever page was actually rendered.  Override this method to release
 *     any resources allocated in the <code>init()</code> method,
 *     or in an event handler.</li>
 * </ul>
 */
public abstract class AbstractFragmentBean extends FacesBean
{


    // ------------------------------------------------------------- Constructor


    /**
     * <p>Construct a new instance of this bean.</p>
     */
    public AbstractFragmentBean() {
    }



    // ------------------------------------------------------- Lifecycle Methods


    /**
     * <p>Callback method that is called whenever a page containing
     * this page fragment is navigated to, either directly via a URL,
     * or indirectly via page navigation.  Override this method to acquire
     * resources that will be needed for event handlers and lifecycle methods.
     * </p>
     *
     * <p>The default implementation does nothing.</p>
     */
    public void init() {
        ;
    }


    /**
     * <p>Callback method that is called after rendering is completed for
     * this request, if <code>init()</code> was called.  Override this
     * method to release resources acquired in the <code>init()</code>
     * method (or acquired during execution of an event handler).</p>
     *
     * <p>The default implementation does nothing.</p>
     */
    public void destroy() {
        ;
    }


}
