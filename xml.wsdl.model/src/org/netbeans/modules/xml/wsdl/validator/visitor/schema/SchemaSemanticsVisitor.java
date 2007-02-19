/*
 * SchemaSemanticsVisitor.java
 *
 * Created on February 9, 2007, 5:11 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.validator.visitor.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.DefaultSchemaVisitor;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.openide.util.NbBundle;

/**
 *
 * @author radval
 */
public class SchemaSemanticsVisitor extends DefaultSchemaVisitor {
    
     /**Import does not have imported document object */
    public static final String VAL_MISSING_IMPORTED_DOCUMENT = "VAL_MISSING_IMPORTED_DOCUMENT";
    public static final String FIX_MISSING_IMPORTED_DOCUMENT = "FIX_MISSING_IMPORTED_DOCUMENT";
  
    
    /** Creates a new instance of SchemaSemanticsVisitor */
    public List<ResultItem> mResultItems = new ArrayList<ResultItem>();
    private Validator mValidator;
    private Validation mValidation;
    private List<Model> mValidatedModels;
    
    /** Creates a new instance of SchemaSemanticsVisitor */
    public SchemaSemanticsVisitor(Validator validator,
                                  Validation validation, 
                                  List<Model> validatedModels) {
        mValidation = validation;
        mValidatedModels = validatedModels;
    }
    
    public List<ResultItem> getResultItems() {
        return mResultItems;
    }
    
    public void visit(Schema s) {
        if(s != null) {
            visitChildren(s);
        }
    }
    
    public void visit(Import im) {
        
        //verify if imported document is available
        Collection<Schema> schemas = im.getModel().findSchemas(im.getNamespace());
        
        
        if(schemas == null || schemas.isEmpty()) {
            // it can be a xsd import
           
                logValidation
                        (Validator.ResultType.ERROR, im,
                        NbBundle.getMessage(SchemaSemanticsVisitor.class, 
                                            VAL_MISSING_IMPORTED_DOCUMENT, 
                                             im.getNamespace(),
                                             im.getSchemaLocation()),
                        NbBundle.getMessage(SchemaSemanticsVisitor.class, FIX_MISSING_IMPORTED_DOCUMENT)
                        );
            
        }
        
        for (Schema s : schemas) {
            mValidation.validate(s.getModel(), ValidationType.COMPLETE);
        }
    }

    private void visitChildren(SchemaComponent w) {
        Collection coll = w.getChildren();
        if (coll != null) {
            Iterator iter = coll.iterator();
            while (iter.hasNext()) {
                SchemaComponent component = (SchemaComponent) iter.next();
                component.accept(this);
            }
        }
    }
    
    private void logValidation(Validator.ResultType type, 
                                 Component component,  
                                 String desc, 
                                 String correction) {
        String message = desc;
        if (correction != null) {
            message = desc + " : " + correction;
        }
        ResultItem item = new Validator.ResultItem(mValidator, type, component, message);
        mResultItems.add(item);
        
    }
}
