<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:project="http://www.netbeans.org/ns/project/1"
                xmlns:xalan="http://xml.apache.org/xslt"
                exclude-result-prefixes="xalan project">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    <xsl:template match="/">
    
<xsl:variable name="name" select="/project:project/project:name"/>
<project name="{$name}" default="all" basedir=".">
    <target name="all" description="Build everything."/>
</project>

    </xsl:template>
    
</xsl:stylesheet> 
