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

package org.netbeans.modules.soa.mapper.basicmapper;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.tree.TreePath;

import org.netbeans.modules.soa.mapper.basicmapper.literal.BasicLiteralEditListener;
import org.netbeans.modules.soa.mapper.basicmapper.methoid.BasicAccumulatingMethoidNode;
import org.netbeans.modules.soa.mapper.basicmapper.methoid.BasicField;
import org.netbeans.modules.soa.mapper.basicmapper.methoid.BasicFieldNode;
import org.netbeans.modules.soa.mapper.basicmapper.methoid.BasicMethoid;
import org.netbeans.modules.soa.mapper.basicmapper.methoid.BasicMethoidNode;
import org.netbeans.modules.soa.mapper.basicmapper.palette.BasicMapperPalette;
import org.netbeans.modules.soa.mapper.basicmapper.util.MapperUtilities;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicController;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicMapper;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicMapperEvent;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicMapperLiteralUpdateEventInfo;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicMapperModel;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicMapperRule;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicViewManager;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicViewModel;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.IMapperCanvasView;
import org.netbeans.modules.soa.mapper.common.basicmapper.literal.ILiteralUpdater;
import org.netbeans.modules.soa.mapper.common.basicmapper.literal.ILiteralUpdaterFactory;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IField;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoid;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoidNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.palette.IPaletteView;
import org.netbeans.modules.soa.mapper.common.basicmapper.tree.IMapperTreeNode;
import org.netbeans.modules.soa.mapper.common.IMapperEvent;
import org.netbeans.modules.soa.mapper.common.IMapperGroupNode;
import org.netbeans.modules.soa.mapper.common.IMapperLink;
import org.netbeans.modules.soa.mapper.common.IMapperListener;
import org.netbeans.modules.soa.mapper.common.IMapperNode;
import org.netbeans.modules.soa.mapper.common.IMapperViewModel;

/**
 * <p>
 *
 * Title: </p> BasicMapper<p>
 *
 * Description: </p> BasicMapper provides a basic look and feel of IBasicMapper
 * interfaces. Itself is a JPanel contains the Mapper view manager and
 * controller, and defaultly layout mapper components in two spliters. The
 * default layout puts the source tree view on the left, the canvas view on the
 * middle, and the destinated tree view on the right. All these mapper view are
 * splits by JSplitPane. The default palette is add to the NORTH of this panel.
 * However, the default paletter has not model.<p>
 *
 * This class is purposely not for directly create. The static method
 * newInstance() returns IBasicMapper is only for tempary usage. There should be
 * a SBYN Component manager that returns a basic mapper, so that the caller
 * application should only know the interfaces but not knowing the
 * implementation class.
 *
 * @author    Un Seng Leong
 * @created   December 4, 2002
 */
public class BasicMapper
        extends JPanel
        implements IBasicMapper {
    
    /**
     * the log instance
     */
    private static final Logger LOGGER = Logger.getLogger(BasicMapper.class.getName());
    
    /**
     * It's used to set correct initialization of the spliters location.
     */
    private boolean painted = false;
    
    /**
     * the left splitpane contains the source tree view and right spliter
     */
    private JSplitPane leftSpliter;
    
    /**
     * the mapper controller
     */
    private IBasicController mMapperController;
    
    /**
     * the mapper rule
     */
    private IBasicMapperRule mMapperRule;
    
    /**
     * the mapper palette
     */
    private IPaletteView mPalette;
    
    /**
     * the view manager
     */
    private IBasicViewManager mViewManager;
    
    /**
     * the right spliter contains the canvas view and the dest tree
     */
    private JSplitPane rightSpliter;
    
    /**
     * Description of the Field
     */
    private float mInitRightDivLocWeight = 0.25f;
    
    /**
     * Description of the Field
     */
    private float mInitLeftDivLocWeight = 0.25f;
    
    /**
     * flag indicates if mapping should be allowed in the mapper.
     */
    private boolean mIsEnableMapping = true;
    
    /**
     * listener for double-click events on input field nodes
     */
    private BasicLiteralEditListener mLiteralEditListener;
    
    static {
        // calling only once for each class
        com.nwoods.jgo.JGoGlobal.setup();
    }
    
    /**
     * Creates a new BasicMapper object.
     */
    public BasicMapper() {
        super();
        
        initializeInstances();
        
        leftSpliter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        rightSpliter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        rightSpliter.setRightComponent(getMapperViewManager().getDestView().getViewComponent());
        rightSpliter.setLeftComponent(leftSpliter);
        
        leftSpliter.setRightComponent(getMapperViewManager().getCanvasView().getViewComponent());
        leftSpliter.setLeftComponent(getMapperViewManager().getSourceView().getViewComponent());
        
        leftSpliter.setDividerSize(6);
        rightSpliter.setDividerSize(6);
        
        leftSpliter.setBorder(null);
        rightSpliter.setBorder(null);
        
        rightSpliter.setResizeWeight(1.0d);
        leftSpliter.setResizeWeight(0.0d);
        
        this.setLayout(new BorderLayout(0, 0));
        this.add(rightSpliter, BorderLayout.CENTER);
        
        setPalette(new BasicMapperPalette(getMapperViewManager()));
    }
    
    /**
     * Return a new instance of mapper. This method is tempary, or for testing
     * purpose. To get a reference from this class, there should be a Sbyn
     * componenet manager to return a IBsicMapper.
     *
     * @return   a new instance of mapper
     */
    public static IBasicMapper newInstance() {
        return new BasicMapper();
    }
    
    /**
     * Return the mapper controller.
     *
     * @return   the mapper controller.
     */
    public IBasicController getMapperController() {
        return mMapperController;
    }
    
    /**
     * Return the model of this mapper.
     *
     * @return   the model of this mapper displaying.
     */
    public IBasicMapperModel getMapperModel() {
        return getMapperViewManager().getMapperModel();
    }
    
    /**
     * Return the mapper rule that handles the permissions of creating mapper
     * object.
     *
     * @return   DOCUMENT ME!
     */
    public IBasicMapperRule getMapperRule() {
        return mMapperRule;
    }
    
    /**
     * Return the view manager of this mapper.
     *
     * @return   the view manager of this mapper.
     */
    public IBasicViewManager getMapperViewManager() {
        return mViewManager;
    }
    
    /**
     * Foward the enbale value to all the view components. Overrides
     * setEnable(boolean) in Component class.
     *
     * @param enable  flag indicates if this mapper should be enable, false
     *      otherwise.
     */
    public void setEnabled(boolean enable) {
        super.setEnabled(enable);
        getMapperViewManager().getSourceView().getTree().setEnabled(enable);
        getMapperViewManager().getDestView().getTree().setEnabled(enable);
        getMapperViewManager().getCanvasView().getCanvas().getUIComponent().setEnabled(enable);
        if (getMapperViewManager().getPaletteView() != null) {
            getMapperViewManager().getPaletteView().getPaletteComponent().setEnabled(enable);
        }
    }
    
    /**
     * Return true is mapping enable, false otherwise. Mapping enable allows
     * user to use mapper normal, if disable, no mouse event goes through the
     * mapper.
     *
     * @return   Return true is mapping enable, false otherwise.
     */
    public boolean isMappingEnable() {
        return mIsEnableMapping;
    }
    
    /**
     * Sets if mapping enable. Mapping enable allows user to use mapper normal,
     * if disable, no mouse event goes through the mapper.
     *
     * @param isMappingEnable  true is mapping enable, false otherwise.
     */
    public void setMappingEnable(boolean isMappingEnable) {
        mIsEnableMapping = isMappingEnable;
        getMapperViewManager().getCanvasView().setIsMapable(isMappingEnable);
        getMapperViewManager().getSourceView().setIsMapable(isMappingEnable);
        getMapperViewManager().getDestView().setIsMapable(isMappingEnable);
    }
    
    /**
     * Set the mapper model to this mapper.
     *
     * @param model  The new mapperModel value
     */
    public void setMapperModel(IBasicMapperModel model) {
        ((BasicViewManager) getMapperViewManager()).setMapperModel(model);
    }
    
    /**
     * Create a new defualt basic mapper model, which contains a default basic
     * view model.
     *
     * @return   a new default basic mapper model.
     */
    public IBasicMapperModel createDefaultMapperModel() {
        BasicMapperModel model = new BasicMapperModel();
        IBasicViewModel viewModel = createDefaultViewModel();
        model.setSelectedViewModel(viewModel);
        return model;
    }
    
    /**
     * Create a new default basic view model.
     *
     * @return   a new default basic view model.
     */
    public IBasicViewModel createDefaultViewModel() {
        return new BasicViewModel();
    }
    
    /**
     * Set the mapper rule that handle the permissions of creating mapper
     * object.
     *
     * @param rule  the mapper rule of this mapper.
     */
    public void setMapperRule(IBasicMapperRule rule) {
        if (mMapperRule != null) {
            this.removeMapperListener(mMapperRule);
        }
        
        mMapperRule = rule;
        addMapperListener(mMapperRule);
    }
    
    /**
     * Gets the palette attribute of the BasicMapper object
     *
     * @return   The palette value
     */
    public IPaletteView getPalette() {
        return mPalette;
    }
    
    /**
     * Sets the palette attribute of the BasicMapper object
     *
     * @param view  The new palette value
     */
    public void setPalette(IPaletteView view) {
        if (mPalette != null) {
            remove(mPalette.getViewComponent());
        }
        mPalette = view;
        if (mPalette != null) {
            add(mPalette.getViewComponent(), BorderLayout.NORTH);
            ((BasicViewManager) getMapperViewManager()).setPaletteView(mPalette);
            mPalette.getPaletteComponent().setEnabled(isEnabled());
        }
    }
    
    /**
     * Retrun a mapper link that connects the fromNode to toNode. And add the
     * new link to the fromNode and toNode.
     *
     * @param fromNode  the start node of the link
     * @param toNode    the end node of the link
     * @return          a mapper link that connects the fromNode to toNode.
     */
    public IMapperLink createLink(IMapperNode fromNode, IMapperNode toNode) {
        MapperLink link = new MapperLink(fromNode, toNode);
        return link;
    }
    
    /**
     * Connected the sepecified link to its contained node. This method called
     * link.getStartNode().addLink and link.getEndNode.addLink to add the
     * specified link to the mapper model.
     *
     * @param link  the link to be connected
     */
    public void connectLink(IMapperLink link) {
        if (link.getStartNode() != null) {
            link.getStartNode().addLink(link);
        }
        if (link.getEndNode() != null) {
            link.getEndNode().addLink(link);
        }
    }
    
    /**
     * Return a methoid field with all the specified basic field information.
     *
     * @param name      the name of this field.
     * @param type      the tyoe of this field.
     * @param tooltip   the tooltip text of this field
     * @param data      the data object of this field
     * @param isInput   flag indicates if this is an input field
     * @param isOutput  flag indicates if this is an output field
     * @param literalInfo optional literal info
     * @return          Description of the Return Value
     */
    public IField createField(String name, String type, String tooltip,
            Object data, boolean isInput, boolean isOutput, ILiteralUpdater literalUpdater) {
        return new BasicField(name, type, tooltip, data, isInput, isOutput, literalUpdater);
    }
    
    /**
     * Return a field Node with the specified field.
     *
     * @param field  the field of the node.
     * @return       a field Node with the specified field.
     */
    public IFieldNode createFieldNode(IField field) {
        return new BasicFieldNode(field);
    }
    
    /**
     * Return a newly created mapper node with the specified methoid object.
     *
     * @param methoid  the methoid object of this mapper node repersents.
     * @return         Description of the Return Value
     */
    public IMethoidNode createMethoidNode(IMethoid methoid) {
        BasicMethoidNode node = null;
        if (methoid.isAccumulative()) {
            node = new BasicAccumulatingMethoidNode(methoid);
        } else {
            node = new BasicMethoidNode(methoid);
        }
        return node;
    }
    
    /**
     * Return a new mapper event by specifying the properties of the event.
     *
     * @param source        the source of the event.
     * @param transferData  the data object to be transfer.
     * @param eventType     the type of the event.
     * @param eventDesc     the description of the event.
     * @return              a new mapper event by specifying the properties of
     *      the event.
     */
    public IMapperEvent createMapperEvent(Object source, Object transferData,
            String eventType, String eventDesc) {
        
        return MapperUtilities.getMapperEvent(source, transferData, eventType, eventDesc);
    }
    
    /**
     * Return a new methoid object with specified all properties of the methoid.
     *
     * @param icon            the icon of this methoid.
     * @param name            the name of this funcotid.
     * @param tooltip         the tooltip of this methoid.
     * @param data            the data of this methoid.
     * @param namespace       the namespace of this methoid.
     * @param inputFields     the input fields of this methoid.
     * @param outputFields    the output fields of this methoid.
     * @param isAccumulative  whether the input fields can grow dynamically
     * @return              a new methoid object with specified all properties
     *      of the methoid.
     */
    public IMethoid createMethoid(Icon icon, String name, String tooltip,
            Object data, IField namespace, List inputFields, List outputFields,
            boolean isAccumulative, boolean isLiteral) {
        return new BasicMethoid(icon, name, tooltip, data, namespace,
                inputFields, outputFields, isAccumulative, isLiteral);
    }
    
    /**
     * Return a new mapper tree node that contains the specified tree path from
     * the specified tree. The tree node may have been already in the model due
     * to the current framework requires there should not have the same mapper
     * tree node in the model. To instaninate a brand new instance of the mapper
     * tree node, use the clone method.
     *
     * @param treePath      the tree path to be store in the mapper tree node.
     * @param isSourceTree  true if the the mapper tree node is from source
     *      tree, false otherwise.
     * @return              a new mapper tree node of the specified tree that
     *      contains the specified tree path.
     */
    public IMapperTreeNode createMapperTreeNode(TreePath treePath, boolean isSourceTree) {
        if (isSourceTree) {
            return getMapperViewManager().getSourceView().getMapperTreeNode(treePath);
        } else {
            return getMapperViewManager().getDestView().getMapperTreeNode(treePath);
        }
    }
    
    /**
     * Add a link to this mapper. This method add the link to the start node and
     * end node if the link is not in the nodes yet. Next, add the start and end
     * node to the model, if any of the two node is a group node, it only adds
     * the root node to the model. Finally, it posts a IMapperEvent.NEW_LINK
     * event.
     *
     * @param link  the link to be added.
     */
    public void addLink(IMapperLink link) {
        IMapperNode startNode = link.getStartNode();
        IMapperNode endNode = link.getEndNode();
        connectLink(link);
        if ((getMapperViewManager().getMapperModel() != null)
        && (getMapperViewManager().getMapperModel().getSelectedViewModel() != null)) {
            IMapperViewModel viewModel = getMapperViewManager().getMapperModel().getSelectedViewModel();
            
            if (startNode != null) {
                IMapperNode groupNode = startNode;
                
                while (groupNode.getGroupNode() != null) {
                    groupNode = groupNode.getGroupNode();
                }
                
                if (!(viewModel.containsNode(groupNode))) {
                    addNode(groupNode);
                }
            }
            
            if (endNode != null) {
                IMapperNode groupNode = endNode;
                
                while (groupNode.getGroupNode() != null) {
                    groupNode = groupNode.getGroupNode();
                }
                
                if (!(viewModel.containsNode(groupNode))) {
                    addNode(groupNode);
                }
            }
        }
        
        
        getMapperViewManager().postMapperEvent(
                MapperUtilities.getMapperEvent(this, link,
                IMapperEvent.LINK_ADDED, "New Link added to mapper"));
    }
    
    /**
     * Add a node to this mapper. Node is added (to fire event) only if there is
     * a selected view model in the mapper model. Then a IMapperEvent.NODE_ADDED
     * event is posted.
     *
     * @param node  the group node to be added.
     */
    public void addNode(IMapperNode node) {
        if ((getMapperViewManager().getMapperModel() != null)
        && (getMapperViewManager().getMapperModel().getSelectedViewModel() != null)) {
            
            IMapperViewModel viewModel = getMapperViewManager().getMapperModel().getSelectedViewModel();
            viewModel.addNode(node);
            
            getMapperViewManager().postMapperEvent(
                    MapperUtilities.getMapperEvent(this, node,
                    IMapperEvent.NODE_ADDED, "New Node added to mapper"));
        }
    }
    
    /**
     * Remove a link from this mapper. If any link end point nodes are tree
     * node, and no other links connect to the node. The tree node will also be
     * removed.
     *
     * @param link  the link to be remove from this mapper.
     */
    public void removeLink(IMapperLink link) {
        IMapperNode startNode = link.getStartNode();
        IMapperNode endNode = link.getEndNode();
        if (startNode != null) {
            startNode.removeLink(link);
        }
        if (endNode != null) {
            endNode.removeLink(link);
        }
        
        if ((getMapperViewManager().getMapperModel() != null)
        && (getMapperViewManager().getMapperModel().getSelectedViewModel() != null)) {
            IMapperViewModel viewModel = getMapperViewManager().getMapperModel().getSelectedViewModel();
            
            if (startNode instanceof IMapperTreeNode
                    && (startNode.getLinkCount() == 0)) {
                viewModel.removeNode(startNode);
            }
            
            if (endNode instanceof IMapperTreeNode
                    && (endNode.getLinkCount() == 0)) {
                viewModel.removeNode(endNode);
            }
        }
        
        getMapperViewManager().postMapperEvent(
                MapperUtilities.getMapperEvent(this, link,
                IMapperEvent.LINK_DEL, "Link removed from mapper"));
    }
    
    /**
     * Remove a mapper node from this mapper. Node is removed only if the node
     * is in the current selected view model. First removed all the links
     * connected to this node by calling removeNodeLinks. Then node is removed
     * from the view model. Finally IMapperEvent.NODE_DEL event is posted.
     *
     * @param node  the node to be removed.
     */
    public void removeNode(IMapperNode node) {
        if ((getMapperViewManager().getMapperModel() != null)
        && (getMapperViewManager().getMapperModel().getSelectedViewModel() != null)) {
            
            IMapperViewModel viewModel = getMapperViewManager().getMapperModel().getSelectedViewModel();
            
            if (viewModel.containsNode(node)) {
                removeNodeLinks(node);
                viewModel.removeNode(node);
                getMapperViewManager().postMapperEvent(
                        MapperUtilities.getMapperEvent(this, node,
                        IMapperEvent.NODE_DEL, "Node removed from mapper"));
            }
        }
    }
    
    public void paint(Graphics g) {
        super.paint(g);
        painted = true;
    }
    
    /**
     * This method calls the super.doLayout() to calcute the size of this panel,
     * then using the size of the panel to initialize the divider location of
     * the mapper.
     */
    public void doLayout() {
        super.doLayout();
        if (!painted && getWidth() > 0) {
            //
            int length = getWidth();
            //
            int rightDivLoc = 0;
            int leftDivLoc = 0;
            if (mInitLeftDivLocWeight == 1.0f) {
                rightDivLoc = length;
                leftDivLoc = length;
            } else if (mInitRightDivLocWeight != 1.0f) {
                rightDivLoc = (int) (length * (1 - mInitRightDivLocWeight));
                leftDivLoc = (int) (length * mInitLeftDivLocWeight);
            }

            LOGGER.fine("leftSpliter divider=" + leftDivLoc);
            LOGGER.fine("rightSpliter divider=" + rightDivLoc);
            rightSpliter.setDividerLocation(rightDivLoc);
            leftSpliter.setDividerLocation(leftDivLoc);
        }
    }
    
    /**
     * Set the viewable size factor of the total width betweem the destination
     * tree view and the mapper when the mapper first show up to the screen. The
     * default value is 0.25.
     *
     * @param destTreeWeight  the destination tree viewable size weight.
     *      IllegalArgumentException is thrown if the specified weight is not
     *      between 0.0 - 1.0.
     */
    public void setInitialDestTreeViewableWeight(float destTreeWeight) {
        if (destTreeWeight < 0.0f) {
            throw new java.lang.IllegalArgumentException(
                    "Initial dest tree viewable weight cannot lesser than 0.0");
        } else if (destTreeWeight > 1.0f) {
            throw new java.lang.IllegalArgumentException(
                    "Initial dest tree viewable weight cannot greater than 1.0");
        }
        mInitRightDivLocWeight = destTreeWeight;
    }
    
    /**
     * Set the viewable size factor of the total width betweem the source tree
     * view and the mapper when the mapper first show up to the screen. The
     * default value is 0.25.
     *
     * @param sourceTreeWeight  the source tree viewable size weight
     *      IllegalArgumentException is thrown if the specified weight is not
     *      between 0.0 - 1.0.
     */
    public void setInitialSourceTreeViewableWeight(float sourceTreeWeight) {
        if (sourceTreeWeight < 0.0f) {
            throw new java.lang.IllegalArgumentException(
                    "Initial source tree viewable weight cannot lesser than zero");
        } else if (sourceTreeWeight > 1.0f) {
            throw new java.lang.IllegalArgumentException(
                    "Initial source tree viewable weight cannot greater than 1.0");
        }
        mInitLeftDivLocWeight = sourceTreeWeight;
    }
    
    /**
     * Return the viewable size factor of the total width betweem the
     * destination tree view and the mapper when the mapper first show up to the
     * screen. The default value is 0.25.
     *
     * @return   the destination tree viewable size weight
     */
    public float getInitialDestTreeViewableWeight() {
        return mInitRightDivLocWeight;
    }
    
    /**
     * Return the viewable size factor of the total width betweem the
     * destination tree view and the mapper when the mapper first show up to the
     * screen. The default value is 0.25.
     *
     * @return   the source tree viewable size weight
     */
    public float getInitialSourceTreeViewableWeight() {
        return mInitRightDivLocWeight;
    }
    
    /**
     * Create a default mapper rule.
     *
     * @return   a default mapper rule.
     */
    public IBasicMapperRule createDefaultMapperRule() {
        return new BasicMapperRule(this);
    }
    
    /**
     * Return the visual repersentation of this mapper. This method return a
     * JPanel contains all the dest and source tree and the canvas.
     *
     * @return   the visual repersentation of this mapper.
     */
    public Component getVisualMapper() {
        return this;
    }
    
    /**
     * Add a mapper listener to listening to mapper events. Available mapper
     * Event is defined in IMapperEvent.
     *
     * @param listener  the mapper listener to be added.
     */
    public void addMapperListener(IMapperListener listener) {
        mMapperController.addMapperListener(listener);
    }
    
    /**
     * Remove a mapper listener from this mapper.
     *
     * @param listener  the mapper listener to be removed.
     */
    public void removeMapperListener(IMapperListener listener) {
        mMapperController.removeMapperListener(listener);
    }
    
    /**
     * Copy visited nodes and links by the specified link to the specified
     * model. This method does not aggressively copy all the links from a mapper
     * tree node. It copy those tree links when visited. It treats mapper tree
     * node is either start point of a chain or an end point of a chain.
     *
     * @param link   the link of the chain start point
     * @param model  the model to put the visited node to.
     * @return       Description of the Return Value
     */
    public IMapperLink copyDirectedChain(IMapperLink link, IBasicViewModel model) {
        return copyDirectedChain(link, model, new HashMap());
    }
    
    /**
     * Copy visited nodes and links by the specified node to the model. This
     * method does not aggressively copy all the links in a mapper tree node. It
     * copy those tree links when visited. It treats mapper tree node is either
     * start point of a chain of end point of a chain. If the specified node is
     * a mapper tree node, only a copy of the tree node will be returned.
     *
     * @param node   the node of the chain start point
     * @param model  the model to put the new chain.
     * @return       the new node that connects with the new chain
     */
    public IMapperNode copyDirectedChain(IMapperNode node, IBasicViewModel model) {
        return copyDirectedChain(node, model, new HashMap());
    }
    
    /**
     * This method do the real work of copying the directed link chain to the
     * model.
     *
     * @param link           the link of the chain start point
     * @param model          the model to put the new chain to.
     * @param visitedObjMap  the link and node that has been visited. Key is old
     *      and value is new.
     * @return               the new link that connects with the new chain
     */
    protected IMapperLink copyDirectedChain(
            IMapperLink link, IBasicViewModel model, Map visitedObjMap) {
        
        if (visitedObjMap.containsKey(link)) {
            return (IMapperLink) visitedObjMap.get(link);
        }
        
        MapperLink newLink = new MapperLink();
        visitedObjMap.put(link, newLink);
        
        IMapperNode clonedStartNode =
                copyDirectedChain(link.getStartNode(), model, visitedObjMap);
        IMapperNode clonedEndNode =
                copyDirectedChain(link.getEndNode(), model, visitedObjMap);
        
        newLink.setStartNode(clonedStartNode);
        newLink.setEndNode(clonedEndNode);
        
        clonedStartNode.addLink(newLink);
        clonedEndNode.addLink(newLink);
        
        return newLink;
    }
    
    /**
     * This method do the real work of copying the directed node chain to the
     * model.
     *
     * @param node           the node of the chain start point
     * @param model          the model to put the new chain to.
     * @param visitedObjMap  the link and node that has been visited. Key is old
     *      and value is new.
     * @return               the new node that connects with the new chain
     */
    public IMapperNode copyDirectedChain(
            IMapperNode node, IBasicViewModel model, Map visitedObjMap) {
        
        if (visitedObjMap.containsKey(node)) {
            return (IMapperNode) visitedObjMap.get(node);
        }
        
        IMapperNode newNode = null;
        if (node instanceof IMapperTreeNode) {
            newNode = cloneMapperTreeNode((IMapperTreeNode) node, model, visitedObjMap);
            
        } else if (node instanceof IFieldNode) {
            newNode = cloneFieldNode((IFieldNode) node, model, visitedObjMap);
            
        } else if (node instanceof IMethoidNode) {
            newNode = cloneMethoidNode((IMethoidNode) node, model, visitedObjMap);
        }
        
        return newNode;
    }
    
    /**
     * Return the a newly created field node that cloned by the specified field
     * node. This method calls cloneMethoidNode to clone the group node and then
     * find the position of the field node in the new group and return it. The
     * actural creation of the field node is in cloneMethoidNode method.
     *
     * @param node           the field node to be cloned
     * @param model          the model to put the new node to.
     * @param visitedObjMap  the link and node that has been visited. Key is old
     *      and value is new.
     * @return               the cloned field node, or the one in the visited
     *      object map.
     */
    protected IFieldNode cloneFieldNode(
            IFieldNode node, IBasicViewModel model, Map visitedObjMap) {
        
        IMapperGroupNode orgGroupNode = node.getGroupNode();
        IMapperGroupNode newGroupNode = (IMapperGroupNode)
        copyDirectedChain(orgGroupNode, model, visitedObjMap);
        
        return (IFieldNode) visitedObjMap.get(node);
    }
    
    /**
     * Return a newly cloned methoid node. This method calls createMethoidNode
     * by passing the specified methoid node's methoid object to instance a new
     * methoid node. It then add each of its new field node to the visited map,
     * and call cloneDirectedChain to clone each link in the field node.
     *
     * @param orgMethoidNode  the methoid node to be cloned
     * @param model           the model to put the new node to.
     * @param visitedObjMap   the link and node that has been visited. Key is
     *      old and value is new.
     * @return                a newly cloned methoid node, or the one in the
     *      visited object map.
     */
    protected IMapperNode cloneMethoidNode(
            IMethoidNode orgMethoidNode, IBasicViewModel model, Map visitedObjMap) {
        
        IMethoidNode newMethoidNode = (IMethoidNode) orgMethoidNode.clone();
        
        visitedObjMap.put(orgMethoidNode, newMethoidNode);
        model.addNode(newMethoidNode);
        
        IMapperNode newFieldNode = newMethoidNode.getFirstNode();
        for (IMapperNode orgFieldNode = orgMethoidNode.getFirstNode();
        orgFieldNode != null && newFieldNode != null;
        orgFieldNode = orgMethoidNode.getNextNode(orgFieldNode)) {
            
            visitedObjMap.put(orgFieldNode, newFieldNode);
            newFieldNode = newMethoidNode.getNextNode(newFieldNode);
        }
        
        for (IMapperNode orgFieldNode = orgMethoidNode.getFirstNode();
        orgFieldNode != null;
        orgFieldNode = orgMethoidNode.getNextNode(orgFieldNode)) {
            
            List links = orgFieldNode.getLinks();
            for (int i = 0; i < links.size(); i++) {
                copyDirectedChain((IMapperLink) links.get(i), model, visitedObjMap);
            }
        }
        return newMethoidNode;
    }
    
    /**
     * Return a newly cloned mapper tree node.
     *
     * @param node           the tree node to be cloned
     * @param model          the model to put the new node to.
     * @param visitedObjMap  the link and node that has been visited. Key is old
     *      and value is new.
     * @return               a newly cloned mapper tree node, or the one in the
     *      visited object map.
     */
    protected IMapperTreeNode cloneMapperTreeNode(
            IMapperTreeNode node, IBasicViewModel model, Map visitedObjMap) {
        
        IMapperTreeNode newNode = (IMapperTreeNode) node.clone();
        model.addNode(newNode);
        visitedObjMap.put(node, newNode);
        return newNode;
    }
    
    /**
     * Move a directed chain, start by the specified link, from the old model,
     * to the new model.
     *
     * @param link      the chain start point
     * @param oldModel  the old model which the chain nodes will be removed
     *      from.
     * @param newModel  the new model which the chain nodes will be stored to.
     */
    public void moveDirectedChain(IMapperLink link, IBasicViewModel oldModel, IBasicViewModel newModel) {
        moveDirectedChain(link, oldModel, newModel, new HashMap());
    }
    
    /**
     * Move a directed chain, start by the specified link, from the old model,
     * to the new model.
     *
     * @param link         the chain start point
     * @param oldModel     the old model which the chain nodes will be removed
     *      from.
     * @param newModel     the new model which the chain nodes will be stored
     *      to.
     * @param visitedObjs  the map contains the node in the old model as the
     *      key, and the cooresponding node in the new model as the value.
     *      Mostly of the time the key node and the value node is the same.
     */
    public void moveDirectedChain(
            IMapperLink link, IBasicViewModel oldModel, IBasicViewModel newModel, Map visitedObjs) {
        
        IMapperNode startNode = link.getStartNode();
        IMapperNode endNode = link.getEndNode();
        
        startNode.removeLink(link);
        endNode.removeLink(link);
        
        if (!visitedObjs.containsKey(startNode)) {
            moveDirectedChain(startNode, oldModel, newModel, visitedObjs);
        }
        startNode = (IMapperNode) visitedObjs.get(startNode);
        if (startNode != null) {
            link.setStartNode(startNode);
            startNode.addLink(link);
        }
        
        if (!visitedObjs.containsKey(endNode)) {
            moveDirectedChain(endNode, oldModel, newModel, visitedObjs);
        }
        endNode = (IMapperNode) visitedObjs.get(endNode);
        if (endNode != null) {
            link.setEndNode(endNode);
            endNode.addLink(link);
        }
    }
    
    /**
     * Move a directed chain, start by the specified node, from the old model,
     * to the new model. For mapper tree node, there might happen that the tree
     * node requires to stay in the old model, therefore, a clone one will be
     * added to the new model. For other mapper nodes, they are moved to the new
     * model.
     *
     * @param node      the chain start point
     * @param oldModel  the old model which the chain nodes will be removed
     *      from.
     * @param newModel  the new model which the chain nodes will be stored to.
     */
    public void moveDirectedChain(IMapperNode node, IBasicViewModel oldModel, IBasicViewModel newModel) {
        moveDirectedChain(node, oldModel, newModel, new HashMap());
    }
    
    /**
     * Move a directed chain, start by the specified node, from the old model,
     * to the new model. For mapper tree node, there might happen that the tree
     * node requires to stay in the old model, therefore, a clone one will be
     * added to the new model. For other mapper nodes, they are moved to the new
     * model.
     *
     * @param node         the chain start point
     * @param oldModel     the old model which the chain nodes will be removed
     *      from.
     * @param newModel     the new model which the chain nodes will be stored
     *      to.
     * @param visitedObjs  the map contains the node in the old model as the
     *      key, and the cooresponding node in the new model as the value.
     *      Mostly of the time the key node and the value node is the same.
     */
    public void moveDirectedChain(
            IMapperNode node, IBasicViewModel oldModel, IBasicViewModel newModel, Map visitedObjs) {
        
        // make sure the tree node is clean
        // from the old model when no more links are
        // in the tree model
        if (node instanceof IMapperTreeNode
                && node.getLinkCount() <= 0
                && oldModel.containsNode(node)) {
            
            oldModel.removeNode(node);
        }
        
        // already visited, no need to move node again
        if (visitedObjs.containsKey(node)) {
            return;
        }
        
        // starting moving node tranvserly
        if (node instanceof IMapperTreeNode) {
            
            // if tree node has no link, use it to the new model
            // otherwise, clone a new one and save into the new model
            IMapperNode newNode =
                    (node.getLinkCount() <= 0) ? node : (IMapperTreeNode) node.clone();
            visitedObjs.put(node, newNode);
            newModel.addNode(newNode);
            
        } else if (node instanceof IFieldNode) {
            
            // the IMethoidNode handling will handle children (FieldNode)
            moveDirectedChain(node.getGroupNode(), oldModel, newModel, visitedObjs);
            
        } else if (node instanceof IMethoidNode) {
            
            // Loop through the children and flag them to be visitied and
            // loop through links of the child and ask them to
            // move to new model. Then, remove the MehtoidNode from old
            // and add to the new model.
            IMethoidNode newNode = (IMethoidNode) node;
            for (IMapperNode nextNode = newNode.getFirstNode();
            nextNode != null;
            nextNode = newNode.getNextNode(nextNode)) {
                
                // flaged this node is visited.
                visitedObjs.put(nextNode, nextNode);
                List links = nextNode.getLinks();
                for (int i = 0; i < links.size(); i++) {
                    IMapperLink link = (IMapperLink) links.get(i);
                    moveDirectedChain(link, oldModel, newModel, visitedObjs);
                }
            }
            
            // flaged this node is visited.
            visitedObjs.put(newNode, newNode);
            if (oldModel.containsNode(newNode)) {
                oldModel.removeNode(newNode);
            }
            if (!newModel.containsNode(newNode)) {
                newModel.addNode(newNode);
            }
        }
    }
    
    /**
     * Add a list of mapper object into the specified mode. This method handles
     * IMapperLink and IMapperNode as the elements in the specified Collection.
     * For IMapperLink, it calls connectLink. For IMapperNode, it will add the
     * top level parent node (IMapperNode.getGroupNode() == null) to the model.
     *
     * @param mapperObjs  the list of mapper object to be added
     * @param model       the model to store the objects.
     */
    public void addObjectsToModel(Collection mapperObjs, IBasicViewModel model) {
        Iterator objIter = mapperObjs.iterator();
        while (objIter.hasNext()) {
            Object obj = objIter.next();
            if (obj instanceof IMapperLink) {
                connectLink((IMapperLink) obj);
            } else if (obj instanceof IMapperNode) {
                while (((IMapperNode) obj).getGroupNode() != null) {
                    obj = ((IMapperNode) obj).getGroupNode();
                }
                model.addNode(((IMapperNode) obj));
            }
        }
    }
    
    /**
     * Remove a directed chian start from the specified link. The method remove
     * all the links and nodes that it visites, however, it will not
     * aggressively remove all the tree node links, only remove the one is
     * visited.
     *
     * @param link   the link of the chian start point
     * @param model  the model that contains all the chain nodes
     */
    public void removeDirectedChain(IMapperLink link, IBasicViewModel model) {
        removeDirectedChain(link, model, new ArrayList());
    }
    
    /**
     * Close this mapper, release system resource.
     */
    public void close() {
        mMapperController.releaseControl();
        if (this.mPalette != null) {
            mPalette.close();
        }
        // get away JGo possible memory leak
        if (com.nwoods.jgo.JGoGlobal.getComponent() == mViewManager.getCanvasView().getCanvas()) {
            com.nwoods.jgo.JGoGlobal.setComponent(null);
        }
    }
    
    /**
     * Remove a directed chian start from the specified link. The method remove
     * all the links and nodes that it visites, however, it will not
     * aggressively remove all the tree node links, only remove the one is
     * visited. The removed object is stored in the specified list including the
     * links and nodes from the model.
     *
     * @param link        the link of the chian start point
     * @param model       the model that contains all the chain nodes
     * @param visitedObj  storing all the visited object in this storage
     */
    protected void removeDirectedChain(IMapperLink link, IBasicViewModel model, List visitedObj) {
        if (!visitedObj.contains(link)) {
            visitedObj.add(link);
        }
        IMapperNode startNode = link.getStartNode();
        IMapperNode endNode = link.getEndNode();
        
        startNode.removeLink(link);
        endNode.removeLink(link);
        
        removeDirectedChain(startNode, model, visitedObj);
        removeDirectedChain(endNode, model, visitedObj);
    }
    
    /**
     * Remove a directed chian start from the specified node. The method remove
     * all the links and nodes that it visites, however, it will not
     * aggressively remove all the tree node links, only remove the one is
     * visited.
     *
     * @param model  the model that contains all the chain nodes
     * @param node   Description of the Parameter
     */
    public void removeDirectedChain(IMapperNode node, IBasicViewModel model) {
        removeDirectedChain(node, model, new ArrayList());
    }
    
    /**
     * Remove a directed chian start from the specified node. The method remove
     * all the links and nodes that it visites, however, it will not
     * aggressively remove all the tree node links, only remove the one is
     * visited. The removed object is stored in the specified list including the
     * links and nodes from the model. If the specified node is a tree node, it
     * will not remove any link from the node; since it does not know which link
     * to start the chain, or is the end of a chain.
     *
     * @param model       the model that contains all the chain nodes
     * @param visitedObj  storing all the visited object in this storage
     * @param node        the start point of the chain to be removed
     */
    protected void removeDirectedChain(IMapperNode node, IBasicViewModel model, List visitedObj) {
        if (node.getGroupNode() != null) {
            removeDirectedChain(node.getGroupNode(), model, visitedObj);
            List links = node.getLinks();
            for (int i = 0; i < links.size(); i++) {
                removeDirectedChain((IMapperLink) links.get(i), model, visitedObj);
            }
        } else if (node instanceof IMapperGroupNode) {
            if (model.containsNode(node)) {
                model.removeNode(node);
                visitedObj.add(node);
                for (IMapperNode childNode = ((IMapperGroupNode) node).getFirstNode();
                childNode != null;
                childNode = ((IMapperGroupNode) node).getNextNode(childNode)) {
                    removeDirectedChain(childNode, model, visitedObj);
                }
            }
        } else if (node instanceof IMapperTreeNode) {
            if (!visitedObj.contains(node)) {
                visitedObj.add(node);
            }
            if (node.getLinkCount() == 0) {
                model.removeNode(node);
            }
        }
    }
    
    /**
     * Traverse through all the nodes contained by this group node, and call
     * removeNodeLinks to remove all the child nodes' links.
     *
     * @param groupNode  the group node to be remove links.
     */
    protected void removeGroupNodeLinks(IMapperGroupNode groupNode) {
        IMapperNode node = groupNode.getFirstNode();
        
        for (; node != null; node = groupNode.getNextNode(node)) {
            removeNodeLinks(node);
        }
    }
    
    /**
     * Remove all the links on this node. If node is a group node,
     * removeGroupNodeLinks is called.
     *
     * @param node  the node to be removed its links.
     */
    protected void removeNodeLinks(IMapperNode node) {
        if (node instanceof IMapperGroupNode) {
            removeGroupNodeLinks((IMapperGroupNode) node);
        }
        
        List links = node.getLinks();
        for (int i = 0; i < links.size(); i++) {
            this.removeLink((IMapperLink) links.get(i));
        }
    }
    
    /**
     * Initialize all the instances of mapper related.
     */
    private synchronized void initializeInstances() {
        // initialize view
        mViewManager = new BasicViewManager();
        
        // initialize model
        setMapperModel(createDefaultMapperModel());
        
        // initialize controller
        mMapperController = new BasicMapperController(getMapperViewManager());
        
        // initailize mapper event listener
        setMapperRule(createDefaultMapperRule());
    }
    
    /**
     * Set the factory which handles setting up literal info on
     * field objects created from the palette.
     */
    public void setLiteralUpdaterFactory(ILiteralUpdaterFactory literalUpdaterFactory) {
        if (mPalette instanceof BasicMapperPalette) {
            BasicMapperPalette basicPalette = (BasicMapperPalette) mPalette;
            basicPalette.setLiteralUpdaterFactory(literalUpdaterFactory);
        }
        // also set up listener on literal edit events
        if (mLiteralEditListener == null) {
            IMapperCanvasView canvasView = getMapperViewManager().getCanvasView();
            mLiteralEditListener = new BasicLiteralEditListener(this);
            canvasView.getCanvas().addCanvasMouseListener(mLiteralEditListener);
        }
    }
    
    /**
     * Called when the mapper detects that a field node has been set
     * with a new literal.
     */
    public void updateFieldLiteral(IBasicMapperLiteralUpdateEventInfo info) {
        getMapperViewManager().postMapperEvent(
                MapperUtilities.getMapperEvent(
                this,
                info,
                IBasicMapperEvent.FIELD_LITERAL_SET,
                "Field literal set: " + info.getFieldNode()));
        
    }
}
