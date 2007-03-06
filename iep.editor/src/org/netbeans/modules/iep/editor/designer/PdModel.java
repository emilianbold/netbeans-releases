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
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;

import com.nwoods.jgo.JGoCopyEnvironment;
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoDocumentChangedEdit;
import com.nwoods.jgo.JGoDocumentEvent;
import com.nwoods.jgo.JGoLayer;
import com.nwoods.jgo.JGoLink;
import com.nwoods.jgo.JGoListPosition;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoObjectSimpleCollection;
import com.nwoods.jgo.JGoPort;
import com.nwoods.jgo.JGoUndoManager;

import org.netbeans.modules.iep.editor.designer.GuiConstants;
import org.netbeans.modules.iep.editor.model.Plan;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponent;

public class PdModel extends JGoDocument implements GuiConstants{
    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(PdModel.class.getName());
    
    private static final long serialVersionUID = -4579746482156252393L;    
    
    // State
    private boolean mOrthoLinks = false;
    
    private transient boolean mIsModified = false;
    private transient Plan mPlan;
    private transient PlanDesigner mDesigner;
            
    // Default constructor
    // needed for JGo cut-copy-paste to work
    public PdModel() {
        super();
        setUndoManager(new JGoUndoManager());
    }
    
    public PdModel(Plan plan)  {
        super();
        setUndoManager(new JGoUndoManager());
        mPlan = plan;
        mPlan.initializeModel(this);
        mPlan.getPropertyChangeSupport().addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                mDesigner.setDirty();
            }
        });
        // set properties
        updatePaperColor();
        setModified(false);
    }
    
    public Plan getPlan() {
        return mPlan;
    }
    
    public void setDesigner(PlanDesigner designer) {
        mDesigner = designer;
    }
    
    public EntityNode findNodeById(String id)  {
        // for larger documents, it would be more efficient to keep a
        // hash table mapping id to ActivityNode
        // for this example, we won't bother with the hash table
        JGoListPosition pos = getFirstObjectPos();
        while (pos != null) {
            JGoObject obj = getObjectAtPos(pos);
            // only consider top-level objects
            pos = getNextObjectPosAtTop(pos);
            
            if (obj instanceof EntityNode) {
                EntityNode node = (EntityNode)obj;
                if (node.getId().equals(id)) {
                    return node;
                }
            }
        }
        return null;
    }
    
    
    public void updatePaperColor() {
        if (isModifiable()) {
            setPaperColor(Color.white);
        } else {
            setPaperColor(new Color(0xDD, 0xDD, 0xDD));
        }
    }
    
    
    // new property--has the document been changed?
    public boolean isModified() {
        return mIsModified;
    }
    
    public void setModified(boolean b) {
        if (mIsModified != b) {
            mIsModified = b;
            // don't need to notify document listeners
        }
    }
    
    
    // Some, but not all, changes to the document should make it "modified"
    public void fireUpdate(int hint, int flags, Object object, int prevInt, Object prevVal) {
        // changing the read-only-ness isn't considered modifying the document
        if (hint == JGoDocumentEvent.MODIFIABLE_CHANGED) {
            updatePaperColor();
        } else if (hint != JGoDocumentEvent.PAPER_COLOR_CHANGED) {
            // don't consider the paper color as part of the document, either
            setModified(true);
        }
        super.fireUpdate(hint, flags, object, prevInt, prevVal);
    }
    
    
    // creating a new flow between activities
    public Link newLink(JGoPort from, JGoPort to) {
        TcgComponent component = mPlan.addNewLink();
        Link ll = new Link(component, from, to);
        ll.initialize();
        ll.setOrthogonal(isOrthogonalFlows());
        addObjectAtTail(ll);
        
        EntityNode fromNode = ll.getFromNode();
        if (fromNode != null) {
            fromNode.updateDownstreamNodes();
        }
        return ll;
    }
    
    public Link findLinkByNodes(EntityNode from, EntityNode to) {
        if (from == null || to == null)
            return null;
        
        JGoPort fromPort = from.getOutputPort();
        if (fromPort == null)
            return null;
        
        JGoPort toPort = to.getInputPort();
        if (toPort == null)
            return null;
        
        JGoListPosition pos = toPort.getFirstLinkPos();
        while (pos != null) {
            JGoLink l = toPort.getLinkAtPos(pos);
            pos = toPort.getNextLinkPos(pos);
            
            JGoPort src = l.getFromPort();
            if (src == fromPort) {
                return (Link)l;
            }
        }
        return null;
    }
    
    
    // toggle the routing style of the links
    void toggleOrthogonalFlows() {
        startTransaction();
        setOrthogonalFlows(!isOrthogonalFlows());
        endTransaction("toggleOrthogonalFlows");
    }
    
    public boolean isOrthogonalFlows()  {
        return mOrthoLinks;
    }
    
    public void setOrthogonalFlows(boolean b) {
        if (mOrthoLinks != b) {
            mOrthoLinks = b;
            // now update all links
            JGoListPosition pos = getFirstObjectPos();
            while (pos != null) {
                JGoObject obj = getObjectAtPos(pos);
                // only consider top-level objects
                pos = getNextObjectPosAtTop(pos);
                if (obj instanceof JGoLink) {
                    JGoLink link = (JGoLink)obj;
                    link.setOrthogonal(b);
                }
            }
        }
    }
    
    
    public void store() {
        try {
            // store properties
            HashMap properties = new HashMap();
            properties.put(ORTHO_FLOW_KEY, Boolean.valueOf(isOrthogonalFlows()));
            mPlan.setProperties(properties);
            
            // store nodes and links
            HashSet set = new HashSet();
            int z = 0;
            JGoListPosition pos = getFirstObjectPos();
            while (pos != null) {
                JGoObject obj = getObjectAtPos(pos);
                pos = getNextObjectPosAtTop(pos);
                if (obj instanceof EntityNode) {
                    EntityNode node = (EntityNode)obj;
                    TcgComponent component = node.getComponent();
                    component.getProperty(X_KEY).setValue(new Integer(node.getLeft()));
                    component.getProperty(Y_KEY).setValue(new Integer(node.getTop()));
                    component.getProperty(Z_KEY).setValue(new Integer(z++));
                    set.add(component);
                } else if (obj instanceof Link) {
                    Link link = (Link)obj;
                    set.add(link.getComponent());
                }
            }
            mPlan.cleanupDanglingReferences(set);
        } catch (Exception e) {
            //e.printStackTrace();
            mLog.warning("Exception: " + e.getMessage());
        }
    }
    
    
    public void endTransaction(String pname) {
        super.endTransaction(pname);
        PdAction.updateAllActions(mDesigner);
    }
    
    public void releasePlan() {
        try {
            JGoListPosition pos = getFirstObjectPos();
            while (pos != null) {
                JGoObject obj = getObjectAtPos(pos);
                pos = getNextObjectPosAtTop(pos);
                if (obj instanceof EntityNode) {
                    EntityNode node = (EntityNode)obj;
                    node.releaseComponent();
                }
                if (obj instanceof Link) {
                    Link link = (Link)obj;
                    link.releaseComponent();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            mLog.warning("Exception: " + e);
        }
    }
    
    /**
     * It first copies all EntityNodes. For each EntityNode n, it creates new values
     * for properties such as: id and name; it reset to default values for properties
     * such as: topoScore, inputIdList, inputSchemaIdList, staticInputIdList, and outputSchemaId;
     * and it creates new name for outputSchemaId if n is a schemaOwner.
     *
     * It then processes all Links. For each Link l whose toNode and fromNode are both
     * in coll, it copies l.
     *
     * @param coll a simple collection of EntityNodes and Links
     * @param offset specify the (x,y) by which all copied objects should be moved
     * @return the copy environment with the results
     */
    public JGoCopyEnvironment islandCopyFromCollection(JGoObjectSimpleCollection coll, Point offset) {
        JGoCopyEnvironment map = createDefaultCopyEnvironment();
        List nodeList = new ArrayList();
        List linkList = new ArrayList();
        JGoListPosition pos = coll.getFirstObjectPos();
        while (pos != null) {
            JGoObject obj = coll.getObjectAtPos(pos);
            obj = obj.getDraggingObject();
            if (obj instanceof EntityNode) {
                nodeList.add(obj);
            } else if (obj instanceof Link) {
                linkList.add(obj);
            }
            pos = coll.getNextObjectPosAtTop(pos);
        }
        
        // copy all EntityNodes and position them
        for (int i = 0, I = nodeList.size(); i < I; i++) {
            EntityNode node = (EntityNode)nodeList.get(i);
            EntityNode newNode = (EntityNode)node.copyObjectAndResetContextProperties(map, mPlan);
            positionNewObj(newNode, node, offset);
        }
        
        // copy all qualified Links and position them
        for (int i = 0, I = linkList.size(); i < I; i++) {
            Link link = (Link)linkList.get(i);
            if (!nodeList.contains(link.getFromNode()) || !nodeList.contains(link.getToNode())) {
                continue;
            }
            Link newLink = (Link)link.copyObjectAndResetContextProperties(map, mPlan);
            link.copyObjectDelayed(map, newLink);
            positionNewObj(newLink, link, offset);
        }
        
        return map;
    }
    
    private void positionNewObj(JGoObject newObj, JGoObject oldObj, Point offset) {
        Point oldLoc = new Point(0, 0);
        
        // use Location here, not TopLeft(), so that centered things move correctly
        newObj.getLocation(oldLoc);
        newObj.setLocationOffset(oldLoc, offset);
        // try to add to the same layer as the original object
        JGoLayer oldlayer = oldObj.getLayer();
        JGoLayer newlayer = null;
        // but can't if it's coming from a different document, or from
        // no document at all
        if (oldlayer != null) {
            if (oldlayer.getDocument() == this) {
                newlayer = oldlayer;
            } else {
                newlayer = findLayer(oldlayer.getIdentifier());
            }
        }
        if (newlayer == null) {
            newlayer = getDefaultLayer();
        }
        newlayer.addObjectAtTail(newObj);
    }
    
    /**
     * This method is called by the JGoDocumentChangedEdit.undo and redo methods
     * to actually perform the state change.
     * <p>
     * For JGoDocumentEvent.CHANGED events, the JGoObject's changeValue method is called.
     * <p>
     * You will want to override this method to handle changing the additional state
     * of your document subclasses.
     *
     * @param e the UndoableEdit that also remembers the kind of change and any
     *          appropriate old and new state for performing an undo or redo
     * @param undo if true, this method should restore the old state/value, otherwise
     *             this method should restore the new state/value
     */
    public void changeValue(JGoDocumentChangedEdit e, boolean undo) {
        super.changeValue(e, undo);
        switch (e.getHint()) {
            case JGoDocumentEvent.INSERTED: {
                JGoObject obj = (JGoObject)e.getObject();
                if (undo) {
                    if (obj instanceof EntityNode) {
                        mPlan.removeOperator(((EntityNode)obj).getComponent());
                    } else if (obj instanceof Link) {
                        mPlan.removeLink(((Link)obj).getComponent());
                    }
                } else {
                    if (obj instanceof EntityNode) {
                        mPlan.addOperator(((EntityNode)obj).getComponent());
                    } else if (obj instanceof Link) {
                        mPlan.addLink(((Link)obj).getComponent());
                    }
                }
                return;}
            case JGoDocumentEvent.REMOVED: {
                JGoObject obj = (JGoObject)e.getObject();
                if (undo) {
                    if (obj instanceof EntityNode) {
                        mPlan.addOperator(((EntityNode)obj).getComponent());
                    } else if (obj instanceof Link) {
                        mPlan.addLink(((Link)obj).getComponent());
                    }
                } else {
                    if (obj instanceof EntityNode) {
                        mPlan.removeOperator(((EntityNode)obj).getComponent());
                    } else if (obj instanceof Link) {
                        mPlan.removeLink(((Link)obj).getComponent());
                    }
                }
                return; }
         }
    }
}

