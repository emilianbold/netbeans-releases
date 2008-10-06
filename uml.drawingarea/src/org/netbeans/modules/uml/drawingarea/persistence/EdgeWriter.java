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
package org.netbeans.modules.uml.drawingarea.persistence;

import java.awt.Point;
import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.List;
import org.netbeans.modules.uml.drawingarea.persistence.util.XMIConstants;

/**
 *
 * @author Jyothi
 */
public class EdgeWriter extends Writer {

    protected String name;
    protected String anchorType;
    protected String xmiid; // a random ID
    protected List<Point> wayPoints;
    protected String srcAnchorID;
    protected String targetAnchorID;
    protected HashMap edgeAttrs = new HashMap();
    private HashMap edgeProperties = new HashMap();

    public EdgeWriter(BufferedWriter writer) {
        super(writer);
//        this.bw = writer;
    }

    public HashMap getEdgeProperties() {
        return edgeProperties;
    }

    public void setEdgeProperties(HashMap edgeProperties) {
        this.edgeProperties = edgeProperties;
    }

    public void setAnchorType(String anchorType) {
        this.anchorType = anchorType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWayPoints(List<Point> wayPoints) {
        this.wayPoints = wayPoints;
    }

    public void setXmiid(String xmiid) {
        this.xmiid = xmiid;
    }

    public void setSrcAnchorID(String srcAnchorID) {
        this.srcAnchorID = srcAnchorID;
    }

    public void setTargetAnchorID(String targetAnchorID) {
        this.targetAnchorID = targetAnchorID;
    }

    public void beginGraphEdge() {
        XMIWriter.writeElement(bw, XMIConstants.UML_GRAPHEDGE, getEdgeAttrs());

        //write graph element position
        XMIWriter.writeGraphElementPosition(bw, "GRAPHELEMENT", location);
        //write graph edge way points
        XMIWriter.writeEdgeWayPoints(bw, wayPoints);
        if (!getEdgeProperties().isEmpty())
        {
            XMIWriter.writeProperties(bw, getDiagramElementProperties(getEdgeProperties()));
        }
        //write graph elemnet symantic model
        XMIWriter.writeSymanticModelBridge(bw, getSymanticModelBridgeAttrs(), elementType, MEID);
    //write contained.
    //write edge anchors
    }

    public void endGraphEdge() {
        XMIWriter.writeEndElement(bw, XMIConstants.UML_GRAPHEDGE);
    }

    @Override
    public void beginGraphNode()
    {
        XMIWriter.writeElement(bw, XMIConstants.UML_GRAPHNODE, getNodeAttrs());
        writePositionSize();
        //do we have any properties?
        if (!getProperties().isEmpty())
        {
            XMIWriter.writeProperties(bw, getDiagramElementProperties(getProperties()));
        }
        //write graph elemnet symantic model
        XMIWriter.writeSimpleSymanticModel(bw, getSimpleSymanticModelEltAttrs());
    }

    public void writeEdgeAnchors() {
        //begin anchor tag
        XMIWriter.writeBeginElement(bw, XMIConstants.UML_GRAPHEDGE_ANCHORS);
        //write source anchor
        XMIWriter.writeEmptyElement(bw, XMIConstants.UML_GRAPHCONNECTOR, getConnectorAttrs(srcAnchorID));
        //write target anchor
        XMIWriter.writeEmptyElement(bw, XMIConstants.UML_GRAPHCONNECTOR, getConnectorAttrs(targetAnchorID));
        //end anchor tag
        XMIWriter.writeEndElement(bw, XMIConstants.UML_GRAPHEDGE_ANCHORS);
    }

    public void writeComment(String str) {
        XMIWriter.writeComment(bw, str);
    }

    @SuppressWarnings(value = "unchecked")
    private HashMap getEdgeAttrs() {
        edgeAttrs.put(XMIConstants.XMI_ID, PEID);
        edgeAttrs.put(XMIConstants.ISVISIBLE, visible);
        return edgeAttrs;
    }

    @SuppressWarnings(value = "unchecked")
    private HashMap getConnectorAttrs(String anchorID) {
        HashMap connectorAttrs = new HashMap();
        connectorAttrs.put(XMIConstants.XMI_IDREF, anchorID);
        return connectorAttrs;
    }
}
