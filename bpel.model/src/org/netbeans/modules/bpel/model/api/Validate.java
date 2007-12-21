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

/**
 *
 */
package org.netbeans.modules.bpel.model.api;

import java.util.List;

import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.ReferenceCollection;

/**
 * @author ads
 *         <p>
 *         Java class for tValidate complex type.
 *         <p>
 *         The following schema fragment specifies the expected content
 *         contained within this class.
 *
 * <pre>
 *   &lt;complexType name=&quot;tValidate&quot;&gt;
 *     &lt;complexContent&gt;
 *       &lt;extension base=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}tActivity&quot;&gt;
 *         &lt;attribute name=&quot;variables&quot; use=&quot;required&quot;&gt;
 *           &lt;simpleType&gt;
 *             &lt;list itemType=&quot;{http://www.w3.org/2001/XMLSchema}NCName&quot; /&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/attribute&gt;
 *       &lt;/extension&gt;
 *     &lt;/complexContent&gt;
 *   &lt;/complexType&gt;
 * </pre>
 */
public interface Validate extends Activity, ReferenceCollection {

    String VARIABLES = "variables";     // NOI18N

    /**
     * Getter for "variables" attribute. Returns list of references to
     * variables.
     * 
     * @return Value of attribute "variables"
     */
    List<BpelReference<VariableDeclaration>> getVaraibles();

    /**
     * Set value for "variables" attribute.
     * 
     * @param list
     *            List with variable references.
     */
    void setVaraibles( List<BpelReference<VariableDeclaration>> list );
}
