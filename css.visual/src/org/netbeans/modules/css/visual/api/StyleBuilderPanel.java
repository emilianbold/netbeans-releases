/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.css.visual.api;

import org.netbeans.modules.css.visual.ui.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

/**
 * Style Builder main panel
 * 
 * @author Marek Fukala
 * @version 1.0
 */
public final class StyleBuilderPanel extends JPanel {

    private List<StyleEditor> styleEditorList = new ArrayList<StyleEditor>();

    public static StyleBuilderPanel createInstance() {
        return new StyleBuilderPanel();
    }
        /** Creates new form StyleBuilderPanel */
    private StyleBuilderPanel() {
        initComponents();
        initialize();
    }

    private void initialize(){
        styleEditorList.add(new FontStyleEditor());
        styleEditorList.add(new BackgroundStyleEditor());
        styleEditorList.add(new TextBlockStyleEditor());
        styleEditorList.add(new BorderStyleEditor());
        styleEditorList.add(new MarginStyleEditor());
        styleEditorList.add(new PositionStyleEditor());
        //styleEditorList.add(new ListStyleEditor());
        //styleEditorList.add(new OtherStyleEditor());
        for(StyleEditor styleEditor : styleEditorList) {
            JScrollPane spane = new JScrollPane(styleEditor);
            spane.setBorder(new EmptyBorder(1,1,1,1));
            jTabbedPane1.addTab(styleEditor.getDisplayName(), spane);
        }
        jTabbedPane1.setSelectedIndex(0);
    }

    public void setContent(CssRuleContext content){
        for(StyleEditor editor : styleEditorList) {
            editor.setContent(content);
        }
    }

    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();

        setLayout(new java.awt.BorderLayout());

        jTabbedPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        add(jTabbedPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables
    
}
