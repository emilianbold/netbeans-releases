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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.xml.jaxb.api.model.events;

/**
 * For JAXBWizEventType.EVENT_BINDING_ADDED:
 * Source is Schemas, oldValue is null, new value is newly added Schema.
 * 
 * For JAXBWizEventType.EVENT_BINDING_CHANGED:
 * Source is Schemas, oldValue is old Schema object, newValue is new Schema 
 * object.
 * 
 * For JAXBWizEventType.EVENT_BINDING_DELETED:
 * Source is Schemas, oldValue is deleted Schema object, newValue is null.
 * 
 * For JAXBWizEventType.EVENT_CFG_FILE_EDITED:
 * Source is new Schemas, oldValue is null, newValue is null.
 * 
 * @author gpatil
 */
public interface JAXBWizEvent {
    public enum JAXBWizEventType {
        EVENT_BINDING_ADDED, 
        EVENT_BINDING_CHANGED, 
        EVENT_BINDING_DELETED, 
        EVENT_CFG_FILE_EDITED
    } ;

    public JAXBWizEventType getEventType();
    public Object getSource();
    public Object getOldValue();
    public Object getNewValue();
}
