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
package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.xml.namespace.QName;

import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.action.WidgetAction.WidgetDropTargetDragEvent;
import org.netbeans.api.visual.action.WidgetAction.WidgetDropTargetDropEvent;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.LayoutFactory.SerialAlignment;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELQName;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.wsdl.ui.actions.ActionHelper;
import org.netbeans.modules.xml.wsdl.ui.actions.NameGenerator;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.border.FilledBorder;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.layout.LeftRightLayout;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.ExtensibilityElementsFolderNode;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

public class CollaborationsWidget extends Widget
        implements DnDHandler, PopupMenuProvider {

    private WSDLModel mModel;
    private Widget mCollaborationContentWidget;
    private Image IMAGE = Utilities.loadImage("org/netbeans/modules/xml/wsdl/ui/view/grapheditor/palette/resources/partnerlinkTypesFolder.png");
    public static final Border MAIN_BORDER = new FilledBorder(1, 1, 8, 8, new Color(0x888888), Color.WHITE);
    private ImageLabelWidget mLabelWidget;
    private Widget mHeaderWidget;
    private ButtonWidget createButtonWidget;
    private PartnerLinkTypeHitPointWidget partnerLinkTypeHitPoint; 
    private Object draggedObject = null;
    private int partnerLinkTypesHitPointIndex = -1;
    private Widget stubWidget;
    /** The Node for the WSDLComponent, if it has been created. */
    private Node componentNode;
	private ButtonWidget addButtonWidget;
    
    public CollaborationsWidget(Scene scene, WSDLModel model) {
        super(scene);
        mModel = model;
        partnerLinkTypeHitPoint = new PartnerLinkTypeHitPointWidget(scene);
        partnerLinkTypeHitPoint.setMinimumSize(new Dimension(WidgetConstants.PARTNERLLINKTYPE_MINIMUM_WIDTH, 0));
        stubWidget = new StubWidget(scene, NbBundle.getMessage(
                CollaborationsWidget.class, 
                "LBL_CollaborationsWidget_ThereAreNoPartnerLinkTypes"));
        stubWidget.setMinimumSize(new Dimension(WidgetConstants.PARTNERLLINKTYPE_MINIMUM_WIDTH, 0));
        setOpaque(true);
        setLayout(LayoutFactory.createVerticalFlowLayout(SerialAlignment.CENTER, WidgetConstants.GAP_BETWEEN_HEADER_AND_CONTENT));
        setBorder(new EmptyBorder(20, 20, 40, 20));
        
        mHeaderWidget = new Widget(scene);
        mHeaderWidget.setMinimumSize(new Dimension(
                WidgetConstants.HEADER_MINIMUM_WIDTH, 0));
        mHeaderWidget.setLayout(new LeftRightLayout(32));
        mHeaderWidget.setBorder(WidgetConstants.HEADER_BORDER);
        addChild(mHeaderWidget);
        
        mHeaderWidget.addChild(createActionWidget(scene));
        
        mCollaborationContentWidget = new Widget(scene);
        addChild(mCollaborationContentWidget);
        mCollaborationContentWidget.setLayout(LayoutFactory.createVerticalFlowLayout(
                SerialAlignment.JUSTIFY, WidgetConstants.GAP_BETWEEN_CHILD_WIDGETS));
        getActions().addAction(((PartnerScene) scene).getDnDAction());
        getActions().addAction(ActionFactory.createPopupMenuAction(this));
        createContent();
        //initially all plt widgets should be collapsed
        collapsePartnerLinkTypeWidgets();
        
    }

    private Widget createActionWidget(Scene scene) {
        Widget actionWidget = new Widget(scene);
        actionWidget.setLayout(LayoutFactory.createHorizontalFlowLayout(
                SerialAlignment.JUSTIFY, 8));

        // Auto-create button.
        createButtonWidget = new ButtonWidget(scene,
                NbBundle.getMessage(CollaborationsWidget.class,
                "LBL_CollaborationsWidget_AutoCreate"), true);
        createButtonWidget.setActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // For each port type, create a role and partnerLinkType.
                WSDLComponentFactory factory = mModel.getFactory();
                QName qname = BPELQName.PARTNER_LINK_TYPE.getQName();
                try {
                    if (mModel.startTransaction()) {
                        Definitions definitions = mModel.getDefinitions();
                        Collection<PortType> ports = getUnusedPortTypes();
                        for (PortType pt : ports) {
                            PartnerLinkType plt = (PartnerLinkType) factory.create(
                                    definitions, qname);
                            String name = pt.getName();
                            //IZ 100518 if portype name is null, reference cannot be created.
                            if (name == null) continue; 
                            int idx = name.toLowerCase().indexOf("porttype");
                            if (idx > 0) {
                                name = name.substring(0, idx);
                            }
                            plt.setName(NameGenerator.generateUniquePartnerLinkType(
                                    name, qname, mModel));
                            Role role = (Role) factory.create(
                                    plt, BPELQName.ROLE.getQName());
                            role.setName("role1");
                            NamedComponentReference<PortType> ptref =
                                    role.createReferenceTo(pt, PortType.class);
                            role.setPortType(ptref);
                            plt.setRole1(role);
                            definitions.addExtensibilityElement(plt);
                        }
                    }
                } finally {
                    mModel.endTransaction();
                }
            }
        });
        actionWidget.addChild(createButtonWidget);

        // Add partnerLinkType button.
        addButtonWidget = new ButtonWidget(scene,
                NbBundle.getMessage(CollaborationsWidget.class,
                "LBL_CollaborationsWidget_AddPartnerLinkType"),true);
        addButtonWidget.setActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PartnerLinkType plt = null;
                try {
                    if (mModel.startTransaction()) {
                        plt = (PartnerLinkType) mModel.
                                getFactory().create(mModel.getDefinitions(),
                                BPELQName.PARTNER_LINK_TYPE.getQName());
                        plt.setName(NameGenerator.generateUniquePartnerLinkType(
                                "partnerlinktype",
                                BPELQName.PARTNER_LINK_TYPE.getQName(), mModel));
                        Role role = (Role) mModel.getFactory().create(
                                plt, BPELQName.ROLE.getQName());
                        role.setName("role1");
                        plt.setRole1(role);
                        mModel.getDefinitions().addExtensibilityElement(plt);
                    }
                } finally {
                    mModel.endTransaction();
                }
                if (plt != null) {
                    ActionHelper.selectNode(plt);
                }
            }
        });
        actionWidget.addChild(addButtonWidget);
        
        return actionWidget;
    }

    public Object hashKey() {
        return mModel.getDefinitions().getName();
    }

    public void updateContent() {
        mLabelWidget.setComment("(" + mModel.getDefinitions().getExtensibilityElements(PartnerLinkType.class).size() + ")");
        mCollaborationContentWidget.removeChildren();
        createContent();
    }

    private void createContent() {
        if (mLabelWidget == null) {
            mLabelWidget = new ImageLabelWidget(getScene(), IMAGE, NbBundle.getMessage(CollaborationsWidget.class, "LBL_CollaborationsWidget_PartnerLinkTypes"), 
                    "(" + mModel.getDefinitions().getExtensibilityElements(PartnerLinkType.class).size() + ")");
            mHeaderWidget.addChild(0, mLabelWidget);
        }
        
        if (stubWidget.getParentWidget() != null) {
            stubWidget.getParentWidget().removeChild(stubWidget);
        }
        
        List<PartnerLinkType> partnerLinkTypes = mModel.getDefinitions().getExtensibilityElements(PartnerLinkType.class);
        if (partnerLinkTypes == null || partnerLinkTypes.isEmpty()) {
            mCollaborationContentWidget.addChild(stubWidget);
        } else {
            Scene scene = getScene();
            WidgetFactory factory = WidgetFactory.getInstance();
            for (PartnerLinkType plType : partnerLinkTypes) {
                Widget widget = factory.createWidget(scene, plType, true);
                mCollaborationContentWidget.addChild(widget);
            }
        }

        // Disable/hide buttons based on current widget availability.
        Collection<PortType> ports = getUnusedPortTypes();
        createButtonWidget.setVisible(ports.size() > 0);
    }

    private boolean hasPartnerLinkTypes() {
        List<PartnerLinkType> partnerLinkTypes = mModel.getDefinitions().getExtensibilityElements(PartnerLinkType.class);
        return partnerLinkTypes == null || !partnerLinkTypes.isEmpty();
    }
    
    //first time createContent is called all partnerlinktype widgets are in collapsed state.
    private void collapsePartnerLinkTypeWidgets() {
        for (Widget w : mCollaborationContentWidget.getChildren()) {
            if (w instanceof PartnerLinkTypeWidget) {
                ((PartnerLinkTypeWidget) w).collapseWidget();
            }
        }
    }
    
    public void dragExit() {
        hideHitPoint();
    }

    public boolean dragOver(Point scenePoint, WidgetDropTargetDragEvent event) {
        try {
            Transferable t = event.getTransferable();
            if (t != null) {
                for (DataFlavor flavor : t.getTransferDataFlavors()) {
                    Class repClass = flavor.getRepresentationClass();
                    if (Node.class.isAssignableFrom(repClass)) {
                        Node node = (Node) t.getTransferData(flavor);
                        if (node.getName().startsWith("PartnerLinkType")) {
                            showHitPoint(scenePoint, node);
                            return true;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            //do nothing
        }
        return false;
    }

    public boolean drop(Point scenePoint, WidgetDropTargetDropEvent event) {      
        Node node = (Node) draggedObject;
        int index = partnerLinkTypesHitPointIndex;
        hideHitPoint();
        if (node != null && index >= 0) {
            try {
                if (mModel.startTransaction()) {
                    PartnerLinkType[] plts = mModel.getDefinitions().
                            getExtensibilityElements(PartnerLinkType.class).
                            toArray(new PartnerLinkType[0]);
                    PartnerLinkType plt = (PartnerLinkType) mModel.
                            getFactory().create(mModel.getDefinitions(),
                            BPELQName.PARTNER_LINK_TYPE.getQName());
                    String pltName = NameGenerator.generateUniquePartnerLinkType(
                            "partnerlinktype", BPELQName.PARTNER_LINK_TYPE.getQName(), mModel);
                    plt.setName(pltName);
                    Role role = (Role) mModel.getFactory().create(
                            plt, BPELQName.ROLE.getQName());
                    role.setName("role1");
                    plt.setRole1(role);
                    
                    if (index == plts.length) {
                        mModel.getDefinitions().addExtensibilityElement(plt);
                    } else {
                        Utility.insertIntoDefinitionsAtIndex(index, mModel, plt,
                                Definitions.EXTENSIBILITY_ELEMENT_PROPERTY);
                    }
                }
            } finally {
                mModel.endTransaction();
            }
            return true;
        }
        return false;
    }

    public void expandForDragAndDrop() {
    }

    public boolean isCollapsed() {
        return !mCollaborationContentWidget.isVisible();
    }
    
    
    private void showHitPoint(Point point, Object draggedObj) {
        this.draggedObject = draggedObj;
        List<PartnerLinkTypeWidget> partnerLinkTypeWidgets = getPartnerLinkTypeWidgets();
        
        if (partnerLinkTypeWidgets == null) return;
        
        int index = placeHolderIndex(point);
        
        if (index < 0) return;
        
        partnerLinkTypesHitPointIndex = index;
        
        if (partnerLinkTypeHitPoint.getParentWidget() != null) {
            partnerLinkTypeHitPoint.getParentWidget().removeChild(partnerLinkTypeHitPoint);
        }
        
        if (stubWidget.getParentWidget() != null) {
            stubWidget.getParentWidget().removeChild(stubWidget);
        }
        mCollaborationContentWidget.addChild(partnerLinkTypesHitPointIndex, partnerLinkTypeHitPoint);
    }
    
    private void hideHitPoint() {
        if (partnerLinkTypeHitPoint.getParentWidget() != null) {
            partnerLinkTypeHitPoint.getParentWidget().removeChild(partnerLinkTypeHitPoint);
        }
        
        if (!hasPartnerLinkTypes() && stubWidget.getParentWidget() == null) {
            mCollaborationContentWidget.addChild(stubWidget);
        }
        partnerLinkTypesHitPointIndex = -1;
        draggedObject = null;
    }

    /**
     * Return a collection of the PortTypes which are not referenced by
     * any Role in our WSDL model. This includes PortTypes in the imported
     * WSDL documents.
     *
     * @return  collection of unused port types, or the empty list.
     */
    private Collection<PortType> getUnusedPortTypes() {
        // Make a list that we can modify (and is our non-null return value).

        List<PortType> allPorts = new ArrayList<PortType>();
        Definitions defs = mModel.getDefinitions();
        Collection<PortType> ports = defs.getPortTypes();
        if (ports != null) {
            //IZ 100518
            for (PortType portType : ports) {
                if (portType.getName() != null) {
                    allPorts.add(portType);
                }
            }
        }
        Collection<Import> imports = defs.getImports();
        for (Import imp : imports) {
            try {
                WSDLModel importedModel = imp.getImportedWSDLModel();
                Definitions importedDefs = importedModel.getDefinitions();
                ports = importedDefs.getPortTypes();
                //IZ 100518
                for (PortType portType : ports) {
                    if (portType.getName() != null) {
                        allPorts.add(portType);
                    }
                }
            } catch (CatalogModelException cme) {
                //ignore the error. validation would find it.
            }
        }
        List<PartnerLinkType> partners = defs.getExtensibilityElements(
                PartnerLinkType.class);
        if ((partners != null && partners.size() > 0) &&
                (allPorts != null && allPorts.size() > 0)) {
            for (PartnerLinkType partner : partners) {
                PortType pt1 = getPortType(partner.getRole1());
                if (pt1 != null) {
                    allPorts.remove(pt1);
                }
                PortType pt2 = getPortType(partner.getRole2());
                if (pt2 != null) {
                    allPorts.remove(pt2);
                }
            }
        }
        return allPorts;
    }

    /**
     * Retrieve the PortType from the given Role, if possible.
     * 
     * @param  role  the Role from which to get the PortType.
     * @return  PortType from Role, or null if none.
     */
    private PortType getPortType(Role role) {
        if (role != null) {
            NamedComponentReference<PortType> ref = role.getPortType();
            if (ref != null) {
                return ref.get();
            }
        }
        return null;
    }

    private List<PartnerLinkTypeWidget> getPartnerLinkTypeWidgets() {
        if (mCollaborationContentWidget.getParentWidget() == null) return null;
        
        List<PartnerLinkTypeWidget> result = new ArrayList<PartnerLinkTypeWidget>();
        
        for (Widget widget : mCollaborationContentWidget.getChildren()) {
            if (widget instanceof PartnerLinkTypeWidget) {
                result.add((PartnerLinkTypeWidget) widget);
            }
        }
        
        return result;
    }
    
    private int placeHolderIndex(Point scenePoint) {
        List<PartnerLinkTypeWidget> partnerLinkTypeWidgets = getPartnerLinkTypeWidgets();
        
        if (partnerLinkTypeWidgets.size() == 0) return 0;
        
        if (partnerLinkTypeHitPoint.getParentWidget() != null) {
            if (partnerLinkTypeHitPoint.isHitAt(partnerLinkTypeHitPoint.convertSceneToLocal(scenePoint))) {
                return -1;
            }
        }
        
        for (int i = 0; i < partnerLinkTypeWidgets.size(); i++) {
            PartnerLinkTypeWidget partnerLinkTypeWidget = partnerLinkTypeWidgets.get(i);
            Point partnerLinkTypePoint = partnerLinkTypeWidget.convertSceneToLocal(scenePoint);
            Rectangle partnerLinkTypeBounds = partnerLinkTypeWidget.getBounds();
            
            
            if (partnerLinkTypePoint.y < partnerLinkTypeBounds.getCenterY()) {
                return i;
            }
        }
        
        return partnerLinkTypeWidgets.size();
    }

    /**
     * Locates the TopComponent parent of the view containing the Scene
     * that owns this widget, if possible.
     *
     * @return  the parent TopComponent, or null if not found.
     */
    protected TopComponent findTopComponent() {
        return (TopComponent) SwingUtilities.getAncestorOfClass(
                TopComponent.class, getScene().getView());
    }

    /**
     * Returns a Node for the WSDL component that this widget represents.
     * If this widget does not have an assigned WSDL component, then this
     * returns an AbstractNode with no interesting properties.
     */
    private synchronized Node getNode() {
        if (componentNode == null) {
            //Show only partnerlinktype in add action.
            Set<String> tnsSet = new HashSet<String>();
            tnsSet.add(BPELQName.PLNK_NS);
            componentNode = new ExtensibilityElementsFolderNode(mModel.getDefinitions(), tnsSet);
            componentNode = new WidgetFilterNode(componentNode);
        }
        return componentNode;
    }

    public JPopupMenu getPopupMenu(Widget widget, Point point) {
        Node node = getNode();
        if (node != null) {
            // Using Node.getContextMenu() appears to bypass our FilterNode,
            // so we must build out the context menu as follows.
            TopComponent tc = findTopComponent();
            Lookup lookup;
            if (tc != null) {
                // Activate the node just as any explorer view would do.
                tc.setActivatedNodes(new Node[] { node });
                // To get the explorer actions enabled, must have the
                // lookup from the parent TopComponent.
                lookup = tc.getLookup();
            } else {
                lookup = Lookup.EMPTY;
            }
            // Remove the actions that we do not want to support in this view.
            Action[] actions = node.getActions(true);
            return Utilities.actionsToPopup(actions, lookup);
        }
        return null;
    }
    
    private class PartnerLinkTypeHitPointWidget extends LabelWidget {
        public PartnerLinkTypeHitPointWidget(Scene scene) {
            super(scene, " ");
            setBorder(new PartnerLinkTypeHitPointBorder());
            setFont(scene.getDefaultFont());
        }
    }
    
    
    private static class PartnerLinkTypeHitPointBorder implements Border {
   
        public Insets getInsets() {
            return new Insets(8, 8, 8, 8);
        }

        
        public void paint(Graphics2D g2, Rectangle rectangle) {
            Paint oldPaint = g2.getPaint();
            Stroke oldStroke = g2.getStroke();
            
            Object oldStrokeControl = g2.getRenderingHint(
                    RenderingHints.KEY_STROKE_CONTROL);
            
            g2.setPaint(Color.WHITE);
            g2.fill(rectangle);
            
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                    RenderingHints.VALUE_STROKE_PURE);
            g2.setPaint(WidgetConstants.HIT_POINT_BORDER); 
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(rectangle.x + 1, rectangle.y + 1, 
                    rectangle.width - 2, rectangle.height - 2, 6, 6);
            
            g2.setPaint(oldPaint);
            g2.setStroke(oldStroke);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                    oldStrokeControl);
        }
        

        public boolean isOpaque() {
            return true;
        }
    }
}
