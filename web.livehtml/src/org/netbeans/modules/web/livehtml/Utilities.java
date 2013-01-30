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
package org.netbeans.modules.web.livehtml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.Named;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author petr-podzimek
 */
public class Utilities {
    
    private static void eraseNewLinesFromElement(StringBuilder sb, int from, int to) {
        for (int i = from; i < to; i++) {
            if (sb.charAt(i) == '\n' || sb.charAt(i) == '\r'|| sb.charAt(i) == '\t') {
                sb.setCharAt(i, ' ');
            }
        }
    }

    public static void eraseNewLines(Element element, StringBuilder sb) {
        if (!(element instanceof Named) || !(element instanceof Node) || ("script".equals(((Named)element).name()))) {
            return;
        }
        Node n = (Node)element;
        for (Element e : n.children()) {
            if (e.type() == ElementType.TEXT) {
                eraseNewLinesFromElement(sb, e.from(), e.to());
            } else {
                eraseNewLines(e, sb);
            }
        }
    }
    
//    public static int getRevisionIndex(Revision sourceRevision, List<Revision> revisions) {
//        if (revisions == null || sourceRevision == null) {
//            return -1;
//        }
//        for (Revision revision : revisions) {
//            if (revision.getTimeStamp() != null && revision.getTimeStamp().equals(sourceRevision.getTimeStamp())) {
//                return revisions.indexOf(revision);
//            }
//        }
//        return -1;
//    }

//    public static int getRevisionIndex(Revision sourceRevision, Analysis analysis) {
//        if (analysis == null || sourceRevision == null) {
//            return -1;
//        }
//        return analysis.getTimeStamps().indexOf(sourceRevision.getTimeStamp());
//    }

    public static StringBuilder convertStackTrace(JSONArray arr) {
        StringBuilder sb = new StringBuilder();
        if (arr == null) {
            return sb;
        }
        for (Object o : arr) {
            JSONObject js = (JSONObject)o;
            sb.append(js.get(StackTrace.FUNCTION));
            sb.append(" ");
            sb.append(js.get(StackTrace.LINE_NUMBER));
            sb.append(":");
            sb.append(js.get(StackTrace.COLUMN_NUMBER));
            sb.append(" at ");
            sb.append(js.get(StackTrace.SCRIPT));
            sb.append("\n");
        }
        return sb;
    }
    
    public static StringBuilder fetchFileContent(InputStream is) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(is)); // NOI18N
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb;
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
            return null;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    // ignore
                }
            }
        }
    }

}
