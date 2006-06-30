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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.dd.impl.common;

import org.openide.filesystems.FileLock;
import org.netbeans.modules.j2ee.dd.api.common.RootInterface;

import java.io.IOException;
import java.io.Reader;

/**
 * @author pfiala
 */
public interface DDProviderDataObject {
    /**
     * Provides Reader to save binary data.
     * @return the Reader
     * @throws IOException
     */
    Reader createReader() throws IOException;

    /**
     * Locks binary data if possible.
     * @return the data lock
     */
    FileLock getDataLock();

    /**
     * Writes data from model to the cache and saves the data if needed.
     * @param model
     * @param dataLock
     */
    void writeModel(RootInterface model, FileLock dataLock);

    /**
     * Obtains data lock, writes data from model to the cache and saves the data if needed.
     * Finally releases the lock.
     * @param model
     */
    void writeModel(RootInterface model) throws IOException;
}
