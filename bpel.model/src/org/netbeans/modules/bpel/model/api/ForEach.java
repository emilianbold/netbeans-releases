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

import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.support.TBoolean;

/**
 * @author ads
 *         <p>
 *         Java class for tForEach complex type.
 *         <p>
 *         The following schema fragment specifies the expected content
 *         contained within this class.
 *
 * <pre>
 *   &lt;complexType name=&quot;tForEach&quot;&gt;
 *     &lt;complexContent&gt;
 *       &lt;extension base=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}tActivity&quot;&gt;
 *         &lt;sequence&gt;
 *           &lt;element name=&quot;iterator&quot; type=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}tForEachIterator&quot;/&gt;
 *           &lt;element name=&quot;completionCondition&quot; type=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}tCompletionCondition&quot; minOccurs=&quot;0&quot;/&gt;
 *           &lt;element ref=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}scope&quot;/&gt;
 *         &lt;/sequence&gt;
 *         &lt;attribute name=&quot;counterName&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}NCName&quot; /&gt;
 *         &lt;attribute name=&quot;parallel&quot; use=&quot;required&quot; type=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}tBoolean&quot; /&gt;
 *       &lt;/extension&gt;
 *     &lt;/complexContent&gt;
 *   &lt;/complexType&gt;
 * </pre>
 */
public interface ForEach extends Activity, ScopeHolder, VariableDeclaration,
    VariableDeclarationScope

{

    /**
     * counterName attribute name.
     */
    String COUNTER_NAME = "counterName";    // NOI18N

    /**
     * parallel attribute value.
     */
    String PARALLEL = "parallel";           // NOI18N

    /**
     * @return "startCounterValue" tag element.
     */
    StartCounterValue getStartCounterValue();

    /**
     * Setter for "startCounterValue" tag element.
     * 
     * @param expression
     *            New Expression for "startCounterValue" child.
     */
    void setStartCounterValue( StartCounterValue expression );

    /**
     * @return "finalCounterValue" tag element.
     */
    FinalCounterValue getFinalCounterValue();

    /**
     * Setter for "finalCounterValue" tag element.
     * 
     * @param expression
     *            New Expression for "finalCounterValue" child.
     */
    void setFinalCounterValue( FinalCounterValue expression );

    /**
     * Getter for "completionCondition" tag entity.
     * 
     * @return CompletionCondition child entity.
     */
    CompletionCondition getCompletionCondition();

    /**
     * Setter or "completionCondition" tag entity.
     * 
     * @param condition
     *            CompletionCondition child entity.
     */
    void setCompletionCondition( CompletionCondition condition );

    /**
     * Removes CompletionCondition child entity.
     */
    void removeCompletionCondition();

    /**
     * Getter for counter name variable. This is local variable that is created
     * implicetly . This is one more place where variable could be defined.
     * 
     * @return Counter name.
     */
    String getCounterName();

    /**
     * Set counter name.
     * 
     * @param value
     *            New name of counter variable.
     * @throws VetoException
     *             Could be thrown if <code>value</code> is not allowable
     *             here.
     */
    void setCounterName( String value ) throws VetoException;

    /**
     * @return parallel attribute value.
     */
    TBoolean getParallel();

    /**
     * @param value
     *            New value for "parallel" attribute.
     */
    void setParallel( TBoolean value );
}
