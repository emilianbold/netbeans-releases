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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.uml.drawingarea.persistence;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.uml.drawingarea.persistence.util.XMIConstants;
import org.openide.util.Exceptions;

/**
 *
 * @author jyothi
 */
public class Writer {

    protected BufferedWriter bw;
    protected String MEID; // Model Element xmi-ID
    protected String PEID; // Presentation Element xmi-ID
    protected String idref; // Referring to ID within the document: eg: graph edge
    protected Point location;
    protected HashMap nodeAttrs = new HashMap();
    protected HashMap properties = new HashMap();
    protected HashMap symanticModelBridgeAttrs = new HashMap();
    protected HashMap symanticModelEltAttrs = new HashMap();
    protected String elementType; //Class, Generalization, etc..
    protected Font font;
    protected Color foreground;
    protected Color background;
    protected boolean visible;
    /* Presentation:
     * Actor - Stickman, Rectangle, UserDefined
     * Interface - Rectangle, Circle, Lillipop, UserDefined
     */
    String presentation = "";
    /* TypeInfo:
     * Compartment - AttributeCompartment; Operation Compartment; etc..
     * For Diagrams - ClassDiagram, SequenceDiagram, StateDiagram etc..
     * For Attributes - Name, Visibility, TypeSeparator, InitialValue, Multiplicity, Ordering, InterfaceCircle, InterfaceLine
     * 
     * For sqd lifelines:
     * Active part of the lifeline - active
     * Cross at the end of the lifeline - destroy
     * Header of a lifeline - header
     */
    String typeInfo;
    Dimension size;
    protected boolean hasPositionSize = false;
//    protected String viewName;

    public Writer(BufferedWriter writer) {
        this.bw = writer;
    }
    
    public BufferedWriter getBufferedWriter() {
        return this.bw;
    }

    public void setHasPositionSize(boolean hasPositionSize) {
        this.hasPositionSize = hasPositionSize;
    }

    public void beginContained() {
        XMIWriter.writeBeginElement(bw, XMIConstants.UML_GRAPHELEMENT_CONTAINED);
    }

    // child writing
    public void beginGraphNode() {
        XMIWriter.writeElement(bw, XMIConstants.UML_GRAPHNODE, getNodeAttrs());
        writePositionSize();
        //write graph elemnet symantic model
        XMIWriter.writeSimpleSymanticModel(bw, getSimpleSymanticModelEltAttrs());
    }

    // node writing
    public void beginGraphNodeWithModelBridge() {
        XMIWriter.writeElement(bw, XMIConstants.UML_GRAPHNODE, getNodeAttrs());
        writePositionSize();      
        // write properties 
        XMIWriter.writeProperties(bw, getDiagramElementProperties(properties));
        //write graph elemnet symantic model
        XMIWriter.writeSymanticModelBridge(bw, getSymanticModelBridgeAttrs(), elementType, MEID);        
    }

    public void endContained() {
        XMIWriter.writeEndElement(bw, XMIConstants.UML_GRAPHELEMENT_CONTAINED);
    }

    public void endGraphNode() {
        XMIWriter.writeEndElement(bw, XMIConstants.UML_GRAPHNODE);
    }

    public void setElementType(String elementType) {
        this.elementType = elementType;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public void setForeground(Color foreground) {
        this.foreground = foreground;
    }

    public void setIdref(String idref) {
        this.idref = idref;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public void setMEID(String MEID) {
        this.MEID = MEID;
    }

    public void setPEID(String PEID) {
        this.PEID = PEID;
    }

    public void setTempText(String string) {
        try {
            bw.write(string);
            bw.newLine();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @SuppressWarnings(value = "unchecked")
    protected HashMap getNodeAttrs() {
        nodeAttrs.put(XMIConstants.XMI_ID, PEID);
        nodeAttrs.put(XMIConstants.ISVISIBLE, visible);
        return nodeAttrs;
    }

    List<HashMap> getDiagramElementProperties(HashMap map) {
        List<HashMap> retVal = new ArrayList<HashMap>();
        if (!map.isEmpty()) {
            for (Iterator<String> it1 = map.keySet().iterator(); it1.hasNext();) {
                String key = it1.next();
                HashMap prop1 = new HashMap();
                prop1.put(XMIConstants.KEY, key);
                prop1.put(XMIConstants.VALUE, map.get(key));
                retVal.add(prop1);
            }
        }
        return retVal;
    }

    public HashMap getProperties() {
        return properties;
    }

    public void setProperties(HashMap properties) {
        this.properties = properties;
    }

    protected HashMap getSimpleSymanticModelEltAttrs() {
        symanticModelEltAttrs.put(XMIConstants.XMI_ID, PEID);
        symanticModelEltAttrs.put(XMIConstants.PRESENTATION, presentation);
        symanticModelEltAttrs.put(XMIConstants.TYPEINFO, typeInfo);
        return symanticModelEltAttrs;
    }

    protected HashMap getSymanticModelBridgeAttrs() {
        symanticModelBridgeAttrs.put(XMIConstants.XMI_ID, "xmi_id");
        symanticModelBridgeAttrs.put(XMIConstants.PRESENTATION, presentation);
        return symanticModelBridgeAttrs;
    }

    protected void writePositionSize() {
        if (hasPositionSize) {
            //  write graph element position
            XMIWriter.writeGraphElementPosition(bw, "GRAPHELEMENT", location);
            // write graph node size
            XMIWriter.writeGraphNodeSize(bw, size);
        }
    }

    public void setBackground(Color background) {
        this.background = background;
    }

    public void setPresentation(String presentation) {
        this.presentation = presentation;
    }

    public void setTypeInfo(String typeInfo) {
        this.typeInfo = typeInfo;
    }

    public String getTypeInfo() {
        return typeInfo;
    }

    public void setSize(Dimension size) {
        this.size = size;
    }
    
    public void beginDependencies()
    {
        XMIWriter.writeBeginElement(bw, XMIConstants.UML_GRAPHELEMENT_DEPENDENCIES); //
    }
    public void endDependencies()
    {
        XMIWriter.writeEndElement(bw, XMIConstants.UML_GRAPHELEMENT_DEPENDENCIES);
    }
}
