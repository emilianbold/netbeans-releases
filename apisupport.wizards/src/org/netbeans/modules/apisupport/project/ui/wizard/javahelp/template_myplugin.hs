<?xml version="1.0" encoding="UTF-8"?>
<#assign licenseFirst = "<!--">
<#assign licensePrefix = "">
<#assign licenseLast = "-->">
<#include "${project.licensePath}">
<!DOCTYPE helpset PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 2.0//EN" "http://java.sun.com/products/javahelp/helpset_2_0.dtd">
<helpset version="2.0">
    <title>${DISPLAY_NAME} Help</title>
    <maps>
        <homeID>${FULL_CODE_NAME}.about</homeID>
        <mapref location="${CODE_NAME}-map.xml"/>
    </maps>
    <view mergetype="javax.help.AppendMerge">
        <name>TOC</name>
        <label>Table of Contents</label>
        <type>javax.help.TOCView</type>
        <data>${CODE_NAME}-toc.xml</data>
    </view>
    <view mergetype="javax.help.AppendMerge">
        <name>Index</name>
        <label>Index</label>
        <type>javax.help.IndexView</type>
        <data>${CODE_NAME}-idx.xml</data>
    </view>
    <view>
        <name>Search</name>
        <label>Search</label>
        <type>javax.help.SearchView</type>
        <data engine="com.sun.java.help.search.DefaultSearchEngine">JavaHelpSearch</data>
    </view>
</helpset>
