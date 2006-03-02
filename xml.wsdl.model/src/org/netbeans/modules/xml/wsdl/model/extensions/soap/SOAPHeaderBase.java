/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model.extensions.soap;

import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.xam.GlobalReference;

/**
 *
 * @author Nam Nguyen
 */
public interface SOAPHeaderBase extends SOAPMessageBase {
    
    public static final String MESSAGE_PROPERTY = "message";
    public static final String PART_PROPERTY = "part";
    
    void setMessage(GlobalReference<Message> message);
    GlobalReference<Message> getMessage();
    
    void setPart(String part);
    String getPart();

}
