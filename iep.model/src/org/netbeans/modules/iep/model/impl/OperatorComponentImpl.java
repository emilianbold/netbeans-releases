package org.netbeans.modules.iep.model.impl;

import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.OperatorType;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.iep.model.SchemaComponent;
import org.w3c.dom.Element;

public class OperatorComponentImpl extends ComponentImpl implements OperatorComponent {

	public OperatorComponentImpl(IEPModel model,  Element e) {
            super(model, e);
        }
        
    public OperatorComponentImpl(IEPModel model) {
        super(model);
    }
        
	public String getDescription() {
		String description = null;
		
		Property p = super.getProperty(PROP_DESCRIPTION);
		if(p != null) {
			description = p.getValue();
		}
		
		return description;
	}

	public void setDescription(String description) {
		Property p = super.getProperty(PROP_DESCRIPTION);
		if(p == null) {
			p = getModel().getFactory().createProperty(getModel());
			p.setName(PROP_DESCRIPTION);
			addProperty(p);
			
		}
		
		p.setValue(description);
		
	}
	
	public List<OperatorComponent> getFromColumnList() {
		
		return null;
	}

	public String getGlobalId() {
		String globalId = null;
		
		Property p = super.getProperty(PROP_GLOBALID);
		if(p != null) {
			globalId = p.getValue();
		}
		
		return globalId;
	}

	public List<Property> getGroupByColumnList() {
		return null;
	}

	public String getId() {
		String id = null;
		
		Property p = super.getProperty(PROP_ID);
		if(p != null) {
			id = p.getValue();
		}
		
		return id;
	}

	public List<OperatorComponent> getInputOperatorList() {
		List<OperatorComponent> inputOperators = new ArrayList<OperatorComponent>();
		
		Property p = getProperty(PROP_INPUT_ID_LIST);
		if(p != null) {
			String value = p.getValue();
		}
		return null;
	}

	public List<SchemaComponent> getInputSchemaIdList() {
		return null;
	}

	public OperatorType getInputType() {
		return null;
	}

	public SchemaComponent getOutputSchemaId() {
		return null;
	}

	public OperatorType getOutputType() {
		return null;
	}

	public List<OperatorComponent> getStaticInputTableList() {
		return null;
	}

	public List<Property> getToColumnList() {
		return null;
	}

	public String getTopoScore() {
		String topoScore = null;
		
		Property p = super.getProperty(PROP_TOPOSCORE);
		if(p != null) {
			topoScore = p.getValue();
		}
		
		return topoScore;
	}

	public String getWhereClause() {
		String whereClause = null;
		
		Property p = super.getProperty(PROP_WHERECLAUSE);
		if(p != null) {
			whereClause = p.getValue();
		}
		
		return whereClause;
		
	}

	public int getX() {
		
		String xStr = null;
		
		Property p = super.getProperty(PROP_X);
		if(p != null) {
			xStr = p.getValue();
		} else {
			xStr = "0";
		}
		
		int x = 0;
		try {
			x = Integer.parseInt(xStr);
		}catch(NumberFormatException ex) {
		}
		
		return x;
		
	}
	
	public void setX(int x) {
		Property p = super.getProperty(PROP_X);
		if(p == null) {
			p = getModel().getFactory().createProperty(getModel());
			p.setName(PROP_X);
			addProperty(p);
			
		}
		
		p.setValue(x+"");
	}
	
	public int getY() {
		String yStr = null;
		
		Property p = super.getProperty(PROP_Y);
		if(p != null) {
			yStr = p.getValue();
		} else {
			yStr = "0";
		}
		
		int y = 0;
		try {
			y = Integer.parseInt(yStr);
		}catch(NumberFormatException ex) {
		}
		
		return y;
	
	}
	
	public void setY(int y) {
		Property p = super.getProperty(PROP_Y);
		if(p == null) {
			p = getModel().getFactory().createProperty(getModel());
			p.setName(PROP_Y);
			addProperty(p);
			
		}
		
		p.setValue(y+"");
	}
	
	public int getZ() {
		String zStr = null;
		
		Property p = super.getProperty(PROP_Z);
		if(p != null) {
			zStr = p.getValue();
		} else {
			zStr = "0";
		}
		
		int z = 0;
		try {
			z = Integer.parseInt(zStr);
		}catch(NumberFormatException ex) {
		}
		
		return z;
		
	}

	public void setZ(int z) {
		Property p = super.getProperty(PROP_Z);
		if(p == null) {
			p = getModel().getFactory().createProperty(getModel());
			p.setName(PROP_Z);
			addProperty(p);
			
		}
		
		p.setValue(z+"");
	}
	
	public boolean isBatchMode() {
		return false;
	}

	public boolean isGlobal() {
		return false;
	}

	public String getDisplayName() {
		
		String displayName = null;
		
		Property p = super.getProperty(PROP_DISPLAYNAME);
		if(p != null) {
			displayName = p.getValue();
		}
		
		return displayName;
	}
}
