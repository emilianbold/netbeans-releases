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


package org.netbeans.modules.bpel.design.model.patterns;



import java.awt.Cursor;
import java.awt.geom.Area;

import java.net.URL;
import java.util.Collection;
import java.util.concurrent.Callable;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.DnDHandler;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.design.geometry.FRange;
import org.netbeans.modules.bpel.design.model.elements.ContentElement;
import org.netbeans.modules.bpel.design.selection.DefaultPlaceholder;
import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.EventHandlers;
import org.netbeans.modules.bpel.model.api.ExtendableActivity;
import org.netbeans.modules.bpel.model.api.FaultHandlers;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.PartnerLinkContainer;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.design.geometry.FPoint;
import org.netbeans.modules.bpel.design.layout.LayoutManager;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.PartnerLinkHelper;
import org.netbeans.modules.bpel.design.model.connections.Connection;
import org.netbeans.modules.bpel.design.model.connections.Direction;
import org.netbeans.modules.bpel.design.model.elements.PlaceHolderElement;
import org.netbeans.modules.bpel.design.model.elements.ProcessBorder;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.design.selection.PlaceHolder;
import org.netbeans.modules.bpel.properties.ImportRegistrationHelper;
import org.netbeans.modules.soa.ui.UserNotification;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.netbeans.modules.websvc.core.WebServiceReference;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Alexey Yarmolenko
 */
public class ProcessPattern extends CompositePattern {
    
    private PlaceHolderElement placeHolder;
    
    private VisualElement startEvent;
    private VisualElement endEvent;
    
    private Connection connection1;
    private Connection connection2;
    
    
    private Connection eventHandlersConnection;
    private VisualElement eventsBadge;
    
    private Connection faultHandlersConnection;
    private VisualElement faultBadge;
    
    
    public ProcessPattern(DiagramModel model) {
        super(model);
        connection1 = new Connection(this);
        connection2 = new Connection(this);
    }
    
    public VisualElement getFirstElement() {
        return null;
    }
    
    public VisualElement getLastElement() {
        return null;
    }
    
    public boolean isDraggable() {
        return false;
    }
    
    
    
    protected void onAppendPattern(Pattern p) {
        BpelEntity entity = p.getOMReference();
        
        if (entity instanceof EventHandlers) {
            appendElement(eventsBadge);
        } else if (entity instanceof FaultHandlers) {
            appendElement(faultBadge);
        } else if (!(p instanceof PartnerLinksPattern)) {
            removeElement(placeHolder);
        }
    }
    
    
    protected void onRemovePattern(Pattern p) {
        Process process = (Process) getOMReference();
        
        if (eventsBadge.hasPattern() && (process.getEventHandlers() == null)) {
            removeElement(eventsBadge);
        } else if (faultBadge.hasPattern() 
                && (process.getFaultHandlers() == null))
        {
            removeElement(faultBadge);
        } else if (!(p instanceof PartnerLinksPattern)) {
            appendElement(placeHolder);
        }
    }
    
    
    public CompositePattern getParent(){
        return null;
    }
    
    public FBounds layoutPattern(LayoutManager manager) {
        Collection<Pattern> patterns = super.getNestedPatterns();
        
        Process process = (Process) getOMReference();
        
        FRange rangeX = new FRange(0);
        double y;
        double yMax = 0;
        
        Pattern rootActivityPattern = getRootActivityPattern();
        startEvent.setCenter(0, 0);
        yMax += LayoutManager.VSPACING + startEvent.getHeight() / 2;
        
        rangeX.extend(startEvent.getWidth() / 2);
        rangeX.extend(-startEvent.getWidth() / 2);
        
        double contentWidth;
        double contentHeight;
        
        if (rootActivityPattern == null) {
            contentWidth = Math.max(placeHolder.getWidth(), MIN_CONTENT_WIDTH);
            contentHeight = Math.max(placeHolder.getHeight(), MIN_CONTENT_HEIGHT);
        } else {
            FBounds contentSize = rootActivityPattern.getBounds();
            contentWidth = Math.max(contentSize.width, MIN_CONTENT_WIDTH);
            contentHeight = Math.max(contentSize.height, MIN_CONTENT_HEIGHT);
        }
        
        double contentTop = startEvent.getHeight() / 2 + LayoutManager.VSPACING;
        double contentBottom = contentTop + contentHeight;
        
        if (rootActivityPattern == null){
            placeHolder.setCenter(0, contentTop + contentHeight / 2);
            
            rangeX.extend(contentWidth / 2);
            rangeX.extend(-contentWidth / 2);
        } else {
            FBounds clientSize = rootActivityPattern.getBounds();
            FPoint origin = manager.getOriginOffset(rootActivityPattern);
            
            double contentPadding = (contentWidth - clientSize.width) / 2;
            
            manager.setPatternPosition(rootActivityPattern, -origin.x,
                    contentTop + (contentHeight - clientSize.height) / 2);
            
            yMax += clientSize.height + LayoutManager.VSPACING;
            
            endEvent.setCenter(0f, yMax + endEvent.getHeight() / 2);
            
            rangeX.extend(-origin.x - contentPadding);
            rangeX.extend(clientSize.width - origin.x + contentPadding);
        }
        
        endEvent.setCenter(0, contentBottom + LayoutManager.VSPACING +
                endEvent.getHeight() / 2);
        
        double y0 = -startEvent.getHeight() / 2;
        double y1 = contentBottom + LayoutManager.VSPACING + endEvent.getHeight();
        
        getBorder().setClientRectangle(rangeX.min, y0, rangeX.getSize(), y1 - y0);
        
        FBounds result = getBorder().getBounds();
        
        PartnerLinkContainer plc = process.getPartnerLinkContainer();
        
        if (plc != null){
            PartnerLinksPattern plc_pattern =
                    (PartnerLinksPattern) getModel().getPattern(plc);
            if (plc_pattern != null && plc_pattern.isInModel()){
                FBounds client = plc_pattern.getBounds();
                manager.setPatternPosition(plc_pattern,
                        rangeX.min - LayoutManager.HSPACING * 5 - client.width,
                        0);
                
                plc_pattern.optimizePositions(manager);
            }
        }
        
        
        EventHandlers eventHandlers = process.getEventHandlers();
        FaultHandlers faultHandlers = process.getFaultHandlers();
        
        if ((eventHandlers == null) && (faultHandlers == null)) return null;
        
        double paddingTop = getBorder().getInsets().top;
        
        double badgeX = result.x + result.width;
        double badgeY = startEvent.getY() + eventsBadge.getHeight() / 2;
        
        double lastBadgeBottom = 0;
        
        double handlerX = badgeX + LayoutManager.HSPACING;
        double handlerY = badgeY + LayoutManager.VSPACING;
        
        if (eventHandlers != null) {
            Pattern p = getNestedPattern(eventHandlers);
            eventsBadge.setCenter(badgeX, badgeY);
            
            lastBadgeBottom = badgeY + eventsBadge.getHeight() / 2;
            
            manager.setPatternPosition(p, handlerX, handlerY);
            
            double handlerHeight = p.getBounds().height
                    + 2 * LayoutManager.VSPACING;
            
            badgeY += handlerHeight;
            handlerY += handlerHeight;
        }
        
        
        if (faultHandlers != null) {
            Pattern p = getNestedPattern(faultHandlers);
            faultBadge.setCenter(badgeX, badgeY);
            
            lastBadgeBottom = badgeY + eventsBadge.getHeight() / 2;
            
            manager.setPatternPosition(p, handlerX, handlerY);
            
            double handlerHeight = p.getBounds().height
                    + 2 * LayoutManager.VSPACING;
            badgeY += handlerHeight;
            handlerY += handlerHeight;
            
        }
        
        
        getBorder().setClientRectangle(rangeX.min, y0, rangeX.getSize(),
                Math.max(y1, lastBadgeBottom) - y0);
        
        
        return null;
    }
    

    public boolean isCollapsable() {
        return false;
    }

    
    protected void createElementsImpl() {
        
        setBorder(new ProcessBorder());
        registerTextElement(getBorder());
        
        //getBorder().setLabelText("Process");
        
        Process process = (Process) getOMReference();
        
        startEvent = ContentElement.createStartEvent();
        startEvent.setText("Process Start"); // NOI18N
        
        endEvent = ContentElement.createEndEvent();
        endEvent.setText("Process End"); // NOI18N
        
        placeHolder = new PlaceHolderElement();
        
        appendElement(startEvent);
        appendElement(endEvent);
        appendElement(placeHolder);
        
        ExtendableActivity a = process.getActivity();
        
        if (a != null) {
            Pattern p = getModel().createPattern(a);
            p.setParent(this);
        }
        
        if (getModel().getFilters().showPartnerlinks() &&
                process.getPartnerLinkContainer() != null ) {
            Pattern p = getModel().createPattern(process.getPartnerLinkContainer());
            p.setParent(this);
        }
        
        // Init handlers
        eventsBadge = ContentElement.createEventBadge();
        faultBadge = ContentElement.createFaultBadge();
        
        EventHandlers eventHandlers = process.getEventHandlers();
        FaultHandlers faultHandlers = process.getFaultHandlers();
        
        if (eventHandlers != null) {
            Pattern p = getModel().createPattern(eventHandlers);
            p.setParent(this);
            //appendElement(eventsBadge);
        }
        
        if (faultHandlers != null) {
            Pattern p = getModel().createPattern(faultHandlers);
            p.setParent(this);
            //appendElement(faultBadge);
        }
    }
    
    
    public String getDefaultName() {
        return "Process"; // NOI18N
    }
    
    
    public void createPlaceholders(Pattern draggedPattern,
            Collection<PlaceHolder> placeHolders) {
        if (draggedPattern == this) return;
        
        if (draggedPattern instanceof PartnerlinkPattern){
            //accept DnD only from palette
            if (draggedPattern.getParent() == null){
                placeHolders.add(new PartnerlinkPlaceholder(draggedPattern));
            }
            //placeHolders.add(new PartnerlinkPlaceholder((DesignView) getView(), this, dndPattern));
        } else if (draggedPattern instanceof ImportPattern ){
            placeHolders.add(new ImportPlaceholder(draggedPattern));
        } else if (draggedPattern.getOMReference() instanceof Activity &&
                
                getRootActivityPattern() == null) {
            placeHolders.add(new InnerPlaceHolder(draggedPattern));
        }
    }
    
    
    public NodeType getNodeType() {
        return NodeType.PROCESS;
    }
    
    
    public void reconnectElements() {
        
        Pattern rootActivity = getRootActivityPattern();
        
        if (rootActivity == null){
            connection1.connect(startEvent, Direction.BOTTOM,
                    placeHolder, Direction.TOP);
            
            connection2.connect(placeHolder, Direction.BOTTOM,
                    endEvent, Direction.TOP);
        } else {
            connection1.connect(startEvent, Direction.BOTTOM,
                    rootActivity.getFirstElement(), Direction.TOP);
            
            connection2.connect(rootActivity.getLastElement(), Direction.BOTTOM,
                    endEvent, Direction.TOP);
        }
        
        Pattern eventHandlersPattern = getEventHandlersPattern();
        Pattern faultHandlersPattern = getFaultHandlersPattern();
        
        if (eventHandlersPattern != null) {
            if (eventHandlersConnection == null) {
                eventHandlersConnection = new Connection(this);
            }
            
            eventHandlersConnection.connect(eventsBadge, Direction.RIGHT,
                    eventHandlersPattern.getFirstElement(), Direction.TOP);
        } else if (eventHandlersConnection != null) {
            eventHandlersConnection.remove();
            eventHandlersConnection = null;
        }
        
        if (faultHandlersPattern != null) {
            if (faultHandlersConnection == null) {
                faultHandlersConnection = new Connection(this);
            }
            faultHandlersConnection.connect(faultBadge, Direction.RIGHT,
                    faultHandlersPattern.getFirstElement(), Direction.TOP);
        } else if (faultHandlersConnection != null) {
            faultHandlersConnection.remove();
            faultHandlersConnection = null;
        }
    }
    
    
    private Pattern getEventHandlersPattern() {
        EventHandlers eh = ((Process) getOMReference()).getEventHandlers();
        return (eh != null) ? getNestedPattern(eh) : null;
    }
    
    
    private Pattern getFaultHandlersPattern() {
        FaultHandlers fh = ((Process) getOMReference()).getFaultHandlers();
        return (fh != null) ? getNestedPattern(fh) : null;
    }
    
    
    private Pattern getRootActivityPattern() {
        Activity a = (Activity) ((Process) getOMReference()).getActivity();
        return (a != null) ? getNestedPattern(a) : null;
    }
    
    public Area createSelection() {
        Area a = new Area(getBorder().getShape());
        if (faultBadge.getPattern() != null) {
            a.add(new Area(faultBadge.getShape()));
        }
        if (eventsBadge.getPattern() != null) {
            a.add(new Area(eventsBadge.getShape()));
        }
        a.subtract(new Area(startEvent.getShape()));
        a.subtract(new Area(endEvent.getShape()));
        return a;
    }
    
    public void relayoutPartnerlinks() {
        PartnerLinkContainer plc = ((Process) getOMReference()).getPartnerLinkContainer();
        
        if(plc == null){
            return;
        }
        
        PartnerLinksPattern plp = (PartnerLinksPattern) getNestedPattern(plc);
        
        if (plc == null){
            return;
        }
        
        
        //pass one: calculate optimal Y coordinates for all PLs
        for (Pattern pl: plp.getNestedPatterns() ){
            float yPos = 0;
            int count = 0;
            for (VisualElement element: pl.getElements()){
                for (Connection conn: element.getAllConnections()){
                    yPos += (conn.getSource() == element)?
                        conn.getTarget().getCenterY():
                        conn.getSource().getCenterY();
                    count++;
                }
            }
            yPos = yPos / count;
            FBounds bounds = pl.getBounds();
            
            //LayoutManager.translatePattern(pl, 0, yPos - bounds.getCenterY());
            
        }
        //pass two:
        
    }
    
    
    public void updateAccordingToViewFiltersStatus() {
        PartnerLinksPattern partnerLinks = null;
        
        for (Pattern p : getNestedPatterns()) {
            if (p instanceof PartnerLinksPattern) {
                partnerLinks = (PartnerLinksPattern) p;
                break;
            }
        }
        
        if (getModel().getFilters().showPartnerlinks()) {
            if (partnerLinks == null) {
                Process process = (Process) getOMReference();
                Pattern p = getModel().createPattern(process
                        .getPartnerLinkContainer());
                p.setParent(this);
            }
        } else {
            if (partnerLinks != null) {
                partnerLinks.setParent(null);
            }
        }
    }
    
    class ImportPlaceholder extends DefaultPlaceholder {
        public ImportPlaceholder(Pattern dndPattern) {
            super( ProcessPattern.this, dndPattern);
            
        }
        public void drop() {
            
            Pattern pattern =  getDraggedPattern();
            
            BpelModel model = getModel().getView().getBPELModel();
            
            Import  new_imp = (Import) pattern.getOMReference();
            
            
            
            if (pattern.getParent() == null) {
                if (getModel().getView().showCustomEditor(pattern, 
                        CustomNodeEditor.EditingMode.CREATE_NEW_INSTANCE)){
                    new ImportRegistrationHelper(model).addImport(new_imp);
                }
            }
        }
    }
    class PartnerlinkPlaceholder extends DefaultPlaceholder{
        
        
        
        public PartnerlinkPlaceholder( Pattern dndPattern) {
            super( ProcessPattern.this, dndPattern);
            
        }
        
        public void drop() {
            
            final Pattern pattern =  getDraggedPattern();
            final PartnerLink pl = (PartnerLink) pattern.getOMReference();
            final Object dndCookie = pl.getCookie(DnDHandler.class);
            
            
            RequestProcessor rp = getRequestProcessor();
            
            //Handle the case of dropped WS node.
            //dndCookie contains the URL of deployed web service
            if (dndCookie instanceof FileObject){
                
                FileObject fo = ((FileObject) dndCookie);
                
                if (!isInOurProject(fo)) {
                    
                    try {
                        URL url = fo.getURL();
                        String name = fo.getName();
                        rp.post(new RetrieveWSDLTask(url, name, pl, false));
                    } catch (FileStateInvalidException ex) {
                        assert false;
                    }
                    
                } else {
                    pl.setCookie(DnDHandler.class, fo);
                    
                }
                rp.post(new AddPartnerLinkTask(pl, pattern));
                
            } else if (dndCookie instanceof WebServiceReference){
                URL url = ((WebServiceReference) dndCookie).getWsdlURL();
                String name = ((WebServiceReference) dndCookie).getWebServiceName();
                if (url != null){
                    rp.post(new RetrieveWSDLTask(url, name, pl, true));
                    rp.post(new AddPartnerLinkTask(pl, pattern));
                } else {
                    //
                    String messageText = NbBundle.getMessage(ProcessPattern.class,
                            "LBL_J2EEWS_NOT_DEPLOYED", // NOI18N
                            ""
                            );
                    UserNotification.showMessageAsinc(messageText);
                    
                }
            } else {
                rp.post(new AddPartnerLinkTask(pl, pattern));
            }
            
            
            
        }
        private boolean isInOurProject(FileObject fo){
            
            
            FileObject bpel_fo = (FileObject) getModel()
            .getView()
            .getBPELModel()
            .getModelSource()
            .getLookup()
            .lookup(FileObject.class);
            
            
            
            if (bpel_fo == null ){
                return false;
            }
            
            Project my_project = FileOwnerQuery.getOwner(bpel_fo);
            Project other_project = FileOwnerQuery.getOwner(fo);
            
            return (my_project != null) && my_project.equals(other_project);
        }
        class RetrieveWSDLTask implements Runnable{
            private URL url;
            private String name;
            private PartnerLink pl;
            private boolean retrieveToFlat;
            public RetrieveWSDLTask(URL url, String name, PartnerLink pl, boolean retrieveToFlat){
                this.url = url;
                this.name = name;
                this.pl = pl;
                this.retrieveToFlat = retrieveToFlat;
            }
            public void run(){
                DesignView view = getModel().getView();
                Cursor oldCursor = view.getCursor();
                view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                
                FileObject fo = new PartnerLinkHelper(getModel()).
                        retrieveWSDL(url, name, retrieveToFlat);
                pl.setCookie(DnDHandler.class, fo);
                
                view.setCursor(oldCursor);
                
            }
            
        }
        
        class AddPartnerLinkTask implements Runnable{
            private PartnerLink pLink;
            private Pattern pattern;
            public AddPartnerLinkTask(PartnerLink pLink, Pattern pattern){
                this.pLink = pLink;
                this.pattern = pattern;
            }
            public void run() {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run() {
                        
                        final BpelModel model = getModel().getView().getBPELModel();
                        try {
                            model.invoke( new Callable() {
                                public Object call() throws Exception {
                                    Process process = model.getProcess();
                                    PartnerLinkContainer plc = process.getPartnerLinkContainer();
                                    boolean isPlContainerCreated = false;
                                    
                                    if (plc == null){
                                        plc = model.getBuilder().createPartnerLinkContainer();
                                        process.setPartnerLinkContainer(plc);
                                        isPlContainerCreated = true;
                                    }
                                    plc.insertPartnerLink(pLink, 0);
                                    
                                    if (!getModel().getView().showCustomEditor(
                                            pattern, CustomNodeEditor.EditingMode.CREATE_NEW_INSTANCE)){
                                        plc.remove(pLink);
                                        if (isPlContainerCreated) {
                                            process.removePartnerLinkContainer();
                                        }
                                    }
                                    return null;
                                }
                            }, this);
                        } catch (Exception ex){
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        }
                        
                    }
                    
                });
            }
        }
        
    }
    class InnerPlaceHolder extends PlaceHolder {
        public InnerPlaceHolder(Pattern draggedPattern) {
            super(ProcessPattern.this, draggedPattern, placeHolder.getCenterX(),
                    placeHolder.getCenterY());
        }
        
        
        public void drop() {
            Pattern p = getDraggedPattern();
            ((Process)getOMReference()).setActivity((Activity) p.getOMReference());
        }
    }
    
    private synchronized RequestProcessor getRequestProcessor(){
        if (wsdlDnDRequestProcessor == null){
            wsdlDnDRequestProcessor = new RequestProcessor(getClass().getName());
        }
        return wsdlDnDRequestProcessor;
        
    }
    RequestProcessor wsdlDnDRequestProcessor;
    
    private static final float INITIAL_SIZE = 200;
    
    private static final float MIN_CONTENT_WIDTH = 200;
    private static final float MIN_CONTENT_HEIGHT = 200;
    
    public Connection getConnection1() {
        return connection1;
    }
    
    public Connection getConnection2() {
        return connection2;
    }

    
}
