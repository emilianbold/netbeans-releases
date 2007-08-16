package org.netbeans.modules.iep.model.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.iep.model.Component;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.ModelHelper;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.OperatorType;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.iep.model.SchemaComponent;
import org.w3c.dom.Element;

public class OperatorComponentImpl extends ComponentImpl implements OperatorComponent {

	private OperatorType mAllowedInputType;
	
	private OperatorType mAllowedOutputType;
	
	
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

	//rit it can not be property it could be column name
	public String getGroupByColumnList() {
		String groupByColumnList = null;
		
		Property p = super.getProperty(PROP_GROUP_BY_COLUMNLIST);
		if(p != null) {
			groupByColumnList = p.getValue();
		}
		
		return groupByColumnList;
	}

	public String getId() {
		String id = null;
		
		Property p = super.getProperty(PROP_ID);
		if(p != null) {
			id = p.getValue();
		}
		
		return id;
	}
	
	public void setId(String id) {
		Property p = super.getProperty(PROP_ID);
		if(p == null) {
			p = getModel().getFactory().createProperty(getModel());
			p.setName(PROP_ID);
			addProperty(p);
			
		}
		
		p.setValue(id+"");
	}

	public List<OperatorComponent> getInputOperatorList() {
		List<OperatorComponent> inputOperators = new ArrayList<OperatorComponent>();
		
		Property p = getProperty(PROP_INPUT_ID_LIST);
		if(p != null) {
			String value = p.getValue();
			List inputs = (List) p.getPropertyType().getType().parse(value);
			if(inputs != null) {
				Iterator it = inputs.iterator();
				while(it.hasNext()) {
					String id = (String) it.next();
					OperatorComponent oc = ModelHelper.findOperator(id, getModel());
					if(oc != null) {
						inputOperators.add(oc);
					}
				}
			}
		}
		
		return inputOperators;
	}

	public List<SchemaComponent> getInputSchemaIdList() {
		List<SchemaComponent> inputSchemas = new ArrayList<SchemaComponent>();
		
		Property p = getProperty(PROP_INPUT_SCHEMA_ID_LIST);
		if(p != null) {
			String value = p.getValue();
			List inputSchemaIds = (List) p.getPropertyType().getType().parse(value);
			if(inputSchemaIds != null) {
				Iterator it = inputSchemaIds.iterator();
				while(it.hasNext()) {
					String name = (String) it.next();
					SchemaComponent sc = ModelHelper.findSchema(name, getModel());
					if(sc != null) {
						inputSchemas.add(sc);
					}
				}
			}
		}
		
		return inputSchemas;
	}

	public OperatorType getInputType() {
		if(mAllowedInputType == null) {
			Property p = getProperty(PROP_INPUTTYPE);
			if(p != null) {
				String value = p.getValue();
				if(value != null) {
					mAllowedInputType = OperatorType.getType(value);
				}
			}
		}
		return mAllowedInputType;
	}

	public SchemaComponent getOutputSchemaId() {
		SchemaComponent outputSchema = null;
		
		Property p = getProperty(PROP_OUTPUT_SCHEMA_ID);
		if(p != null) {
			String name = p.getValue();
			if(name != null) {
				outputSchema = ModelHelper.findSchema(name, getModel());
			}
		}
		
		return outputSchema;
	}

	public OperatorType getOutputType() {
		if(mAllowedOutputType == null) {
			Property p = getProperty(PROP_INPUTTYPE);
			if(p != null) {
				String value = p.getValue();
				if(value != null) {
					mAllowedOutputType = OperatorType.getType(value);
				}
			}
		}
		return mAllowedOutputType;
		
	}

	public List<OperatorComponent> getStaticInputTableList() {
		List<OperatorComponent> inputTables = new ArrayList<OperatorComponent>();
		
		Property p = getProperty(PROP_STATIC_INPUT_ID_LIST);
		if(p != null) {
			String value = p.getValue();
			List inputSchemaIds = (List) p.getPropertyType().getType().parse(value);
			if(inputSchemaIds != null) {
				Iterator it = inputSchemaIds.iterator();
				while(it.hasNext()) {
					String id = (String) it.next();
					OperatorComponent sc = ModelHelper.findOperator(id, getModel());
					if(sc != null) {
						inputTables.add(sc);
					}
				}
			}
		}
		
		return inputTables;
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

	public String getFromClause() {
		String fromClause = null;
		
		Property p = super.getProperty(PROP_FROMCLAUSE);
		if(p != null) {
			fromClause = p.getValue();
		}
		
		return fromClause;
		
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
	
	public boolean isSchemaOwner() {
		return false;
	}
	
	public String getDisplayName() {
		
		String displayName = null;
		
		Property p = super.getProperty(PROP_NAME);
		if(p != null) {
			displayName = p.getValue();
		}
		
		return displayName;
	}
	
	public void setDisplayName(String displayName) {
		Property p = super.getProperty(PROP_NAME);
		if(p == null) {
			p = getModel().getFactory().createProperty(getModel());
			p.setName(PROP_NAME);
			addProperty(p);
			
		}
		
		p.setValue(displayName);
	}
	
	
	public String toString() {
		return "name:" + getName() + " type:" + getType();
	}
}
