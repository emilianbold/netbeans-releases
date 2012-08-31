/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.livehtml.ui.stacktrace;

import java.net.MalformedURLException;
import java.net.URL;
import org.json.simple.JSONObject;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.clientproject.api.ServerURLMapping;
import org.netbeans.modules.web.livehtml.StackTrace;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;

/**
 *
 * @author petr-podzimek
 */
public class StackTraceToolTipLeafNode extends AbstractNode {
    
    public StackTraceToolTipLeafNode(Object object, Project project) {
        super(Children.LEAF);
        
        if (object instanceof JSONObject) {
            JSONObject jSONObject = (JSONObject) object;
            final String function = (String)jSONObject.get(StackTrace.FUNCTION);
            final Number lineNumber = (Number)jSONObject.get(StackTrace.LINE_NUMBER);
            final Number columnNumber = (Number)jSONObject.get(StackTrace.COLUMN_NUMBER);
            String script = (String)jSONObject.get(StackTrace.SCRIPT);
            
            StringBuilder sb = new StringBuilder();
            
            if (script != null) {
                try {
                    URL url = new URL(script);
                    if (project != null) {
                        FileObject fo = ServerURLMapping.fromServer(project, url);
                        if (fo != null) {
                            String relPath = FileUtil.getRelativePath(project.getProjectDirectory(), fo);
                            if (relPath != null) {
                                script = relPath;
                            }
                        } else {
                            FileObject localFile = URLMapper.findFileObject(url);
                            if (localFile != null) {
                                String p = FileUtil.getRelativePath(project.getProjectDirectory(), localFile);
                                if (p != null) {
                                    script = p;
                                }
                            }
                        }
                    } else {
                        int index = script.lastIndexOf('/');
                        if (index != -1) {
                            script = script.substring(index+1);
                        }
                    }
                } catch (MalformedURLException murl) {}
                sb.append(script);
            }
            
            if (lineNumber != null) {
                sb.append(":");
                sb.append(lineNumber);
            }
            
            if (columnNumber != null) {
                sb.append(":");
                sb.append(columnNumber);
            }
            
//            if (function != null) {
//                sb.append(" "+function);
//            }
            
            setDisplayName(sb.toString());
        }
        
    }
    
}
