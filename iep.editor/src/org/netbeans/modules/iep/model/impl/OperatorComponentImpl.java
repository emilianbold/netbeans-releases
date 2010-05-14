package org.netbeans.modules.iep.model.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.IEPVisitor;
import org.netbeans.modules.iep.model.ModelHelper;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.IOType;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.iep.model.SchemaComponent;
import org.netbeans.modules.iep.model.WSType;
import org.w3c.dom.Element;

public class OperatorComponentImpl extends ComponentImpl implements OperatorComponent {

    public OperatorComponentImpl(IEPModel model, Element e) {
        super(model, e);
    }

    public OperatorComponentImpl(IEPModel model) {
        super(model);
    }

    @Override
    public void accept(IEPVisitor visitor) {
        visitor.visitOperatorComponent(this);
    }

    public WSType getWsType() {
        String value = getComponentType().getPropertyType(PROP_WS_TYPE).getDefaultValueAsString();
        return WSType.getType(value);
    }

    public List<OperatorComponent> getInputOperatorList() {
        List<OperatorComponent> inputOperators = new ArrayList<OperatorComponent>();

        Property p = getProperty(PROP_INPUT_ID_LIST);
        if (p != null) {
            String value = p.getValue();
            List inputs = (List) p.getPropertyType().getType().parse(value);
            if (inputs != null) {
                Iterator it = inputs.iterator();
                while (it.hasNext()) {
                    String id = (String) it.next();
                    OperatorComponent oc = ModelHelper.findOperator(id, getModel());
                    if (oc != null) {
                        inputOperators.add(oc);
                    }
                }
            }
        }

        return inputOperators;
    }

    public List<SchemaComponent> getInputSchemaList() {
        List<SchemaComponent> inputSchemas = new ArrayList<SchemaComponent>();

        Property p = getProperty(PROP_INPUT_SCHEMA_ID_LIST);
        if (p != null) {
            String value = p.getValue();
            List inputSchemaIds = (List) p.getPropertyType().getType().parse(value);
            if (inputSchemaIds != null) {
                Iterator it = inputSchemaIds.iterator();
                while (it.hasNext()) {
                    String name = (String) it.next();
                    SchemaComponent sc = ModelHelper.findSchema(name, getModel());
                    if (sc != null) {
                        inputSchemas.add(sc);
                    }
                }
            }
        }

        return inputSchemas;
    }

    public IOType getInputType() {
        String value = getComponentType().getPropertyType(PROP_INPUT_TYPE).getDefaultValueAsString();
        if (value != null) {
            return IOType.getType(value);
        }
        return IOType.NONE;
    }

    public SchemaComponent getOutputSchema() {
        SchemaComponent outputSchema = null;

        Property p = getProperty(PROP_OUTPUT_SCHEMA_ID);
        if (p != null) {
            String name = p.getValue();
            if (name != null) {
                outputSchema = ModelHelper.findSchema(name, getModel());
            }
        }

        return outputSchema;
    }

    public void setOutputSchema(SchemaComponent sc) {
        String outputschemaId = "";
        if (sc != null) {
            outputschemaId = sc.getName();
        }

        Property p = getProperty(PROP_OUTPUT_SCHEMA_ID);
        if (p == null) {
            p = getModel().getFactory().createProperty(getModel());
            p.setName(PROP_OUTPUT_SCHEMA_ID);
            addProperty(p);
        }

        p.setValue(outputschemaId);
    }

    public IOType getOutputType() {
        String value = getComponentType().getPropertyType(PROP_OUTPUT_TYPE).getDefaultValueAsString();
        if (value != null) {
            return IOType.getType(value);
        }
        return IOType.NONE;
    }

    public List<OperatorComponent> getStaticInputList() {
        List<OperatorComponent> inputTables = new ArrayList<OperatorComponent>();

        Property p = getProperty(PROP_STATIC_INPUT_ID_LIST);
        if (p != null) {
            String value = p.getValue();
            List inputSchemaIds = (List) p.getPropertyType().getType().parse(value);
            if (inputSchemaIds != null) {
                Iterator it = inputSchemaIds.iterator();
                while (it.hasNext()) {
                    String id = (String) it.next();
                    OperatorComponent sc = ModelHelper.findOperator(id, getModel());
                    if (sc != null) {
                        inputTables.add(sc);
                    }
                }
            }
        }

        return inputTables;
    }

    @Override
    public String toString() {
        return "name:" + getName() + " type:" + getType();
    }
}
