/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.iep.editor.designer;

import org.netbeans.modules.iep.editor.model.ModelManager;
import org.netbeans.modules.iep.editor.tcg.ps.TcgComponentNodeView;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.nwoods.jgo.JGoArea;
import com.nwoods.jgo.JGoCopyEnvironment;
import com.nwoods.jgo.JGoImage;
import com.nwoods.jgo.JGoListPosition;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPort;
import com.nwoods.jgo.JGoView;

import org.netbeans.modules.iep.editor.designer.GuiConstants;
import org.netbeans.modules.iep.editor.model.Plan;
import org.netbeans.modules.iep.editor.model.Schema;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponent;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponentType;
import org.netbeans.modules.iep.editor.tcg.model.TcgModelManager;
import org.netbeans.modules.iep.editor.tcg.model.TcgProperty;


// Activities are implemented as SimpleNodes.
//
// Properties include Text, ActivityType, and ID.
// The Text property is actually just the SimpleNode's Label's Text.
//
// This class also supports a standard set of icons (as JGoImages)
// and the notion of a standard size for the node.
public class EntityNode extends SimpleNode 
    implements GuiConstants, PropertyChangeListener, ComponentHolder, TcgComponentNodeView 
{
    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(EntityNode.class.getName());

    private static Dimension mStdSize = new Dimension(16, 16);

    private static Point mStdPoint = new Point();  // don't care


    public static Dimension getStdSize() {
        return mStdSize;
    }

    public static void setStdSize(int w, int h) {
        mStdSize.width = w;
        mStdSize.height = h;
    }


    // State
    private transient Plan mPlan;
    private transient TcgComponent mComponent;
    
    // Used for carring content of mComponent and possbile Schema during cut, copy, and paste
    private String mComponentXml;
    private String mOutputSchemaXml;
    
    public EntityNode() {
        super();
    }
    
    public EntityNode(Plan plan, TcgComponent component, Point dc) {
        super();
        try {
            initialize(plan, component);
            // create an input port and an output port, each instances of SimpleNodePort
            String type = (String)component.getProperty(INPUT_TYPE_KEY).getValue();
            boolean hasInput = type.equals(IO_TYPE_STREAM) || type.equals(IO_TYPE_RELATION);
            type = (String)component.getProperty(OUTPUT_TYPE_KEY).getValue();
            boolean hasOutput = type.equals(IO_TYPE_STREAM) || type.equals(IO_TYPE_RELATION)|| type.equals(IO_TYPE_TABLE);
            super.initialize(dc, getStdSize(), getImage(), getLabelString(), hasInput, hasOutput);
        } catch (Exception e) {
            e.printStackTrace();
            mLog.warning(e.getMessage());
        }
    }
    
    protected void initialize(Plan plan, TcgComponent component) {
        mPlan = plan;
        mComponent = component;
        mComponent.getPropertyChangeSupport().addPropertyChangeListener(this);
    }
    /**
     *  When called to copy from PdCanvas to Clipboard, mComponent is not null
     *  When called to copy from Clipboard to PdCanvas, mComponent is null
     */
    protected void copyChildren(JGoArea newarea, JGoCopyEnvironment env) {
        EntityNode newObj = (EntityNode)newarea;
        
        super.copyChildren(newarea, env);
        if (mComponent == null) {
            return;
        }
        try {
            newObj.mComponentXml = mComponent.toXml();
            boolean isSchemaOwner = mComponent.getProperty(IS_SCHEMA_OWNER_KEY).getBoolValue();
            if (isSchemaOwner) {
                String outputSchemaId = mComponent.getProperty(OUTPUT_SCHEMA_ID_KEY).getStringValue();
                Schema schema = mPlan.getSchema(outputSchemaId);
                if (schema != null) {
                    newObj.mOutputSchemaXml = schema.toXml();
                }
            }
        } catch (Exception e) {
            mLog.log(Level.SEVERE, java.util.ResourceBundle.getBundle("org/netbeans/modules/iep/editor/designer/Bundle").getString("EntityNode.copyChildren_failed."), e);
        }
    }

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
    
    private Color getLabelColor() {
        return mComponent.validate().hasError()? Color.red : Color.black;
    }

    public PdModel getDoc() {
        return (PdModel)getDocument(); 
    }
    

    // You'll probably want to replace this with somewhat more interesting information
    public String getToolTipText() {
        String msg = "";
        try {
            msg = (String)mComponent.getProperty(NAME_KEY).getValue();
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
        if (view instanceof PdCanvas) {
            PdCanvas pdCanvas = (PdCanvas)view;
            pdCanvas.getDesigner().showPropertyPane(mComponent, (PdCanvas)view);
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
        try {
            Object obj = mComponent.getProperty(INPUT_ID_LIST_KEY).getValue();
            if (obj == null) {
                return 0;
            }
            return ((List)obj).size();
        } catch (Exception e) {
            e.printStackTrace();
            mLog.warning(java.util.ResourceBundle.getBundle("org/netbeans/modules/iep/editor/designer/Bundle").getString("EntityNode.Exception:_") + e);
        }
        return 0;
    }
    
    public int getInputMaxCount() {
        try {
            Object obj = mComponent.getProperty(INPUT_MAX_COUNT_KEY).getValue();
            if (obj == null) {
                return 0;
            }
            return ((Integer)obj).intValue();
        } catch (Exception e) {
            e.printStackTrace();
            mLog.warning(java.util.ResourceBundle.getBundle("org/netbeans/modules/iep/editor/designer/Bundle").getString("EntityNode.Exception:_") + e);
        }
        return 0;
    }
    
    public int getStaticInputCount() {
        try {
            Object obj = mComponent.getProperty(STATIC_INPUT_ID_LIST_KEY).getValue();
            if (obj == null) {
                return 0;
            }
            return ((List)obj).size();
        } catch (Exception e) {
            e.printStackTrace();
            mLog.warning(java.util.ResourceBundle.getBundle("org/netbeans/modules/iep/editor/designer/Bundle").getString("EntityNode.Exception:_") + e);
        }
        return 0;
    }
    
    public int getStaticInputMaxCount() {
        try {
            Object obj = mComponent.getProperty(STATIC_INPUT_MAX_COUNT_KEY).getValue();
            if (obj == null) {
                return 0;
            }
            return ((Integer)obj).intValue();
        } catch (Exception e) {
            e.printStackTrace();
            mLog.warning(java.util.ResourceBundle.getBundle("org/netbeans/modules/iep/editor/designer/Bundle").getString("EntityNode.Exception:_") + e);
        }
        return 0;
    }
    
    public String getInputType() {
        try {
            return mComponent.getProperty(INPUT_TYPE_KEY).getStringValue();
        } catch (Exception e) {
            e.printStackTrace();
            mLog.warning(java.util.ResourceBundle.getBundle("org/netbeans/modules/iep/editor/designer/Bundle").getString("EntityNode.Exception:_") + e);
        }
        return IO_TYPE_NONE;
    }
    
    public String getOutputType() {
        try {
            return mComponent.getProperty(OUTPUT_TYPE_KEY).getStringValue();
        } catch (Exception e) {
            e.printStackTrace();
            mLog.warning(java.util.ResourceBundle.getBundle("org/netbeans/modules/iep/editor/designer/Bundle").getString("EntityNode.Exception:_") + e);
        }
        return IO_TYPE_NONE;
    }
    
    public void defineSchema(String schemaId) {
        try {
            TcgProperty prop = mComponent.getProperty(OUTPUT_SCHEMA_ID_KEY); 
            if (prop != null) {
                prop.setValue(schemaId);
                updateDownstreamNodes();
            }
        } catch (Exception e) {
            e.printStackTrace();
            mLog.warning(java.util.ResourceBundle.getBundle("org/netbeans/modules/iep/editor/designer/Bundle").getString("EntityNode.Exception:_") + e);
        }
    }
    
    public String getSchemaId() {
        String schemaId = "";
        try {
            schemaId = mComponent.getProperty(OUTPUT_SCHEMA_ID_KEY).getStringValue();
        } catch (Exception e) {
            e.printStackTrace();
            mLog.warning(java.util.ResourceBundle.getBundle("org/netbeans/modules/iep/editor/designer/Bundle").getString("EntityNode.Exception:_") + e);
        }
        return schemaId;
    }

    //========================================
    /**
     * Used to paste from Clipboard to PdCanvas
     */
    public JGoObject copyObjectAndResetContextProperties(JGoCopyEnvironment env, Plan plan) {
        EntityNode newObj = (EntityNode)super.copyObject(env);
        
        TcgComponent component = null;
        try {
            component = TcgModelManager.getComponent("Clipboard", mComponentXml);
        } catch (Exception e) {
            mLog.log(Level.SEVERE, java.util.ResourceBundle.getBundle("org/netbeans/modules/iep/editor/designer/Bundle").getString("EntityNode.copyObjectAndResetContextProperties_failed"), e);
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
            TcgProperty p = c.getProperty(TOPO_SCORE_KEY);
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
            mLog.log(Level.SEVERE, java.util.ResourceBundle.getBundle("org/netbeans/modules/iep/editor/designer/Bundle").getString("EntityNode.copyChildren_failed"), e);
        }
        return newObj;
    }
    
    // TcgComponentNodeView
    public void updateTcgComponentNodeView() {
        updateDownstreamNodes();
    }
    
}
