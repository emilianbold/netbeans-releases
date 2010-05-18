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
* @author raghunadh.teegavarapu@sun.com
*/
public interface HL7Message extends HL7Component {
    public static final String HL7_USE_PROPERTY = "use";
    public static final String HL7_ENCODINGSTYLE_PROPERTY = "encodingStyle";
    public static final String HL7_PART_PROPERTY = "part";
   
    
    //<hl7:message use="encoded"
    public String getUse();
    public void setUse(String use);
    //<hl7:message encodingStyle="hl7encoder-1.0"
    public String getEncodingStyle();
    public void setEncodingStyle(String encodingStyle);
    //<hl7:message part="body"
    public String getPart();
    public void setPart(String use);
    
    enum Use{
        
        LITERAL("literal"),
        ENCODED("encoded");
        
        private String name;
        Use(String name){
            this.name = name;
        }
        
        public String getName(){
            return this.name;
        }
    }
  
}

