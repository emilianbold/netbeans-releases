/*
 * BPELStaticAnalysisValidator.java
 *
 * Created on June 29, 2006, 12:54 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.model.extensions.bpel.validation.staticanalysis;

import org.netbeans.modules.xml.wsdl.model.extensions.bpel.validation.AbstractValidator;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.validation.ValidationVisitor;

/**
 *
 * @author radval
 */
public class BPELExtensionStaticAnalysisValidator extends AbstractValidator {
    
    /**
     * Returns name of this validation service.
     */
    public String getName() {
        return getClass().getName();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.validation.AbstractValidator#getVisitor()
     */
    @Override
    protected ValidationVisitor getVisitor()
    {
        return new BPELExtensionStaticAnalysisVisitor(this);
    }


    
}

