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

package org.netbeans.modules.j2ee.common.source;

import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.xml.sax.SAXException;

/**
 *
 * @author Andrei Badae
 */
public class RepositoryImpl extends Repository {

    private XMLFileSystem system;

    public RepositoryImpl() {
        super(createDefFs());
    }

    private static FileSystem createDefFs() {
        try
        {
            FileSystem writeFs = FileUtil.createMemoryFileSystem();
            FileSystem j2eeserverFs = new XMLFileSystem(RepositoryImpl.class.getClassLoader().getResource("org/netbeans/modules/j2ee/deployment/impl/layer.xml"));
            FileSystem javaProjectFs = new XMLFileSystem(RepositoryImpl.class.getClassLoader().getResource("org/netbeans/modules/java/project/layer.xml"));
            return new MultiFileSystem(new FileSystem[] { writeFs, j2eeserverFs, javaProjectFs });
        } catch (SAXException e) {
            AssertionError ae = new AssertionError(e.getMessage());
            ae.initCause(e);
            throw ae;
        }
    }
}
