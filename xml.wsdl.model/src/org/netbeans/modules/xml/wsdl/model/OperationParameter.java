/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model;

import org.netbeans.modules.xml.xam.GlobalReference;
import org.netbeans.modules.xml.xam.Named;

/**
 *
 * @author Nam Nguyen
 */
public interface OperationParameter extends Named<WSDLComponent>, WSDLComponent {
    public static final String MESSAGE_PROPERTY = "message";
    
    GlobalReference<Message> getMessage();
    void setMessage(GlobalReference<Message> message);
}
