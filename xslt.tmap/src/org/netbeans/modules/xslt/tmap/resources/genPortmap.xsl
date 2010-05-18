<?xml version="1.0" encoding="UTF-8"?>
<!--
  The contents of this file are subject to the terms of the Common Development
  and Distribution License (the License). You may not use this file except in
  compliance with the License.

  You can obtain a copy of the License at http://www.netbeans.org/cddl.html
  or http://www.netbeans.org/cddl.txt.

  When distributing Covered Code, include this CDDL Header Notice in each file
  and include the License file at http://www.netbeans.org/cddl.txt.
  If applicable, add the following below the CDDL Header, with the fields
  enclosed by brackets [] replaced by your own identifying information:
  "Portions Copyrighted [year] [name of copyright owner]"

  The Original Software is NetBeans. The Initial Developer of the Original
  Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
  Microsystems, Inc. All Rights Reserved.
-->
<!--
Translate from 
<xsltmap>
    <requestReplyService>
        <input file="xsl.xsl" 
            messageType="{http://j2ee.netbeans.org/wsdl/newWSDL}operatReply" 
            operation="operat" 
            partnerLink="{http://j2ee.netbeans.org/wsdl/newWSDL}PartnerLinkType" 
            portType="portType" 
            roleName="Role" 
            transformJBI="false"/>
    </requestReplyService>
    .........
</xsltmap>

to
<jbi version="1.0" xmlns="http://java.sun.com/xml/ns/jbi">
<services binding-component="false">
    
    
        <provides 
            endpoint-name="purchaseWSPortTypeRole" 
            interface-name="purchaseWSPortType" 
            service-name="{http://j2ee.netbeans.org/wsdl/purchaseWS}purchaseWSPartner"/>
    
    
    
</services>
</jbi>

    
        <provides 
            endpoint-name="AirlineReservationCallbackServiceRole_myRole" 
            interface-name="ns2:AirlineReservationCallbackPortType" 
            service-name="ns0:Airline"/>
    
        <consumes 
            endpoint-name="AirlineReservationServiceRole_partnerRole" 
            interface-name="ns2:AirlineReservationPortType" 
            service-name="ns0:Airline"/>
    
    -->
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0"
>
<xsl:output method="xml" indent="no" encoding="UTF-8"/>
<xsl:template match="xsltmap">
    <jbi 
        xmlns="http://java.sun.com/xml/ns/jbi" 
        version="1.0">
        <services binding-component="false">
            <xsl:apply-templates/>
        </services>
    </jbi>
</xsl:template>

<xsl:template match="input">
    <provides>
      <xsl:attribute name="endpoint-name">
        <xsl:value-of select="@roleName" />
      </xsl:attribute>        
      <xsl:attribute name="interface-name">
        <xsl:value-of select="@portType" />
      </xsl:attribute>        
      <xsl:attribute name="service-name">
        <xsl:value-of select="@partnerLink" />
      </xsl:attribute>        
    </provides>
    
</xsl:template>

<xsl:template match="output">
    <consumes>
      <xsl:attribute name="endpoint-name">
        <xsl:value-of select="@roleName" />
      </xsl:attribute>        
      <xsl:attribute name="interface-name">
        <xsl:value-of select="@portType" />
      </xsl:attribute>        
      <xsl:attribute name="service-name">
        <xsl:value-of select="@partnerLink" />
      </xsl:attribute>        
    </consumes>
    
</xsl:template>    

</xsl:stylesheet>

