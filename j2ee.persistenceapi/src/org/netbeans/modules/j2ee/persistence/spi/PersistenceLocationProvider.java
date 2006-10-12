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

package org.netbeans.modules.j2ee.persistence.spi;

import java.io.IOException;
import org.openide.filesystems.FileObject;

/**
 * This interface should be implemented in a context which supports
 * Java Persistence API, whether or not is contains a persistence.xml file. It
 * contains methods for creating/retrieving the default location for persistence.xml
 * files. For example it can be implemented by a project and can be used by 
 * a client which wants to create a persistence scope in that project.
 *
 * @author Andrei Badea
 */
public interface PersistenceLocationProvider {

    /**
     * Returns the default location for persistence.xml and related files.
     *
     * @return the default location or null if it does not exist.
     */
    FileObject getLocation();

    /**
     * Creates (if it does not exist) and returns the default location for
     * persistence.xml and related files.
     *
     * @return the default location; never null.
     *
     * @throws IOException if an error occured while creating the location
     * of persistence.xml
     */
    FileObject createLocation() throws IOException;
}
