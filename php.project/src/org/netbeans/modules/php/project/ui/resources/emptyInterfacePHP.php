<?php
<#assign licenseFirst = "/* ">
<#assign licensePrefix = " * ">
<#assign licenseLast = " */">
<#include "../Licenses/license-${project.license}.txt">

<#if namespace?? && namespace?length &gt; 0>
namespace ${namespace};
</#if>

/**
 *
 * @author ${user}
 */
interface ${name} {
    //put your code here
}
?>
