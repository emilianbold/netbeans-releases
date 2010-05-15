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
package org.netbeans.modules.bpel.design;

import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import org.openide.filesystems.FileObject;
import org.openide.windows.TopComponent;
import org.netbeans.modules.bpel.design.decoration.components.LinkToolButton;
import org.netbeans.modules.bpel.design.geometry.FPoint;
import org.netbeans.modules.bpel.design.selection.DnDTool;
import org.netbeans.modules.bpel.design.selection.FlowlinkTool;
import org.netbeans.modules.bpel.model.api.BPELElementsBuilder;
import org.netbeans.modules.xml.reference.ReferenceChild;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.bpel.editors.api.dnd.DnDFactory;
import org.netbeans.modules.bpel.editors.api.dnd.DnDCallback;
import org.netbeans.modules.bpel.model.api.For;
import org.netbeans.modules.bpel.model.api.ForEach;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.Pick;
import org.netbeans.modules.bpel.model.api.Receive;
import org.netbeans.modules.bpel.model.api.Sequence;
import org.netbeans.modules.bpel.model.api.Wait;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.support.TBoolean;
import org.netbeans.modules.bpel.properties.ImportRegistrationHelper;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.nodes.actions.AddOnAlarmAction;
import org.netbeans.modules.websvc.core.WebServiceReference;
import org.openide.ErrorManager;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * @author Alexey
 */
public class DnDHandler implements DragSourceListener, DragGestureListener, DropTargetListener {

    private DesignView designView;
    private DragSource dragSource;
    private String status;
    private MessageFlowDataFlavor flowDataFlavor = new MessageFlowDataFlavor();
    private BpelDataFlavor bpelDataFlavor = new BpelDataFlavor();
    private List<DiagramView> views;

    public DnDHandler(DesignView designView) {
        this.designView = designView;

        views = new ArrayList<DiagramView>(3);
        views.add(designView.getProcessView());
        views.add(designView.getConsumersView());
        views.add(designView.getProvidersView());

        dragSource = DragSource.getDefaultDragSource();// new DragSource();

        for (DiagramView view : views) {
            dragSource.createDefaultDragGestureRecognizer(view, DnDConstants.ACTION_MOVE, this);
            new DropTarget(view, DnDConstants.ACTION_MOVE, (DropTargetListener) this, true);

        }
    }

    public DesignView getDesignView() {
        return designView;
    }

    public void dragEnter(DragSourceDragEvent dsde) {
//System.out.println("DragSource.dragEnter");
    }

    public void dragOver(DragSourceDragEvent dsde) {
        dsde.getDragSourceContext().setCursor(dragSource.DefaultMoveDrop);
    }

    public void dropActionChanged(DragSourceDragEvent dsde) {
    }

    public void dragExit(DragSourceEvent dse) {
        dse.getDragSourceContext().setCursor(dragSource.DefaultMoveNoDrop);
    }

    public void dragDropEnd(DragSourceDropEvent dsde) {
        clear();
//System.out.println();
//System.out.println("DragSource.dragDropEnd");
        if (dsde.getDropAction() == DnDConstants.ACTION_MOVE) {
//            BpelEntity be = getBpelEntity(dsde.getDragSourceContext().getTransferable());
//            BpelContainer parent = (BpelContainer)be.getParent();
//            parent.remove(be);
        }
    }
    
    public String getStatus(){
        return status;
    }
    
    private void setStatus(String status){
        this.status = status;
    }
    
    public void clear() {
        setStatus(null);

        for (DiagramView view : views) {
            view.getPlaceholderManager().clear();
        }
        getFlowLinkTool().clear();
    }

    public void dragGestureRecognized(DragGestureEvent dge) {
//System.out.println("DragSource.dragGestureRecognized");
        Object src = dge.getTriggerEvent().getSource();

        if (src instanceof LinkToolButton) {
            dge.startDrag(DragSource.DefaultLinkDrop,
                    new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB),
                    new Point(0, 0),
                    new MessageFlowTransferable((LinkToolButton) src), this);
        } else {

            if (dge.getComponent() instanceof DiagramView) {

                DiagramView view = (DiagramView) dge.getComponent();
                Pattern clicked = view.findPattern(dge.getDragOrigin());
                Pattern selected = designView.getSelectionModel().getSelectedPattern();

                if (clicked == null) {
                    return;
                }

                if (!clicked.isDraggable()) {
                    return;
                }

                if (clicked == selected || clicked.isNestedIn(selected)) {
                    //start a move tool
                    if (!getDesignView().getModel().isReadOnly()) {
                        dge.startDrag(DragSource.DefaultMoveDrop,
                                new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB),
                                new Point(0, 0),
                                new BpelTransferable(selected), this);
                    }
                }
            }
        }
    }

    public void dragEnter(DropTargetDragEvent dtde) {
//System.out.println();
//System.out.println("DropTarget.dragEnter");
//System.out.println();
        Transferable tr = dtde.getTransferable();

        if (tr.isDataFlavorSupported(flowDataFlavor)) {
            FPoint p;
            try {
                LinkToolButton btn = (LinkToolButton) tr.getTransferData(flowDataFlavor);
                getFlowLinkTool().init(btn);
                
                if (designView.getModel().isReadOnly()) {
                    setStatus(NbBundle.getMessage(getClass(), "LBL_ReadOnly")); //NOI18N
//System.out.println("  REG: 1");
                    dtde.rejectDrag();
                    return;
                }

                if (!designView.getModel().getFilters().showPartnerlinks()) {
                    setStatus(NbBundle.getMessage(getClass(), "LBL_CanNotCreateMessageFlow")); //NOI18N
//System.out.println("  REG: 2");
                    dtde.rejectDrag();
                }
            } catch (UnsupportedFlavorException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            BpelEntity entity = getBpelEntity(tr);

            if (entity == null) {
//System.out.println("  REG: 3");
                dtde.rejectDrag();
                return;
            }
            if (entity.getModel() != getDesignView().getBPELModel()) {
//System.out.println("  REG: 4");
                dtde.rejectDrag();
                return;
            }
            if (designView.getModel().isReadOnly()) {
                setStatus(NbBundle.getMessage(getClass(), "LBL_ReadOnly")); //NOI18N
//System.out.println("  REG: 5");
                dtde.rejectDrag();
                return;
            }
            if (!designView.getModel().getFilters().showImplicitSequences() && (entity instanceof Sequence)) {
                setStatus(NbBundle.getMessage(getClass(), "LBL_CanNotAddSequence")); // NOI18N
//System.out.println("  REG: 6");
                dtde.rejectDrag();
            } else if (!designView.getModel().getFilters().showPartnerlinks() && (entity instanceof PartnerLink)) {
                setStatus(NbBundle.getMessage(getClass(), "LBL_CanNotAddPartnerLink")); // NOI18N
//System.out.println("  REG: 7");
                dtde.rejectDrag();
            } else {
                Pattern pattern = designView.getModel().getPattern(entity);

                if (pattern == null) {
                    pattern = designView.getModel().createPattern(entity);
                }
//System.out.println("PATTERN: " + pattern.getClass().getName());
                for (DiagramView view : views) {
                    view.getPlaceholderManager().init(pattern);
                }
                dtde.acceptDrag(DnDConstants.ACTION_MOVE);
            }
        }
    }

    public void dragOver(DropTargetDragEvent dtde) {
//System.out.println();
//System.out.println("DropTarget.dragOver");
        Transferable tr = dtde.getTransferable();
        DnDTool tool = null;

        if (tr.isDataFlavorSupported(flowDataFlavor)) {
            tool = getFlowLinkTool();
            tool.move(null);
        } else {
                DiagramView view = (DiagramView) dtde.getDropTargetContext().getComponent();
                if (view != null) {
                    FPoint mp = view.convertScreenToDiagram(dtde.getLocation());
                    tool = view.getPlaceholderManager();
                    tool.move(mp);
                }
        }
        if (tool != null && tool.isValidLocation()) {
            dtde.acceptDrag(DnDConstants.ACTION_MOVE);
        } else {
            dtde.rejectDrag();
        }
    }

    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    public void dragExit(DropTargetEvent dte) {
        clear();
    }

    public void drop(final DropTargetDropEvent dtde) {
//System.out.println();
//System.out.println("DropTarget.drop");
        getDesignView().getTopComponent().requestActive();
        getDesignView().requestFocusInWindow();

        final DiagramView view = (DiagramView) dtde.getDropTargetContext().getComponent();

        if (view == null) {
            return;
        }
        final FPoint location = view.convertScreenToDiagram(dtde.getLocation());
        Callable<Object> callable = null;

        if (dtde.isDataFlavorSupported(flowDataFlavor)) {
//System.out.println("1");
            callable = new Callable<Object>() {

                public Object call() {
                    getFlowLinkTool().drop(location);
                    return null;
                }
            };
        }
        else {
            callable = new Callable<Object>() {
                public Object call() {
//System.out.println();
//System.out.println("drop: " + view.getPlaceholderManager().getClass().getName());
                    view.getPlaceholderManager().drop(location);
                    clear();
                    return null;
                }
            };
        }
        try {
            if (callable != null) {
                designView.getBPELModel().invoke(callable, this);
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }

    private BpelEntity getBpelEntity(Transferable t) {
//System.out.println();
        BpelEntity entity = null;
        Collection factories = Lookup.getDefault().lookupAll(DnDFactory.class);
      
        for (Object factory : factories){
            BpelEntity e = ((DnDFactory) factory).getEntity(designView.getBPELModel(), t);

            if (e != null) {
                return e;
            }
        }
        try {
            for (DataFlavor flavor : t.getTransferDataFlavors()) {
                Class repClass = flavor.getRepresentationClass();
                Object data = t.getTransferData(flavor);

//System.out.println("getBpelEntity: " + repClass.getName());
                if (BpelNode.class.isAssignableFrom(repClass)) {
                    //DnD from Diagram or Nav
//System.out.println("  1");
                    Object ref = ((BpelNode) data).getReference();

                    if (ref instanceof BpelEntity) {
                        entity = (BpelEntity) ref;
                    }
                }
                else if (Node.class.isAssignableFrom(repClass)) {
//System.out.println("  2");
                    // DnD from palette or ProjectTree
                    Node node = (Node) data;
                    entity = getPaletteItem(node);
                    String name = node.getName();
//System.out.println("Node: " + node);
//System.out.println("name: " + name);
//System.out.println("    : " + node.getClass().getName());
                    
                    if (name != null && name.toLowerCase().endsWith(".xsd") && node instanceof ReferenceChild) {
                        FileObject file = ((ReferenceChild) node).getFileObject();
//System.out.println("here: "  + node.getLookup().lookup(FileObject.class));
//System.out.println("  fo: " + file);
                        entity = new ImportRegistrationHelper(designView.getBPELModel()).createImport(file);
                    }
                    // Referenced Resources DnD are represented by node which has no related DataObject 
                    else if (entity == null && node.getLookup().lookup(DataObject.class) == null) {
                        // it can be wsdl from Referenced Resources
                        entity = designView.getBPELModel().getBuilder().createPartnerLink();
                        entity.setCookie(DnDHandler.class, data);
                        entity.setCookie(FileObject.class, getSelectedFileObject()); 
                    }
                }
                else if (WebServiceReference.class.isAssignableFrom(repClass)) {
//System.out.println("  3");
//System.out.println("WebServiceReference: " + data);
                    // WS node from EJB project, referenced by URL
                    entity = designView.getBPELModel().getBuilder().createPartnerLink();
                    entity.setCookie(DnDHandler.class, data);
                    entity.setCookie(FileObject.class, getSelectedFileObject()); 
                }
                else if (DataObject.class.isAssignableFrom(repClass)) {
//System.out.println("  4");
                    DataObject dataObj = (DataObject) data;
                    String ext = dataObj.getPrimaryFile().getExt();
//System.out.println(" ext: " + ext);
                    if (ext.compareToIgnoreCase("wsdl") == 0) { // NOI18N
                        // for WSDl first just try to create PL based on PLTS found in that WSDL
                        entity = designView.getBPELModel().getBuilder().createPartnerLink();
                        entity.setCookie(DnDHandler.class, dataObj.getPrimaryFile());
                    } 
                    else if (ext.compareToIgnoreCase("xsd") == 0) { // NOI18N
//System.out.println("  xsd");
                        // for schema just add imprt
                        entity = new ImportRegistrationHelper(designView.getBPELModel()).createImport(dataObj.getPrimaryFile());
//System.out.println("  entity: " + entity);
                    }
                }
                if (entity != null) {
                    break;
                }
            }
        }
        catch (UnsupportedFlavorException ufe) {}
        catch (IOException ioe) {}

        return entity;
    }

    public static FileObject getSelectedFileObject() {
      Node node = getSelectedNode();

      if (node == null) {
        return null;
      }
      return node.getLookup().lookup(FileObject.class);
    }

    public static Node getSelectedNode() {
      Node [] nodes = getSelectedNodes();

      if (nodes == null) {
        return null;
      }
      return nodes [0];
    }

    public static Node [] getSelectedNodes() {
      TopComponent top = getActiveTopComponent();

      if (top == null) {
        return null;
      }
      Node [] nodes = top.getActivatedNodes();

      if (nodes == null || nodes.length == 0) {
        return null;
      }
      return nodes;
    }

    public static TopComponent getActiveTopComponent() {
      return TopComponent.getRegistry().getActivated();
    }

    class MessageFlowDataFlavor extends DataFlavor {
        private static final long serialVersionUID = 1;

        public MessageFlowDataFlavor() {
            super(FlowlinkTool.class, "Message flow link"); // NOI18N
        }
    }

    class MessageFlowTransferable implements Transferable {
        private LinkToolButton button;

        public MessageFlowTransferable(LinkToolButton button) {
            this.button = button;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{flowDataFlavor};
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return (flavor.getRepresentationClass() == FlowlinkTool.class);
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return button;
        }
    }

    class BpelDataFlavor extends DataFlavor {
        private static final long serialVersionUID = 1;

        public BpelDataFlavor() {
            super(BpelNode.class, "Bpel element"); // NOI18N
        }
    }

    class BpelTransferable implements Transferable {

        private BpelNode draggedNode;

        public BpelTransferable(BpelNode draggedNode) {
            this.draggedNode = draggedNode;
        }

        public BpelTransferable(Pattern pattern) {
            BpelEntity omRef = pattern.getOMReference();
            assert omRef != null;

            this.draggedNode = (BpelNode) designView.getNodeForPattern(pattern);

        }

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{bpelDataFlavor};
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return (flavor.getRepresentationClass() == BpelNode.class);
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }

            return draggedNode;
        }
    }

    private BpelEntity getPaletteItem(Node data) {
        // assuming to be item from palette
        String item = data.getName();
        int k = item.indexOf("_"); // NOI18N

        if (k != -1) {
            item = item.substring(0, k);
        }
        BPELElementsBuilder builder = designView.getBPELModel().getBuilder();

        if (item.equals("reply")) { // NOI18N
            return builder.createReply();
        } else if (item.equals("invoke")) { // NOI18N
            return builder.createInvoke();
        } else if (item.equals("receive")) { // NOI18N
            Receive rcv = builder.createReceive();
            // indicator that object is being DnD from pallete. 
            // Diagram may use this value to set CreateInstance attribute
            rcv.setCookie(DnDHandler.class, DnDHandler.class);
            return rcv;
        } else if (item.equals("pick")) { // NOI18N
            Pick p = builder.createPick();
            p.addOnMessage(builder.createOnMessage());
            return p;
        } else if (item.equals("assign")) { // NOI18N
            return builder.createAssign();
        } else if (item.equals("javascript")) { // NOI18N
            return builder.createJavaScript();
        } else if (item.equals("sequence")) { // NOI18N
            return builder.createSequence();
        } else if (item.equals("flow")) { // NOI18N
            return builder.createFlow();
        } else if (item.equals("while")) { // NOI18N
            return builder.createWhile();
        } else if (item.equals("repeatuntil")) { // NOI18N
            return builder.createRepeatUntil();
        } else if (item.equals("foreach")) { // NOI18N
            ForEach fe = builder.createForEach();
            fe.setParallel(TBoolean.NO);
            try {
                fe.setCounterName(fe.getName() + "Counter"); // NOI18N
            } catch (VetoException ex) {
                // Somebody does not like this counter name 
                // or property is not supported or something else.
                // Anyway we unable to determine cause of problem, 
                // so will ignore this exception.            
            }
            return fe;
        } else if (item.equals("scope")) { // NOI18N
            return builder.createScope();
        } else if (item.equals("if")) { // NOI18N
            return builder.createIf();
        } else if (item.equals("wait")) { // NOI18N
            Wait w = builder.createWait();
            For f = builder.createFor();
            try {
                f.setContent(AddOnAlarmAction.DEFAULT_FOR_VALUE); //NOI18N
            } catch (VetoException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
            w.setTimeEvent(f);
            return w;
        } else if (item.equals("throw")) { // NOI18N
            return builder.createThrow();
        } else if (item.equals("rethrow")) { // NOI18N
            return builder.createRethrow();
        } else if (item.equals("compensate")) { // NOI18N
            return builder.createCompensate();
        } else if (item.equals("compensatescope")) { // NOI18N
            return builder.createCompensateScope();
        } else if (item.equals("empty")) { // NOI18N
            return builder.createEmpty();
        } else if (item.equals("partner")) { // NOI18N
            return builder.createPartnerLink();
        } else if (item.equals("exit")) { // NOI18N
            return builder.createExit();
        } else if (item.equals("validate")) { // NOI18N
            return builder.createValidate();
        } else {
            //System.out.println("Warning: can't recognize dragged item: " + item); // NOI18N
        }
        return null;
    }

    public FlowlinkTool getFlowLinkTool() {
        return designView.getFlowLinkTool();
    }
}
