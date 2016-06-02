/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.cnd.otool.debugger.api;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author Nikolay Koldunov
 */
public class SourceCodeProvider {
    public static String getSourcefile(String name) {
        StringBuilder builder = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(name)));
            String s;

            while ((s = br.readLine()) != null) {
                builder.append(s);
            }

            br.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return builder.toString();
    }

    public static String getHTMLForSourcefile(String name) {
        StringBuilder builder = new StringBuilder();

        builder.append("<html>"); // NOI18N
        builder.append("<head>"); // NOI18N
        builder.append("<title>Source Code</title>"); // NOI18N
        builder.append("</head>"); // NOI18N
        builder.append("<body>"); // NOI18N
        builder.append("<pre style=\"font-size: medium; font-family: Courier, monospace;\">"); // NOI18N

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(name)));
            String s;
            int count = 0;
            while ((s = br.readLine()) != null) {
                builder.append("<input type=\"button\" id=\"" + ++count + "\" value=\"" + count + "\" onClick=\"toggleLineBreakpoint(id)\" class=\"glyph\"/>"); // NOI18N
                builder.append("<span id=\"line" + count + "\">"); // NOI18N
                builder.append(s);
                builder.append("</span>"); // NOI18N
                builder.append("<br>"); // NOI18N
            }
            
            br.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        builder.append("</pre>"); // NOI18N
        builder.append("</body>"); // NOI18N
        builder.append("</html>"); // NOI18N
        
        return builder.toString();
    }
}
