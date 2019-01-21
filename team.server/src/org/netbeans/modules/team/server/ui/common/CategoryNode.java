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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.team.server.ui.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.netbeans.modules.team.commons.treelist.LeafNode;
import org.netbeans.modules.team.commons.treelist.TreeLabel;

/**
 * Category Node. E.g. My Projects, Open Projects
 *
 * 
 */
public class CategoryNode extends LeafNode {

    private JPanel panel;
    private JLabel name;
    private String categoryName;
    private Icon icon;

    private final Object LOCK = new Object();

    public CategoryNode( String name, Icon icon ) {
        super( null );
        this.categoryName = name;
        this.icon = icon;
    }

    @Override
    protected JComponent getComponent(Color foreground, Color background, boolean isSelected, boolean hasFocus, int maxWidth) {
        if (null == panel) {
            panel = new JPanel(new BorderLayout());
            panel.setBorder(new EmptyBorder(0, 0, 0, 0));
            panel.setOpaque(false);

            if (icon != null) {
                panel.add(new JLabel(icon), BorderLayout.EAST);
            }
            name = new TreeLabel(categoryName);
            name.setBorder(new EmptyBorder(0, 0, 0, 5));
            panel.add(name, BorderLayout.WEST);
            name.setFont(name.getFont().deriveFont(Font.BOLD));
        }

        name.setForeground(foreground);
        return panel;
    }

    @Override
    protected Type getType() {
        return Type.TITLE;
    }
}
