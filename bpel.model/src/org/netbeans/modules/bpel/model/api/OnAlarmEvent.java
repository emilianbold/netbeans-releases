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

/**
 * @author ads
 *         <p>
 *         Java class for tOnAlarmEvent complex type.
 *         <p>
 *         The following schema fragment specifies the expected content
 *         contained within this class.
 *
 * <pre>
 *   &lt;complexType name=&quot;tOnAlarmEvent&quot;&gt;
 *     &lt;complexContent&gt;
 *       &lt;extension base=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}tExtensibleElements&quot;&gt;
 *         &lt;sequence&gt;
 *           &lt;choice&gt;
 *             &lt;sequence&gt;
 *               &lt;group ref=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}forOrUntilGroup&quot;/&gt;
 *               &lt;element name=&quot;repeatEvery&quot; type=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}tDuration-expr&quot; minOccurs=&quot;0&quot;/&gt;
 *             &lt;/sequence&gt;
 *             &lt;element name=&quot;repeatEvery&quot; type=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}tDuration-expr&quot;/&gt;
 *           &lt;/choice&gt;
 *           &lt;element ref=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}scope&quot;/&gt;
 *         &lt;/sequence&gt;
 *       &lt;/extension&gt;
 *     &lt;/complexContent&gt;
 *   &lt;/complexType&gt;
 * </pre>
 */
public interface OnAlarmEvent extends ExtensibleElements, ScopeHolder, 
    TimeEventHolder 
{

    /**
     * @return "repeatEvery" tag expression object.
     */
    RepeatEvery getRepeatEvery();

    /**
     * @param expression
     *            Set new "repeatEvery" object .
     */
    void setRepeatEvery( RepeatEvery expression );

    /**
     * Removes "repeatEvery" tag expression object.
     */
    void removeRepeatEvery();
    
    /**
     * Removes "for" or "until" tag expression object.
     */
    void removeTimeEvent();

}
