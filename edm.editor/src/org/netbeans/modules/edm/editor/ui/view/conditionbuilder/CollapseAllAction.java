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
package org.netbeans.modules.edm.editor.ui.view.conditionbuilder;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.net.URL;

import javax.swing.Action;
import javax.swing.ImageIcon;

import javax.swing.KeyStroke;
import java.util.logging.Logger;
import org.netbeans.modules.edm.editor.graph.jgo.IGraphView;
import org.netbeans.modules.edm.editor.graph.actions.GraphAction;
import org.openide.util.NbBundle;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class CollapseAllAction extends GraphAction {

    private static final URL collapseAllImgUrl = CollapseAllAction.class.getResource("/org/netbeans/modules/edm/editor/resources/collapse_all_edm.png");
    private static transient final Logger mLogger = Logger.getLogger(ExpandAllAction.class.getName());

    public CollapseAllAction() {
        //action name
        this.putValue(Action.NAME, NbBundle.getMessage(CollapseAllAction.class, "LBL_Collapse_All"));

        //action icon
        this.putValue(Action.SMALL_ICON, new ImageIcon(collapseAllImgUrl));

        //action tooltip
        this.putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(CollapseAllAction.class, "TOOLTIP_CollapseAll"));
       // Acceleratot Shift-C
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('C', InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_MASK));
    }

    /**
     * called when this action is performed in the ui
     * 
     * @param ev event
     */
    public void actionPerformed(ActionEvent ev) {
        IGraphView graphView = (IGraphView) ev.getSource();
        graphView.collapseAll();
    }
}

