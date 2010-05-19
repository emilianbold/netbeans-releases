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
import org.netbeans.modules.bpel.model.api.support.ImportHelper;
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
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.netbeans.modules.xml.reference.ReferenceUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.xml.xam.Model;
import org.openide.util.NbBundle;

/**
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
               
                manager.setPatternPosition(plc_pattern,0 ,0);
                
                //plc_pattern.optimizePositions(manager);
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
        startEvent.setText(NbBundle.getMessage(ProcessPattern.class, "LBL_Process_Start")); // NOI18N
        
        endEvent = ContentElement.createEndEvent();
        endEvent.setText(NbBundle.getMessage(ProcessPattern.class, "LBL_Process_End")); // NOI18N
        
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
        
        if (draggedPattern instanceof ImportPattern ){
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
    
    public void reloadPartnerlinks() {
        PartnerLinkContainer plc = ((Process) getOMReference()).getPartnerLinkContainer();
        
        if (plc == null) {
            return;
        }
        PartnerLinksPattern plp = (PartnerLinksPattern) getNestedPattern(plc);
        
        if (plp != null) {
            plp.setParent(null);
        }
        if (getModel().getFilters().showPartnerlinks() && plc != null) {
            Pattern p = getModel().createPattern(plc);
            p.setParent(this);
        }
    }
    
    public void updateAccordingToViewFiltersStatus() {
        reloadPartnerlinks();
    }
    
    class ImportPlaceholder extends DefaultPlaceholder {
        public ImportPlaceholder(Pattern dndPattern) {
            super( ProcessPattern.this, dndPattern);
        }

        public void drop() {
//System.out.println("!!!!!!!!!!");
            Pattern pattern =  getDraggedPattern();
            BpelModel model = getModel().getView().getBPELModel();
            Import new_imp = (Import) pattern.getOMReference();
            Model schema = ImportHelper.getSchemaModel(new_imp);
//System.out.println("new_imp: " + new_imp.getImportType());
            
            if (pattern.getParent() != null) {
              return;
            }
            if ( !new_imp.getImportType().equals(Import.SCHEMA_IMPORT_TYPE)) {
              return;
            }
            // vlv: dnd
            FileObject modelFileObject = SoaUtil.getFileObjectByModel(model);
            FileObject schemaFileObject = SoaUtil.getFileObjectByModel(schema);
            
            if (!ReferenceUtil.isSameProject(modelFileObject, schemaFileObject)) {
                ReferenceUtil.addFile(modelFileObject, schemaFileObject);
            }
            new ImportRegistrationHelper(model).addImport(schema);
        }
    }

    class InnerPlaceHolder extends PlaceHolder {
        public InnerPlaceHolder(Pattern draggedPattern) {
            super(ProcessPattern.this, draggedPattern, placeHolder.getCenterX(), placeHolder.getCenterY());
        }
        
        public void drop() {
            Pattern p = getDraggedPattern();
            ((Process)getOMReference()).setActivity((Activity) p.getOMReference());
        }
    }
    
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
