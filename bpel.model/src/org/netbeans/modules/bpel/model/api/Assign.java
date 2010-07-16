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

package org.netbeans.modules.bpel.model.api;

import org.netbeans.modules.bpel.model.api.support.TBoolean;

/**
 * <p>
 * Java class for tAssign complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 *   &lt;complexType name=&quot;tAssign&quot;&gt;
 *     &lt;complexContent&gt;
 *       &lt;extension base=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}tActivity&quot;&gt;
 *         &lt;sequence&gt;
 *           &lt;choice maxOccurs=&quot;unbounded&quot;&gt;
 *             &lt;element ref=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}copy&quot;/&gt;
 *             &lt;element ref=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}extensibleAssign&quot;/&gt;
 *           &lt;/choice&gt;
 *         &lt;/sequence&gt;
 *         &lt;attribute name=&quot;validate&quot; type=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}tBoolean&quot; default=&quot;no&quot; /&gt;
 *       &lt;/extension&gt;
 *     &lt;/complexContent&gt;
 *   &lt;/complexType&gt;
 * </pre>
 * 
 * @author ads
 */
public interface Assign extends Activity, BpelContainer {

    /**
     * "validate" attribute name.
     */
    String VALIDATE = "validate"; // NOI18N

    /**
     * @return list of children entities in this assign.
     */
    AssignChild[] getAssignChildren();

    /**
     * @param i
     *            position in array.
     * @return <code>i</code>th child element.
     */
    AssignChild getAssignChild( int i );

    /**
     * Removes <code>i</code>th child element.
     * 
     * @param i
     *            position in array.
     */
    void removeAssignChild( int i );

    /**
     * Set <code>child</code> to the <code>i</code>th position.
     * 
     * @param child
     *            object for set.
     * @param i
     *            position for set.
     */
    void setAssignChild( AssignChild child, int i );

    /**
     * Add <code>child</code> to the end of copies list.
     * 
     * @param child
     *            object for add.
     */
    void addAssignChild( AssignChild child );

    /**
     * Insert <code>copy</code> to the <code>i</code>th position.
     * 
     * @param child
     *            object for insert.
     * @param i
     *            position for insert.
     */
    void insertAssignChild( AssignChild child, int i );

    /**
     * Set new list of children.
     * 
     * @param children
     *            array for set.
     */
    void setAssignChildren( AssignChild[] children );

    /**
     * @return size of copies elements.
     */
    int sizeOfAssignChildren();

    /**
     * Getter for "validate" attribute.
     * 
     * @return "validate" attribute value.
     */
    TBoolean getValidate();

    /**
     * Setter for "validate" attribute.
     * 
     * @param value
     *            New attribute value.
     */
    void setValidate( TBoolean value );

    /**
     * Removes "validate" attribute.
     */
    void removeValidate();

    boolean isJavaScript();
    String getJavaScript();
    void setJavaScript(String value);
    String getInput();
    void setInput(String value);
    String getOutput();
    void setOutput(String value);
}
