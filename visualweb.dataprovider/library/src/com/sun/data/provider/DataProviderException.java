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

package com.sun.data.provider;

/**
 * <p>The DataProviderException is generic runtime exception that wraps an
 * underlying cause exception for {@link DataProvider} methods.  Each of the
 * DataProvider methods throw this runtime exception to allow for clean
 * wrapping of underlying exceptions.  Since it is unknown what implementations
 * of DataProvider will exist, it cannot be known what types of exceptions this
 * will wrap.</p>
 *
 * <p>The decision to make this a RuntimeException was intentional so as not to
 * force a <code>try...catch</code> block around every invocation of a
 * DataProvider method, but still explicitly declare the throws clause on each
 * method making it clear that the user should consult the documentation for the
 * specific DataProvider implementation to see what exceptions might be thrown.
 * </p>
 *
 * @author Joe Nuxoll
 */
public class DataProviderException extends RuntimeException {

    /**
     * Constructs a default DataProviderException with no message with no
     * wrapped cause exception.
     */
    public DataProviderException() {}

    /**
     * Constructs a DataProviderException with the specified message and no
     * wrapped cause exception.
     *
     * @param message The desired exception message
     */
    public DataProviderException(String message) {
        super(message);
    }

    /**
     * Constructs a DataProviderException with the specified message and
     * wrapped cause exception.
     *
     * @param message The desired exception message
     * @param cause The underlying cause exception
     */
    public DataProviderException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a DataProviderException with the specified wrapped cause
     * exception.
     *
     * @param cause The underlying cause exception
     */
    public DataProviderException(Throwable cause) {
        super(cause);
    }
}
