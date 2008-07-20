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
package org.netbeans.modules.uml.diagrams.nodes;

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.diagrams.DefaultWidgetContext;
import org.netbeans.modules.uml.diagrams.Util;
import org.netbeans.modules.uml.drawingarea.ModelElementChangedKind;
import org.netbeans.modules.uml.drawingarea.persistence.NodeWriter;
import org.netbeans.modules.uml.drawingarea.persistence.PersistenceUtil;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;

/**
 *
 * @author treyspiva
 */
public class OperationWidget extends FeatureWidget implements PropertyChangeListener {

    private Font lastFont;

    public OperationWidget(Scene scene) {
        super(scene);

        addToLookup(new DefaultWidgetContext("Operation"));
    }

    ///////////////////////////////////////////////////////////////
    // PropertyChangeListener Implementation
    /**
     * The property change listener is used by the diagram to notify widgets of
     * model element property change events.
     */
    public void propertyChange(PropertyChangeEvent event) {
        String eventName = event.getPropertyName();
        if (eventName.equals(ModelElementChangedKind.ELEMENTMODIFIED.toString()) == false) {
            //updateUI();update cause problem with currently selected because of label recreation
            String formatedStr = formatElement();
            if (formatedStr == null)
            {
                return;
            }
            getLabel().setLabel(formatedStr);
            setFont(getFont());
        }
        else
        {
            UMLNodeWidget parentNode=(UMLNodeWidget) Util.getParentByClass(this, UMLNodeWidget.class);
            if(parentNode!=null)
            {
                parentNode.updateSizeWithOptions();
            }
        }
    }

    @Override
    protected void notifyFontChanged(Font font) {
        IPresentationElement pe=getObject();
        if(pe==null)return;
        if(font==null)return;//will not update null font
        IOperation op=(IOperation) pe.getFirstSubject();
        
        Font applyFont=font;
        if(op.getIsAbstract() && !applyFont.isItalic())applyFont=applyFont.deriveFont(applyFont.getStyle()|Font.ITALIC);
        else if(!op.getIsAbstract() && applyFont.isItalic())applyFont=applyFont.deriveFont(applyFont.getStyle()&(Font.PLAIN|Font.BOLD));//do not need to handle/keep underlined because it should be dependent on static

        HashMap  map=new HashMap();
        if(op.getIsStatic())map.put(TextAttribute.UNDERLINE,TextAttribute.UNDERLINE_ON);//currently may work on java 6 only
        else map.put(TextAttribute.UNDERLINE,-1);
        applyFont=applyFont.deriveFont(map);
        boolean same=applyFont.equals(lastFont);
        if(same)
        {
            //rechack attributes
            same&=(applyFont.getAttributes().get(TextAttribute.UNDERLINE)==null && lastFont.getAttributes().get(TextAttribute.UNDERLINE)==null) || (applyFont.getAttributes().get(TextAttribute.UNDERLINE)!=null && applyFont.getAttributes().get(TextAttribute.UNDERLINE).equals(lastFont.getAttributes().get(TextAttribute.UNDERLINE)));
        }
        if(same)return;//font was processed by handler, don't need to apply, avoid stackoverflow
        lastFont=applyFont;//need to assign before setFont because setFont will trigger notification again
        setFont(applyFont);
    }

    @Override
    public void save(NodeWriter nodeWriter) {
//        //long save version compliant to DI-Spec
//        if (nodeWriter instanceof DINodeWriter) {
//            setNodeWriterValues(nodeWriter, this);
//
//            nodeWriter.beginGraphNodeWithModelBridge();
//            nodeWriter.beginContained();
//        //now write everthing inside the operation..
//        /* Nested Elements:
//            [StereotypeCompartment],
//            [Visibility], Name, DelimitedParameterList,
//             * [GraphNode{“TypeSeparator”}, ParameterList],
//            [PropertyCompartment]
//             */
//
//            Scene scene = getScene();
//            if (scene instanceof GraphScene) {
//                IPresentationElement oper = (IPresentationElement) ((GraphScene) scene).findObject(this);
//                if (oper != null) {
//                    setNodeWriterValues(nodeWriter, this);
//                    nodeWriter.writeVisibility();
//                    nodeWriter.writeName();
//
//                    nodeWriter.writeParameterStart();
//
//                    //write all parameters..
//                    IElement pModelElt = PersistenceUtil.getModelElement(this);
//                    if (pModelElt instanceof IOperation && pModelElt instanceof IBehavioralFeature) {
//                        List<IParameter> params = ((IBehavioralFeature) pModelElt).getFormalParameters();
//                        for (IParameter par : params) {
//                            nodeWriter.setElementType("Parameter");
//                            nodeWriter.setMEID(par.getXMIID()); //param idref
//                            nodeWriter.beginParameter();
//                            nodeWriter.writeName();
//                            nodeWriter.writeTypeSeperator();
//
//                            nodeWriter.setElementType("DataType");
//                            nodeWriter.setMEID(par.getTypeID()); //datatype idref
//                            nodeWriter.beginDataType();
//                            nodeWriter.writeName();
//                            nodeWriter.endDataType();
//
//                            nodeWriter.endParameter();
//                            nodeWriter.writeParameterSeperator(); //this should NOT appear - when only one param exist - or at the end of par list
//                        }
//                    }
//
//                    nodeWriter.writeParameterEnd();
//                    nodeWriter.writeTypeSeperator();
//
//                    //write return parameter.
//                    IParameter returnType = ((IBehavioralFeature) pModelElt).getReturnType();
//                    if (returnType != null) {
//                        nodeWriter.setElementType("Parameter");
//                        nodeWriter.setMEID(returnType.getXMIID());
//                        nodeWriter.beginParameter();
//                        nodeWriter.setElementType("DataType");
//                        nodeWriter.setMEID(returnType.getTypeID()); //datatype idref
//                        nodeWriter.beginDataType();
//                        nodeWriter.writeName();
//                        nodeWriter.endDataType();
//                        nodeWriter.endParameter();
//                    }
//                }
//            }
//            nodeWriter.endContained();
//            nodeWriter.endGraphNode();
//        } else 
        {
            //short save version
            nodeWriter = PersistenceUtil.populateNodeWriter(nodeWriter, this);
            nodeWriter.setHasPositionSize(false);
            //clear all existing properties
            PersistenceUtil.clearProperties(nodeWriter);
            //populate properties key/val
            PersistenceUtil.populateProperties(nodeWriter, this);
            nodeWriter.beginGraphNodeWithModelBridge();
            nodeWriter.beginContained();
            nodeWriter.endContained();
            nodeWriter.endGraphNode();
        }
    }

    public String getWidgetID() {
        return UMLWidgetIDString.OPERATIONWIDGET.toString();
    }
}
