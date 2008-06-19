<#-- This is a FreeMarker template -->
<#-- You can change the contents of the license inserted into
 #   each template by opening Tools | Templates and editing
 #   Licenses | Default License  -->
<#assign licensePrefix = "# ">
<#include "../Licenses/license-${project.license}.txt">

<#assign indent = "">
<#-- If the "outermodules" parameter is set, emit a series of module Name lines -->
<#if outermodules?? && outermodules != "">
<#assign modulelist = outermodules?split("::")>
<#list modulelist as modulename>
${indent}module ${modulename}
<#assign indent = indent + "  ">
</#list>
</#if>
${indent}module ${module}
${indent}    
${indent}end
<#if modulelist??>
<#list modulelist as x>
<#assign indent = indent?substring(2)>
${indent}end
</#list>
</#if>
