/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.dd.impl.common;

import org.openide.filesystems.FileLock;

import java.io.InputStream;
import java.io.IOException;

/**
 * @author pfiala
 */
public interface DDProviderDataObject {
    /**
     * Provides InputStream to save binary data.
     * @return the InputStream
     */
    InputStream createInputStream();

    /**
     * Locks binary data if possible.
     * @return the data lock
     */
    FileLock getDataLock();

    /**
     * Writes data from model to the cache and saves the data if needed.
     * @param dataLock
     */
    void writeModel(FileLock dataLock);

    /**
     * Obtains data lock, writes data from model to the cache and saves the data if needed.
     * Finally releases the lock.
     */
    void writeModel() throws IOException;
}
