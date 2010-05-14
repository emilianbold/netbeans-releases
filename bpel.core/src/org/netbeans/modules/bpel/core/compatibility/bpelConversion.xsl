<?xml version="1.0" encoding="UTF-8"?>

<!--
The XSLT is intended to convert format of a BPEL with TypeCast extensions.
The format was changed between GlassFish ESB v2.0 and v2.1

-->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:bpws="http://docs.oasis-open.org/wsbpel/2.0/process/executable"
                xmlns:sxt="http://www.sun.com/wsbpel/2.0/process/executable/SUNExtension/Trace" 
                xmlns:sxed="http://www.sun.com/wsbpel/2.0/process/executable/SUNExtension/Editor"
                xmlns:sxed2="http://www.sun.com/wsbpel/2.0/process/executable/SUNExtension/Editor2"
                >

    <xsl:output method="xml" indent="yes"/>
    
    <!-- Process Editor NS declaration -->

<!--
    <xsl:template match="@*[. = 'http://www.sun.com/wsbpel/2.0/process/executable/SUNExtension/Editor']">
    <xsl:template match="@xmlns:sxed">
        <xsl:copy>
            'http://www.sun.com/wsbpel/2.0/process/executable/SUNExtension/Editor2'
        </xsl:copy>
    </xsl:template>
-->

    <!-- Copy everything -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- Process Variables -->
    <xsl:template match="bpws:variable/sxed:editor">
        <xsl:call-template name="copy-editor-content"/>
    </xsl:template>
    <xsl:template match="bpws:variable/sxed:editor/sxed:predicates">
        <xsl:call-template name="copy-editor-content"/>
    </xsl:template>
    <xsl:template match="bpws:variable/sxed:editor/sxed:casts">
        <xsl:call-template name="copy-editor-content"/>
    </xsl:template>

    <!-- Process Copy -->
    <xsl:template match="bpws:copy/sxed:editor"/><!-- skip the sxed:editor element inside of the bpws:copy -->
    <xsl:template match="bpws:copy/bpws:from">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
            <xsl:for-each select="../sxed:editor[*/*/@source='from']" >
                <xsl:call-template name="copy-editor-content-from"/>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="bpws:copy/bpws:to">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
            <xsl:for-each select="../sxed:editor[*/*/@source='to']" >
                <xsl:call-template name="copy-editor-content-to"/>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>

    <!-- Process If -->
    <xsl:template match="bpws:if/sxed:editor"/><!-- skip the sxed:editor element inside of the bpws:if -->
    <xsl:template match="bpws:if/bpws:condition">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
            <xsl:for-each select="../sxed:editor" >
                <xsl:call-template name="copy-editor-content"/>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>

    <!-- Process ElseIf -->
    <xsl:template match="bpws:elseif/sxed:editor"/><!-- skip the sxed:editor element inside of the bpws:elseif -->
    <xsl:template match="bpws:elseif/bpws:condition">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
            <xsl:for-each select="../sxed:editor" >
                <xsl:call-template name="copy-editor-content"/>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>

    <!-- Process While -->
    <xsl:template match="bpws:while/sxed:editor"/><!-- skip the sxed:editor element inside of the bpws:while -->
    <xsl:template match="bpws:while/bpws:condition">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
            <xsl:for-each select="../sxed:editor" >
                <xsl:call-template name="copy-editor-content"/>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>

    <!-- Process RepeatUntil -->
    <xsl:template match="bpws:repeatUntil/sxed:editor"/><!-- skip the sxed:editor element inside of the bpws:repeatUntil -->
    <xsl:template match="bpws:repeatUntil/bpws:condition">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
            <xsl:for-each select="../sxed:editor" >
                <xsl:call-template name="copy-editor-content"/>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>

    <!-- Process ForEach -->
    <xsl:template match="bpws:forEach/sxed:editor"/><!-- skip the sxed:editor element inside of the bpws:forEach -->
    <xsl:template match="bpws:forEach/bpws:startCounterValue">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
            <xsl:for-each select="../sxed:editor" >
                <xsl:call-template name="copy-editor-content"/>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="bpws:forEach/bpws:finalCounterValue">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
            <xsl:for-each select="../sxed:editor" >
                <xsl:call-template name="copy-editor-content"/>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="bpws:forEach/bpws:completionCondition/bpws:branches">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
            <xsl:for-each select="../../sxed:editor" >
                <xsl:call-template name="copy-editor-content"/>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>

    <!-- Process Wait -->
    <xsl:template match="bpws:wait/sxed:editor"/><!-- skip the sxed:editor element inside of the bpws:wait -->
    <xsl:template match="bpws:wait/bpws:for | bpws:wait/bpws:until">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
            <xsl:for-each select="../sxed:editor" >
                <xsl:call-template name="copy-editor-content"/>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>

    <!-- Process OnAlarmPick -->
    <xsl:template match="bpws:pick/bpws:onAlarm/sxed:editor"/><!-- skip the sxed:editor element inside of the bpws:pick/bpws:onAlarm -->
    <xsl:template match="bpws:pick/bpws:onAlarm/bpws:for | bpws:pick/bpws:onAlarm/bpws:until">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
            <xsl:for-each select="../sxed:editor" >
                <xsl:call-template name="copy-editor-content"/>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>

    <!-- Process OnAlarmEvent -->
    <xsl:template match="bpws:eventHandlers/bpws:onAlarm/sxed:editor"/><!-- skip the sxed:editor element inside of the bpws:eventHandlers/bpws:onAlarm -->
    <xsl:template match="bpws:eventHandlers/bpws:onAlarm/bpws:for | 
                         bpws:eventHandlers/bpws:onAlarm/bpws:until |
                         bpws:eventHandlers/bpws:onAlarm/bpws:repeatEvery">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
            <xsl:for-each select="../sxed:editor" >
                <xsl:call-template name="copy-editor-content"/>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>

    <!-- Process Log -->
    <xsl:template match="sxt:trace/sxt:log/sxed:editor"/><!-- skip the sxed:editor element inside of the sxt:trace/sxt:log -->
    <xsl:template match="sxt:trace/sxt:log/bpws:from">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
            <xsl:for-each select="../sxed:editor" >
                <xsl:call-template name="copy-editor-content"/>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>

    <!-- Process Alert -->
    <xsl:template match="sxt:trace/sxt:alert/sxed:editor"/><!-- skip the sxed:editor element inside of the sxt:trace/sxt:alert -->
    <xsl:template match="sxt:trace/sxt:alert/bpws:from">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
            <xsl:for-each select="../sxed:editor" >
                <xsl:call-template name="copy-editor-content"/>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>

    <!-- Named templates -->
    <xsl:template name="copy-editor-content">
        <sxed2:editor>
            <xsl:apply-templates select="*/sxed:cast"/>
            <xsl:apply-templates select="*/sxed:pseudoComp"/>
            <xsl:apply-templates select="*/sxed:predicate"/>
        </sxed2:editor>
    </xsl:template>

    <xsl:template name="copy-editor-content-from">
        <sxed2:editor>
            <xsl:apply-templates select="*/sxed:cast[@source='from']"/>
            <xsl:apply-templates select="*/sxed:pseudoComp[@source='from']"/>
            <xsl:apply-templates select="*/sxed:predicate[@source='from']"/>
        </sxed2:editor>
    </xsl:template>

    <xsl:template name="copy-editor-content-to" >
        <sxed2:editor>
            <xsl:apply-templates select="*/sxed:cast[@source='to']"/>
            <xsl:apply-templates select="*/sxed:pseudoComp[@source='to']"/>
            <xsl:apply-templates select="*/sxed:predicate[@source='to']"/>
        </sxed2:editor>
    </xsl:template>

    <xsl:template match="sxed:cast">
        <xsl:element name="sxed2:cast">
            <xsl:attribute name="path">
                <xsl:value-of select="@path"/>
            </xsl:attribute>
            <xsl:attribute name="type">
                <xsl:value-of select="@type"/>
            </xsl:attribute>
            <xsl:attribute name="source">
                <xsl:value-of select="@source"/>
            </xsl:attribute>
            <xsl:apply-templates select="*|@*|text()"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="sxed:pseudoComp">
        <xsl:element name="sxed2:pseudoComp">
            <xsl:attribute name="parentPath">
                <xsl:value-of select="@parentPath"/>
            </xsl:attribute>
            <xsl:attribute name="type">
                <xsl:value-of select="@type"/>
            </xsl:attribute>
            <xsl:attribute name="qName">
                <xsl:value-of select="@qName"/>
            </xsl:attribute>
            <xsl:attribute name="isAttribute">
                <xsl:value-of select="@isAttribute"/>
            </xsl:attribute>
            <xsl:attribute name="source">
                <xsl:value-of select="@source"/>
            </xsl:attribute>
            <xsl:apply-templates select="*|@*|text()"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="sxed:predicate">
        <xsl:element name="sxed2:predicate">
            <xsl:attribute name="path">
                <xsl:value-of select="@path"/>
            </xsl:attribute>
            <xsl:attribute name="source">
                <xsl:value-of select="@source"/>
            </xsl:attribute>
            <xsl:apply-templates select="*|@*|text()"/>
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>
