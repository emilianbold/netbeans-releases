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
