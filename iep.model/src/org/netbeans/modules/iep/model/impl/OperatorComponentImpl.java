package org.netbeans.modules.iep.model.impl;

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
