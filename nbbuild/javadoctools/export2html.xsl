<?xml version="1.0" encoding="UTF-8" ?>

<!--
    Document   : api-questions-to-html.xsl
    Created on : November 4, 2002, 4:51 PM
    Author     : jarda
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html"/>

    <!-- unique key over all groups of apis -->
    <xsl:key match="//api[@type='export']" name="apiGroups" use="@group" />
    
    <xsl:template match="/apis" >
        <html>
        <head>
            <!-- projects.netbeans.org -->
           <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
           <title>NetBeans API List</title>
            <link rel="StyleSheet" href="http://www.netbeans.org/netbeans.css" type="text/css" title="NetBeans OpenSource Style" />
        <!--    <link rel="StyleSheet" href="docs.css" type="text/css"> -->

          <link REL="icon" href="http://www.netbeans.org/favicon.ico" type="image/ico" />
          <link REL="shortcut icon" href="http://www.netbeans.org/favicon.ico" />

        </head>

        <body style="margin-left: 20px; margin-right: 20px; margin-top: 0px;" bgcolor="#FFFFFF" >

        <center><h1>NetBeans API List</h1></center>

        This document provides a list of <em>NetBeans APIs</em> with a short description
        of what they are good for and a table describing different types of interfaces
        (see <a href="http://openide.netbeans.org/tutorial/api-design.html#api">what is
        an API</a> description to understand why we list DTDs, files and etc.) and with
        a stability category (see <a
        href="http://openide.netbeans.org/tutorial/api-design.html#life">API
        life-cycle</a> for a list of possible categories and description of what they
        mean). The aim is to provide as depth definition of NetBeans modules 
        external interfaces as possible and give other developers a chance to decide
        whether they want to depend on particular API or not.
        <P/>
        To get API of your module listed here, see documentation for the 
        javadoc building 
        <a href="http://openide.netbeans.org/tutorial/api.html">infrastructure</a>.

        <hr/>
        <xsl:call-template name="list-modules" />
        <hr/>
        <xsl:apply-templates />
        
        </body>
        </html>
       
    </xsl:template>
    
    <xsl:template name="list-modules">
        <h3>Content</h3>
        <ul>
            <xsl:for-each select="/apis/module" >
                <xsl:choose>
                    <xsl:when test="api" >
                        <li><a><xsl:attribute name="href">#def-api-<xsl:value-of select="@name"/></xsl:attribute>
                            <xsl:value-of select="@name" />
                            </a> - <xsl:value-of select="substring-before(description, '.')" disable-output-escaping="yes" />.
                        </li>
                    </xsl:when>
                    <xsl:otherwise>
                        <li>
                            <xsl:variable name="where" select="substring-before(@target, '/')" />
                            <b><xsl:value-of select="$where" /></b> - no API description provided
                            (see <a><xsl:attribute name="href">http://openide.netbeans.org/tutorial/api.html</xsl:attribute>how to 
                            do</a><xsl:text> </xsl:text>
                            <a><xsl:attribute name="href"><xsl:value-of select="$where" />/index.html</xsl:attribute>it</a>)
                            
                        </li>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </ul>
    </xsl:template>

    <xsl:template match="module">
            <xsl:variable name="interfaces" select="api[@type='export']" />
            <xsl:variable name="module.name" select="@name" />
            <xsl:variable name="arch.stylesheet" select="@stylesheet" />
            <xsl:variable name="arch.overviewlink" select="@overviewlink" />
            <xsl:variable name="arch.footer" select="@footer" />
            <xsl:variable name="arch.target" select="@target" />

            <xsl:if test="$interfaces">
                <h5>

                    <a>
                        <xsl:attribute name="name">
                            <xsl:text>def-api-</xsl:text><xsl:value-of select="$module.name" />
                        </xsl:attribute>
                        <xsl:value-of select="$module.name" />
                    </a>
                    
                    (<a>
                        <xsl:attribute name="href">
                            <xsl:call-template name="filedirapi" >
                                <xsl:with-param name="arch.target" select="$arch.target" />
                            </xsl:call-template>
                            <xsl:text>/index.html</xsl:text>
                        </xsl:attribute>
                        <xsl:text>javadoc</xsl:text>
                    </a>,
                    <a>
                        <xsl:attribute name="href">
                            <xsl:call-template name="filedirapi" >
                                <xsl:with-param name="arch.target" select="$arch.target" />
                            </xsl:call-template>
                            <xsl:text>.zip</xsl:text>
                        </xsl:attribute>
                        <xsl:text>download</xsl:text>
                    </a>)
                </h5>

                <xsl:apply-templates select="description"/>
                <P/>

                <table border="3" cellpadding="6" width="90%">
                    <thead>
                        <th valign="bottom" width="30%"><b>Interface Name</b></th>
                        <th valign="bottom" width="15%"><b>Stability Classification</b></th>
                        <th valign="bottom" width="45%"><b>Specified in What Document?</b></th>
                    </thead>

                    <xsl:for-each select="$interfaces">
                        <tr/>
                        <xsl:if test="@group='java'" >
                            <xsl:call-template name="api" />
                        </xsl:if>
                    </xsl:for-each>

                    <xsl:for-each select="//api[generate-id() = generate-id(key('apiGroups', @group))]">
                        <xsl:variable name="grp" select="@group" />
                        <xsl:if test="$grp!='java'" >
                            <xsl:variable name="apis" select="/apis" />
                            <xsl:variable name="module" select="$apis/module[@name=$module.name]" />

                            <xsl:variable name="allOfTheGroup" select="$module/api[@group=$grp]" />
                            <xsl:if test="$allOfTheGroup">
                                <tr/>
                                <td>Set of <xsl:value-of select="$grp"/> APIs</td>
                                <td>Individual</td>
                                <td>
                                    <a>
                                        <xsl:attribute name="href" >
                                            <xsl:value-of select="$arch.target" /><xsl:text>#group-</xsl:text><xsl:value-of select="$grp"/>
                                        </xsl:attribute>
                                        table with definitions
                                    </a>
                                </td>
                            </xsl:if>
                        </xsl:if>
                    </xsl:for-each>

                </table>
            </xsl:if>


            <P/>

    </xsl:template>

    <xsl:template name="api">
        <xsl:variable name="name" select="@name" />
        <xsl:variable name="type" select="@type" />
        <xsl:variable name="category" select="@category" />
        <xsl:variable name="url" select="@url" />

        <tbody>
            <td>
                <a>
                    <xsl:attribute name="name" >
                        <xsl:text>api-</xsl:text><xsl:value-of select="$name" />
                    </xsl:attribute>
                    <xsl:value-of select="$name" />
                </a>
            </td>
            <!--
            <td>
                <xsl:choose>
                    <xsl:when test="$type='import'">Imported</xsl:when>
                    <xsl:when test="$type='export'">Exported</xsl:when>
                    <xsl:otherwise>WARNING: <xsl:value-of select="$type" /></xsl:otherwise>
                </xsl:choose>
            </td> -->
            <td> <!-- stability category -->
                <xsl:choose>
                    <xsl:when test="$category='official'">Official</xsl:when>
                    <xsl:when test="$category='stable'">Stable</xsl:when>
                    <xsl:when test="$category='devel'">Under Development</xsl:when>
                    <xsl:when test="$category='third'">Third party</xsl:when>
                    <xsl:when test="$category='standard'">Standard</xsl:when>
                    <xsl:when test="$category='friend'">Friend</xsl:when>
                    <xsl:when test="$category='private'">Private</xsl:when>
                    <xsl:when test="$category='deprecated'">Deprecated</xsl:when>
                    <xsl:otherwise>WARNING: <xsl:value-of select="$category" /></xsl:otherwise>
                </xsl:choose>
            </td>

            <td> <!-- url -->
                <a>
                    <xsl:attribute name="href" >
                        <xsl:value-of select="$url" />
                    </xsl:attribute>
                    <xsl:value-of select="$url" />
                </a>
            </td>
        </tbody>

    </xsl:template>

    <xsl:template match="api-ref">
        <!-- simply bold the name, it link will likely be visible bellow -->
        <b>
            <xsl:value-of select="@name" />
        </b>
    </xsl:template>

    <!-- extracts first part before slash from LoadersAPI/bleble.html or
     and prints it or prints OpenAPIs as a fallback -->

    <xsl:template name="filedirapi" >
        <xsl:param name="arch.target" />
    
        <xsl:if test="substring-before($arch.target,'/')">
            <xsl:value-of select="substring-before($arch.target,'/')" />
        </xsl:if>
        <xsl:if test="not (substring-before($arch.target,'/'))">
            <xsl:text>OpenAPIs</xsl:text>
        </xsl:if>
    </xsl:template>


    <!-- Format random HTML elements as is: -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>


