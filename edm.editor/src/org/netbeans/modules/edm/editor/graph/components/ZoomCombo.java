/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.edm.editor.graph.components;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;

import org.netbeans.modules.edm.editor.graph.MashupGraphManager;

/**
 *
 * @author karthikeyan s
 */
public class ZoomCombo extends JComboBox {

    public ZoomCombo(final MashupGraphManager manager) {
        super();
        addItem("33.3 %");
        addItem("50 %");
        addItem("75 %");
        addItem("100 %");
        addItem("125 %");
        addItem("150 %");
        addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String zoomValue = (String) getSelectedItem();
                if (zoomValue.equals("33.3 %")) {
                    manager.zoomGraph(.33f);
                } else if (zoomValue.equals("50 %")) {
                    manager.zoomGraph(.5f);
                } else if (zoomValue.equals("75 %")) {
                    manager.zoomGraph(.75f);
                } else if (zoomValue.equals("100 %")) {
                    manager.zoomGraph(1.0f);
                } else if (zoomValue.equals("125 %")) {
                    manager.zoomGraph(1.25f);
                } else if (zoomValue.equals("150 %")) {
                    manager.zoomGraph(1.5f);
                } 
            }
        });
        setSelectedItem("100 %");
        Dimension dim = new Dimension(75, 20);
        if(!System.getProperty("os.name").contains("Win")) {
            dim = new Dimension(100, 20);
        }
        setPreferredSize(dim);
        setSize(dim);
        setMaximumSize(dim);
        setMinimumSize(dim);
    }
}