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

package org.netbeans.modules.wsdlextensions.hl7;

/**
 *
 * Represents the address element under the wsdl port for HL7 binding
 * @author raghunadh.teegavarapu@sun.com
*/
public interface HL7Address extends HL7Component {
    public static final String HL7_SVR_LOCATIONURL = "location";
    public static final String HL7_TRANS_PROTOCOL_NAME = "transportProtocolName";
      
	public void setHL7ServerLocationURL(String url);
	public String getHL7ServerLocationURL();
	public void setTransportProtocolName(String val);
	public String getTransportProtocolName();
        
        enum TransportProtocol {
            TCPIP("tcp-ip");
            
            private String name;
            TransportProtocol(String name){
                this.name =  name;
            }
            
            public String getName(){
                return  this.name;
            }
            
        }
}
