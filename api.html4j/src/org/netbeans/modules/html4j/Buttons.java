/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html4j;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import net.java.html.js.JavaScriptBody;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class Buttons {
    @JavaScriptBody(args = {}, body = 
        "var list = window.document.getElementsByTagName('button');\n" +
        "var arr = [];\n" +
        "for (var i = 0; i < list.length; i++) {\n" +
        "  var b = list[i];\n" +
        "  if (b.hidden === true) {\n" +
        "    arr.push(b.id);\n" +
        "    arr.push(b.innerHTML);\n" +
        "  }\n" +
        "}\n" +
        "return arr;\n"
    )
    private static native Object[] list();
    
    public static JButton[] buttons() {
        List<JButton> arr = new ArrayList<>();
        final Object[] all = list();
        for (int i = 0; i < all.length; i += 2) {
            JButton b = new JButton();
            b.setName(all[i].toString());
            b.setText(all[i + 1].toString());
            arr.add(b);
        }
        return arr.toArray(new JButton[0]);
    }
}
