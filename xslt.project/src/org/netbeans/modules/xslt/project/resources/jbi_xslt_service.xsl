<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:template match="/">
        <jbi:message xmlns:ns2="http://sun.com/EmplOutput" 
                type="ns2:output-msg" version="1.0" 
                xmlns:jbi="http://java.sun.com/xml/ns/jbi/wsdl-11-wrapper">
            <jbi:part>
                <xsl:apply-templates/>
            </jbi:part>
            <jbi:part>
                <xsl:apply-templates/>
            </jbi:part>
        </jbi:message>
    </xsl:template>
</xsl:stylesheet>
