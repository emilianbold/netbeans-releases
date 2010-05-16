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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.model.ext.js.api;

import java.util.List;
import org.netbeans.modules.bpel.model.api.CDataContentElement;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.model.api.ExtensionEntity;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.ext.Extensions;

/**
 *
 * @author Vitaly Bychkov
 */
public interface Expression extends ExtensionEntity, ExtensibleElements, CDataContentElement {
    String JS_NAMESPACE_URI = Extensions.SUN_JS_EXT_URI;

    String EXPRESSION_LANGUAGE = "expressionLanguage";
    String INPUT_VARS = "inputVars";
    String OUTPUT_VARS = "outputVars";

    /**
     * Getter for "inputVars" attribute. Returns list of inputVars names
     *
     * @return Value of attribute "inputVars"
     */
    List<String> getInputVariables();

    /**
     * Set value for "inputVars" attribute.
     *
     * @param list
     *            List with inputVars names
     */
    void setInputVariables(List<String> list) throws VetoException;

    String getInputVariablesList();
    void setInputVariablesList(String value) throws VetoException;
    void removeInputVariablesList();
    

    /**
     * Getter for "inputVars" attribute. Returns list of inputVars names
     *
     * @return Value of attribute "inputVars"
     */
    List<String> getOutputVariables();

    /**
     * Set value for "inputVars" attribute.
     *
     * @param list
     *            List with inputVars names
     */
    void setOutputVariables(List<String> list) throws VetoException;

    String getOutputVariablesList();
    void setOutputVariablesList(String value) throws VetoException;
    void removeOutputVariablesList();

    ExpressionLanguage getExpressionLanguage();
    void setExpressionLanguage(ExpressionLanguage value);
    void removeExpressionLanguage();
}
