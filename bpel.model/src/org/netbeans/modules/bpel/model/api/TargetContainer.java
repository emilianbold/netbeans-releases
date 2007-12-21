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
 *         Java class for tTargets complex type.
 *         <p>
 *         The following schema fragment specifies the expected content
 *         contained within this class.
 *
 * <pre>
 *   &lt;complexType name=&quot;tTargets&quot;&gt;
 *     &lt;complexContent&gt;
 *       &lt;extension base=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}tExtensibleElements&quot;&gt;
 *         &lt;sequence&gt;
 *           &lt;element name=&quot;joinCondition&quot; type=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}tCondition&quot; minOccurs=&quot;0&quot;/&gt;
 *           &lt;element name=&quot;target&quot; type=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}tTarget&quot; maxOccurs=&quot;unbounded&quot;/&gt;
 *         &lt;/sequence&gt;
 *       &lt;/extension&gt;
 *     &lt;/complexContent&gt;
 *   &lt;/complexType&gt;
 * </pre>
 */
public interface TargetContainer extends ExtensibleElements {

    /**
     * @return array of all Targets
     */
    Target[] getTargets();

    /**
     * @param i
     *            position in array.
     * @return <code>i</code>th Taget.
     */
    Target getTarget( int i );

    /**
     * Remove i-th Target ( index in Target set ).
     * 
     * @param i
     *            position in array.
     */
    void removeTarget( int i );

    /**
     * Set <code>target</code> to the <code>i</code>th palce.
     * 
     * @param target
     *            object for set.
     * @param i
     *            position for set.
     */
    void setTarget( Target target, int i );

    /**
     * Adds <code>target</code> to the end.
     * 
     * @param target
     *            object for add
     */
    void addTarget( Target target );

    /**
     * Set new list of targets to this activity.
     * 
     * @param target
     *            array for set.
     */
    void setTargets( Target[] target );

    /**
     * Insert <code>target</code> to the <code>i</code>th place.
     * 
     * @param target
     *            object for insert.
     * @param i
     *            position for insert.
     */
    void insertTarget( Target target, int i );

    /**
     * @return "joinCondition" tag entity.
     */
    Condition getJoinCondition();

    /**
     * Set new Condition child.
     * 
     * @param condition
     *            New Condition child object.
     */
    void setJoinCondition( Condition condition );

    /**
     * Removes "joinCondition" tag entity.
     */
    void removeJoinCondition();

}
