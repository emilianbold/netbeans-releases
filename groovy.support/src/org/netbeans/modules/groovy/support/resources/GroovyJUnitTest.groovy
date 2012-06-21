/*  This template should not be ever used */

<#assign licenseFirst = "/*">
<#assign licensePrefix = " * ">
<#assign licenseLast = " */">
<#include "../Licenses/license-${project.license}.txt">

<#if package?? && package != "">
package ${package}

</#if>
/**
 *
 * @author ${user}
 */
class ${name} {

}
