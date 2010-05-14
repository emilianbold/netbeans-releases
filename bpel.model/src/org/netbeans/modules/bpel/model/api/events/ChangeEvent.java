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
package org.netbeans.modules.bpel.model.api.events;

import java.util.EventObject;

import org.netbeans.modules.bpel.model.api.BpelEntity;

/**
 * Base class for events in BPEL OM. Contains common methods for each event.
 *
 * @author ads
 */
public abstract class ChangeEvent extends EventObject {

    /**
     * Constructor for ChangeEvent class.
     *
     * @param source
     *            this is reserved object that could point to source of event (
     *            who sent this event ).
     * @param parent
     *            parent entity. Inside this parent event has occured.
     * @param name
     *            name of attribute or tag. Not always have sense.
     */
    public ChangeEvent( Object source, BpelEntity parent, String name ) {
        super(source);
        myParent = parent;
        myName = name;
    }

    /**
     * @return parent for enitity or attribute in OM for which this event
     *         occurs.
     */
    public BpelEntity getParent() {
        return myParent;
    }

    /**
     * @return name of tag or attribute of affected entity(ies).
     */
    public String getName() {
        return myName;
    }

    /**
     * @return Is this event last in chain of events that are fired 
     * in one transaction.
     */
    public boolean isLastInAtomic() {
        if (isNotLast == null) {
            return true;
        }
        else {
            return !isNotLast;
        }
    }

    /**
     * This method set flag for event that it is last event in chain of events.
     * It could be used only one time. After setting flag it could not be changed.
     */
    public void setLast() {
        if (isNotLast != null) {
            throw new UnsupportedOperationException("Flag for this event " // NOI18N
                    + "is already set."); // NOI18N
        }
        isNotLast = false;
    }

    /**
     * This method set flag for event that it is not last event in chain of events.
     * It could be used only one time. After setting flag it could not be changed.
     */
    public void setNotLast() {
        if (isNotLast != null) {
            throw new UnsupportedOperationException("Flag for this event " // NOI18N
                    + "is already set."); // NOI18N
        }
        isNotLast = true;
    }

    private BpelEntity myParent;

    private String myName;

    /**
     * This flag signals (if true) that more events will follow after this
     * event. And those events could be handled as one atomic change of model.
     * If this flag equals false then this is last event ( or just single at all ).
     */
    private Boolean isNotLast;
}
