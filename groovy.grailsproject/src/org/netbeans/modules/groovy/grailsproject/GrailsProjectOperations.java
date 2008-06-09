/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.grailsproject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.openide.filesystems.FileObject;
import org.netbeans.api.project.Project;

/**
 *
 * @author schmidtm
 */
public class GrailsProjectOperations implements DeleteOperationImplementation {

    private final Project project;

    public GrailsProjectOperations(Project project) {
        this.project = project;
    }

    public void notifyDeleting() throws IOException {
        return;
    }

    public void notifyDeleted() throws IOException {
        return;
    }

    public List<FileObject> getMetadataFiles() {
        return Collections.emptyList();
    }

    public List<FileObject> getDataFiles() {
        List<FileObject> dataFiles = new ArrayList<FileObject>();

        dataFiles.add(project.getProjectDirectory());

        return dataFiles;
    }

}
