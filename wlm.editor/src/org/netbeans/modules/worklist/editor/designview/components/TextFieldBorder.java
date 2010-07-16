/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.designview.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.JTextField;
import javax.swing.border.Border;

/**
 *
 * @author anjeleevich
 */
public class TextFieldBorder implements Border {
    
    /** Creates a new instance of TextFieldBorder */
    public TextFieldBorder() {
    }
    
    public void paintBorder(Component c, Graphics g, int x, int y, 
            int width, int height) 
    {
        if (((JTextField) c).isEditable()) {
            g.setColor(new Color(0x7A8A99));
            g.drawRect(x, y, width - 1, height - 1);

            int x1 = x + 1;
            int y1 = y + 1;
            int x2 = x + width - 2;
            int y2 = y + height - 2;

            g.setColor(new Color(0xB8CFE5));
            g.drawLine(x1, y1, x2, y1);
            g.drawLine(x1, y1, x1, y2);
        } else {
            g.setColor(new Color(0xB8CFE5));
            g.drawRect(x, y, width - 1, height - 1);
        }
    }

    public Insets getBorderInsets(Component c) {
        return new Insets(3, 4, 3, 4);
    }

    public boolean isBorderOpaque() {
        return false;
    }

    public static final Border INSTANCE = new TextFieldBorder();
}
