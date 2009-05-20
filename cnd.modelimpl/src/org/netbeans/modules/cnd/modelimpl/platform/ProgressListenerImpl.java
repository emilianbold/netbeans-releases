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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.cnd.modelimpl.platform;

import java.util.*;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;

/**
 *
 * @author vk155633
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.cnd.api.model.CsmProgressListener.class)
public class ProgressListenerImpl implements CsmProgressListener {

    private final Map<CsmProject, ParsingProgress> handles = new HashMap<CsmProject, ParsingProgress>();

    private synchronized ParsingProgress getHandle(CsmProject project, boolean createIfNeed) {
        ParsingProgress handle = handles.get(project);
        if (handle == null && createIfNeed) {
            handle = new ParsingProgress(project);
            handles.put(project, handle);
        }
        return handle;
    }

    public void projectParsingStarted(CsmProject project) {
        if (TraceFlags.TRACE_PARSER_QUEUE) {
            System.err.println("ProgressListenerImpl.projectParsingStarted " + project.getName());
        }
        getHandle(project, true).start();
    }

    public void projectFilesCounted(CsmProject project, int filesCount) {
        if (TraceFlags.TRACE_PARSER_QUEUE) {
            System.err.println("ProgressListenerImpl.projectFilesCounted " + project.getName() + ' ' + filesCount);
        }
        getHandle(project, true).switchToDeterminate(filesCount);
    }

    public void projectParsingFinished(CsmProject project) {
        if (TraceFlags.TRACE_PARSER_QUEUE) {
            System.err.println("ProgressListenerImpl.projectParsingFinished " + project.getName());
        }
        done(project);
    }

    public void projectLoaded(CsmProject project) {
        if (TraceFlags.TRACE_PARSER_QUEUE) {
            System.err.println("ProgressListenerImpl.projectLoaded " + project.getName());
        }
    }

    public void projectParsingCancelled(CsmProject project) {
        if (TraceFlags.TRACE_PARSER_QUEUE) {
            System.err.println("ProgressListenerImpl.projectParsingCancelled " + project.getName());
        }
        done(project);
    }

    private void done(CsmProject project) {
        getHandle(project, true).finish();
        synchronized (this) {
            handles.remove(project);
        }
    }

    public void fileInvalidated(CsmFile file) {
    }

    public void fileAddedToParse(CsmFile file) {
        CsmProject project = file.getProject();
        ParsingProgress handle = getHandle(project, false);
        if (handle != null) {
            handle.addedToParse(file);
        } else if (project.isArtificial()) {
            for (CsmProject p : CsmModelAccessor.getModel().projects()){
                if (!p.isArtificial()) {
                    if (p.getLibraries().contains(project)){
                        handle = getHandle(p, false);
                        if (handle != null) {
                            handle.addedToParse(file);
                        }
                    }
                }
            }
        }
    }

    public void fileParsingStarted(CsmFile file) {
        if (TraceFlags.TRACE_PARSER_QUEUE) {
            System.err.println("  ProgressListenerImpl.fileParsingStarted " + file.getAbsolutePath());
        }
        CsmProject project = file.getProject();
        ParsingProgress handle = getHandle(project, false);
        if (handle != null) {
            handle.nextCsmFile(file);
        } else if (project.isArtificial()) {
            for (CsmProject p : CsmModelAccessor.getModel().projects()){
                if (!p.isArtificial()) {
                    if (p.getLibraries().contains(project)){
                        handle = getHandle(p, false);
                        if (handle != null) {
                            handle.nextCsmFile(file);
                        }
                    }
                }
            }
        }
    }

    public void fileParsingFinished(CsmFile file) {
        if (TraceFlags.TRACE_PARSER_QUEUE) {
            System.err.println("  ProgressListenerImpl.fileParsingFinished " + file.getAbsolutePath());
        }
    }

    public void parserIdle() {
        if (TraceFlags.TRACE_PARSER_QUEUE) {
            System.err.println("  ProgressListenerImpl.parserIdle");
        }
    }
}
