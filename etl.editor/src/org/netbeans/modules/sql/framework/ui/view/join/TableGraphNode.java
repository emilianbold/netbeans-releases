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
package org.netbeans.modules.sql.framework.ui.view.join;

import com.nwoods.jgo.JGoLink;
import java.awt.Color;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.sql.framework.model.SQLJoinTable;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.ui.graph.IGraphPort;
import org.netbeans.modules.sql.framework.ui.graph.impl.BasicCanvasArea;
import org.netbeans.modules.sql.framework.ui.graph.impl.BasicCellArea;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoRectangle;
import com.nwoods.jgo.JGoText;

/**
 * This class represents a one cell level representation of a table
 *
 * @author radval
 */
public class TableGraphNode extends BasicCanvasArea {

    protected JGoRectangle columnRect;
    private BasicCellArea numberArea;
    private BasicCellArea cellArea;

    /** Creates a new instance of ColumnGraphNode */
    public TableGraphNode() {
        super();
        this.setResizable(false);
        this.setSelectable(false);
        this.setUpdateGuiInfo(false);
    }

    public TableGraphNode(SQLJoinTable table) {
        this();
        this.setDataObject(table);
        SourceTable sTable = table.getSourceTable();

        this.setToolTipText(sTable.getQualifiedName());

        //set up the number area
        numberArea = new BasicCellArea("?");
        numberArea.setSelectable(false);
        numberArea.setResizable(false);

        numberArea.drawBoundingRect(true);
        numberArea.setTextAlignment(JGoText.ALIGN_CENTER);
        numberArea.setLeftGap(3);
        numberArea.setIconTextGap(0);
        numberArea.setInsets(new Insets(1, 0, 1, 1));
        numberArea.setSize(numberArea.getMinimumWidth(), numberArea.getMinimumHeight());
        numberArea.setBorder(JGoPen.black);
        numberArea.setBackGroundColor(new Color(241, 240, 227));
        this.addObjectAtTail(numberArea);

        cellArea = new BasicCellArea(BasicCellArea.RIGHT_PORT_AREA, sTable.getQualifiedName());
        cellArea.setSelectable(false);
        cellArea.setResizable(false);

        cellArea.drawBoundingRect(true);
        this.addObjectAtHead(cellArea);
        //this is a hack to add 1 in width to forec this to repaint and all its children
        //so that BasicCellArea can draw a bounding rect around it
        this.setSize(this.getMinimumWidth(), this.getMinimumHeight());
    }

    public TableGraphNode(String tableName) {
        this();
        this.setDataObject(null);

        this.setToolTipText(tableName);

        //set up the number area
        numberArea = new BasicCellArea("100");
        numberArea.setSelectable(true);
        numberArea.setResizable(true);

        numberArea.drawBoundingRect(true);
        numberArea.setTextAlignment(JGoText.ALIGN_CENTER);
        numberArea.setLeftGap(3);
        numberArea.setIconTextGap(0);
        numberArea.setInsets(new Insets(1, 0, 1, 1));
        numberArea.setSize(numberArea.getMinimumWidth(), numberArea.getMinimumHeight());
        numberArea.setBorder(JGoPen.black);
        numberArea.setBackGroundColor(new Color(241, 240, 227));
        this.addObjectAtTail(numberArea);

        cellArea = new BasicCellArea(BasicCellArea.RIGHT_PORT_AREA, tableName);
        cellArea.setSelectable(true);
        cellArea.setResizable(true);

        cellArea.drawBoundingRect(true);
        this.addObjectAtHead(cellArea);
        //this is a hack to add 1 in width to forec this to repaint and all its children
        //so that BasicCellArea can draw a bounding rect around it
        this.setSize(this.getMinimumWidth(), this.getMinimumHeight());
    }

    /**
     * get the minimum height
     *
     * @return min height
     */
    @Override
    public int getMinimumHeight() {
        return cellArea.getMinimumHeight();
    }

    /**
     * get the minimum width
     *
     * @return min width
     */
    @Override
    public int getMinimumWidth() {
        return cellArea.getMinimumWidth() + numberArea.getMinimumWidth();
    }

    /**
     * Lays out this area's child objects.
     */
    @Override
    public void layoutChildren() {
        Rectangle rect = this.getBoundingRect();
        numberArea.setBoundingRect(rect.x, rect.y, numberArea.getMinimumWidth(), rect.height);
        cellArea.setBoundingRect(rect.x + numberArea.getWidth(), rect.y, cellArea.getMinimumWidth() + 1, rect.height);
    }

    /**
     * get output graph port , given a field name
     *
     * @param fieldName field name
     * @return graph port
     */
    @Override
    public IGraphPort getOutputGraphPort(String fieldName) {
        if (cellArea != null) {
            return cellArea.getRightGraphPort();
        }
        return null;
    }

    /**
     * Gets field name associated with the given port
     *
     * @param graphPort graph port
     * @return field name
     */
    @Override
    public String getFieldName(IGraphPort graphPort) {
        SQLJoinTable joinTable = (SQLJoinTable) this.getDataObject();
        SourceTable sourceTable = joinTable.getSourceTable();

        if (sourceTable != null) {
            return sourceTable.toString();
        }

        return null;
    }

    /**
     * Gets a list of all input and output links
     *
     * @return list of input links
     */
    @Override
    public List<JGoLink> getAllLinks() {
        ArrayList<JGoLink> list = new ArrayList<JGoLink>();
        if (cellArea != null) {
            IGraphPort port = cellArea.getRightGraphPort();
            addLinks(port, list);
        }

        return list;
    }

    public void setNumber(String num) {
        this.numberArea.setText(num);
        this.setSize(this.getMinimumWidth(), this.getMinimumHeight());
    }
}