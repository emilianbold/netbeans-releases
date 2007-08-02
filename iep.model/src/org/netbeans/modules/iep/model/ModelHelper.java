package org.netbeans.modules.iep.model;

import java.util.Iterator;
import java.util.List;

public class ModelHelper {

	public static OperatorComponent findOperator(String id, IEPModel model) {
		if(id == null) {
			return null;
		}
		
		OperatorComponent operatorComponent = null;
		
		PlanComponent pComponent = model.getPlanComponent();
		OperatorComponentContainer operatorsComponent = pComponent.getOperatorComponentContainer();
		if(operatorsComponent != null) {
			List<OperatorComponent> operators = operatorsComponent.getAllOperatorComponent();
			Iterator<OperatorComponent> it = operators.iterator(); 
			while(it.hasNext()) {
				OperatorComponent oc = it.next();
				if(id.equals(oc.getId())) {
					operatorComponent = oc;
					break;
				}
			}
		}
		
		return operatorComponent;
		
	}
	
	public static LinkComponent findLink(OperatorComponent fromComponent, OperatorComponent toComponent, IEPModel model) {
		LinkComponent linkComponent = null;
		
		PlanComponent pComponent = model.getPlanComponent();
		LinkComponentContainer linksComponent = pComponent.getLinkComponentContainer();
		if(linksComponent != null) {
			List<LinkComponent> operators = linksComponent.getAllLinkComponents();
			Iterator<LinkComponent> it = operators.iterator(); 
			while(it.hasNext()) {
				LinkComponent lc = it.next();
				OperatorComponent fc = lc.getFrom();
				OperatorComponent tc = lc.getTo();
				
				if(fc != null 
				   && fc.equals(fromComponent)
				   && tc != null 
				   && tc.equals(toComponent)) {
					linkComponent = lc;
					break;
				}
			}
		}
		
		return linkComponent;
		
	}
	
	public static LinkComponent findLink(String name, IEPModel model) {
		if(name == null) {
			return null;
		}
		
		LinkComponent linkComponent = null;
		
		PlanComponent pComponent = model.getPlanComponent();
		LinkComponentContainer linksComponent = pComponent.getLinkComponentContainer();
		if(linksComponent != null) {
			List<LinkComponent> operators = linksComponent.getAllLinkComponents();
			Iterator<LinkComponent> it = operators.iterator(); 
			while(it.hasNext()) {
				LinkComponent lc = it.next();
				
				if(name.equals(lc.getName())) {
					linkComponent = lc;
					break;
				}
			}
		}
		
		return linkComponent;
		
	}
	public static SchemaComponent findSchema(String name, IEPModel model) {
		if(name == null) {
			return null;
		}
		
		SchemaComponent schemaComponent = null;
		
		PlanComponent pComponent = model.getPlanComponent();
		SchemaComponentContainer schemasComponent = pComponent.getSchemaComponentContainer();
		if(schemasComponent != null) {
			List<SchemaComponent> schemas = schemasComponent.getAllSchemaComponents();
			Iterator<SchemaComponent> it = schemas.iterator(); 
			while(it.hasNext()) {
				SchemaComponent oc = it.next();
				if(name.equals(oc.getName())) {
					schemaComponent = oc;
					break;
				}
			}
		}
		
		return schemaComponent;		
	}
	
}
