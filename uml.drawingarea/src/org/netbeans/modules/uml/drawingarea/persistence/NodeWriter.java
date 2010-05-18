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

import java.awt.Dimension;
import java.awt.Point;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.modules.uml.drawingarea.persistence.util.XMIConstants;

/**
 *
 * @author Jyothi
 */
public class NodeWriter extends Writer {

    protected String diagramName;
    protected String diagramNamespace;
    protected String projectID;
    protected String anchorType;
    private String documentation;
    protected double zoom;
    protected Point viewport;
//    private String xmiid; // a random ID
    protected boolean rootNode;
    private HashMap<Anchor, String> anchorEdgeMap = new HashMap();
    protected HashMap connectorAttrs = new HashMap();
    private List<HashMap> diagProperties = new ArrayList<HashMap>();
    private HashMap diagramAttrs = new HashMap();

    public NodeWriter(BufferedWriter writer) {
        super(writer);
//        this.bw = writer;
    }

    public void setAnchorType(String anchorType) {
        this.anchorType = anchorType;
    }

    public void setProjectID(String projectID) {
        this.projectID = projectID;
    }

    public void setRootNode(boolean rootNode) {
        this.rootNode = rootNode;
    }

    public void setDiagramName(String diagramName) {
        this.diagramName = diagramName;
    }

    public void setDiagramNamespace(String diagramNamespace) {
        this.diagramNamespace = diagramNamespace;
    }

    public void setViewport(Point viewport) {
        this.viewport = viewport;
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
    }

    public void beginWriting() {
        if (rootNode) {
            beginScene();
        }
    }

    public void endWriting() {
        if (rootNode) {
            endScene(bw);
        }
    }

    //add anchor location and edge id to the map
    public void addAnchorEdge(Anchor anchor, String edgeID) {
        anchorEdgeMap.put(anchor, edgeID);
    }
    public void clearAnchorEdgeMap() {
        anchorEdgeMap.clear();
    }

    public void writeAnchorage() {
        //begin anchor
        XMIWriter.writeBeginElement(bw, XMIConstants.UML_GRAPHELEMENT_ANCHORAGE);
        //loop thru all the anchors..
        HashMap<Anchor, String> map = anchorEdgeMap;
        for (Anchor anchor : map.keySet()) {
            //begin connector with xmiid
            XMIWriter.writeElement(bw, XMIConstants.UML_GRAPHCONNECTOR, getConnectorAttrs(anchor));
            //write connector position
            XMIWriter.writeGraphElementPosition(bw, "GRAPHCONNECTOR", anchor.getRelatedSceneLocation());
            //write idref
            XMIWriter.writeGraphConnectorIDREF(bw, "GraphEdge", map.get(anchor)); //PEID of the edge
            //end connector
            XMIWriter.writeEndElement(bw, XMIConstants.UML_GRAPHCONNECTOR);
        }
        //end anchor
        XMIWriter.writeEndElement(bw, XMIConstants.UML_GRAPHELEMENT_ANCHORAGE);
    }

    //scene writing
    private void beginScene() {
        XMIWriter.writeStartDocument(bw);
//        write xmi content
        XMIWriter.writeBeginElement(bw, XMIConstants.XMI_CONTENT);
//        write diagram info
        XMIWriter.writeElement(bw, XMIConstants.UML_DIAGRAM, getDiagramAttrs());
        writeDiagramInfo();
        XMIWriter.writeBeginElement(bw, XMIConstants.UML_GRAPHELEMENT_CONTAINED);
    }

    private void writeDiagramInfo() {
        writePositionSize();
        // write diagram viewport
        XMIWriter.writeDiagramViewport(bw, viewport);
        //write namespace as custom-property
        XMIWriter.writeProperties(bw, getDiagProperties());
        //write graph elemnet symantic model
        XMIWriter.writeSimpleSymanticModel(bw, getSimpleSymanticModelEltAttrs());
    }

    public void endScene(BufferedWriter bw) {
        XMIWriter.writeEndElement(bw, XMIConstants.UML_GRAPHELEMENT_CONTAINED);
//        write diagram owner
        XMIWriter.writeDiagramOwner(bw);
        XMIWriter.writeEndElement(bw, XMIConstants.UML_DIAGRAM); // </UML:Diagram>
        XMIWriter.writeEndElement(bw, XMIConstants.XMI_CONTENT); // </XMI.content
        XMIWriter.writeEndDocument(bw); // </XMI>
    }

//attribute methods
    public void writeName() {
        setTypeInfo("Name");
        setTempPositionLocationPEID();
        beginGraphNode();
        endGraphNode();
    }

    public void writeTypeSeperator() {
        this.setTypeInfo("TypeSeperator");
        setTempPositionLocationPEID();
        beginGraphNode();
        endGraphNode();
    }

    public void writeVisibility() {
        setTypeInfo("Visibility");
        setTempPositionLocationPEID();
        beginGraphNode();
        endGraphNode();
    }

    public void beginStructuralFeature() {
        setTypeInfo("StructuralFeatureType");
        beginGraphNode();
        beginContained();
    }

    public void endStructuralFeature() {
        endContained();
        endGraphNode();
    }

    public void beginDataType() {
        beginGraphNodeWithModelBridge();
        beginContained();
    }

    public void endDataType() {
        endContained();
        endGraphNode();
    }

    public void writeParameterStart() {
        setTypeInfo("ParameterStart");
        setTempPositionLocationPEID();
        beginGraphNode();
        endGraphNode();
    }

    public void writeParameterEnd() {
        setTypeInfo("ParameterEnd");
        setTempPositionLocationPEID();
        beginGraphNode();
        endGraphNode();
    }

    public void beginParameter() {
        beginGraphNodeWithModelBridge();
        beginContained();
    }

    public void endParameter() {
        endContained();
        endGraphNode();
    }

    public void writeParameterSeperator() {
        this.setTypeInfo("ParameterSeperator");
        setTempPositionLocationPEID();
        beginGraphNode();
        endGraphNode();
    }

    private void setTempPositionLocationPEID() {
        this.setLocation(new Point(0, 0));
        this.setSize(new Dimension(0, 0));
//        this.setPEID(PersistenceUtil.getPEID(null));
        this.setPEID("PEID");
    }

    @SuppressWarnings(value = "unchecked")
    private List<HashMap> getDiagProperties() {
        diagProperties = new ArrayList<HashMap>();
        //First put namespace and project id props
        HashMap nsProp = new HashMap();
        nsProp.put(XMIConstants.KEY, "netbeans-diagram-namespace");
        nsProp.put(XMIConstants.VALUE, diagramNamespace);
        diagProperties.add(nsProp);
        HashMap prjIDProp = new HashMap();
        prjIDProp.put(XMIConstants.XMI_ID, "Property2_PEID");
        prjIDProp.put(XMIConstants.KEY, "netbeans-diagram-projectID");
        prjIDProp.put(XMIConstants.VALUE, projectID);
        diagProperties.add(prjIDProp);
        //now add other misc props..
        if (!properties.isEmpty()) {
            for (Iterator<String> it1 = properties.keySet().iterator(); it1.hasNext();) {
                String key = it1.next();
                HashMap prop1 = new HashMap();
                prop1.put(XMIConstants.KEY, key);
                prop1.put(XMIConstants.VALUE, properties.get(key));
                diagProperties.add(prop1);
            }
        }
        return diagProperties;
    }

    @SuppressWarnings(value = "unchecked")
    private HashMap getDiagramAttrs() {
        if(diagramAttrs.size()==0)
        {
            diagramAttrs.put(XMIConstants.XMI_ID, PEID);
            diagramAttrs.put(XMIConstants.ISVISIBLE, visible);
            diagramAttrs.put(XMIConstants.NAME, diagramName);
            diagramAttrs.put(XMIConstants.ZOOM, zoom);
            diagramAttrs.put(XMIConstants.DOCUMENTATION,documentation);
        }
        return diagramAttrs;
    }
   
    @SuppressWarnings(value = "unchecked")
    HashMap getConnectorAttrs( Anchor anchor) {
        connectorAttrs.put(XMIConstants.XMI_ID, PersistenceUtil.findAnchor(anchor));
        return connectorAttrs;
    }

    /**
     * @return the documentation
     */
    public String getDocumentation() {
        return documentation;
    }

    /**
     * @param documentation the documentation to set
     */
    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }
}
