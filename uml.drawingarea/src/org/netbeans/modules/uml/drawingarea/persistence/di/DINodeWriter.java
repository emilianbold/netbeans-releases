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

package org.netbeans.modules.uml.drawingarea.persistence.di;

import java.awt.Dimension;
import java.awt.Point;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.modules.uml.drawingarea.persistence.NodeWriter;
import org.netbeans.modules.uml.drawingarea.persistence.PersistenceUtil;
import org.netbeans.modules.uml.drawingarea.persistence.util.XMIConstants;

/**
 *
 * @author jyothi
 */
public class DINodeWriter extends NodeWriter {
    
    public DINodeWriter(BufferedWriter writer) {
        super(writer);
    }
    
    @Override
    public void beginWriting() {
        if (rootNode) {
            beginScene();
        }
    }

    @Override
    public void endWriting() {
        if (rootNode) {
            endScene(bw);
        }
    }
    
    private void beginScene() {
        DIXMIWriter.writeStartDocument(bw);
//        write the header
        writeHeader();
//        write xmi content
        DIXMIWriter.writeBeginElement(bw, XMIConstants.XMI_CONTENT);
//        write model.. TODO: link the model xmi here..
//        write diagram info
        DIXMIWriter.writeElement(bw, XMIConstants.UML_DIAGRAM, getDiagramAttrs());
        writeDiagramInfo();
        DIXMIWriter.writeBeginElement(bw, XMIConstants.UML_GRAPHELEMENT_CONTAINED);
    }

    private void writeHeader() {
        DIXMIWriter.writeBeginElement(bw, XMIConstants.XMI_HEADER); //<XMI.header>
//        TODO: write documentation..
        DIXMIWriter.writeEndElement(bw, XMIConstants.XMI_HEADER); //</XMI.header>
    }

    private void writeDiagramInfo() {
        writePositionSize();
        // write diagram viewport
        DIXMIWriter.writeDiagramViewport(bw, viewport);
        //write namespace as custom-property
        DIXMIWriter.writeProperties(bw, getDiagProperties());
        //write graph elemnet symantic model
        DIXMIWriter.writeSimpleSymanticModel(bw, getSimpleSymanticModelEltAttrs());
    }

    public void endScene(BufferedWriter bw) {
        DIXMIWriter.writeEndElement(bw, XMIConstants.UML_GRAPHELEMENT_CONTAINED);
//        write diagram owner
        DIXMIWriter.writeDiagramOwner(bw);
        DIXMIWriter.writeEndElement(bw, XMIConstants.UML_DIAGRAM); // </UML:Diagram>
        DIXMIWriter.writeEndElement(bw, XMIConstants.XMI_CONTENT); // </XMI.content
        DIXMIWriter.writeEndDocument(bw); // </XMI>
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
        HashMap prop1 =  new HashMap();
        prop1.put(XMIConstants.XMI_ID, "Property1_PEID");
        prop1.put(XMIConstants.KEY, "netbeans-diagram-namespace");
        prop1.put(XMIConstants.VALUE, diagramNamespace);
        diagProperties.add(prop1);
        
        HashMap prop2 = new HashMap();
        prop2.put(XMIConstants.XMI_ID, "Property2_PEID");
        prop2.put(XMIConstants.KEY, "netbeans-diagram-projectID");
        prop2.put(XMIConstants.VALUE, projectID);
        diagProperties.add(prop2);
        
        return diagProperties;
    }

    @SuppressWarnings(value = "unchecked")
    private HashMap getDiagramAttrs() {
        diagramAttrs.put(XMIConstants.XMI_ID, PEID);
        diagramAttrs.put(XMIConstants.ISVISIBLE, visible);
        diagramAttrs.put(XMIConstants.NAME, diagramName);
        diagramAttrs.put(XMIConstants.ZOOM, zoom);
        return diagramAttrs;
    }
    private List<HashMap> diagProperties = new ArrayList<HashMap>();
    private HashMap diagramAttrs = new HashMap();

    @SuppressWarnings(value = "unchecked")
    HashMap getConnectorAttrs(Anchor anchor) {
        connectorAttrs.put(XMIConstants.XMI_ID, PersistenceUtil.findAnchor(anchor));
        return connectorAttrs;
    }

}
