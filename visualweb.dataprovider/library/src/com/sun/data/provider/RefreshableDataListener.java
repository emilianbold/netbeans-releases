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
 * <p>RefreshableDataListener is an event listener interface that
 * supports processing events produced by a corresponding
 * {@link RefreshableDataProvider} instance, in addition to those fired
 * by the underlying {@link DataProvider}.</p>
 *
 * @author Joe Nuxoll
 */
public interface RefreshableDataListener extends DataListener {

    /**
     * <p>The <code>refresh()</code> method was called on the specified
     * {@link RefreshableDataProvider}.</p>
     *
     * @param provider <code>RefreshableDataProvider</code> on which this event
     *        occurred
     */
    public void refreshed(RefreshableDataProvider provider);
}
