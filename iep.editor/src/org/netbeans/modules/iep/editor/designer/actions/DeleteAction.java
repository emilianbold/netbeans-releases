package org.netbeans.modules.iep.editor.designer.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

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

public class DeleteAction extends AbstractAction {

	public final static String DELETE_NAME = "delete";
    
	private PlanCanvas mCanvas;
	
	private IEPModel mModel;
	
	
	public DeleteAction(PlanCanvas canvas, IEPModel model) {
	   super(DELETE_NAME);
	   this.mCanvas = canvas;
	   this.mModel = model;
	}
	
	public void actionPerformed(ActionEvent e) {
		List<OperatorComponent> nodeList = new ArrayList<OperatorComponent>();
        List<LinkComponent> linkList = new ArrayList<LinkComponent>();
        List<SchemaComponent> schemaList =  new ArrayList<SchemaComponent>();
        
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
                nodeList.add(oc);
                
                addInputLinksOfAnOperator(oc, linkList);
                
                addOutputLinksOfAnOperator(oc, linkList);
            
                if(oc.isSchemaOwner()) {
                	SchemaComponent schema = oc.getOutputSchemaId();
                	if(schema != null) {
                		schemaList.add(schema);
                	}
                }
                
                continue;
            }
            
            if (obj instanceof Link) {
                linkList.add((LinkComponent) ((Link)obj).getModelComponent());
                continue;
            }
        }
        
        
        
        PlanComponent planComp = mModel.getPlanComponent();
        OperatorComponentContainer opContainer = planComp.getOperatorComponentContainer();
        LinkComponentContainer linkContainer = planComp.getLinkComponentContainer();
        SchemaComponentContainer schemaContainer = planComp.getSchemaComponentContainer();
        
        
        mModel.startTransaction();
        
        for (int i = 0, I = nodeList.size(); i < I; i++) {
            OperatorComponent opComp = (OperatorComponent)nodeList.get(i);
            opContainer.removeOperatorComponent(opComp);
        }
        
        for (int i = 0, I = linkList.size(); i < I; i++) {
            LinkComponent lc = (LinkComponent)linkList.get(i);
            linkContainer.removeLinkComponent(lc);
        }
        
        for (int i = 0, I = schemaList.size(); i < I; i++) {
            SchemaComponent sc = (SchemaComponent)schemaList.get(i);
            schemaContainer.removeSchemaComponent(sc);
        }
        
        mModel.endTransaction();
	}
	
	private void addInputLinksOfAnOperator(OperatorComponent opComp, List linkList) {
		List<OperatorComponent> inputOps = opComp.getInputOperatorList();
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
	
	private void addOutputLinksOfAnOperator(OperatorComponent opComp, List linkList) {
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
