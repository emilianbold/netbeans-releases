package org.netbeans.modules.iep.model.validator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.netbeans.modules.iep.model.Component;
import org.netbeans.modules.iep.model.Documentation;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.IEPVisitor;
import org.netbeans.modules.iep.model.Import;
import org.netbeans.modules.iep.model.LinkComponent;
import org.netbeans.modules.iep.model.LinkComponentContainer;
import org.netbeans.modules.iep.model.ModelHelper;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.OperatorComponentContainer;
import org.netbeans.modules.iep.model.PlanComponent;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.iep.model.SchemaAttribute;
import org.netbeans.modules.iep.model.SchemaComponent;
import org.netbeans.modules.iep.model.SchemaComponentContainer;
import org.netbeans.modules.iep.model.share.SharedConstants;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public class IEPSemanticsVisitor implements IEPVisitor {

    private List<ResultItem> mResultItems = new ArrayList<ResultItem>();
    private Validator mValidator;
    private static Logger mLogger = Logger.getLogger(IEPSemanticsVisitor.class.getName());

    private List<String> mValidTypes = new ArrayList<String>();
    
    /** Creates a new instance of IEPSemanticsVisitor */
    public IEPSemanticsVisitor(Validator validator, Validation validation, List<Model> validatedModels) {
        mValidator = validator;

        for(int i = 0; i < SharedConstants.SQL_TYPE_NAMES.length; i++) {
            mValidTypes.add(SharedConstants.SQL_TYPE_NAMES[i]);
        }
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
        org.netbeans.modules.tbls.model.TcgPropertyType propertyType = property.getPropertyType();
        if (propertyType != null && propertyType.isRequired() && (property.getValue() == null || property.getValue().equals(""))) {
            String message = NbBundle.getMessage(IEPSemanticsVisitor.class, "DefaultValidator.property_is_required_but_undefined", property.getName());
            ResultItem item = new ResultItem(mValidator, Validator.ResultType.ERROR, property, message);
            mResultItems.add(item);
        }

        //we should not need to report warning if a property is
        //using default value as default values are also valid
        //value, if we want user to always change a default value
        //then it we should not even add a default and above check would
        //catch it
//        if (propertyType.isRequired() && property.getValue().equals(propertyType.getDefaultValue())) {
//            Object defVal = propertyType.getDefaultValue();
//            String strVal = propertyType.getType().format(defVal);
//            String message = NbBundle.getMessage(IEPSemanticsVisitor.class, "DefaultValidator.property_uses_default_value", property.getName()) + " (" + strVal + ")";
//
//            ResultItem item = new ResultItem(mValidator, Validator.ResultType.WARNING, property, message);
//            mResultItems.add(item);
//        }
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
        boolean isGlobal = component.getBoolean(PROP_IS_GLOBAL);
        if (!isGlobal) {
            return;
        }

        String globalId = component.getString(PROP_GLOBAL_ID);
        if (globalId == null || globalId.trim().equals("")) {
            String message = NbBundle.getMessage(IEPSemanticsVisitor.class, "IEPSemanticsVisitor.property_must_be_defined_for_a_global_entity");
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
        if (packageName == null) {
            packageName = "";
        }
        IEPModel model = component.getModel();
        FileObject iepFile = model.getModelSource().getLookup().lookup(FileObject.class);
        if(iepFile == null) {
            File f = model.getModelSource().getLookup().lookup(File.class);
            if(f != null) {    
                iepFile = FileUtil.toFileObject(f);
            }
        }
        
        
        String expectedPackageName = ModelHelper.getPackageName(iepFile);
        if (!expectedPackageName.equals(packageName)) {
            String message = NbBundle.getMessage(IEPSemanticsVisitor.class, "IEPSemanticsVisitor.expected_packageName_does_not_match_specified_package_name", new Object[]{expectedPackageName, packageName});
            ResultItem item = new ResultItem(mValidator, Validator.ResultType.ERROR, component, message);
            mResultItems.add(item);
        }



        OperatorComponentContainer opContainer = component.getOperatorComponentContainer();
        if (opContainer == null) {
            String message = NbBundle.getMessage(IEPSemanticsVisitor.class, "IEPSemanticsVisitor.at_least_one_operator_required");
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

        visitComponent(component);
    }

    public void visitSchemaComponentContainer(SchemaComponentContainer component) {
        visitComponent(component);
    }

    public void visitSchemaComponent(SchemaComponent component) {
        visitComponent(component);

        //validate for duplicate schema attribute name
        Set<String> names = new HashSet<String>();
        
        List<SchemaAttribute> attrs = component.getSchemaAttributes();
        Iterator<SchemaAttribute> it = attrs.iterator();
        while (it.hasNext()) {
            SchemaAttribute sa = it.next();
            if(names.contains(sa.getAttributeName())) {
                //error attribute name must be unique in a schema
                //component
                String message = NbBundle.getMessage(IEPSemanticsVisitor.class, "IEPSemanticsVisitor.schema_attribute_name_must_be_unique", sa.getAttributeName());
                ResultItem item = new ResultItem(mValidator, Validator.ResultType.ERROR, component, message);
                mResultItems.add(item);
            } else {
                names.add(sa.getAttributeName());
            }
        }
        

        //validate each schema attribute
        it = attrs.iterator();
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
        String name = component.getAttributeName();
        String size = component.getAttributeSize();
        String type = component.getAttributeType();

        //validate if type is a valid type
        /* due to issue http://www.netbeans.org/issues/show_bug.cgi?id=148892
         * it is decided to remove CLOB from the attribute type list and for 
         * projects that are already using CLOB we will exclude only CLOB from
         * the validation sanity test. Hence the !type.equals(SharedConstants.SQL_TYPE_CLOB)
         * check.
         * Also issue http://www.netbeans.org/issues/show_bug.cgi?id=149576
         * TIME datatype is removed. 
         */
        if(!mValidTypes.contains(type) && 
        	!(type.equals(SharedConstants.SQL_TYPE_CLOB) || type.equals(SharedConstants.SQL_TYPE_TIME))) {
            Property typeProp = component.getProperty(PROP_TYPE);
            String message = NbBundle.getMessage(IEPSemanticsVisitor.class, "DefaultValidator.datatype_is_not_a_valid_type", type, name, mValidTypes.toString());
            ResultItem item = new ResultItem(mValidator, Validator.ResultType.ERROR, typeProp, message);
            mResultItems.add(item);
        }
        
        //size is required for VARCHAR and optional for everything else
        if ("VARCHAR".equals(type) && (size == null || size.trim().equals(""))) {
            String message = NbBundle.getMessage(IEPSemanticsVisitor.class, "DefaultValidator.property_is_required_but_undefined", PROP_SIZE);
            ResultItem item = new ResultItem(mValidator, Validator.ResultType.ERROR, component, message);
            mResultItems.add(item);
        }
        
        

    }
}
