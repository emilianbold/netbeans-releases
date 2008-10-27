<#-- This is a FreeMarker template -->
<#-- You can change the contents of the license inserted into
 #   each template by opening Tools | Templates and editing
 #   Licenses | Default License  -->
<#assign licensePrefix = "# ">
<#include "../Licenses/license-${project.license}.txt">

$:.unshift File.join(File.dirname(__FILE__),'..','lib')

require 'test/unit'
require '${classfile}'

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
${indent}  def test_foo
${indent}    #TODO: Write test
${indent}    flunk "TODO: Write test"
${indent}    # assert_equal("foo", bar)
${indent}  end
${indent}end
<#if modulelist??>
<#list modulelist as x>
<#assign indent = indent?substring(2)>
${indent}end
</#list>
</#if>
