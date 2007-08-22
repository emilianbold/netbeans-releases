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

package org.netbeans.modules.j2ee.ejbcore.test;

import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.xml.sax.SAXException;

/**
 *
 * @author Andrei Badea
 */
public class RepositoryImpl extends Repository {

    public RepositoryImpl() {
        super(new MultiFileSystemImpl());
    }

    public static final class MultiFileSystemImpl extends MultiFileSystem {

        public MultiFileSystemImpl() {
            super(createFileSystems());
        }

        public void reset() {
            setDelegates(createFileSystems());
        }

        private static FileSystem[] createFileSystems() {
            try {
                FileSystem j2eeserverFs = new XMLFileSystem(RepositoryImpl.class.getClassLoader().getResource("org/netbeans/modules/j2ee/deployment/impl/layer.xml"));
                FileSystem ejbCoreFs = new XMLFileSystem(RepositoryImpl.class.getClassLoader().getResource("org/netbeans/modules/j2ee/ejbcore/resources/layer.xml"));
                FileSystem projectUiFs = new XMLFileSystem(RepositoryImpl.class.getClassLoader().getResource("org/netbeans/modules/project/ui/resources/layer.xml"));
                FileSystem writableFileSystem = FileUtil.createMemoryFileSystem();
                return new FileSystem[] { writableFileSystem, j2eeserverFs, ejbCoreFs, projectUiFs };
            } catch (SAXException e) {
                AssertionError assertionError = new AssertionError(e.getMessage());
                assertionError.initCause(e);
                throw assertionError;
            }
        }
    }
}
