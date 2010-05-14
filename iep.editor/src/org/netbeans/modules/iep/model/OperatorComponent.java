package org.netbeans.modules.iep.model;

import java.util.List;

public interface OperatorComponent extends Component {

    List<SchemaComponent> getInputSchemaList();

    SchemaComponent getOutputSchema();

    void setOutputSchema(SchemaComponent sc);

    IOType getInputType();

    IOType getOutputType();

    List<OperatorComponent> getInputOperatorList();

    List<OperatorComponent> getStaticInputList();

    WSType getWsType();
}
