/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.edm.editor.graph.actions;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.Anchor.Entry;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.edm.editor.dataobject.MashupDataObject;
import org.netbeans.modules.edm.editor.utils.ImageConstants;
import org.netbeans.modules.edm.editor.utils.MashupGraphUtil;
import org.netbeans.modules.edm.editor.widgets.EDMConnectionWidget;
import org.netbeans.modules.edm.editor.widgets.EDMNodeAnchor;
import org.netbeans.modules.edm.editor.widgets.EDMNodeWidget;
import org.netbeans.modules.edm.model.SourceTable;
import org.netbeans.modules.edm.model.impl.SQLGroupByImpl;

/**
 *
 * @author Shankari
 */
public class RemoveGroupbyAction extends AbstractAction {

    private SQLGroupByImpl grpBy;
    private MashupDataObject mObj;
    private Widget widget;

    public RemoveGroupbyAction(MashupDataObject dObj, SQLGroupByImpl obj, Widget widget,String name) {
        super(name, new ImageIcon(MashupGraphUtil.getImage(ImageConstants.REMOVE)));
        this.mObj = dObj;
        this.grpBy = obj;
        this.widget = widget;
    }

    public void actionPerformed(ActionEvent arg0) {
        ((SourceTable) grpBy.getParentObject()).setSQLGroupBy(null);
        try {
            HashMap<EDMNodeWidget, Anchor> edgesMap = mObj.getGraphManager().getScene().getEdgesMap();
            if (edgesMap != null) {
                EDMNodeAnchor anchor = (EDMNodeAnchor) edgesMap.get(widget);
                EDMNodeWidget edmWidget = (EDMNodeWidget) anchor.getRelatedWidget();
                if (edmWidget.getNodeName().trim().equals("Group By")) {
                    List<Anchor.Entry> entries = anchor.getEntries();
                    for (Entry entry : entries) {
                        EDMConnectionWidget connWd = (EDMConnectionWidget) entry.getAttachedConnectionWidget();
                        connWd.removeFromParent();
                    }
                    edmWidget.removeFromParent();
                    mObj.getGraphManager().validateScene();
                }
            }
            widget.removeFromParent();
            mObj.getGraphManager().validateScene();
            mObj.getModel().setDirty(true);
            mObj.setModified(true);
        } catch (Exception ex) {
        }
    }
}
