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


package org.netbeans.modules.maven.embedder.exec;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.maven.BuildFailureException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.execution.ReactorManager;
import org.apache.maven.lifecycle.DefaultLifecycleExecutor;
import org.apache.maven.lifecycle.LifecycleExecutionException;
import org.apache.maven.monitor.event.EventDispatcher;
import org.apache.maven.project.MavenProject;

/**
 *
 * @author mkleint
 */
public class MyLifecycleExecutor extends DefaultLifecycleExecutor {

    private static ThreadLocal<List<File>> fileList = new ThreadLocal<List<File>>();
    
    public static List<File> getAffectedProjects() {
        return fileList.get() == null ? Collections.<File>emptyList() : fileList.get();
    }
    public List<File> readAffectedProjects(ReactorManager rm) {
        if (rm != null) {
            List lst = rm.getSortedProjects();
            Iterator it = lst.iterator();
            List<File> toRet = new ArrayList<File>();
            while (it.hasNext()) {
                MavenProject elem = (MavenProject) it.next();
                toRet.add(elem.getFile());
            }
            return toRet;
        }
        return Collections.<File>emptyList(); 
    }
    
    /**
     * Execute a task. Each task may be a phase in the lifecycle or the
     * execution of a mojo.
     *
     * @param session
     * @param rm
     * @param dispatcher
     */
    @Override
    public void execute( MavenSession session, ReactorManager rm, EventDispatcher dispatcher )
        throws BuildFailureException, LifecycleExecutionException
    {
        fileList.set(readAffectedProjects(rm));
        super.execute( session, rm, dispatcher);
    }
}
