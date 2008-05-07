/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.grailsproject.execution;

import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Hejl
 */
public final class DefaultDescriptor implements Descriptor {

    private final Project project;

    private final LineSnooper snooper;

    private final Runnable post;

    private final boolean suspend;

    private final boolean progress;

    private final boolean front;

    private final boolean input;

    public DefaultDescriptor(Project project, boolean suspend) {
        this(project, null, null, suspend, true, true, true);
    }

    public DefaultDescriptor(Project project, LineSnooper snooper, Runnable post, boolean suspend) {
        this(project, snooper, post, suspend, true, true, true);
    }

    public DefaultDescriptor(Project project, LineSnooper snooper, Runnable post,
            boolean suspend, boolean progress, boolean front, boolean input) {
        this.project = project;
        this.snooper = snooper;
        this.post = post;
        this.suspend = suspend;
        this.progress = progress;
        this.front = front;
        this.input = input;
    }

    public FileObject getFileObject() {
        return project.getProjectDirectory();
    }

    public LineSnooper getOutputSnooper() {
        return snooper;
    }

    public Runnable getPostExecution() {
        return post;
    }

    public boolean isFrontWindow() {
        return front;
    }

    public boolean isInputVisible() {
        return input;
    }

    public boolean showProgress() {
        return progress;
    }

    public boolean showSuspended() {
        return suspend;
    }


}
