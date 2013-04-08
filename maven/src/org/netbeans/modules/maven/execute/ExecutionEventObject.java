/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.execute;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.execution.ExecutionEvent;
import org.json.simple.JSONObject;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.FileUtil;
import org.openide.windows.IOPosition;

/**
 * a stub to be filled with parsed JSON values, vaguely related to ExecutionEventObject in maven codebase.
 * @author mkleint
 */

public final class ExecutionEventObject {

    final ExecutionEvent.Type type;
    final int projectCount;
    final GAV currentProject;
    final File currentProjectLocation;
    final MojoExecution execution;

    private  ExecutionEventObject(ExecutionEvent.Type type, GAV currentProject, File currentProjectLocation, MojoExecution execution, int projectCount) {
        this.type = type;
        this.currentProject = currentProject;
        this.currentProjectLocation = currentProjectLocation;
        this.execution = execution;
        this.projectCount = projectCount;
    }

    public static class MojoExecution {

        final String goal;
        final GAV plugin;
        final String phase;
        final String executionId;

        private MojoExecution(String goal, @NonNull GAV plugin, String phase, String executionId) {
            this.goal = goal;
            this.plugin = plugin;
            this.phase = phase;
            this.executionId = executionId;
        }
        
    }
    
    public static class GAV {
        final String groupId;
        final String artifactId;
        final String version;

        private GAV(@NonNull String groupId, @NonNull String artifactId, @NonNull String version) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
        }
        
    }
    
    public static ExecutionEventObject create(JSONObject obj) {
        String s = (String) obj.get("type");
        ExecutionEvent.Type t = ExecutionEvent.Type.valueOf(s);
        GAV prjGav = null;
        File prjFile = null;
        MojoExecution exec = null;
        String excMessage = null;
        Long count = (Long) obj.get("prjcount");
        int prjCount = -1;
        if (count != null) {
            prjCount = count.intValue();
        }
        JSONObject prj = (JSONObject) obj.get("prj");
        if (prj != null) {
            String id = (String) prj.get("id");
            String[] ids = id.split(":");
            prjGav = new GAV(ids[0], ids[1], ids[2]);
            String file = (String) prj.get("file");
            if (file != null) {
                prjFile = FileUtil.normalizeFile(new File(file));
            }
        }
        JSONObject mojo = (JSONObject) obj.get("mojo");
        if (mojo != null) {
            String id = (String) mojo.get("id");
            String[] ids = id.split(":");
            GAV mojoGav = new GAV(ids[0], ids[1], ids[2]);
            String goal = (String) mojo.get("goal");
            String execId = (String) mojo.get("execId");
            String phase = (String) mojo.get("phase");
            exec = new MojoExecution(goal, mojoGav, phase, execId);
        }
//        JSONObject exc = (JSONObject) obj.get("exc");
//        if (exc != null) {
//            String message = (String) exc.get("msg");
//            if (message != null) {
//                try {
//                    byte[] bytes = Base64.decodeBase64(message.getBytes("UTF-8"));
//                    excMessage = new String(bytes, "UTF-8");
//                    System.out.println("exc message=" + excMessage);
//                } catch (UnsupportedEncodingException ex) {
//                    Exceptions.printStackTrace(ex);
//                }
//            }
//        }
        
        return new ExecutionEventObject(t, prjGav, prjFile, exec, prjCount);
    }
    
    //experimental
    public static class Tree {
        final ExecutionEventObject current;
        final ExecutionEventObject.Tree parentNode;
        final List<ExecutionEventObject.Tree> childrenNodes = new ArrayList<ExecutionEventObject.Tree>();
        private IOPosition.Position startOffset;
        private IOPosition.Position endOffset;

        public Tree(ExecutionEventObject current, ExecutionEventObject.Tree parent) {
            this.current = current;
            this.parentNode = parent;
        }

        public IOPosition.Position getStartOffset() {
            return startOffset;
        }

        public void setStartOffset(IOPosition.Position startOffset) {
            this.startOffset = startOffset;
        }

        public IOPosition.Position getEndOffset() {
            return endOffset;
        }

        public void setEndOffset(IOPosition.Position endOffset) {
            this.endOffset = endOffset;
        }
        
        
        
    }
    
}
