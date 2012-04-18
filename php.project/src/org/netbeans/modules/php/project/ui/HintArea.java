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
package org.netbeans.modules.php.project.ui;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JTextPane;
import javax.swing.UIManager;

/**
 * Inspired by HTMLArea from profiler.
 */
public class HintArea extends JTextPane {

    private static final long serialVersionUID = 76873543674545L;


    public HintArea() {
        Color hintBackground = Utils.getHintBackground();
        setOpaque(true);
        setAutoscrolls(true);
        setForeground(UIManager.getColor("Label.foreground")); // NOI18N
        setFont(UIManager.getFont("Label.font")); // NOI18N
        setBackground(hintBackground);
        setBorder(BorderFactory.createMatteBorder(10, 10, 10, 10, hintBackground));
        setContentType("text/html"); // NOI18N
        setEditable(false);
    }

    @Override
    public void setText(String value) {
        if (value == null) {
            return;
        }

        Font font = getFont();
        Color textColor = getForeground();
        value = value.replaceAll("\\n\\r|\\r\\n|\\n|\\r", "<br>"); // NOI18N
        value = value.replace("<code>", "<code style=\"font-size: " + font.getSize() + "pt;\">"); // NOI18N

        String colorText = "rgb(" + textColor.getRed() + "," + textColor.getGreen() + "," + textColor.getBlue() + ")"; // NOI18N
        String newText = "<html><body style=\"color: " + colorText + "; font-size: " + font.getSize()  // NOI18N
                + "pt; font-family: " + font.getName() + ";\">" + value + "</body></html>"; // NOI18N

        setDocument(getEditorKit().createDefaultDocument()); // Workaround for http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5042872
        super.setText(newText);
    }

}
