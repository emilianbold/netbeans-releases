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

package org.netbeans.modules.xslt.model;


/**
 * The main categories of XSLT instruction are as follows:
 * </p>
 * <ul>
 * <li>
 * <p>
 * instructions that create new nodes: <code>xsl:document</code>,
 * <code>xsl:element</code>, <code>xsl:attribute</code>,
 * <code>xsl:processing-instruction</code>, <code>xsl:comment</code></a>,
 * <code>xsl:value-of</code>, <code>xsl:text</code>,
 * <code>xsl:namespace</code>;
 * </p>
 * </li>
 * <li>
 * <p>
 * an instruction that returns an arbitrary sequence by evaluating an XPath
 * expression: <code>xsl:sequence</code>;
 * </p>
 * </li>
 * <li>
 * <p>
 * instructions that cause conditional or repeated evaluation of nested
 * instructions: <code>xsl:if</code>, <code>xsl:choose</code>,
 * <code>xsl:for-each</code>, <code>xsl:for-each-group</code>;
 * </p>
 * </li>
 * <li>
 * <p>
 * instructions that invoke templates: <code>xsl:apply-templates</code>,
 * <code>xsl:apply-imports</code>, <code>xsl:call-template</code>,
 * <code>xsl:next-match</code>;
 * </p>
 * </li>
 * <li>
 * <p>
 * Instructions that declare variables: <code>xsl:variable</code>,
 * <code>xsl:param</code>;
 * </p>
 * </li>
 * <li>
 * <p>
 * other specialized instructions: <code>xsl:number</code>,
 * <code>xsl:analyze-string</code>, <code>xsl:message</code>,
 * <code>xsl:result-document</code>.
 * </p>
 * </li>
 * </ul>
 * 
 * @author ads
 */

public interface Instruction extends XslComponent, SequenceElement {

}
