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

import org.netbeans.modules.versioning.spi.VCSInterceptor;

import java.io.File;
import java.util.*;

/**
 * @author Maros Sandor
 */
public class TestVCSInterceptor extends VCSInterceptor {

    private final TestVCS       vcs;
    private final List<File>    createdFiles = new ArrayList<File>();

    public TestVCSInterceptor(TestVCS vcs) {
        this.vcs = vcs;
    }

    public List<File> getCreatedFiles() {
        return createdFiles;
    }

    public void afterCreate(File file) {
        createdFiles.add(file);
    }
}
