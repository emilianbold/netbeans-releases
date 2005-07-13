<?xml version="1.0" encoding="UTF-8" ?>
<!--
                Sun Public License Notice

The contents of this file are subject to the Sun Public License
Version 1.0 (the "License"). You may not use this file except in
compliance with the License. A copy of the License is available at
http://www.sun.com/

The Original Code is NetBeans. The Initial Developer of the Original
Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
Microsystems, Inc. All Rights Reserved.
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml"/>
    
    <!-- print out <api /> dependencies on all needed netbeans subprojects -->
    <xsl:template match="/" >
        <p>
            The sources for the module are in <a href="http://www.netbeans.org">NetBeans</a>
            CVS in 
            <a>
                <xsl:attribute name="href">
                    <xsl:text>http://www.netbeans.org/source/browse/</xsl:text>
                    <xsl:value-of select="//project/cvs-location" />
                </xsl:attribute>
                <xsl:value-of select="//project/cvs-location" />
            </a>
            directory.
        </p>
    </xsl:template>    
        </xsl:stylesheet> 

