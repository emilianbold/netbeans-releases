<#-- This is a FreeMarker template -->
<#-- You can change the contents of the license inserted into
 #   each template by opening Tools | Templates and editing
 #   Licenses | Default License  -->
<#assign licensePrefix = "# ">
<#include "../Licenses/license-${project.license}.txt">

<#assign indent = "">
<#-- If the "module" parameter is set, emit a series of module Name lines -->
<#if module?? && module != "">
<#assign modulelist = module?split("::")>
<#list modulelist as modulename>
${indent}module ${modulename}
<#assign indent = indent + "  ">
</#list>
</#if>
<#-- If the "extend" parameter is set, add < Superclass to the class definition -->
${indent}class ${class}<#if extend?? && extend != ""> < ${extend}</#if>
${indent}  def initialize
${indent}    
${indent}  end
${indent}end
<#if modulelist??>
<#list modulelist as x>
<#assign indent = indent?substring(2)>
${indent}end
</#list>
</#if>
