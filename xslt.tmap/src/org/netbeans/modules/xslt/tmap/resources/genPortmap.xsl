<?xml version="1.0" encoding="UTF-8"?>
<!--
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.


The contents of this file are subject to the terms of either the GNU
General Public License Version 2 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://www.netbeans.org/cddl-gplv2.html
or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License file at
nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
particular file as subject to the "Classpath" exception as provided
by Sun in the GPL Version 2 section of the License file that
accompanied this code. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

Contributor(s):

  The Original Software is NetBeans. The Initial Developer of the Original
  Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
  Microsystems, Inc. All Rights Reserved.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
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

