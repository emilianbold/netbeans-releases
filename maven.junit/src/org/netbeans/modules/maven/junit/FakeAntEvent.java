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
package org.netbeans.modules.maven.junit;

import java.io.File;
import java.util.Set;
import org.apache.tools.ant.module.run.LoggerTrampoline;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntSession;
import org.apache.tools.ant.module.spi.TaskStructure;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mkleint
 */
public class FakeAntEvent implements LoggerTrampoline.AntEventImpl {
    private String taskName;
    private AntSession session;
    private TaskStructure taskStructure;
    private Project project;
    private String message;
    private int logLevel = AntEvent.LOG_INFO;


    FakeAntEvent(AntSession session, Project prj) {
        this.session = session;
        project = prj;
    }

    public AntSession getSession() {
        return session;
    }

    public void consume() throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isConsumed() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public File getScriptLocation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getLine() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getTargetName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    void setMessage(String string) {
        message = string;
    }

    void setTaskName(String string) {
        taskName = string;
    }
    
    public String getTaskName() {
        return taskName;
    }

    public TaskStructure getTaskStructure() {
        return taskStructure;
    }
    
    public void setTaskStructure(TaskStructure struct) {
        taskStructure = struct;
    }

    public String getMessage() {
        return message;
    }

    public int getLogLevel() {
        return logLevel;
    }
    
    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    public Throwable getException() {
        return null;
    }

    public String getProperty(String name) {
        if ("basedir".equals(name)) {
            return FileUtil.toFile(project.getProjectDirectory()).getAbsolutePath();
        }
        throw new UnsupportedOperationException("Not supported yet. - " + name);
    }

    public Set<String> getPropertyNames() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String evaluate(String text) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public static class FakeTaskStructure implements LoggerTrampoline.TaskStructureImpl {
        private TaskStructure[] children;
        private String name;

        public FakeTaskStructure(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }

        public String getAttribute(String name) {
            return null;
        }

        public Set<String> getAttributeNames() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String getText() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public TaskStructure[] getChildren() {
            return children;
        }
        
        public void setChildren(TaskStructure[] childs) {
            children = childs;
        }
        
    }


}
