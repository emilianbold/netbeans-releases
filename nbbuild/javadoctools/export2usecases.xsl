<?xml version="1.0" encoding="UTF-8" ?>
<!--
                Sun Public License Notice

The contents of this file are subject to the Sun Public License
Version 1.0 (the "License"). You may not use this file except in
compliance with the License. A copy of the License is available at
http://www.sun.com/

The Original Code is NetBeans. The Initial Developer of the 77Original
Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
Microsystems, Inc. All Rights Reserved.
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html"/>
    <xsl:param name="date" />
    
    <xsl:template match="/" >
        <html>
        <head>
            <!-- projects.netbeans.org -->
           <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
           <title>How to use certain NetBeans APIs</title>
            <link rel="stylesheet" href="OpenAPIs/netbeans.css" type="text/css"/>

          <link REL="icon" href="http://www.netbeans.org/favicon.ico" type="image/ico" />
          <link REL="shortcut icon" href="http://www.netbeans.org/favicon.ico" />

        </head>

        <body>
            <center><h1>How to use certain NetBeans APIs</h1></center>

            This page contains extracted usecases for some of the NetBeans modules
            that <a href="index.html">offer an API</a>. 


            <xsl:for-each select="//module/arch-usecases[not(../@name='_no module_')]" >
                <hr/>
                <h2><a>
                        <xsl:attribute name="name">
                            <xsl:text>usecase-</xsl:text>
                            <xsl:value-of select="../@name"/>
                        </xsl:attribute>
                        <xsl:text>How to use </xsl:text>
                    </a>
                    <a>
                        <xsl:attribute name="href" >
                            <xsl:text>index.html#def-api-</xsl:text>
                            <xsl:value-of select="../@name"/>
                        </xsl:attribute>
                        <xsl:value-of select="../@name"/>
                    </a>?
                </h2>
                <xsl:apply-templates select="../description" />
                <p/>
                <xsl:apply-templates />
            </xsl:for-each>
         </body>
         </html>
    </xsl:template>
    
    <xsl:template match="api-ref">
        <!-- simply bold the name, it link will likely be visible bellow -->
        <b>
            <xsl:value-of select="@name" />
        </b>
    </xsl:template>
    
    <xsl:template match="@*|node()">
       <xsl:copy  >
          <xsl:apply-templates select="@*|node()"/>
       </xsl:copy>
    </xsl:template>
        
</xsl:stylesheet>


