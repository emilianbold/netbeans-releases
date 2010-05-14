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
 *         Java class for tIf complex type.
 *         <p>
 *         The following schema fragment specifies the expected content
 *         contained within this class.
 * <pre>
 *   &lt;complexType name=&quot;tIf&quot;&gt;
 *     &lt;complexContent&gt;
 *       &lt;extension base=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}tActivity&quot;&gt;
 *         &lt;sequence&gt;
 *           &lt;element name=&quot;condition&quot; type=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}tBoolean-expr&quot;/&gt;
 *           &lt;sequence&gt;
 *           &lt;group ref=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}activity&quot;/&gt;
 *           &lt;/sequence&gt;
 *           &lt;element name=&quot;elseif&quot; maxOccurs=&quot;unbounded&quot; minOccurs=&quot;0&quot;&gt;
 *             &lt;complexType&gt;
 *               &lt;complexContent&gt;
 *                 &lt;extension base=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}tExtensibleElements&quot;&gt;
 *                   &lt;sequence&gt;
 *                     &lt;element name=&quot;condition&quot; type=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}tBoolean-expr&quot;/&gt;
 *                     &lt;group ref=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}activity&quot;/&gt;
 *                   &lt;/sequence&gt;
 *                 &lt;/extension&gt;
 *               &lt;/complexContent&gt;
 *             &lt;/complexType&gt;
 *           &lt;/element&gt;
 *           &lt;element name=&quot;else&quot; type=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}tActivityContainer&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;/sequence&gt;
 *       &lt;/extension&gt;
 *     &lt;/complexContent&gt;
 *   &lt;/complexType&gt;
 * </pre>
 */
public interface If extends Activity, ActivityHolder, ConditionHolder {

    /**
     * @return Array of ElseIf entities.
     */
    ElseIf[] getElseIfs();

    /**
     * Set new ElseIf children to this container.
     * 
     * @param value
     *            New array of children.
     */
    void setElseIfs( ElseIf[] value );

    /**
     * Insert new <code>value</code> in ElseIf array children.
     * 
     * @param value
     *            New entity.
     * @param i
     *            index for insert.
     */
    void insertElseIf( ElseIf value, int i );

    /**
     * Adds new <code>value</code> to the end of list.
     * 
     * @param value
     *            New ElseIf entity.
     */
    void addElseIf( ElseIf value );

    /**
     * @param i
     *            index in array.
     * @return <code>i</code>-th ElseIf entity.
     */
    ElseIf getElseIf( int i );

    /**
     * Set <code>value</code> on the <code>i</code>-th place.
     * 
     * @param value
     *            New value.
     * @param i
     *            index in array.
     */
    void setElseIf( ElseIf value, int i );

    /**
     * Removes <code>i</code>-th element .
     * 
     * @param i
     *            index in array.
     */
    void removeElseIf( int i );
    
    /**
     * @return Size of  ElseIf children elements.
     */
    int sizeElseIfs();

    /**
     * @return Else child entity.
     */
    Else getElse();

    /**
     * Set new <code>value</code> as Else entity.
     * 
     * @param value
     *            New Else entity.
     */
    void setElse( Else value );

    /**
     * Removes Else entity.
     */
    void removeElse();
}
