/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
