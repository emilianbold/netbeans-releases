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

import java.util.EventListener;

/**
 * <p>DataListener is an event listener interface that supports
 * processing events produced by a corresponding {@link DataProvider}
 * instance.  Specialized subinterfaces of this interface add support for
 * events produced by corresponding specialised {@link DataProvider} instances
 * that implement specialized subinterfaces of that API.</p>
 *
 * @author Joe Nuxoll
 */
public interface DataListener extends EventListener {

    /**
     * <p>Process an event indicating that a data element's value has been
     * changed.</p>
     *
     * @param provider <code>DataProvider</code> containing the data element
     *                 that has had a value change
     * @param fieldKey <code>FieldKey</code> representing the specific
     *                data element that has had a value change
     * @param oldValue The old value of this data element
     * @param newValue The new value of this data element
     */
    public void valueChanged(DataProvider provider, FieldKey fieldKey,
                             Object oldValue, Object newValue);

    /**
     * <p>Process an event indicating that the DataProvider has changed in
     * a way outside the bounds of the other event methods.</p>
     *
     * @param provider The DataProvider that has changed
     */
    public void providerChanged(DataProvider provider);
}
