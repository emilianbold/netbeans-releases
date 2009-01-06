<#-- This is a FreeMarker template -->
<#-- You can change the contents of the license inserted into
 #   each template by opening Tools | Templates and editing
 #   Licenses | Default License  -->
<#assign licensePrefix = "# ">
<#include "../Licenses/license-${project.license}.txt">

<#-- the file to require variable here can be a path to spec_helper (if it exists) -->
<#-- or the name of the tested file. quoting is handled in RubyTargetChooserPanel -->
require ${file_to_require}

describe ${classname} do
  before(:each) do
    @${classfield} = ${classname}.new
  end

  it "should desc" do
    # TODO
  end
end

