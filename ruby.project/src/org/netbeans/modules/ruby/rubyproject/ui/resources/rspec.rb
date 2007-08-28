<#-- This is a FreeMarker template -->
<#-- You can change the contents of the license inserted into
 #   each template by opening Tools | Templates and editing
 #   Licenses | Default License  -->
<#assign licenseFirst = "# ">
<#assign licensePrefix = "# ">
<#assign licenseLast = " ">
<#include "../Licenses/license-${project.license}.txt">
require '${classfile}'

describe ${classname} do
  before(:each) do
    @${classfield} = ${classname}.new
  end

  it "should desc" do
    # TODO
  end
end

