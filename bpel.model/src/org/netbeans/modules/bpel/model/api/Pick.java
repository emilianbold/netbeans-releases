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

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB)
// Reference Implementation, v2.0-06/22/2005 01:29 PM(ryans)-EA2
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source
// schema.
// Generated on: 2005.09.05 at 07:05:33 PM MSD
//
package org.netbeans.modules.bpel.model.api;

/**
 * <p>
 * Java class for tPick complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 *   &lt;complexType name=&quot;tPick&quot;&gt;
 *     &lt;complexContent&gt;
 *       &lt;extension base=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}tActivity&quot;&gt;
 *         &lt;sequence&gt;
 *           &lt;element name=&quot;onMessage&quot; type=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}tOnMessage&quot; maxOccurs=&quot;unbounded&quot;/&gt;
 *           &lt;element name=&quot;onAlarm&quot; type=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}tOnAlarmPick&quot; maxOccurs=&quot;unbounded&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;/sequence&gt;
 *         &lt;attribute name=&quot;createInstance&quot; type=&quot;{http://docs.oasis-open.org/wsbpel/2.0/process/executable}tBoolean&quot; default=&quot;no&quot; /&gt;
 *       &lt;/extension&gt;
 *     &lt;/complexContent&gt;
 *   &lt;/complexType&gt;
 * </pre>
 */
public interface Pick extends Activity, CreateInstanceActivity {

    /**
     * @return Array of OnMessage children entities.
     */
    OnMessage[] getOnMessages();

    /**
     * Set new list of OnMessage children entities.
     * 
     * @param messages
     *            New array of children.
     */
    void setOnMessages( OnMessage[] messages );

    /**
     * Getter for <code>i</code>-th OnMessage child.
     * 
     * @param i
     *            Index in children array.
     * @return <code>i</code>-th OnMessage child
     */
    OnMessage getOnMessage( int i );

    /**
     * Setter for <code>i</code>-th OnMessage child.
     * 
     * @param message
     *            OnMessage child entity.
     * @param i
     *            index in children array.
     */
    void setOnMessage( OnMessage message, int i );

    /**
     * Insert <code>i</code>-th OnMessage child.
     * 
     * @param message
     *            OnMessage child entity.
     * @param i
     *            index in children array.
     */
    void insertOnMessage( OnMessage message, int i );

    /**
     * Add <code>i</code>-th OnMessage child.
     * 
     * @param message
     *            OnMessage child entity.
     */
    void addOnMessage( OnMessage message  );

    /**
     * Removes <code>i</code>-th OnMessage child.
     * 
     * @param i
     *            index in children array.
     */
    void removeOnMessage( int i );

    /**
     * @return Array of OnAlarmPick children entities.
     */
    OnAlarmPick[] getOnAlarms();

    /**
     * Set new list of OnAlarmPick children entities.
     * 
     * @param alarms
     *            New array of children.
     */
    void setOnAlarms( OnAlarmPick[] alarms );

    /**
     * Getter for <code>i</code>-th OnAlarmPick child.
     * 
     * @param i
     *            index in children array.
     * @return <code>i</code>-th OnMessage child
     */
    OnAlarmPick getOnAlarm( int i );

    /**
     * Setter for <code>i</code>-th OnAlarmPick child.
     * 
     * @param alarm
     *            New OnAlarmPick child entity.
     * @param i
     *            index in children array.
     */
    void setOnAlarm( OnAlarmPick alarm, int i );

    /**
     * Insert new OnAlarmPick on the <code>i</code>-th place.
     * 
     * @param alarm
     *            New OnAlarmPick child entity.
     * @param i
     *            index in children array.
     */
    void insertOnAlarm( OnAlarmPick alarm, int i );

    /**
     * Add new OnAlarmPick in the end of children list.
     * 
     * @param alarm
     *            New OnAlarmPick child entity.
     */
    void addOnAlarm( OnAlarmPick alarm );

    /**
     * Removes <code>i</code>-th OnAlarmPick child.
     * 
     * @param i
     *            index in children array.
     */
    void removeOnAlarm( int i );

    /**
     * @return size of OnMessages array.
     */
    int sizeOfOnMessages();

    /**
     * @return size of OnAlarmPicks array.
     */
    int sizeOfOnAlarms();
}
