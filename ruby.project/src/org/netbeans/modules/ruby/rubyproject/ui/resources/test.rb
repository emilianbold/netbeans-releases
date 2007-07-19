<#-- This is a FreeMarker template -->
<#-- You can change the contents of the license inserted into
 #   each template by opening Tools | Templates and editing
 #   Licenses | Default License  -->
<#assign licenseFirst = "# ">
<#assign licensePrefix = "# ">
<#assign licenseLast = " ">
<#include "../Licenses/license-${project.license}.txt">

$:.unshift File.join(File.dirname(__FILE__),'..','lib')

require 'test/unit'

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
${indent}  def test_fail
${indent}    assert(false, 'Assertion was false.')
${indent}  end
${indent}   
${indent}  def test_foo
${indent}    # assert_equal("foo", bar)
${indent}
${indent}    # assert, assert_block, assert_equal, assert_in_delta, assert_instance_of,
${indent}    # assert_kind_of, assert_match, assert_nil, assert_no_match, assert_not_equal,
${indent}    # assert_not_nil, assert_not_same, assert_nothing_raised, assert_nothing_thrown,
${indent}    # assert_operator, assert_raise, assert_raises, assert_respond_to, assert_same,
${indent}    # assert_send, assert_throws
${indent}
${indent}    flunk "TODO: Write test"
${indent}  end
${indent}end
<#if modulelist??>
<#list modulelist as x>
<#assign indent = indent?substring(2)>
${indent}end
</#list>
</#if>
