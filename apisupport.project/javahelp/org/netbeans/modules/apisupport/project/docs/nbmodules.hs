<?xml version='1.0' encoding='ISO-8859-1' ?>
<!--
*     Copyright © 2005 Sun Microsystems, Inc. All rights reserved.
*     Use is subject to license terms.
-->
<!DOCTYPE helpset
  PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 2.0//EN"
         "http://java.sun.com/products/javahelp/helpset_2_0.dtd">
<helpset version="2.0">

<!-- last updated 05Feb04-->

  <!-- title -->
  <title>NetBeans Plug-in Help</title>
  
  <!-- maps -->
  <maps>
     <homeID>nbmodule_about</homeID>
     <mapref location="nbmodule-map.jhm" />
  </maps>
  
  <!-- views -->
  <view>
    <name>TOC</name>
    <label>Table Of Contents</label>
    <type>javax.help.TOCView</type>
    <data>nbmodule-tocs.xml</data> 
  </view>

  <view>
    <name>Index</name>
    <label>Index</label>
    <type>javax.help.IndexView</type>
    <data>nbmodule-index.xml</data>
  </view>

  <view>
    <name>Search</name>
    <label>Search</label>
    <type>javax.help.SearchView</type> 
    <data engine="com.sun.java.help.search.DefaultSearchEngine">      
      JavaHelpSearch
    </data>  
  </view>

</helpset>
