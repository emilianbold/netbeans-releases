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
package org.netbeans.modules.sql.framework.ui.view.conditionbuilder;

import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;

import org.netbeans.modules.sql.framework.model.ColumnRef;
import org.netbeans.modules.sql.framework.model.GUIInfo;
import org.netbeans.modules.sql.framework.model.SQLCanvasObject;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.ui.event.SQLDataEvent;
import org.netbeans.modules.sql.framework.ui.graph.IGraphNode;
import org.netbeans.modules.sql.framework.ui.view.graph.SQLGraphView;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import net.java.hulp.i18n.Logger;
import com.sun.sql.framework.exception.BaseException;
import org.netbeans.modules.etl.logger.Localizer;


/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class ConditionGraphView extends SQLGraphView {

    private static transient final Logger mLogger = Logger.getLogger(ConditionGraphView.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    /** Creates a new instance of ConditionGraphView */
    public ConditionGraphView() {
        super();
    }

    public void drop(java.awt.dnd.DropTargetDropEvent e) {
        try {

            if (e.isDataFlavorSupported(mDataFlavorArray[0])) {
                Transferable tr = e.getTransferable();

                if (!(tr.getTransferData(mDataFlavorArray[0]) instanceof SQLDBColumn)) {
                    super.drop(e);
                    return;
                }
                e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                SQLDBColumn column = (SQLDBColumn) tr.getTransferData(mDataFlavorArray[0]);

                Point viewCoord = e.getLocation();
                Point docCoord = viewToDocCoords(viewCoord);
                ColumnRef columnRef = SQLModelObjectFactory.getInstance().createColumnRef(column);

                GUIInfo gInfo = columnRef.getGUIInfo();
                gInfo.setX(docCoord.x);
                gInfo.setY(docCoord.y);

                ((ConditionGraphController) this.getGraphController()).handleNodeAdded(columnRef);
            } else {
                e.rejectDrop();
            }

        } catch (Exception ex) {
            mLogger.errorNoloc(mLoc.t("EDIT158: error in doing reload{0}", ConditionGraphView.class.getName()), ex);
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message("Can not create Node in the canvas " + ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
        } finally {
            e.dropComplete(true);
        }

    }

    protected void createGraphNode(SQLDataEvent event) throws BaseException {
        SQLCanvasObject canvasObj = event.getCanvasObject();
        if (!(canvasObj instanceof ColumnRef)) {
            super.createGraphNode(event);
            return;
        }

        IGraphNode canvasNode = this.findGraphNode(canvasObj);
        // If graph node already exists then simply return
        if (canvasNode != null) {
            return;
        }

        GUIInfo gInfo = canvasObj.getGUIInfo();
        Point location = new Point(gInfo.getX(), gInfo.getY());

        ColumnGraphNode columnNode = new ColumnGraphNode((ColumnRef) canvasObj);
        columnNode.setLocation(location);
        this.addNode(columnNode);

    }
}

