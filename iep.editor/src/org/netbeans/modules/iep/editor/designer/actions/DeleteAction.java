package org.netbeans.modules.iep.editor.designer.actions;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;

import org.netbeans.modules.iep.editor.designer.EntityNode;
import org.netbeans.modules.iep.editor.designer.Link;
import org.netbeans.modules.iep.editor.designer.PlanCanvas;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.LinkComponent;
import org.netbeans.modules.iep.model.LinkComponentContainer;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.OperatorComponentContainer;
import org.netbeans.modules.iep.model.PlanComponent;
import org.netbeans.modules.iep.model.SchemaComponent;
import org.netbeans.modules.iep.model.SchemaComponentContainer;

import com.nwoods.jgo.JGoListPosition;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoSelection;
import org.netbeans.modules.iep.model.share.SharedConstants;

public class DeleteAction extends AbstractAction implements SharedConstants {

    public final static String DELETE_NAME = "delete";
    
    private PlanCanvas mCanvas;
    
    private IEPModel mModel;
    
    
    public DeleteAction(PlanCanvas canvas, IEPModel model) {
       super(DELETE_NAME);
       this.mCanvas = canvas;
       this.mModel = model;
    }
    
    public void actionPerformed(ActionEvent e) {
        Set<OperatorComponent> nodeSet = new HashSet<OperatorComponent>();
        Set<LinkComponent> linkSet = new HashSet<LinkComponent>();
        Set<SchemaComponent> schemaSet =  new HashSet<SchemaComponent>();
        
        JGoSelection sel = mCanvas.getSelection();
        JGoListPosition pos = sel.getFirstObjectPos();
        while (pos != null) {
            JGoObject obj = sel.getObjectAtPos(pos);
            pos = sel.getNextObjectPos(pos);
            if (obj.getLayer() != null && !obj.getLayer().isModifiable()) {
                // skip this object because it's in an unmodifiable layer
                continue;
            }
            obj = obj.getDraggingObject();
            if (obj instanceof EntityNode) {
                OperatorComponent oc = (OperatorComponent) ((EntityNode)obj).getModelComponent(); 
                nodeSet.add(oc);
                
                addInputLinksOfAnOperator(oc, linkSet);
                
                addOutputLinksOfAnOperator(oc, linkSet);
            
                if(oc.getBoolean(PROP_IS_SCHEMA_OWNER)) {
                    SchemaComponent schema = oc.getOutputSchema();
                    if(schema != null) {
                        schemaSet.add(schema);
                    }
                }
                
                continue;
            }
            
            if (obj instanceof Link) {
                linkSet.add((LinkComponent) ((Link)obj).getModelComponent());
                continue;
            }
        }
        
        
        
        PlanComponent planComp = mModel.getPlanComponent();
        OperatorComponentContainer opContainer = planComp.getOperatorComponentContainer();
        LinkComponentContainer linkContainer = planComp.getLinkComponentContainer();
        SchemaComponentContainer schemaContainer = planComp.getSchemaComponentContainer();
        
        
        mModel.startTransaction();
        
        Iterator<OperatorComponent> itComp = nodeSet.iterator();
        while(itComp.hasNext()) {
            OperatorComponent opComp = itComp.next();
            opContainer.removeOperatorComponent(opComp);
        }
        
        Iterator<LinkComponent> itLink = linkSet.iterator();
        while(itLink.hasNext()) {
            LinkComponent lc = itLink.next();
            linkContainer.removeLinkComponent(lc);
        }
        
        Iterator<SchemaComponent> itSchema = schemaSet.iterator();
        while(itSchema.hasNext()) {
            SchemaComponent sc = itSchema.next();
            schemaContainer.removeSchemaComponent(sc);
        }
        
        
        mModel.endTransaction();
    }
    
    private void addInputLinksOfAnOperator(OperatorComponent opComp, Set<LinkComponent> linkList) {
        List<OperatorComponent> inputOps = opComp.getInputOperatorList();
        /* #150437 have to check to see if there are links tied to the staticInputTableList
         * if present they have to be deleted too. */
        List<OperatorComponent> staticInputOps = opComp.getStaticInputList();
        if (staticInputOps != null && staticInputOps.size() > 0) {
            inputOps.addAll(staticInputOps);
        }
        Iterator<OperatorComponent> it = inputOps.iterator();
        
        PlanComponent planComp = opComp.getModel().getPlanComponent();
        LinkComponentContainer lcContainer = planComp.getLinkComponentContainer();
        
        while(it.hasNext()) {
            OperatorComponent oc = it.next();
            LinkComponent lcComp = lcContainer.findLink(oc, opComp);
            if(lcComp != null) {
                linkList.add(lcComp);
            }
        }
        
    }
    
    private void addOutputLinksOfAnOperator(OperatorComponent opComp, Set<LinkComponent> linkList) {
        PlanComponent planComp = opComp.getModel().getPlanComponent();
        LinkComponentContainer lcContainer = planComp.getLinkComponentContainer();
        OperatorComponentContainer ocContainer = planComp.getOperatorComponentContainer();
        List<OperatorComponent> outputOps = ocContainer.findOutputOperator(opComp);
        Iterator<OperatorComponent> it = outputOps.iterator();
        
        while(it.hasNext()) {
            OperatorComponent oc = it.next();
            LinkComponent lcComp = lcContainer.findLink(opComp, oc);
            if(lcComp != null) {
                linkList.add(lcComp);
            }
        }
        
    }
}
