/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.xam.spi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.openide.util.NbBundle;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Base class for Schema validator. Each domain specific schema validator can extend this.
 * @author Shivanand Kini
 * @author Nam Nguyen
 * @author Praveen Savur
 */
public abstract class XsdBasedValidator implements Validator {
    
   
    /** Creates a new instance of XsdBasedValidation */
    public XsdBasedValidator() {
    }
    
    
    
    
    /**
     * Get Schemas that the model has to be validated against.
     * @returns compiled Schema to be used in validation of the given
     * model; or null if this validator does not know how to validate the model.
     * @param model Get Schemas that the model has to be validated against.
     * @return Compiled Schema object.
     */
    abstract protected Schema getSchema(Model model);
    
    
    /**
     * Entry point to validate a model.
     * @param model Model to validate.
     * @param validation Reference to Validation object.
     * @param validationType Type of validation. Complete(slow) or partial(fast).
     * @return ValidationResults.
     */
    public ValidationResult validate(Model model, Validation validation, Validation.ValidationType validationType) {
        Collection<ResultItem> results = Collections.emptyList();
        List<Model> validateds = Collections.emptyList();
        Schema schema = getSchema(model);
        if (schema == null) {
            return new ValidationResult(results, validateds);
        }
        
        Handler handler = new Handler(model);
        validate(model, schema, handler);
        results = handler.getResultItems();
        validateds = Collections.singletonList(model);
        
        return new ValidationResult(results, validateds);
    }
    
    
    
    /**
     * 
     * @param model 
     * @param handler 
     * @return 
     */
    protected Source getSource(Model model, Handler handler) {
        Source source = (Source) model.getModelSource().getLookup().lookup(Source.class);

        // Try to get Source via File from lookup.
        if(source == null) {
            File file = (File) model.getModelSource().getLookup().lookup(File.class);
            if(file != null) {
                try {
                    source =  new SAXSource(new InputSource(new FileInputStream(file)));
		    source.setSystemId(file.toURI().toString());
                } catch (FileNotFoundException ex) {
                    // catch error.
                }
            }
        }
        
        if (source == null) {
            String msg = NbBundle.getMessage(XsdBasedValidator.class, "MSG_NoSAXSource");
            handler.logValidationErrors(Validator.ResultType.WARNING, msg);
        }
        return source;
    }
    
    
    
    /**
     * Validates the model against the schema. Errors are sent to the handler.
     * @param model Model to be validated.
     * @param schema Compiled schema against which the model is validated.
     * @param handler Handler to receive validation messages.
     */
    protected void validate(Model model, Schema schema, Handler handler) {

        javax.xml.validation.Validator validator = schema.newValidator();
        Source source = getSource(model, handler);
        
        if(source != null) {
            validator.setErrorHandler(handler);
            
            // validate needs SAX or DOMSource.
            assert ((source instanceof SAXSource) || (source instanceof DOMSource)):
                "Source is not instance of SAXSource or DOMSource"; // NOI18N
            
            try {
                validator.validate(source);
            } catch (IOException ex) {
                Logger.getLogger(getClass().getName()).log(Level.FINE, "validate", ex);
            } catch (SAXException ex) {
                // If there is a fatal error (for example not well formed xml),
                // a SAXException is thrown. Simply ignore this error.
            }
        }
    }
    
    
    /**
     * Subclasses can use this to get a compiled schema object.
     * @param schemas Input stream of schemas.
     * @param lsResourceResolver  resolver can be supplied optionally. Otherwise pass null.
     * @return  Compiled Schema object.
     */
    protected Schema getCompiledSchema(InputStream[] schemas,
            LSResourceResolver lsResourceResolver) {
        
        Schema schema = null;
        // Convert InputStream[] to StreamSource[]
        StreamSource[] schemaStreamSources = new StreamSource[schemas.length];
        for(int index1=0 ; index1<schemas.length ; index1++)
            schemaStreamSources[index1] = new StreamSource(schemas[index1]);
        
        // Create a compiled Schema object.
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        schemaFactory.setResourceResolver(lsResourceResolver);
        try {
            schema = schemaFactory.newSchema(schemaStreamSources);            
        } catch(SAXException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "getCompiledSchema", ex);
        } 
        
        return schema;
    }
    
    protected Schema getCompiledSchema(Source[] schemas,
            LSResourceResolver lsResourceResolver,
            ErrorHandler errorHandler) {
        
        Schema schema = null;
        
        // Create a compiled Schema object.
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        schemaFactory.setResourceResolver(lsResourceResolver);
        schemaFactory.setErrorHandler(errorHandler);
        try {
            schema = schemaFactory.newSchema(schemas);            
        } catch(SAXException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "getCompiledSchema", ex);
        } 
        
        return schema;
    }
    
    /**
     *  Handler to receive parse events.
     */
    protected class Handler implements ErrorHandler {
        private Collection<ResultItem> resultItems;
        private List<Integer> linePositions = null;
        private DocumentModel model;
        private HashMap<DocumentModel,Handler> relatedHandlers = new HashMap<DocumentModel,Handler>();
        
        /**
         * Constructor to create a SAX Error Handler.
         * @param model Model which is being validated.
         */
        public Handler(Model model) {
            assert model instanceof DocumentModel : "Invalid model class"; //NOI18N
            resultItems = new ArrayList<ResultItem>();
            this.model = (DocumentModel) model;
        }
        
        /**
         * Return validation results.
         * @return Return results.
         */
        public Collection<ResultItem> getResultItems() {
            addResultsFromHandlers(relatedHandlers.values());
            return resultItems;
        }
        
        /**
         * Adds resultItems from the handler collection to the resultItems of the
         * current handler. One user of this will be the schema validator
         * (which will use a handler instance for each model)
         * and will pass in the list of handlers it has created.
         * @param handlers Handlers from which resultItems have to be collected.
         */
        public void addResultsFromHandlers(Collection<Handler> handlers) {
            for(Handler handler: handlers) {
                resultItems.addAll(handler.getResultItems());  
            }
        }
        
        
        
        private void setupLinePositions() {
            linePositions = new ArrayList<Integer>();
            Document document = ((AbstractDocumentModel) model).getBaseDocument();
            if (document == null) {
                return;
            }
            try {
                String str = document.getText(0, document.getLength() - 1);
                String[] lines = str.split("\n"); //NOI18N
                linePositions.add(Integer.valueOf(-1));
                int pos = 0;
                for (String line : lines) {
                    linePositions.add(pos);
                    pos += line.length() + 1; // make sure we also count the \n
                }
            } catch (BadLocationException e) {
                Logger.getLogger(getClass().getName()).log(Level.FINE, "setupLinePositions", e); //NOI18N
            }
        }
        
        /**
         * Given 1-based line number and 1-based column number,
         * @returns 0-base position.
         */
        private int getPosition(int lineNumber, int columnNumber) {
            if (linePositions == null) {
                setupLinePositions();
            }
            if (lineNumber < 1 || lineNumber > linePositions.size()) {
                return 0;
            }
            Integer beginningPos = linePositions.get(lineNumber);
            return beginningPos == null ? 0 : beginningPos.intValue() + columnNumber-1;
        }
        
        /**
         * 
         * @param exception 
         * @throws org.xml.sax.SAXException 
         */
        public void error(SAXParseException exception) throws SAXException {
            logValidationErrors( Validator.ResultType.ERROR, exception);
        }
        
        /**
         * 
         * @param exception 
         * @throws org.xml.sax.SAXException 
         */
        public void fatalError(SAXParseException exception) throws SAXException {
            logValidationErrors( Validator.ResultType.ERROR, exception);
        }
        
        /**
         * 
         * @param exception 
         * @throws org.xml.sax.SAXException 
         */
        public void warning(SAXParseException exception) throws SAXException {
            logValidationErrors( Validator.ResultType.WARNING, exception);
        }
        
        public void logValidationErrors(Validator.ResultType resultType, SAXParseException sax) {
            String systemId = sax.getSystemId();
            DocumentModel errorModel = null;
            if (systemId != null) {
                errorModel = resolveResource(systemId, model);
            }
            
            Handler h = null;
            if (errorModel != null && model != errorModel) {
                h = relatedHandlers.get(errorModel);
                if (h == null) {
                    h = new Handler(errorModel);
                    relatedHandlers.put(errorModel, h);
                }
            }
            
            if (h == null) {
                h = this;
            }
            h.logValidationErrors(resultType, sax.getMessage(), sax.getLineNumber(), sax.getColumnNumber()-1);
        }
        
        /**
         * 
         * @param resultType 
         * @param errorDescription 
         * @param lineNumber 
         * @param columnNumber 
         */
        public void logValidationErrors(Validator.ResultType resultType,
                String errorDescription,
                int lineNumber,
                int columnNumber) {
            
            // Double check if columnNumber becomes invalid.
            if(columnNumber <= 0)
                columnNumber = 1;
            
            // Create Result Item using a constructor based on whether
            // model is valid or not.
            if(model.getState().equals(Model.State.VALID)) {
                int position = getPosition(lineNumber, columnNumber);
                assert position >= 0 : "Invalid position value "+position;
                Component component = model.findComponent(position);
                if (component == null) {
                    component = model.getRootComponent();
                }
                resultItems.add(new ResultItem(
                        XsdBasedValidator.this, resultType, component, errorDescription));
            } else {
                resultItems.add(new ResultItem(XsdBasedValidator.this, resultType,
                        errorDescription, lineNumber, columnNumber, model));
            }
        }
        
        /**
         * 
         * @param resultType 
         * @param errorDescription 
         */
        public void logValidationErrors(Validator.ResultType resultType, String errorDescription) {
            logValidationErrors(resultType, errorDescription,1,1);
        }
    }

    public DocumentModel resolveResource(String systemId, Model currentModel) {
        return null;
    }
    
}
