<#-- This is a FreeMarker template -->
<#-- You can change the contents of the license inserted into
 #   each template by opening Tools | Templates and editing
 #   Licenses | Default License  -->
<#assign licensePrefix = "# ">
<#include "../Licenses/license-${project.license}.txt">

__author__="${user}"
__date__ ="$${date} ${time}$"

if __name__ == "__main__":
    print "Hello World"
