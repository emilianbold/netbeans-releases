<#-- This is a FreeMarker template -->
<#-- You can change the contents of the license inserted into
 #   each template by opening Tools | Templates and editing
 #   Licenses | Default License  -->
<#assign licenseFirst = "<%#">
<#assign licensePrefix = "# ">
<#assign licenseLast = "%>">
<#include "../Licenses/license-${project.license}.txt">
<#-- End of license section; GSP contents follow -->

<html>
  <head>
    <title>Our books</title>
  </head>
  <body>
    <ul>
      <g:each it="books">
        <li>${it.title} (${it.author.name})</li>
      </g:each>
    </ul>
  </body>
</html>
