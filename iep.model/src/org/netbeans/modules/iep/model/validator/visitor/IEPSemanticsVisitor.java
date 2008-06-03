package org.netbeans.modules.iep.model.validator.visitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.netbeans.modules.iep.model.Component;
import org.netbeans.modules.iep.model.Documentation;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.IEPVisitor;
import org.netbeans.modules.iep.model.Import;
import org.netbeans.modules.iep.model.InputOperatorComponent;
import org.netbeans.modules.iep.model.InvokeStreamOperatorComponent;
import org.netbeans.modules.iep.model.LinkComponent;
import org.netbeans.modules.iep.model.LinkComponentContainer;
import org.netbeans.modules.iep.model.ModelHelper;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.OperatorComponentContainer;
import org.netbeans.modules.iep.model.OutputOperatorComponent;
import org.netbeans.modules.iep.model.PlanComponent;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.iep.model.SchemaAttribute;
import org.netbeans.modules.iep.model.SchemaComponent;
import org.netbeans.modules.iep.model.SchemaComponentContainer;
import org.netbeans.modules.iep.model.TableInputOperatorComponent;
import org.netbeans.modules.iep.model.lib.TcgComponentType;
import org.netbeans.modules.iep.model.lib.TcgPropertyType;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

public class IEPSemanticsVisitor implements IEPVisitor {

    private List<ResultItem> mResultItems = new ArrayList<ResultItem>();
    private Validation mValidation;
    private List<Model> mValidatedModels;
    private Validator mValidator;
    private static Logger mLogger = Logger.getLogger(IEPSemanticsVisitor.class.getName());

    /** Creates a new instance of IEPSemanticsVisitor */
    public IEPSemanticsVisitor(Validator validator, Validation validation, List<Model> validatedModels) {
        mValidator = validator;
        mValidation = validation;
        mValidatedModels = validatedModels;


    }

    public List<ResultItem> getResultItems() {
        return mResultItems;
    }

    public void visitComponent(Component component) {
        List<Property> properties = component.getProperties();
        Iterator<Property> it = properties.iterator();

        while (it.hasNext()) {
            Property property = it.next();
            visitProperty(property);

        }

        //validate all child components
        List<Component> childComponents = component.getChildComponents();
        Iterator<Component> itC = childComponents.iterator();

        while (itC.hasNext()) {
            Component child = itC.next();
            child.accept(this);
        }

    }

    public void visitProperty(Property property) {
        org.netbeans.modules.iep.model.lib.TcgPropertyType propertyType = property.getPropertyType();
        if (propertyType.isRequired() && (property.getValue() == null || property.getValue().equals(""))) {
            String message = NbBundle.getMessage(IEPSemanticsVisitor.class, "DefaultValidator.property_is_required_but_undefined", property.getName());
            ResultItem item = new ResultItem(mValidator, Validator.ResultType.ERROR, property, message);
            mResultItems.add(item);
        }


        if (propertyType.isRequired() && property.getValue().equals(propertyType.getDefaultValue())) {
            Object defVal = propertyType.getDefaultValue();
            String strVal = propertyType.getType().format(defVal);
            String message = NbBundle.getMessage(IEPSemanticsVisitor.class, "DefaultValidator.property_uses_default_value", property.getName()) + " (" + strVal + ")";

            ResultItem item = new ResultItem(mValidator, Validator.ResultType.WARNING, property, message);
            mResultItems.add(item);
        }
    }

    public void visitImport(Import imp) {
        
    }

    
    public void visitDocumentation(Documentation doc) {
    }

    public void visitComponent(LinkComponent component) {
    }

    public void visitLinkComponentContainer(LinkComponentContainer component) {
        visitComponent(component);
    }

    public void visitOperatorComponent(OperatorComponent component) {
        visitComponent(component);

        doDefaultOperatorValidation(component);


        //do operator specific validation
        OperatorValidatorFactory factory = OperatorValidatorFactory.getDefault(mValidator);
        OperatorValidator validator = factory.newOperatorValidator(component);
        if (validator != null) {
            List<ResultItem> results = validator.validate(component);
            if (results != null) {
                mResultItems.addAll(results);
            }
        }

    }

    private void doDefaultOperatorValidation(OperatorComponent component) {
        Property isGlobal = component.getProperty(OperatorComponent.PROP_ISGLOBAL);
        Boolean isGlobalBool = (Boolean) isGlobal.getPropertyType().getType().parse(isGlobal.getValue());
        if (!isGlobalBool.booleanValue()) {
            return;
        }

        Property glbID = component.getProperty(OperatorComponent.PROP_GLOBALID);
        if (glbID.getValue() == null || glbID.getValue().trim().equals("")) {
            String message = NbBundle.getMessage(IEPSemanticsVisitor.class, "DefaultOperatorValidator.property_must_be_defined_for_a_global_entity");
            ResultItem item = new ResultItem(mValidator, Validator.ResultType.ERROR, component, message);
            mResultItems.add(item);
        }
    }

    public void visitOperatorComponentContainer(OperatorComponentContainer component) {
        visitComponent(component);
    }

    public void visitPlanComponent(PlanComponent component) {
        //validate package name not matching directory structure
        String packageName = component.getPackageName();
        if(packageName == null) {
            String message = NbBundle.getMessage(IEPSemanticsVisitor.class, "DefaultOperatorValidator.packageName_should_be_specified");
            ResultItem item = new ResultItem(mValidator, Validator.ResultType.ERROR, component, message);
            mResultItems.add(item);
        } else {
            IEPModel model = component.getModel();
            
            DataObject iepFile = model.getModelSource().getLookup().lookup(DataObject.class);
            String expectedPackageName = ModelHelper.getPackageName(iepFile);
            if(!expectedPackageName.equals(packageName)) {
                String message = NbBundle.getMessage(IEPSemanticsVisitor.class, "DefaultOperatorValidator.expected_packageName_does_not_match_specified_package_name", new Object[] {expectedPackageName, packageName});
                ResultItem item = new ResultItem(mValidator, Validator.ResultType.ERROR, component, message);
                mResultItems.add(item);
            }
        }
        
        
        
        OperatorComponentContainer opContainer = component.getOperatorComponentContainer();
        if (opContainer == null) {
            String message = NbBundle.getMessage(IEPSemanticsVisitor.class, "DefaultOperatorValidator.atleast_one_operator_required");
            ResultItem item = new ResultItem(mValidator, Validator.ResultType.ERROR, component, message);
            mResultItems.add(item);
        } else {

            List<OperatorComponent> operators = opContainer.getAllOperatorComponent();
            Iterator<OperatorComponent> it = operators.iterator();

            while (it.hasNext()) {
                OperatorComponent operator = it.next();
                operator.accept(this);
            }
        }


        IEPModel model = component.getModel();

        //inputs
        List<InputOperatorComponent> inputs = model.getInputList();
        
        if (inputs.size() == 0) {
            List<TableInputOperatorComponent> tableInputs = model.getPlanComponent().getOperatorComponentContainer().getTableInputOperatorComponent();
            if(tableInputs.size() == 0) {
                String message = NbBundle.getMessage(IEPSemanticsVisitor.class, "DefaultOperatorValidator.atleast_one_input_required");
                ResultItem item = new ResultItem(mValidator, Validator.ResultType.ERROR, component, message);
                mResultItems.add(item);
            }

        }

        //outputs
        //check for atleast one output if there are no InvokeStream operators.
        
        List<InvokeStreamOperatorComponent> invokeOps = Collections.EMPTY_LIST;
        if(opContainer != null) {
            invokeOps = opContainer.getInvokeStreamOperatorComponent();
        }
        
        
        List<OutputOperatorComponent> outputs = model.getOutputList();
        if (outputs.size() == 0) {
            //if there is no invoke stream then we should have atleat one output
            if(invokeOps.size() == 0) {
                String message = NbBundle.getMessage(IEPSemanticsVisitor.class, "DefaultOperatorValidator.atleast_one_output_required");
                ResultItem item = new ResultItem(mValidator, Validator.ResultType.ERROR, component, message);
                mResultItems.add(item);
            }
        }

        visitComponent(component);
    }

    public void visitSchemaComponentContainer(SchemaComponentContainer component) {
        visitComponent(component);
    }

    public void visitSchemaComponent(SchemaComponent component) {
        visitComponent(component);

        List<SchemaAttribute> attrs = component.getSchemaAttributes();
        Iterator<SchemaAttribute> it = attrs.iterator();

        while (it.hasNext()) {
            SchemaAttribute sa = it.next();
            visitSchemaAttribute(sa);
        }

    }

    public void visitLinkComponent(LinkComponent component) {
        visitComponent(component);

    }

    public void visitSchemaAttribute(SchemaAttribute component) {
        visitComponent(component);

        String size = component.getAttributeSize();
        String type = component.getAttributeType();

        //size is required for VARCHAR and optional for everything else
        if ("VARCHAR".equals(type) && (size == null || size.trim().equals(""))) {
            String message = NbBundle.getMessage(IEPSemanticsVisitor.class, "DefaultValidator.property_is_required_but_undefined", SchemaAttribute.PROP_SIZE);
            ResultItem item = new ResultItem(mValidator, Validator.ResultType.ERROR, component, message);
            mResultItems.add(item);
        }

    }
}
