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
    <xsl:output method="text"/>

    <xsl:template match="/" >
        <xsl:choose>
            <xsl:when test="descendant::api[@category='stable' and @group='java' and @type='export']"><![CDATA[
stability.color=#ffffff
stability.title=Stable
stability.definition.url=http://openide.netbeans.org/tutorial/api-design.html#category-stable
]]></xsl:when>
            <xsl:when test="descendant::api[@category='official' and @group='java' and @type='export']"><![CDATA[
stability.color=#ffffff
stability.title=Official
stability.definition.url=http://openide.netbeans.org/tutorial/api-design.html#category-official
]]></xsl:when>
            <xsl:when test="descendant::api[@category='devel' and @group='java' and @type='export']"><![CDATA[
stability.color=#ddcc80
stability.image=resources/stability-devel.png
stability.title=Under Development
stability.definition.url=http://openide.netbeans.org/tutorial/api-design.html#category-devel
]]></xsl:when>
            <xsl:when test="descendant::api[@category='deprecated' and @group='java' and @type='export']"><![CDATA[
stability.color=#afafaf
stability.image=resources/stability-deprecated.png
stability.title=Deprecated
stability.definition.url=http://openide.netbeans.org/tutorial/api-design.html#category-deprecated
]]></xsl:when>
            <xsl:otherwise><![CDATA[
stability.color=#e0a0a0
stability.image=resources/stability-friend.png
stability.title=Friend, Private or Third Party
stability.definition.url=http://openide.netbeans.org/tutorial/api-design.html#category-friend
]]></xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
