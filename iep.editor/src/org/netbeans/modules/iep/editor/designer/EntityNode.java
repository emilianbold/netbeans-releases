/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.iep.editor.designer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import javax.swing.ImageIcon;

import org.netbeans.modules.iep.editor.model.NameGenerator;
import org.netbeans.modules.tbls.editor.ps.TcgComponentNode;
import org.netbeans.modules.iep.model.Documentation;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.OperatorComponentContainer;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.iep.model.SchemaAttribute;
import org.netbeans.modules.iep.model.SchemaComponent;
import org.netbeans.modules.iep.model.SchemaComponentContainer;
import org.netbeans.modules.tbls.model.TcgModelManager;
import org.netbeans.modules.tbls.model.TcgComponent;
import org.netbeans.modules.tbls.model.TcgComponentType;
import org.netbeans.modules.tbls.model.TcgModelConstants;
import org.openide.nodes.Node;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;

import com.nwoods.jgo.JGoArea;
import com.nwoods.jgo.JGoCopyEnvironment;
import com.nwoods.jgo.JGoImage;
import com.nwoods.jgo.JGoListPosition;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPort;
import com.nwoods.jgo.JGoView;
//
// Properties include Text, ActivityType, and ID.
// The Text property is actually just the SimpleNode's Label's Text.
//
// This class also supports a standard set of icons (as JGoImages)
// and the notion of a standard size for the node.
public class EntityNode extends SimpleNode
        implements GuiConstants, PropertyChangeListener, CanvasWidget {

    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(EntityNode.class.getName());
    private static Dimension mStdSize = new Dimension(32, 32);
    private static Point mStdPoint = new Point();  // don't care

    public static Dimension getStdSize() {
        return mStdSize;
    }

    public static void setStdSize(int w, int h) {
        mStdSize.width = w;
        mStdSize.height = h;
    }    //private transient TcgComponent mComponent;
    private OperatorComponent mComp;

    public EntityNode() {
        super();
    }

    public EntityNode(OperatorComponent operator) {
        super();
        this.mComp = operator;

        Point dc = new Point(operator.getInt(PROP_X), operator.getInt(PROP_Y));

        // create an input port and an output port, each instances of SimpleNodePort
        String type = (String) this.mComp.getInputType().getType();
        boolean hasInput = type.equals(IO_TYPE_STREAM) || type.equals(IO_TYPE_RELATION);
        type = (String) this.mComp.getOutputType().getType();
        boolean hasOutput = type.equals(IO_TYPE_STREAM) || type.equals(IO_TYPE_RELATION) || type.equals(IO_TYPE_TABLE);

        super.initialize(dc, getStdSize(), getImage(), getLabelString(), hasInput, hasOutput);

        Documentation documentation = this.mComp.getDocumentation();
        if (documentation != null) {
            getDocumentationNode().setVisible(true);
            getDocumentationNode().setToolTipText(documentation.getTextContent());
        }


    }

    public JGoObject copyObject(JGoCopyEnvironment env) {
        EntityNode node = (EntityNode) super.copyObject(env);
        node.mComp = this.mComp;

        return node;
    }

    protected void copyChildren(JGoArea newarea, JGoCopyEnvironment env) {
        EntityNode node = (EntityNode) newarea;
        node.mComp = this.mComp;
        super.copyChildren(newarea, env);

    }

    public JGoImage getImage() {
        JGoImage image = new JGoImage(mStdPoint, getStdSize());
        //TcgComponentType ct = mComponent.getType();

        try {
            TcgComponentType componentType = TcgModelManager.getTcgComponentType(this.mComp.getType());
            if (componentType != null) {
                ImageIcon icon = componentType.getIcon();
                if (icon == null) {
                    icon = TcgModelConstants.UNKNOWN_ICON;
                }

                image.loadImage(icon.getImage(), true);
            }
        //mIcon = ImageUtil.getImageIcon(mIconName);

        } catch (Throwable e) {
            // loadImage method throws null pointer exception from within
            // MediaTracker.waitForID(0). This exception must be caught
            // so that Plan can be opened
            // java.lang.NullPointerException
            // at java.awt.ImageMediaEntry.getStatus(MediaTracker.java:872)
            // at java.awt.MediaTracker.statusID(MediaTracker.java:669)
            // at java.awt.MediaTracker.waitForID(MediaTracker.java:617)
            // at java.awt.MediaTracker.waitForID(MediaTracker.java:586)
            // at com.nwoods.jgo.JGoImage.waitForImage(JGoImage.java:187)
            // at com.nwoods.jgo.JGoImage.loadImage(JGoImage.java:110)        
        }
        return image;
    }

    public String getLabelString() {
        return mComp.getString(PROP_NAME);
    }

    private Color getLabelColor() {
        return Color.black;
    }

    public PdModel getDoc() {
        return (PdModel) getDocument();
    }
    // You'll probably want to replace this with somewhat more interesting information
    public String getToolTipText() {
        String msg = "";
        try {
            msg = (String) mComp.getProperty(NAME_KEY).getValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msg;
    }
    // Events
    public boolean doMouseClick(int modifiers, Point dc, Point vc, JGoView view) {
        mouseSelect(view);
        return true;
    }

    public void mouseSelect(JGoView view) {
        if (view instanceof PlanCanvas) {
            PlanCanvas pdCanvas = (PlanCanvas) view;
            //PlanCanvas.getDesigner().showPropertyPane(mComponent, (PdCanvas)view);

            TopComponent tc = TopComponent.getRegistry().getActivated();
            if (tc != null && tc instanceof CloneableTopComponent) {
                Node node = getPropertyNode();
                tc.setActivatedNodes(new Node[]{node});
            }
        }
    }

    public void refreshProperties() {
        TopComponent tc = TopComponent.getRegistry().getActivated();
        if (tc != null && tc instanceof CloneableTopComponent) {
            Node node = getPropertyNode();
            tc.setActivatedNodes(new Node[]{node});
        }
    }

    public Node getPropertyNode() {
        return new TcgComponentNode(getModelComponent(), getModelComponent().getModel(), (PlanCanvas) getView());
    }

    public boolean downstreamNodeContainsFlag(int nFlag) {
        //recurse looking for a node containing nFlag
        if ((getFlags() & nFlag) != 0) {
            return true;
        }
        JGoPort outputPort = getOutputPort();
        if (outputPort == null) {
            return false;
        }
        for (JGoListPosition pos = outputPort.getFirstLinkPos();
                pos != null;
                pos = outputPort.getNextLinkPos(pos)) {
            Link outputLink = (Link) outputPort.getLinkAtPos(pos);
            EntityNode toNode = (EntityNode) outputLink.getToNode();
            if (toNode.downstreamNodeContainsFlag(nFlag)) {
                return true;
            }
        }
        return false;
    }


    public void updateDownstreamNodes() {
        PdModel model = getDoc();
        if (model != null && model.isReloading()) {
            return;
        }

        if (mComp == null) {
            // This happens when this node, its upstream node, and the link between
            // them are copied from PdCans to Clipboard. See SimpleNodePort.linkChanged
            return;
        }

        if (mComp.getModel() != null) {
            updateDownstreamToNode();
        }

        // Then call updateDownstreamNodes() on all direct downstream nodes to force
        // recursive updates of all downstream nodes.
        JGoPort outputPort = getOutputPort();
        if (outputPort != null) {
            for (JGoListPosition pos = outputPort.getFirstLinkPos(); pos != null; pos = outputPort.getNextLinkPos(pos)) {
                Link outputLink = (Link) outputPort.getLinkAtPos(pos);
                EntityNode toNode = (EntityNode) outputLink.getToNode();
                toNode.updateDownstreamNodes();
            }
        }
    }

    private void updateDownstreamToNode() {
        // Compute this node's properties by using 
        // all direct upstream node's properties 
        JGoPort inputPort = getInputPort();
        if (inputPort != null) {
            try {
                List<String> inputIdList = new ArrayList<String>();
                List<String> inputSchemaIdList = new ArrayList<String>();
                List<String> staticInputIdList = new ArrayList<String>();
                boolean isSchemaOwner = false;
                isSchemaOwner = mComp.getBoolean(PROP_IS_SCHEMA_OWNER);

                boolean isRelationInputStatic = isRelationInputStatic();

                int maxTopoScore = 0;
                for (JGoListPosition pos = inputPort.getFirstLinkPos(); pos != null; pos = inputPort.getNextLinkPos(pos)) {
                    Object o = inputPort.getLinkAtPos(pos);
                    if (!(o instanceof Link)) {
                        continue;
                    }
                    Link inputLink = (Link) inputPort.getLinkAtPos(pos);
                    EntityNode fromNode = inputLink.getFromNode();
                    OperatorComponent fromComp = fromNode.mComp;

                    String outputType = fromComp.getOutputType().getType();
                    String id = fromComp.getString(PROP_ID);
                    if (outputType.equals(IO_TYPE_TABLE)) {
                        staticInputIdList.add(id);
                    } else if (outputType.equals(IO_TYPE_RELATION) && isRelationInputStatic) {
                        staticInputIdList.add(id);
                    } else {
                        inputIdList.add(id);
                    }


                    String outputSchemaId = fromComp.getString(PROP_OUTPUT_SCHEMA_ID).trim();
                    if (outputSchemaId != null && !outputSchemaId.equals("") &&
                            (outputType.equals(IO_TYPE_STREAM) || (outputType.equals(IO_TYPE_RELATION) && !isRelationInputStatic))) {
                        inputSchemaIdList.add(outputSchemaId);
                    }

                    int topoScore = fromComp.getInt(PROP_TOPO_SCORE);
                    maxTopoScore = Math.max(maxTopoScore, topoScore);
                }
                mComp.getModel().startTransaction();

                StringBuffer newInputIdListSb = new StringBuffer();
                for (int i = 0; i < inputIdList.size(); i++) {
                    if (i > 0) {
                        newInputIdListSb.append("\\");
                    }
                    newInputIdListSb.append(inputIdList.get(i));
                }
                StringBuffer newInputSchemaIdListSb = new StringBuffer();
                for (int i = 0; i < inputSchemaIdList.size(); i++) {
                    if (i > 0) {
                        newInputSchemaIdListSb.append("\\");
                    }
                    newInputSchemaIdListSb.append(inputSchemaIdList.get(i));
                }
                StringBuffer newStaticInputIdListSb = new StringBuffer();
                for (int i = 0; i < staticInputIdList.size(); i++) {
                    if (i > 0) {
                        newStaticInputIdListSb.append("\\");
                    }
                    newStaticInputIdListSb.append(staticInputIdList.get(i));
                }

                //there is a loop when we set inputIdList etc
                //which triggers call to this method again 
                //see PlanCanvas where this method is invoked when
                //Property value is updated.
                //to avoid loop we set property only if we have a new value
                String inputIdListStr = mComp.getString(PROP_INPUT_ID_LIST);
                String inputSchemaIdListStr = mComp.getString(PROP_INPUT_SCHEMA_ID_LIST);
                String staticInputListStr = mComp.getString(PROP_STATIC_INPUT_ID_LIST);
                String outputSchemaIdStr = mComp.getString(PROP_OUTPUT_SCHEMA_ID);

                if (!newInputIdListSb.toString().equals(inputIdListStr)) {
                    mComp.setString(PROP_INPUT_ID_LIST, newInputIdListSb.toString());
                }

                if (!newInputSchemaIdListSb.toString().equals(inputSchemaIdListStr)) {
                    mComp.setString(PROP_INPUT_SCHEMA_ID_LIST, newInputSchemaIdListSb.toString());
                }

                if (!newStaticInputIdListSb.toString().equals(staticInputListStr)) {
                    mComp.setString(PROP_STATIC_INPUT_ID_LIST, newStaticInputIdListSb.toString());
                }

                mComp.setInt(PROP_TOPO_SCORE, maxTopoScore + 1);
                if (!isSchemaOwner) {
                    if (inputSchemaIdList.size() > 0) {
                        if (!inputSchemaIdList.get(0).equals(outputSchemaIdStr)) {
                            mComp.setString(PROP_OUTPUT_SCHEMA_ID, inputSchemaIdList.get(0));
                        }
                    } else {
                        if (!outputSchemaIdStr.equals("")) {
                            mComp.setString(PROP_OUTPUT_SCHEMA_ID, "");
                        }
                    }
                }

                mComp.getModel().endTransaction();

            } catch (Exception e) {
                e.printStackTrace();
                mLog.warning(e.getMessage());
            }
        }


    }

    public TcgComponent getComponent() {
        return null;
    }

    public void releaseComponent() {
        mComp = null;
    }

    public String getId() {
        String id = "";
        try {
            id = mComp.getString(PROP_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }

    public String getFrom() {
        return mComp.getString(PROP_FROM);
    }
    
    public String getTo() {
        return mComp.getString(PROP_TO);
    }
    
    public void propertyChange(PropertyChangeEvent e) {
        Object src = e.getSource();
        if (src instanceof TcgComponent) {
            if (e.getPropertyName().equals(NAME_KEY)) {
                updateLabelText();
                return;
            }
        }
    }

    private void updateLabelText() {
        getLabel().setText(getLabelString());
        layoutChildren(getLabel());
    }

    public int getInputCount() {
        return mComp.getInputOperatorList().size();
    }

    public int getInputMaxCount() {
        return mComp.getInt(PROP_INPUT_MAX_COUNT);
    }


    public int getStaticInputCount() {
        List staticInputs = mComp.getStaticInputList();//getProperty(PROP_STATIC_INPUT_ID_LIST).getValue();
        return staticInputs.size();

    }

    public int getStaticInputMaxCount() {
        return mComp.getInt(PROP_STATIC_INPUT_MAX_COUNT);
    }

    public String getInputType() {
        try {
            return mComp.getInputType().getType();
        } catch (Exception e) {
            //e.printStackTrace();
            mLog.warning("Exception:" + e.getMessage());
        }
        return IO_TYPE_NONE;
    }

    public boolean isRelationInputStatic() {
        return mComp.getBoolean(PROP_IS_RELATION_INPUT_STATIC);
    }

    public String getOutputType() {
        try {
            return mComp.getOutputType().getType();
        } catch (Exception e) {
            //e.printStackTrace();
            mLog.warning("Exception:" + e.getMessage());
        }
        return IO_TYPE_NONE;
    }

    public OperatorComponent copyObjectAndResetContextProperties(IEPModel targetModel) {

        OperatorComponent operator = this.getModelComponent();
        OperatorComponentContainer opContainer = targetModel.getPlanComponent().getOperatorComponentContainer();

        OperatorComponent newOperator = targetModel.getFactory().createOperator(targetModel, operator.getType());

        String operatorId = operator.getString(PROP_ID);
        String operatorDisplayName = operator.getString(PROP_NAME);

        if (opContainer.findChildComponent(operatorId) != null) {
            operatorId = NameGenerator.generateId(opContainer, "o");
        }

        if (opContainer.findOperator(operatorDisplayName) != null) {
            operatorDisplayName = NameGenerator.generateNewName(opContainer, operator.getComponentType());
        }

        newOperator.setString(PROP_NAME, operatorDisplayName);
        newOperator.setString(PROP_ID, operatorId);
        newOperator.setName(operatorId);
        newOperator.setTitle(operatorId);

        newOperator.setInt(PROP_X, operator.getInt(PROP_X));
        newOperator.setInt(PROP_Y, operator.getInt(PROP_Y));

        Documentation doc = operator.getDocumentation();
        if (doc != null) {
            Documentation newDoc = targetModel.getFactory().createDocumentation(targetModel);
            newDoc.setTextContent(doc.getTextContent());
            newOperator.setDocumentation(newDoc);
        }

        try {

            // isSchemaOwner? new outputSchemaId : reset outputSchemaId
            boolean isSchemaOwner = operator.getBoolean(PROP_IS_SCHEMA_OWNER);
            if (isSchemaOwner) {
                SchemaComponent outputSchemaComp = operator.getOutputSchema();
                if (outputSchemaComp != null) {
                    SchemaComponentContainer scContainer = targetModel.getPlanComponent().getSchemaComponentContainer();

                    SchemaComponent newSCComp = copySchemaComponent(outputSchemaComp, targetModel);
                    scContainer.addSchemaComponent(newSCComp);

                    newOperator.setString(PROP_OUTPUT_SCHEMA_ID, newSCComp.getName());
                }
            }
        } catch (Exception e) {
            mLog.log(Level.SEVERE, "copyChildren failed", e);
        }
        return newOperator;

    }

    private SchemaComponent copySchemaComponent(SchemaComponent component, IEPModel targetModel) {
        SchemaComponent sComponent = targetModel.getFactory().createSchema(targetModel);
        SchemaComponentContainer scContainer = targetModel.getPlanComponent().getSchemaComponentContainer();

        String schemaName = component.getName();
        if (scContainer.findSchema(schemaName) != null) {
            schemaName = NameGenerator.generateSchemaName(scContainer);
        }

        sComponent.setName(schemaName);
        sComponent.setType(component.getType());
        sComponent.setTitle(schemaName);

        Documentation scDoc = component.getDocumentation();
        if (scDoc != null) {
            Documentation doc = targetModel.getFactory().createDocumentation(targetModel);
            doc.setTextContent(scDoc.getTextContent());
            sComponent.setDocumentation(doc);
        }

        List<Property> properties = component.getProperties();
        Iterator<Property> itP = properties.iterator();

        while (itP.hasNext()) {
            Property property = itP.next();
            Property newP = copyProperty(property, targetModel);
            sComponent.addProperty(newP);
        }

        List<SchemaAttribute> attributes = component.getSchemaAttributes();
        Iterator<SchemaAttribute> itSA = attributes.iterator();

        while (itSA.hasNext()) {
            SchemaAttribute attr = itSA.next();
            SchemaAttribute newAttr = copySchemaAttribute(attr, targetModel);
            sComponent.addSchemaAttribute(newAttr);
        }

        return sComponent;
    }

    private SchemaAttribute copySchemaAttribute(SchemaAttribute attribute, IEPModel targetModel) {
        SchemaAttribute sAttribute = targetModel.getFactory().createSchemaAttribute(targetModel);

        sAttribute.setName(attribute.getName());
        sAttribute.setType(attribute.getType());
        sAttribute.setTitle(attribute.getTitle());
        sAttribute.setAttributeName(attribute.getAttributeName());
        sAttribute.setAttributeType(attribute.getAttributeType());
        sAttribute.setAttributeScale(attribute.getAttributeScale());
        sAttribute.setAttributeSize(attribute.getAttributeSize());
        sAttribute.setAttributeComment(attribute.getAttributeComment());

        return sAttribute;
    }

    private Property copyProperty(Property property, IEPModel targetModel) {
        Property sProperty = targetModel.getFactory().createProperty(targetModel);
        sProperty.setName(property.getName());
        sProperty.setValue(property.getValue());

        return property;
    }

    public OperatorComponent getModelComponent() {
        return this.mComp;
    }

    @Override
    public void moveChildren(Rectangle prevRect) {
        super.moveChildren(prevRect);
    }

    public void refreshOperator() {
        //display name
        String operatorDisplayName = this.mComp.getString(PROP_NAME);
        setOperatorDisplayName(operatorDisplayName);

        //x, y location
        Point newLoc = new Point(this.mComp.getInt(PROP_X), this.mComp.getInt(PROP_Y));
        if (!newLoc.equals(getLocation())) {
            setTopLeft(newLoc);
        }
    }

    public void refresh() {
        if (getDocumentationNode() == null) {
            return;
        }

        Documentation doc = this.mComp.getDocumentation();
        if (doc != null && doc.getTextContent() != null && !doc.getTextContent().trim().equals("")) {
            getDocumentationNode().setVisible(true);
            getDocumentationNode().setToolTipText(doc.getTextContent());
        } else {
            getDocumentationNode().setVisible(false);
            getDocumentationNode().setToolTipText("");
        }
    }
}
