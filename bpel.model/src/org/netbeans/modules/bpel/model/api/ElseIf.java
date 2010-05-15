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
 *         Java class for anonymous complex type.
 *         <p>
 *         The following schema fragment specifies the expected content
 *         contained within this class.
 *
 * <pre>
 *   &lt;complexType&gt;
 *     &lt;complexContent&gt;
 *       &lt;extension base=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}tExtensibleElements&quot;&gt;
 *         &lt;sequence&gt;
 *           &lt;element name=&quot;condition&quot; type=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}tBoolean-expr&quot;/&gt;
 *           &lt;group ref=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}activity&quot;/&gt;
 *         &lt;/sequence&gt;
 *       &lt;/extension&gt;
 *     &lt;/complexContent&gt;
 *   &lt;/complexType&gt;
 * </pre>
 */
public interface ElseIf extends ActivityHolder, ConditionHolder {

}
