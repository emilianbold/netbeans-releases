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

import java.util.List;

/**
 * <p>Application exception class that wraps the one or more runtime
 * exceptions that were intercepted and cached during the execution of
 * a particular request's lifecycle.  Call the <code>getExceptions()</code>
 * method to retrieve the cached exception instances.</p>
 */
public class ApplicationException extends RuntimeException {


    // ------------------------------------------------------------ Constructors


    /**
     * <p>Construct a new exception with no additional information.</p>
     */
    public ApplicationException() {
        this(null, null, null);
    }


    /**
     * <p>Construct a new exception with the specified detail message.</p>
     *
     * @param message Detail message for this exception
     */
    public ApplicationException(String message) {
        this(message, null, null);
    }


    /**
     * <p>Construct a new exception with the specified detail message and
     * root cause.</p>
     *
     * @param message Detail message for this exception
     * @param cause Root cause for this exception
     */
    public ApplicationException(String message, Throwable cause) {
        this(message, cause, null);
    }


    /**
     * <p>Construct a new exception with the specified root cause.</p>
     *
     * @param cause Root cause for this exception
     */
    public ApplicationException(Throwable cause) {
        this(cause.getMessage(), cause, null);
    }


    /**
     * <p>Construct a new exception with the specified root cause and
     * list of cached exceptions.</p>
     *
     * @param cause Root cause for this exception
     * @param list <code>List</code> of cached exceptions
     */
    public ApplicationException(Throwable cause, List list) {
        this(cause.getMessage(), cause, list);
    }


    /**
     * <p>Construct a new exception with the specified detail message,
     * root cause, and list of cached exceptions.</p>
     *
     * @param message Detail message for this exception
     * @param cause Root cause for this exception
     * @param list <code>List</code> of cached exceptions
     */
    public ApplicationException(String message, Throwable cause, List list) {
        super(message, cause);
        this.list = list;
    }


    // ------------------------------------------------------------- Properties


    /**
     * <p><code>List</code> of cached exceptions associated with this
     * exception.</p>
     */
    private List list = null;


    /**
     * <p>Return a <code>List</code> of the cached exceptions associated with
     * this exception.  If no such exceptions were associated, return
     * <code>null</code> instead.</p>
     */
    public List getExceptions() {
        return this.list;
    }


}
