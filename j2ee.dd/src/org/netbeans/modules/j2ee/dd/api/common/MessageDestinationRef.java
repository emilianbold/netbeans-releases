/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.dd.api.common;

/**
 * Generated interface for MessageDestinationRef element.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
public interface MessageDestinationRef extends CommonDDBean, DescriptionInterface {
    
    public static final String MESSAGE_DESTINATION_REF_NAME = "MessageDestinationRefName";	// NOI18N
    public static final String MESSAGE_DESTINATION_TYPE = "MessageDestinationType";	// NOI18N
    public static final String MESSAGE_DESTINATION_USAGE = "MessageDestinationUsage";	// NOI18N
    public static final String MESSAGE_DESTINATION_LINK = "MessageDestinationLink";	// NOI18N
    public static final String MESSAGE_DESTINATION_USAGE_CONSUMES = "Consumes";	// NOI18N
    public static final String MESSAGE_DESTINATION_USAGE_PRODUCES = "Produces";	// NOI18N
    public static final String MESSAGE_DESTINATION_USAGE_CONSUMESPRODUCES = "ConsumesProduces";	// NOI18N
    
    /** Setter for message-destination-ref-name property.
     * @param value property value
     */
    public void setMessageDestinationRefName(java.lang.String value);
    /** Getter for message-destination-ref-name property.
     * @return property value 
     */
    public java.lang.String getMessageDestinationRefName();
    /** Setter for message-destination-type property.
     * @param value property value
     */
    public void setMessageDestinationType(java.lang.String value);
    /** Getter for message-destination-type property.
     * @return property value 
     */
    public java.lang.String getMessageDestinationType();
    /** Setter for message-destination-usage property.
     * @param value property value
     */
    public void setMessageDestinationUsage(java.lang.String value);
    /** Getter for message-destination-usage property.
     * @return property value 
     */
    public java.lang.String getMessageDestinationUsage();
    /** Setter for message-destination-link property.
     * @param value property value
     */
    public void setMessageDestinationLink(java.lang.String value);
    /** Getter for message-destination-link property.
     * @return property value 
     */
    public java.lang.String getMessageDestinationLink();

}
