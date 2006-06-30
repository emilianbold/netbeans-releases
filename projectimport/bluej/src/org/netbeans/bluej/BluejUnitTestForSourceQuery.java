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

package org.netbeans.bluej;

import java.net.URL;
import org.netbeans.spi.java.queries.MultipleRootsUnitTestForSourceQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author mkleint
 */
public class BluejUnitTestForSourceQuery implements MultipleRootsUnitTestForSourceQueryImplementation {

    private BluejProject project;

    /** Creates a new instance of BluejUnitTestForSourceQuery */
    public BluejUnitTestForSourceQuery(BluejProject proj) {
        project = proj;
    }

    /**
     * Returns the test roots for a given source root.
     *
     * @param source a Java package root with sources
     * @return a corresponding Java package roots with unit tests. The
     *     returned URLs need not point to an existing folder. It can be null
     *     when no mapping from source to unit test is known.
     */
    public URL[] findUnitTests(FileObject source) {
        return new URL[] { URLMapper.findURL(project.getProjectDirectory(), URLMapper.EXTERNAL) }; 
    }

    /**
     * Returns the source roots for a given test root.
     *
     * @param unitTest a Java package roots with unit tests
     * @return a corresponding Java package roots with sources. It can be null
     *     when no mapping from unit test to source is known.
     */
    public URL[] findSources(FileObject unitTest) {
        return new URL[] { URLMapper.findURL(project.getProjectDirectory(), URLMapper.EXTERNAL) }; 
    }
    
}
