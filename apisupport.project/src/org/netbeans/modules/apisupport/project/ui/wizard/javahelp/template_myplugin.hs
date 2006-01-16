<?xml version='1.0' encoding='ISO-8859-1'?>

<!-- NOTE: There was an extra double quote at the end of the DTD URI (from the tute) -->
<!DOCTYPE helpset PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 2.0//EN"
                     "http://java.sun.com/products/javahelp/helpset_2_0.dtd">

<helpset version="2.0">
  
   <title>@@MyPlugin_DisplayName@@ Help</title>
  
   <maps>
     <homeID>@@MyPlugin@@</homeID>
     <mapref location="@@MyPlugin@@-map.jhm"/>
   </maps>
  
   <view mergetype="javax.help.AppendMerge">
     <name>TOC</name>
     <label>Table of Contents</label>
     <type>javax.help.TOCView</type>
     <data>@@MyPlugin@@-toc.xml</data>
   </view>
	
   <view mergetype="javax.help.AppendMerge">
      <name>Index</name>
      <label>Index</label>
      <type>javax.help.IndexView</type>
      <data>@@MyPlugin@@-idx.xml</data>
   </view>
	
   <view>
      <name>Search</name>
      <label>Search</label>
      <type>javax.help.SearchView</type>
      <data engine="com.sun.java.help.search.DefaultSearchEngine">JavaHelpSearch</data>
   </view>

</helpset>