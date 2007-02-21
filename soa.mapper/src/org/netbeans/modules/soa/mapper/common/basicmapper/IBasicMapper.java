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

package org.netbeans.modules.soa.mapper.common.basicmapper;

import java.awt.Component;
import java.util.Collection;
import java.util.List;

import javax.swing.Icon;
import javax.swing.tree.TreePath;

import org.netbeans.modules.soa.mapper.common.basicmapper.literal.ILiteralUpdater;
import org.netbeans.modules.soa.mapper.common.basicmapper.literal.ILiteralUpdaterFactory;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IField;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoid;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoidNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.palette.IPaletteView;
import org.netbeans.modules.soa.mapper.common.basicmapper.tree.IMapperTreeNode;
import org.netbeans.modules.soa.mapper.common.IMapperEvent;
import org.netbeans.modules.soa.mapper.common.IMapperLink;
import org.netbeans.modules.soa.mapper.common.IMapperListener;
import org.netbeans.modules.soa.mapper.common.IMapperNode;

/**
 * <p>
 *
 * Title: Mapper Interface </p> <p>
 *
 * Description: Generic interface describe a mapper component. This Mapper
 * should be the only instance that add or remove link and group node into
 * model, within the design of Mapper. Without a MapperRule set to a mapper. No
 * link or group node should be created or removed unless some other customize
 * MapperListener listen on those request events and calls new or remove link
 * and group node of this interface, accordingly. </p> <p>
 *
 * The design of Mapper framework provides as much as flexiblility for caller to
 * provides its own implementation base on the default implemenation. However,
 * the base line is not able to change the java.awt.Component that this mapper
 * views repersents, except palette view. For Example: To add this mapper to a
 * window, you can <pre>
 *     public class MapperWin extends JFrame () {
 *
 *         public MapperWin () {
 *             IBasicMapper mapper = (get a reference of mapper implementation)
 *             this.getContentPane().add (mapper.getVisualMapper(), BorderLayout.CENTER);
 *             ...
 *             ...
 *         }
 *     }
 * </pre> To provide a customized canvas field node, proivdes a new class that
 * implements ICanvasObjectFactory and set the customized factory to the canvas.
 * <pre>
 *     class MyFactory implements ICanvasObjectFactory {
 *       ICanvasObjectFactory defaultFactory;
 *       public MyFactory (ICanvasObjectFactory defaultFactory) {
 *          this.defaultFactory = defaultFoctory;
 *       }
 *
 *       ....
 *       ....
 *
 *       public ICanvasMapperLink createLink(IMapperLink link) {
 *         return defaultFactory.createLink(link);
 *       }
 *
 *       public ICanvasFieldNode createFieldNode(IMapperFieldNode node) {
 *         ICanvasFieldNode customizedNode = (do your own implemnatin of field node).
 *         ....
 *         ....
 *         return customizedNode;
 *       }
 *     }
 *
 *     class MyMapper {
 *
 *       public void initialize() {
 *         IBasicMapper mapper = (get a reference of mapper implementation)
 *         ICanvasObjectFactory defaultFactory =
 *           mapper.getMapperViewManager().getCanvasView()
 *             .getCanvas().getCanvasObjectFactory();
 *         mapper.getMapperViewManager().getCanvasView()
 *           .getCanvas().setCanvasObjectFactory (new MyFactory (defaultFactory));
 *         ....
 *         ....
 *       }
 *     }
 * </pre> Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 4, 2002
 * @version   1.0
 */
public interface IBasicMapper {

    /**
     * Return the model of this mapper.
     *
     * @return   the model of this mapper displaying.
     */
    public IBasicMapperModel getMapperModel();

    /**
     * Set the mapper model to this mapper.
     *
     * @param mapperModel  the model to be displayed
     */
    public void setMapperModel(IBasicMapperModel mapperModel);

    /**
     * Create a new default basic mapper model.
     *
     * @return   a new default basic mapper model.
     */
    public IBasicMapperModel createDefaultMapperModel();

    /**
     * Create a new default basic view model.
     *
     * @return   a new default basic view model.
     */
    public IBasicViewModel createDefaultViewModel();

    /**
     * Copy visited nodes and links by the specified link to the model. This
     * method does not aggressively copy all the links in a mapper tree node. It
     * copy those tree links when visited. It treats mapper tree node is either
     * start point of a chain of end point of a chain.
     *
     * @param link   the link of the chain start point
     * @param model  the model to put the link in.
     * @return       the new link that connects with the new chain
     */
    public IMapperLink copyDirectedChain(IMapperLink link, IBasicViewModel model);

    /**
     * Copy visited nodes and links by the specified node to the model. This
     * method does not aggressively copy all the links in a mapper tree node. It
     * copy those tree links when visited. It treats mapper tree node is either
     * start point of a chain of end point of a chain. If the specified node is
     * a mapper tree node, a copy of the tree node will be returned.
     *
     * @param node   the node of the chain start point
     * @param model  the model to put the link in.
     * @return       the new node that connects with the new chain
     */
    public IMapperNode copyDirectedChain(IMapperNode node, IBasicViewModel model);

    /**
     * Remove a directed chian start from the specified link. The method remove
     * all the links and nodes that it visites, however, it will not
     * aggressively remove all the tree node links, only remove the one is
     * visited.
     *
     * @param link   the link of the chian start point
     * @param model  the model that contains all the chain nodes
     */
    public void removeDirectedChain(IMapperLink link, IBasicViewModel model);

    /**
     * Remove a directed chian start from the specified node. The method remove
     * all the links and nodes that it visites, however, it will not
     * aggressively remove all the tree node links, only remove the one is
     * visited.
     *
     * @param model  the model that contains all the chain nodes
     * @param node   Description of the Parameter
     */
    public void removeDirectedChain(IMapperNode node, IBasicViewModel model);

    /**
     * Move a directed chain, start by the specified link, from the old model,
     * to the new model.
     *
     * @param link      the chain start point
     * @param oldModel  the old model which the chain nodes will be removed
     *      from.
     * @param newModel  the new model which the chain nodes will be stored to.
     */
    public void moveDirectedChain(IMapperLink link, IBasicViewModel oldModel, IBasicViewModel newModel);

    /**
     * Move a directed chain, start by the specified node, from the old model,
     * to the new model.
     *
     * @param node      the chain start point
     * @param oldModel  the old model which the chain nodes will be removed
     *      from.
     * @param newModel  the new model which the chain nodes will be stored to.
     */
    public void moveDirectedChain(IMapperNode node, IBasicViewModel oldModel, IBasicViewModel newModel);

    /**
     * Set the mapper rule that handle the permissions of creating mapper
     * object.
     *
     * @param rule  the mapper rule of this mapper.
     */
    public void setMapperRule(IBasicMapperRule rule);

    /**
     * Return the mapper rule that handles the permissions of creating mapper
     * object.
     *
     * @return   DOCUMENT ME!
     */
    public IBasicMapperRule getMapperRule();

    /**
     * Create a default mapper rule.
     *
     * @return   a default mapper rule.
     */
    public IBasicMapperRule createDefaultMapperRule();

    /**
     * Retrun a mapper link that connects the fromNode to toNode.
     *
     * @param fromNode  the start node of the link
     * @param toNode    the end node of the link
     * @return          a mapper link that connects the fromNode to toNode.
     */
    public IMapperLink createLink(IMapperNode fromNode, IMapperNode toNode);

    /**
     * Connected the sepecified link to its contained node.
     *
     * @param link  the link to be connected
     */
    public void connectLink(IMapperLink link);

    /**
     * Return a methoid field with all the specified basic field information.
     *
     * @param name      the name of this field.
     * @param type      the tyoe of this field.
     * @param tooltip   the tooltip text of this field
     * @param data      the data object of this field
     * @param isInput   flag indicates if this is an input field
     * @param isOutput  flag indicates if this is an output field
     * @param literalInfo  optional literal info
     * @return          Description of the Return Value
     */
    public IField createField(String name, String type, String tooltip,
        Object data, boolean isInput, boolean isOutput, ILiteralUpdater literalUpdater);
    
    /**
     * Return a field Node with the specified field.
     *
     * @param field  the field of the node.
     * @return       a field Node with the specified field.
     */
    public IFieldNode createFieldNode(IField field);

    /**
     * Return a newly created mapper node with the specified methoid object.
     *
     * @param methoid  the methoid object of this mapper node repersents.
     * @return         Description of the Return Value
     */
    public IMethoidNode createMethoidNode(IMethoid methoid);

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
    public IMapperEvent createMapperEvent(Object source, Object transferData, String eventType, String eventDesc);

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
            boolean isAccumulative, boolean isLiteral);
    
    /**
     * Return a new mapper tree node that contains the specified tree path from
     * the specified tree.
     *
     * @param treePath      the tree path to be store in the mapper tree node.
     * @param isSourceTree  true if the the mapper tree node is from source
     *      tree, false otherwise.
     * @return              a new mapper tree node of the specified tree that
     *      contains the specified tree path.
     */
    public IMapperTreeNode createMapperTreeNode(TreePath treePath, boolean isSourceTree);

    /**
     * Add a link to this mapper.
     *
     * @param link  the link to be added.
     */
    public void addLink(IMapperLink link);

    /**
     * Add a node to this mapper.
     *
     * @param node  the group node to be added.
     */
    public void addNode(IMapperNode node);

    /**
     * Add a list of mapper object into the specified mode.
     *
     * @param mapperObjs  the list of mapper object to be added
     * @param model       the model to store the objects.
     */
    public void addObjectsToModel(Collection mapperObjs, IBasicViewModel model);

    /**
     * Remove a link from this mapper.
     *
     * @param link  the link will be removed from this mapper.
     */
    public void removeLink(IMapperLink link);

    /**
     * Remove a node node from this mapper.
     *
     * @param node  the group node will be removed from this mapper.
     */
    public void removeNode(IMapperNode node);

    /**
     * Return true is mapping enable, false otherwise. Mapping enable allows
     * user to use mapper normal, if disable, no mouse event goes through the
     * mapper.
     *
     * @return   Return true is mapping enable, false otherwise.
     */
    public boolean isMappingEnable();

    /**
     * Sets if mapping enable. Mapping enable allows user to use mapper normal,
     * if disable, no mouse event goes through the mapper.
     *
     * @param isMappingEnable  true is mapping enable, false otherwise.
     */
    public void setMappingEnable(boolean isMappingEnable);

    /**
     * Set the palette view of this mapper.
     *
     * @param paletteView  the palette of this mapper.
     */
    public void setPalette(IPaletteView paletteView);

    /**
     * Return the palette view of this mapper.
     *
     * @return   the palette view of this mapper.
     */
    public IPaletteView getPalette();

    /**
     * Return the view manager of this mapper.
     *
     * @return   the view manager of this mapper.
     */
    public IBasicViewManager getMapperViewManager();

    /**
     * Return the mapper controller.
     *
     * @return   the mapper controller.
     */
    public IBasicController getMapperController();

    /**
     * Return the visual repersentation of this mapper.
     *
     * @return   the visual repersentation of this mapper.
     */
    public Component getVisualMapper();

    /**
     * Add a mapper listener to listening to mapper events. Available mapper
     * Event is defined in IMapperEvent.
     *
     * @param listener  the mapper listener to be added.
     */
    public void addMapperListener(IMapperListener listener);

    /**
     * Remove a mapper listener from this mapper.
     *
     * @param listener  the mapper listener to be removed.
     */
    public void removeMapperListener(IMapperListener listener);

    /**
     * Return the viewable size factor of the total width betweem the
     * destination tree view and the mapper when the mapper first show up to the
     * screen.
     *
     * @return   the destination tree viewable size weight
     */
    public float getInitialDestTreeViewableWeight();

    /**
     * Set the viewable size factor of the total width betweem the destination
     * tree view and the mapper when the mapper first show up to the screen.
     *
     * @param destTreeWeight  the destination tree viewable size weight.
     */
    public void setInitialDestTreeViewableWeight(float destTreeWeight);

    /**
     * Return the viewable size factor of the total width betweem the
     * destination tree view and the mapper when the mapper first show up to the
     * screen.
     *
     * @return   the source tree viewable size weight
     */
    public float getInitialSourceTreeViewableWeight();

    /**
     * Set the viewable size factor of the total width betweem the source tree
     * view and the mapper when the mapper first show up to the screen.
     *
     * @param sourceTreeWeight  the source tree viewable size weight
     */
    public void setInitialSourceTreeViewableWeight(float sourceTreeWeight);

    /**
     * Close this mapper, release any system resource.
     */
    public void close();
    
    /**
     * Set the factory which handles setting up literal info on
     * field objects created from the palette.
     */
    public void setLiteralUpdaterFactory(ILiteralUpdaterFactory infoFactory);
    
    /**
     * Called when the mapper detects that a field node has been set 
     * with a new literal.
     */
    public void updateFieldLiteral(IBasicMapperLiteralUpdateEventInfo info);
}
