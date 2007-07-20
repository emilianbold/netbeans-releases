/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
package org.netbeans.modules.versioning.spi.testvcs;

import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.netbeans.modules.versioning.spi.VCSInterceptor;

import java.io.File;

/**
 * Test versioning system.
 * 
 * @author Maros Sandor
 */
public class TestVCS extends VersioningSystem {

    private static TestVCS instance;
    private VCSInterceptor interceptor;

    public static TestVCS getInstance() {
        return instance;
    }
    
    public TestVCS() {
        instance = this;
        interceptor = new TestVCSInterceptor(this);
    }

    public File getTopmostManagedAncestor(File file) {
        File topmost = null;
        for (; file != null; file = file.getParentFile()) {
            if (file.getName().endsWith("-test-versioned")) {
                topmost = file;
            }
        }
        return topmost;
    }

    public VCSInterceptor getVCSInterceptor() {
        return interceptor;
    }
}
