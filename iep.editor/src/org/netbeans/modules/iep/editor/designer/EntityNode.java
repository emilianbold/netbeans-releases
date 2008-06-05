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


import org.netbeans.modules.iep.editor.model.ModelObjectFactory;
import org.netbeans.modules.iep.editor.model.NameGenerator;
import org.netbeans.modules.iep.editor.tcg.ps.TcgComponentNode;
import org.netbeans.modules.iep.editor.tcg.ps.TcgComponentNodeView;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import com.nwoods.jgo.JGoArea;
import com.nwoods.jgo.JGoCopyEnvironment;
import com.nwoods.jgo.JGoImage;
import com.nwoods.jgo.JGoListPosition;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPort;
import com.nwoods.jgo.JGoView;

import org.netbeans.modules.iep.editor.tcg.model.TcgModelManager;
import org.netbeans.modules.iep.model.Component;
import org.netbeans.modules.iep.model.Documentation;
import org.netbeans.modules.iep.model.IEPComponent;
import org.netbeans.modules.iep.model.IEPComponentFactory;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.LinkComponentContainer;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.OperatorComponentContainer;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.iep.model.SchemaAttribute;
import org.netbeans.modules.iep.model.SchemaComponent;
import org.netbeans.modules.iep.model.SchemaComponentContainer;
import org.netbeans.modules.iep.model.lib.TcgComponent;
import org.netbeans.modules.iep.model.lib.TcgModelConstants;


// Activities are implemented as SimpleNodes.
import org.netbeans.modules.iep.model.lib.TcgComponentType;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
//
// Properties include Text, ActivityType, and ID.
// The Text property is actually just the SimpleNode's Label's Text.
//
// This class also supports a standard set of icons (as JGoImages)
// and the notion of a standard size for the node.
public class EntityNode extends SimpleNode 
    implements GuiConstants, PropertyChangeListener, TcgComponentNodeView, CanvasWidget 
{
    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(EntityNode.class.getName());

    private static Dimension mStdSize = new Dimension(32, 32);

    private static Point mStdPoint = new Point();  // don't care

    
    public static Dimension getStdSize() {
        return mStdSize;
    }

    public static void setStdSize(int w, int h) {
        mStdSize.width = w;
        mStdSize.height = h;
    }


    
    //private transient TcgComponent mComponent;
    private OperatorComponent mComp;
    
    public EntityNode() {
        super();
    }
    
    public EntityNode(OperatorComponent operator) {
        super();
        this.mComp = operator;
        this.mComp.getModel().addComponentListener(new IEPModelListener());
        
        Point dc = new Point(operator.getX(), operator.getY());
      
        // create an input port and an output port, each instances of SimpleNodePort
        String type = (String)this.mComp.getProperty(INPUT_TYPE_KEY).getValue();
        boolean hasInput = type.equals(IO_TYPE_STREAM) || type.equals(IO_TYPE_RELATION);
        type = (String)this.mComp.getProperty(OUTPUT_TYPE_KEY).getValue();
        boolean hasOutput = type.equals(IO_TYPE_STREAM) || type.equals(IO_TYPE_RELATION)|| type.equals(IO_TYPE_TABLE);
        
        super.initialize(dc, getStdSize(), getImage(), getLabelString(), hasInput, hasOutput);
        
        Documentation documentation = this.mComp.getDocumentation();
        if(documentation != null) {
            getDocumentationNode().setVisible(true);
            getDocumentationNode().setToolTipText(documentation.getTextContent());
        }
        
        
    }
    
    public JGoObject copyObject(JGoCopyEnvironment env)
    {
        EntityNode node = (EntityNode) super.copyObject(env);
        node.mComp = this.mComp;
        
        return node;
    }
   
    protected void copyChildren(JGoArea newarea, JGoCopyEnvironment env) {
          EntityNode node = (EntityNode)newarea;
          node.mComp = this.mComp;
          super.copyChildren(newarea, env);
          
    }
    
    /**
     *  When called to copy from PdCanvas to Clipboard, mComponent is not null
     *  When called to copy from Clipboard to PdCanvas, mComponent is null
     */
    
//    protected void copyChildren(JGoArea newarea, JGoCopyEnvironment env) {
//        EntityNode newObj = (EntityNode)newarea;
//        
//        super.copyChildren(newarea, env);
//        /*if (mComponent == null) {
//            return;
//        }
//        try {
//            newObj.mComponentXml = mComponent.toXml();
//            boolean isSchemaOwner = mComponent.getProperty(IS_SCHEMA_OWNER_KEY).getBoolValue();
//            if (isSchemaOwner) {
//                String outputSchemaId = mComponent.getProperty(OUTPUT_SCHEMA_ID_KEY).getStringValue();
//                Schema schema = mPlan.getSchema(outputSchemaId);
//                if (schema != null) {
//                    newObj.mOutputSchemaXml = schema.toXml();
//                }
//            }
//        } catch (Exception e) {
//            mLog.log(Level.SEVERE,"copyChildren failed", e);
//        }*/
//    }
//    
    
    public JGoImage getImage() {
        JGoImage image = new JGoImage(mStdPoint, getStdSize());
        //TcgComponentType ct = mComponent.getType();
        
        try {
            TcgComponentType componentType = TcgModelManager.getTcgComponentType(this.mComp.getType());
            if(componentType != null) {
                ImageIcon icon = componentType.getIcon();
                if(icon == null) {
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
    
    /*
    public JGoImage getImage() {
        JGoImage image = new JGoImage(mStdPoint, getStdSize());
        TcgComponentType ct = mComponent.getType();
        
        try {
            image.loadImage(ct.getIcon().getImage(), true);
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
        String s = "";
        try {
//            s = (String)mComponent.getProperty(NAME_KEY).getValue()
//                + "(" + (String)mComponent.getProperty(ID_KEY).getValue() + ")";
              s = (String)mComponent.getProperty(NAME_KEY).getValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s; 
    }
    */
    
    public String getLabelString() {
        return mComp.getDisplayName();
    }
    
    /*
    private Color getLabelColor() {
        return mComponent.validate().hasError()? Color.red : Color.black;
    }
     */
    

    private Color getLabelColor() {
        return  Color.black;
    }

    
    public PdModel getDoc() {
        return (PdModel)getDocument(); 
    }
    

    // You'll probably want to replace this with somewhat more interesting information
    public String getToolTipText() {
        String msg = "";
        try {
            msg = (String)mComp.getProperty(NAME_KEY).getValue();
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
            PlanCanvas pdCanvas = (PlanCanvas)view;
            //PlanCanvas.getDesigner().showPropertyPane(mComponent, (PdCanvas)view);
          
            TopComponent tc = TopComponent.getRegistry().getActivated();
            if(tc != null) {
                Node node = new TcgComponentNode(getModelComponent(), getModelComponent().getModel(), pdCanvas);
                tc.setActivatedNodes(new Node[]{node});
            }
        }
    }
    
    public void refreshProperties() {
        TopComponent tc = TopComponent.getRegistry().getActivated();
        if(tc != null) {
            Node node = new TcgComponentNode(getModelComponent(), getModelComponent().getModel(), (PlanCanvas) getView());
            tc.setActivatedNodes(new Node[]{node});
        }
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
             pos = outputPort.getNextLinkPos(pos)) 
        {
            Link outputLink = (Link) outputPort.getLinkAtPos(pos);
            EntityNode toNode = (EntityNode) outputLink.getToNode();
            if (toNode.downstreamNodeContainsFlag(nFlag)) {
                return true;
            }
        }
        return false;
    }
    
    /*rit
    // The triggering call of this method should be
    // 1. When a link is made from some other node to this node
    // 2. When this node's output schema is defined by SchemaWizard
    // 3. When any direct upstream node is deleted
    public void updateDownstreamNodes() {
        if (mComponent == null) {
            // This happens when this node, its upstream node, and the link between
            // them are copied from PdCans to Clipboard. See SimpleNodePort.linkChanged
            return;
        }
        // Compute this node's properties by using 
        // all direct upstream node's properties 
        JGoPort inputPort = getInputPort();
        if (inputPort != null) {
            try {
                List inputIdList = new ArrayList();
                List inputSchemaIdList = new ArrayList();
                List staticInputIdList = new ArrayList();
                boolean isSchemaOwner = mComponent.getProperty(IS_SCHEMA_OWNER_KEY).getBoolValue();
                int maxTopoScore = 0;
                for (JGoListPosition pos = inputPort.getFirstLinkPos(); pos != null; pos = inputPort.getNextLinkPos(pos)) {
                    Object o = inputPort.getLinkAtPos(pos);
                    if (!(o instanceof Link)) {
                        continue;
                    }
                    Link inputLink = (Link)inputPort.getLinkAtPos(pos);
                    EntityNode fromNode= inputLink.getFromNode();
                    TcgComponent fromComponent = fromNode.mComponent;
                    
                    String outputType = fromComponent.getProperty(OUTPUT_TYPE_KEY).getStringValue();
                    String id = fromComponent.getProperty(ID_KEY).getStringValue();
                    if (outputType.equals(IO_TYPE_TABLE)) {
                        staticInputIdList.add(id);
                    } else {
                        inputIdList.add(id);
                    }
                    
                    String outputSchemaId = fromComponent.getProperty(OUTPUT_SCHEMA_ID_KEY).getStringValue().trim();
                    if (outputSchemaId != null && !outputSchemaId.equals("") && !outputType.equals(IO_TYPE_TABLE)) {
                        inputSchemaIdList.add(outputSchemaId);
                    }
                    
                    int topoScore = fromComponent.getProperty(TOPO_SCORE_KEY).getIntValue();
                    maxTopoScore = Math.max(maxTopoScore, topoScore);
                }
                mComponent.getProperty(INPUT_ID_LIST_KEY).setValue(inputIdList);
                mComponent.getProperty(INPUT_SCHEMA_ID_LIST_KEY).setValue(inputSchemaIdList);
                mComponent.getProperty(STATIC_INPUT_ID_LIST_KEY).setValue(staticInputIdList);
                mComponent.getProperty(TOPO_SCORE_KEY).setValue(new Integer(maxTopoScore + 1));
                if (!isSchemaOwner) {
                    if (inputSchemaIdList.size() > 0) {
                        mComponent.getProperty(OUTPUT_SCHEMA_ID_KEY).setValue(inputSchemaIdList.get(0));
                    } else {
                        mComponent.getProperty(OUTPUT_SCHEMA_ID_KEY).setValue("");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                mLog.warning(e.getMessage());
            }
        }
        */
        
        public void updateDownstreamNodes() {
            PdModel model = getDoc();
            if(model != null && model.isReloading()) {
                return;
            }
            
            if (mComp == null) {
                // This happens when this node, its upstream node, and the link between
                // them are copied from PdCans to Clipboard. See SimpleNodePort.linkChanged
                return;
            }
            // Compute this node's properties by using 
            // all direct upstream node's properties 
            JGoPort inputPort = getInputPort();
            if (inputPort != null) {
                try {
                    //List inputIdList = new ArrayList();
                    //List inputSchemaIdList = new ArrayList();
                    //List staticInputIdList = new ArrayList();
                    StringBuffer inputIdList = new StringBuffer();
                    StringBuffer inputSchemaIdList = new StringBuffer();
                    StringBuffer staticInputIdList = new StringBuffer();
                    boolean isSchemaOwner = false;
                    
                    isSchemaOwner = (Boolean) mComp.getComponentType().getPropertyType(IS_SCHEMA_OWNER_KEY).getDefaultValue();
                    
                    
                    int counter = 0;
                    String resultantOutputSchemaId = "";
                    
                    int maxTopoScore = 0;
                    for (JGoListPosition pos = inputPort.getFirstLinkPos(); pos != null; pos = inputPort.getNextLinkPos(pos)) {
                        Object o = inputPort.getLinkAtPos(pos);
                        if (!(o instanceof Link)) {
                            continue;
                        }
                        Link inputLink = (Link)inputPort.getLinkAtPos(pos);
                        EntityNode fromNode= inputLink.getFromNode();
                        OperatorComponent fromComp = fromNode.mComp;
                        
                        String outputType = fromComp.getProperty(OUTPUT_TYPE_KEY).getValue();
                        String id = fromComp.getProperty(ID_KEY).getValue();
                        if (outputType.equals(IO_TYPE_TABLE)) {
                            if(counter != 0) {
                                staticInputIdList.append("\\");
                            }
                            staticInputIdList.append(id);
                        } else {
                            if(counter != 0) {
                                inputIdList.append("\\");
                            }
                            inputIdList.append(id);
                        }
                        
                        
                        String outputSchemaId = fromComp.getProperty(OUTPUT_SCHEMA_ID_KEY).getValue().trim();
                        if (outputSchemaId != null && !outputSchemaId.equals("") && !outputType.equals(IO_TYPE_TABLE)) {
                            if(counter != 0) {
                                inputSchemaIdList.append("\\");
                            } else {
                                resultantOutputSchemaId = outputSchemaId;
                            }
                            inputSchemaIdList.append(outputSchemaId);
                        }
                        
                        int topoScore = 0;
                        String topoScoreStr = fromComp.getProperty(TOPO_SCORE_KEY).getValue(); 
                        if(topoScoreStr != null) { 
                            try {
                            topoScore = Integer.parseInt(topoScoreStr);
                            } catch(Exception ex) {
                                //ignore
                            }
                        }
                        maxTopoScore = Math.max(maxTopoScore, topoScore);
                        
                        counter++;
                    }
                    mComp.getModel().startTransaction();
                    
                    //there is a loop when we set inputIdList etc
                    //which triggers call to this method again 
                    //see PlanCanvas where this method is invoked when
                    //Property value is updated.
                    //to avoid loop we set property only if we have a new value
                    
                    String inputIdListStr = mComp.getProperty(INPUT_ID_LIST_KEY).getValue();
                    String inputSchemaIdListStr = mComp.getProperty(INPUT_SCHEMA_ID_LIST_KEY).getValue();
                    String staticInputListStr = mComp.getProperty(STATIC_INPUT_ID_LIST_KEY).getValue();
                    String resultantOutputSchemaIdStr = mComp.getProperty(OUTPUT_SCHEMA_ID_KEY).getValue();
                    
                    if(!inputIdList.toString().equals(inputIdListStr)) {
                        mComp.getProperty(INPUT_ID_LIST_KEY).setValue(inputIdList.toString());
                    }
                    
                    if(!inputSchemaIdList.toString().equals(inputSchemaIdListStr)) {
                        mComp.getProperty(INPUT_SCHEMA_ID_LIST_KEY).setValue(inputSchemaIdList.toString());
                    }
                    
                    if(!staticInputIdList.toString().equals(staticInputListStr)) {
                        mComp.getProperty(STATIC_INPUT_ID_LIST_KEY).setValue(staticInputIdList.toString());
                    }
                    
                    mComp.getProperty(TOPO_SCORE_KEY).setValue(""+new Integer(maxTopoScore + 1));
                    if (!isSchemaOwner) {
                        if (inputSchemaIdList.length() > 0) {
                            if(!resultantOutputSchemaId.equals(resultantOutputSchemaIdStr)) {
                                mComp.getProperty(OUTPUT_SCHEMA_ID_KEY).setValue(resultantOutputSchemaId);
                            }
                        } else {
                            if(!resultantOutputSchemaIdStr.equals("")) {
                                mComp.getProperty(OUTPUT_SCHEMA_ID_KEY).setValue("");
                            }
                        }
                    }
                    
                    mComp.getModel().endTransaction();
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    mLog.warning(e.getMessage());
                }
            }

        // Then call updateDownstreamNodes() on all direct downstream nodes to force
        // recursive updates of all downstream nodes.
        JGoPort outputPort = getOutputPort();
        if (outputPort != null) {
            for (JGoListPosition pos = outputPort.getFirstLinkPos(); pos != null; pos = outputPort.getNextLinkPos(pos)) {
                Link outputLink = (Link)outputPort.getLinkAtPos(pos);
                EntityNode toNode = (EntityNode)outputLink.getToNode();
                toNode.updateDownstreamNodes();
            }
        }
    }
    
    /*
    // The triggering call of this method should be
    // 1. When a link is made from some other node to this node
    // 2. When this node's output schema is defined by SchemaWizard
    // 3. When any direct upstream node is deleted
    public void updateDownstreamNodes() {
        if (mComponent == null) {
            // This happens when this node, its upstream node, and the link between
            // them are copied from PdCans to Clipboard. See SimpleNodePort.linkChanged
            return;
        }
        // Compute this node's properties by using 
        // all direct upstream node's properties 
        JGoPort inputPort = getInputPort();
        if (inputPort != null) {
            try {
                List inputIdList = new ArrayList();
                List inputSchemaIdList = new ArrayList();
                List staticInputIdList = new ArrayList();
                boolean isSchemaOwner = mComponent.getProperty(IS_SCHEMA_OWNER_KEY).getBoolValue();
                int maxTopoScore = 0;
                for (JGoListPosition pos = inputPort.getFirstLinkPos(); pos != null; pos = inputPort.getNextLinkPos(pos)) {
                    Object o = inputPort.getLinkAtPos(pos);
                    if (!(o instanceof Link)) {
                        continue;
                    }
                    Link inputLink = (Link)inputPort.getLinkAtPos(pos);
                    EntityNode fromNode= inputLink.getFromNode();
                    TcgComponent fromComponent = fromNode.mComponent;
                    
                    String outputType = fromComponent.getProperty(OUTPUT_TYPE_KEY).getStringValue();
                    String id = fromComponent.getProperty(ID_KEY).getStringValue();
                    if (outputType.equals(IO_TYPE_TABLE)) {
                        staticInputIdList.add(id);
                    } else {
                        inputIdList.add(id);
                    }
                    
                    String outputSchemaId = fromComponent.getProperty(OUTPUT_SCHEMA_ID_KEY).getStringValue().trim();
                    if (outputSchemaId != null && !outputSchemaId.equals("") && !outputType.equals(IO_TYPE_TABLE)) {
                        inputSchemaIdList.add(outputSchemaId);
                    }
                    
                    int topoScore = fromComponent.getProperty(TOPO_SCORE_KEY).getIntValue();
                    maxTopoScore = Math.max(maxTopoScore, topoScore);
                }
                mComponent.getProperty(INPUT_ID_LIST_KEY).setValue(inputIdList);
                mComponent.getProperty(INPUT_SCHEMA_ID_LIST_KEY).setValue(inputSchemaIdList);
                mComponent.getProperty(STATIC_INPUT_ID_LIST_KEY).setValue(staticInputIdList);
                mComponent.getProperty(TOPO_SCORE_KEY).setValue(new Integer(maxTopoScore + 1));
                if (!isSchemaOwner) {
                    if (inputSchemaIdList.size() > 0) {
                        mComponent.getProperty(OUTPUT_SCHEMA_ID_KEY).setValue(inputSchemaIdList.get(0));
                    } else {
                        mComponent.getProperty(OUTPUT_SCHEMA_ID_KEY).setValue("");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                mLog.warning(e.getMessage());
            }
        }

        // Then call updateDownstreamNodes() on all direct downstream nodes to force
        // recursive updates of all downstream nodes.
        JGoPort outputPort = getOutputPort();
        if (outputPort != null) {
            for (JGoListPosition pos = outputPort.getFirstLinkPos(); pos != null; pos = outputPort.getNextLinkPos(pos)) {
                Link outputLink = (Link)outputPort.getLinkAtPos(pos);
                EntityNode toNode = (EntityNode)outputLink.getToNode();
                toNode.updateDownstreamNodes();
            }
        }
    }
    */
    
        /*
    // Properties
    public TcgComponent getComponent() { 
        return mComponent; 
    }
    
    public void releaseComponent() {
        mComponent.getPropertyChangeSupport().removePropertyChangeListener(this);
        mComponent = null;
    }
    
    public String getId() {
        String id = "";
        try {
            id = mComponent.getProperty(ID_KEY).getStringValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }
     */
    
//  Properties
    public TcgComponent getComponent() { 
        return null; 
    }
    
    public void releaseComponent() {
        mComp = null;
    }
    
    public String getId() {
        String id = "";
        try {
            id = mComp.getProperty(ID_KEY).getValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
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
    
    /*
    public int getInputCount() {
        try {
            Object obj = mComponent.getProperty(INPUT_ID_LIST_KEY).getValue();
            if (obj == null) {
                return 0;
            }
            return ((List)obj).size();
        } catch (Exception e) {
            //e.printStackTrace();
            mLog.warning(e.getMessage());
        }
        return 0;
    }
    */
    
    public int getInputMaxCount() {
        try {
            Object obj = mComp.getComponentType().getPropertyType(INPUT_MAX_COUNT_KEY).getDefaultValue();
            if (obj == null) {
                return 0;
            }
            return ((Integer)obj).intValue();
        } catch (Exception e) {
            //e.printStackTrace();
            mLog.warning("Exception:"+ e.getMessage());
        }
        return 0;
    }
    
    /*
    public int getInputMaxCount() {
        try {
            Object obj = mComponent.getProperty(INPUT_MAX_COUNT_KEY).getValue();
            if (obj == null) {
                return 0;
            }
            return ((Integer)obj).intValue();
        } catch (Exception e) {
            //e.printStackTrace();
            mLog.warning("Exception:"+ e.getMessage());
        }
        return 0;
    }*/
    
    public int getStaticInputCount() {
            List staticInputs = mComp.getStaticInputTableList();//getProperty(STATIC_INPUT_ID_LIST_KEY).getValue();
            return staticInputs.size();
        
    }
    /*
    public int getStaticInputCount() {
        try {
            Object obj = mComponent.getProperty(STATIC_INPUT_ID_LIST_KEY).getValue();
            if (obj == null) {
                return 0;
            }
            return ((List)obj).size();
        } catch (Exception e) {
            //e.printStackTrace();
            mLog.warning("Exception:"+ e.getMessage());
        }
        return 0;
    }*/
    
    public int getStaticInputMaxCount() {
        try {
            Object obj = mComp.getComponentType().getPropertyType(STATIC_INPUT_MAX_COUNT_KEY).getDefaultValue();
            if (obj == null) {
                return 0;
            }
            return ((Integer)obj).intValue();
        } catch (Exception e) {
            //e.printStackTrace();
            mLog.warning("Exception:"+ e.getMessage());
        }
        return 0;
    }
    /*
    public int getStaticInputMaxCount() {
        try {
            Object obj = mComponent.getProperty(STATIC_INPUT_MAX_COUNT_KEY).getValue();
            if (obj == null) {
                return 0;
            }
            return ((Integer)obj).intValue();
        } catch (Exception e) {
            //e.printStackTrace();
            mLog.warning("Exception:"+ e.getMessage());
        }
        return 0;
    }*/
    
    public String getInputType() {
        try {
            return mComp.getProperty(INPUT_TYPE_KEY).getValue();
        } catch (Exception e) {
            //e.printStackTrace();
            mLog.warning("Exception:"+ e.getMessage());
        }
        return IO_TYPE_NONE;
    }
    
    /*
    public String getInputType() {
        try {
            return mComponent.getProperty(INPUT_TYPE_KEY).getStringValue();
        } catch (Exception e) {
            //e.printStackTrace();
            mLog.warning("Exception:"+ e.getMessage());
        }
        return IO_TYPE_NONE;
    }*/
    
    public String getOutputType() {
        try {
            return mComp.getProperty(OUTPUT_TYPE_KEY).getValue();
        } catch (Exception e) {
            //e.printStackTrace();
            mLog.warning("Exception:"+ e.getMessage());
        }
        return IO_TYPE_NONE;
    }
    
    /*
    public String getOutputType() {
        try {
            return mComponent.getProperty(OUTPUT_TYPE_KEY).getStringValue();
        } catch (Exception e) {
            //e.printStackTrace();
            mLog.warning("Exception:"+ e.getMessage());
        }
        return IO_TYPE_NONE;
    }*/
    //========================================
//    /**
//     * Used to paste from Clipboard to PdCanvas
//     */
//    public JGoObject copyObjectAndResetContextProperties(JGoCopyEnvironment env, Plan plan) {
//       EntityNode newObj = (EntityNode)super.copyObject(env);
//        
//        TcgComponent component = null;
//        try {
//            component = TcgModelManager.getComponent("Clipboard", mComponentXml);
//        } catch (Exception e) {
//            mLog.log(Level.SEVERE,"copyObjectAndResetContextProperties failed", e);
//            return newObj;
//        }
//        // new id and name for newObj.mComponent
//        newObj.initialize(plan, plan.copyAndAddOperator(component));
//
//        // JGoImage.myImage is transient, hence we must set it again.
//        JGoObject icon = newObj.getIcon();
//        if (icon instanceof JGoImage) {
//            ((JGoImage)icon).loadImage(component.getType().getIcon().getImage(), true);
//        }
//
//        newObj.updateLabelText();
//
//        try {
//            TcgComponent c = newObj.mComponent;
//            
//            // reset topoScore
//            org.netbeans.modules.iep.model.lib.TcgProperty p = c.getProperty(TOPO_SCORE_KEY);
//            p.setValue(p.getType().getDefaultValue());
//            
//            // reset inputIdList
//            p = c.getProperty(INPUT_ID_LIST_KEY);
//            p.setValue(p.getType().getDefaultValue());
//
//            // reset inputSchemaIdList
//            p = c.getProperty(INPUT_SCHEMA_ID_LIST_KEY);
//            p.setValue(p.getType().getDefaultValue());
//
//            // reset staticInputIdList
//            p = c.getProperty(STATIC_INPUT_ID_LIST_KEY);
//            p.setValue(p.getType().getDefaultValue());
//        
//            // isSchemaOwner? new outputSchemaId : reset outputSchemaId
//            p = c.getProperty(OUTPUT_SCHEMA_ID_KEY);
//            boolean isSchemaOwner = c.getProperty(IS_SCHEMA_OWNER_KEY).getBoolValue();
//            if (isSchemaOwner) {
//                if (mOutputSchemaXml != null && !mOutputSchemaXml.trim().equals("")) {
//                    TcgComponent schemaComponent = TcgModelManager.getComponent("Clipboard", mOutputSchemaXml);
//                    Schema schema = ModelManager.getSchema(schemaComponent);
//                    Schema newSchema = plan.copyAndAddSchema(schema);
//                    p.setStringValue(newSchema.getName());
//                } else {
//                    p.setValue(p.getType().getDefaultValue());
//                }
//            } else {
//                p.setValue(p.getType().getDefaultValue());
//            }
//
//            // reset globalId
//            p = c.getProperty(GLOBAL_ID_KEY);
//            p.setValue(p.getType().getDefaultValue());
//        } catch (Exception e) {
//            mLog.log(Level.SEVERE,"copyChildren failed", e);
//        }
//        return newObj;
//        
//    }

    public OperatorComponent copyObjectAndResetContextProperties(IEPModel targetModel) {
        
        OperatorComponent operator = this.getModelComponent();
        OperatorComponentContainer opContainer = targetModel.getPlanComponent().getOperatorComponentContainer();
        
        OperatorComponent newOperator = ModelObjectFactory.getInstance().createOperatorComponent(operator.getType(), targetModel);
        
        String operatorId = operator.getId(); 
        String operatorDisplayName = operator.getDisplayName();
        
        if(opContainer.findChildComponent(operatorId) != null) {
            operatorId = NameGenerator.generateId(opContainer, "o");
        }
        
        if(opContainer.findOperator(operatorDisplayName) != null) {
            operatorDisplayName = NameGenerator.generateNewName(opContainer, operator.getComponentType());
        }
            
        newOperator.setDisplayName(operatorDisplayName);
        newOperator.setId(operatorId);
        newOperator.setName(operatorId);
        newOperator.setTitle(operatorId);
        
        newOperator.setX(operator.getX());
        newOperator.setY(operator.getY());
        
        Documentation doc = operator.getDocumentation();
        if(doc != null) {
            Documentation newDoc = targetModel.getFactory().createDocumentation(targetModel);
            newDoc.setTextContent(doc.getTextContent());
            newOperator.setDocumentation(newDoc);
        }
        
//        EntityNode newObj = (EntityNode)super.copyObject(env);
         
//         TcgComponent component = null;
//         try {
//             component = TcgModelManager.getComponent("Clipboard", mComponentXml);
//         } catch (Exception e) {
//             mLog.log(Level.SEVERE,"copyObjectAndResetContextProperties failed", e);
//             return newObj;
//         }
//         // new id and name for newObj.mComponent
//         newObj.initialize(plan, plan.copyAndAddOperator(component));
//
//         // JGoImage.myImage is transient, hence we must set it again.
//         JGoObject icon = newObj.getIcon();
//         if (icon instanceof JGoImage) {
//             ((JGoImage)icon).loadImage(component.getType().getIcon().getImage(), true);
//         }
//
//         newObj.updateLabelText();

         try {
//             TcgComponent c = newObj.mComponent;
//             
//             // reset topoScore
//             org.netbeans.modules.iep.model.lib.TcgProperty p = c.getProperty(TOPO_SCORE_KEY);
//             p.setValue(p.getType().getDefaultValue());
//             
//             // reset inputIdList
//             p = c.getProperty(INPUT_ID_LIST_KEY);
//             p.setValue(p.getType().getDefaultValue());
//
//             // reset inputSchemaIdList
//             p = c.getProperty(INPUT_SCHEMA_ID_LIST_KEY);
//             p.setValue(p.getType().getDefaultValue());
//
//             // reset staticInputIdList
//             p = c.getProperty(STATIC_INPUT_ID_LIST_KEY);
//             p.setValue(p.getType().getDefaultValue());
         
             // isSchemaOwner? new outputSchemaId : reset outputSchemaId
             Property p = operator.getProperty(OUTPUT_SCHEMA_ID_KEY);
             boolean isSchemaOwner = operator.isSchemaOwner();
             
             
             
             if (isSchemaOwner) {
                 SchemaComponent outputSchemaComp =  operator.getOutputSchemaId();
                 if (outputSchemaComp != null) {
                     SchemaComponentContainer scContainer = targetModel.getPlanComponent().getSchemaComponentContainer();
                     
                     SchemaComponent newSCComp = copySchemaComponent(outputSchemaComp, targetModel);
                     scContainer.addSchemaComponent(newSCComp);
                    
                     Property outputSchema = newOperator.getProperty(OUTPUT_SCHEMA_ID_KEY);
                     outputSchema.setValue(newSCComp.getName());
                     
                 } 
             }

//             // reset globalId
//             p = c.getProperty(GLOBAL_ID_KEY);
//             p.setValue(p.getType().getDefaultValue());
         } catch (Exception e) {
             mLog.log(Level.SEVERE,"copyChildren failed", e);
         }
         return newOperator;
         
     }

    private SchemaComponent copySchemaComponent(SchemaComponent component, IEPModel targetModel) {
        SchemaComponent sComponent = targetModel.getFactory().createSchema(targetModel);
        SchemaComponentContainer scContainer = targetModel.getPlanComponent().getSchemaComponentContainer();
        
        String schemaName = component.getName();
        if(scContainer.findSchema(schemaName) != null) {
            schemaName = NameGenerator.generateSchemaName(scContainer);
        }
        
        sComponent.setName(schemaName);
        sComponent.setType(component.getType());
        sComponent.setTitle(schemaName);
        
        Documentation scDoc = component.getDocumentation();
        if(scDoc != null) {
            Documentation doc = targetModel.getFactory().createDocumentation(targetModel);
            doc.setTextContent(scDoc.getTextContent());
            sComponent.setDocumentation(doc);
        }
        
        List<Property> properties = component.getProperties();
        Iterator<Property> itP = properties.iterator();
        
        while(itP.hasNext()) {
            Property property = itP.next();
            Property newP = copyProperty(property, targetModel);
            sComponent.addProperty(newP);
        }
        
        List<SchemaAttribute> attributes = component.getSchemaAttributes();
        Iterator<SchemaAttribute> itSA = attributes.iterator();
        
        while(itSA.hasNext()) {
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
    
    
    /*
    public JGoObject copyObjectAndResetContextProperties(JGoCopyEnvironment env, Plan plan) {
        EntityNode newObj = (EntityNode)super.copyObject(env);
        
        TcgComponent component = null;
        try {
            component = TcgModelManager.getComponent("Clipboard", mComponentXml);
        } catch (Exception e) {
            mLog.log(Level.SEVERE,"copyObjectAndResetContextProperties failed", e);
            return newObj;
        }
        // new id and name for newObj.mComponent
        newObj.initialize(plan, plan.copyAndAddOperator(component));

        // JGoImage.myImage is transient, hence we must set it again.
        JGoObject icon = newObj.getIcon();
        if (icon instanceof JGoImage) {
            ((JGoImage)icon).loadImage(component.getType().getIcon().getImage(), true);
        }

        newObj.updateLabelText();

        try {
            TcgComponent c = newObj.mComponent;
            
            // reset topoScore
            org.netbeans.modules.iep.model.lib.TcgProperty p = c.getProperty(TOPO_SCORE_KEY);
            p.setValue(p.getType().getDefaultValue());
            
            // reset inputIdList
            p = c.getProperty(INPUT_ID_LIST_KEY);
            p.setValue(p.getType().getDefaultValue());

            // reset inputSchemaIdList
            p = c.getProperty(INPUT_SCHEMA_ID_LIST_KEY);
            p.setValue(p.getType().getDefaultValue());

            // reset staticInputIdList
            p = c.getProperty(STATIC_INPUT_ID_LIST_KEY);
            p.setValue(p.getType().getDefaultValue());
        
            // isSchemaOwner? new outputSchemaId : reset outputSchemaId
            p = c.getProperty(OUTPUT_SCHEMA_ID_KEY);
            boolean isSchemaOwner = c.getProperty(IS_SCHEMA_OWNER_KEY).getBoolValue();
            if (isSchemaOwner) {
                if (mOutputSchemaXml != null && !mOutputSchemaXml.trim().equals("")) {
                    TcgComponent schemaComponent = TcgModelManager.getComponent("Clipboard", mOutputSchemaXml);
                    Schema schema = ModelManager.getSchema(schemaComponent);
                    Schema newSchema = plan.copyAndAddSchema(schema);
                    p.setStringValue(newSchema.getName());
                } else {
                    p.setValue(p.getType().getDefaultValue());
                }
            } else {
                p.setValue(p.getType().getDefaultValue());
            }

            // reset globalId
            p = c.getProperty(GLOBAL_ID_KEY);
            p.setValue(p.getType().getDefaultValue());
        } catch (Exception e) {
            mLog.log(Level.SEVERE,"copyChildren failed", e);
        }
        return newObj;
    }
    */

    // TcgComponentNodeView
    public void updateTcgComponentNodeView() {
        updateDownstreamNodes();
    }
    
   
    
    public OperatorComponent getModelComponent() {
        return this.mComp;
    }
    
    @Override
    public void moveChildren(Rectangle prevRect) {
        super.moveChildren(prevRect);
        
//        Runnable r = new Runnable() {
//            
//            public void run() {
//                OperatorComponent opComp = getModelComponent();
//                int x = getLocation().x;
//                int y = getLocation().y;
//                
//                IEPModel model = opComp.getModel();
//                model.startTransaction();
//                opComp.setX(x);
//                opComp.setY(y);
//                model.endTransaction();
//            }
//        };
//        
//        SwingUtilities.invokeLater(r);
//        
    }
    
    
    private void refreshOperator() {
        //display name
        String operatorDisplayName = this.mComp.getDisplayName();
        setOperatorDisplayName(operatorDisplayName);
        
        //x, y location
        Point newLoc = new Point(this.mComp.getX(), this.mComp.getY());
        if(!newLoc.equals(getLocation())) { 
            setTopLeft(newLoc);
        }
    }
    
    private void refresh() {
        Documentation doc = this.mComp.getDocumentation();
        if(doc != null && doc.getTextContent() != null && !doc.getTextContent().trim().equals("")) {
            getDocumentationNode().setVisible(true);
            getDocumentationNode().setToolTipText(doc.getTextContent());
        } else {
            getDocumentationNode().setVisible(false);
            getDocumentationNode().setToolTipText("");
        }
    }
    
    class IEPModelListener implements ComponentListener {
            
            public void childrenAdded(ComponentEvent evt) {
                Object source = evt.getSource();
                if(source instanceof OperatorComponent && source.equals(mComp)) {
                    refresh();
                } 
            }
            
            public void childrenDeleted(ComponentEvent evt) {
                Object source = evt.getSource();
                if(source instanceof OperatorComponent && source.equals(mComp)) {
                    refresh();
                }
                
            }
            
            public void valueChanged(ComponentEvent evt) {
                Object source = evt.getSource();
                if(source instanceof Property) {
                    Property prop = (Property) source;
                    IEPComponent parent = prop.getParent();
                    if(parent.equals(getModelComponent())) {
                        refreshOperator();
                    }
                }
                //if mouse is moved we do not want to update operator property
                //since we may be moving operators which will trigger update
                //to xy location , and which in turn can trigger update to
                //node's xy location from model which is unnecessary
                //and can still have old xy location in model.
//                if(canvas != null && canvas.getState() != JGoView.MouseStateMove) {
                    
                    
//                }
            }
    }
    

}
