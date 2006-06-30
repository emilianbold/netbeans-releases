<?xml version="1.0" encoding="UTF-8" ?>
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
Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

