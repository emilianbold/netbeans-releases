/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.compapp.configextension.handlers.model;

import java.util.Arrays;
import java.util.List;


/**
 * A JAX-WS/JAX-RS Handler/Filter.
 *
 * @author jqian
 */
public class Handler {

    private String name;
    private String projectPath;
    private List<String> jarPaths;
    private String className;
    private List<HandlerParameter> parameters;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<HandlerParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<HandlerParameter> parameters) {
        this.parameters = parameters;
    }

    public List<String> getJarPaths() {
        return jarPaths;
    }

    public void setJarPaths(List<String> jarPaths) {
        this.jarPaths = jarPaths;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("Handler ["); // NOI18N

        sb.append("name=" + name); // NOI18N
        sb.append(", projectPath=" + projectPath); // NOI18N
        sb.append(", jarPaths=" + jarPaths); // NOI18N
        sb.append(", className=" + className); // NOI18N
        sb.append(", parameters=" + parameters); // NOI18N
        sb.append("]"); // NOI18N

        return sb.toString();
    }

    // Convenience methods:
    public String getJarPathsAsString() {
        StringBuffer sb = new StringBuffer();

        if (jarPaths != null) {
            for (String jarPath : jarPaths) {
                sb.append(jarPath);
                sb.append(";"); // NOI18N
            }

            if (sb.length() > 0) {
                sb = sb.deleteCharAt(sb.length() - 1); // strip the last ";"
            }
        }

        return sb.toString();
    }

    public void setJarPaths(String jarPaths) {
        if (jarPaths == null) {
            this.jarPaths = null;
        } else {
            this.jarPaths = Arrays.asList(jarPaths.split(";")); // NOI18N
        }
    }
}

