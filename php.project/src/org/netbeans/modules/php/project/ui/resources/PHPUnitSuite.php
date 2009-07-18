<?php
<#assign licenseFirst = "/* ">
<#assign licensePrefix = " * ">
<#assign licenseLast = " */">
<#include "../Licenses/license-${project.license}.txt">

/**
 * To regenerate this file, simply delete it and run all PHPUnit tests.
 * @author ${user}
 */

/**
 * Generic test suite containing all tests.
 *
 * Recursively scans the test-directory and it's
 * sub-directories. All found unit-tests will be
 * added and executed.
 *
 * To run this suite from CLI: phpunit NetBeansSuite.php
 */
class NetBeansSuite extends PHPUnit_Framework_TestSuite {

    public static function suite() {
        $suite = new NetBeansSuite();
        foreach (self::rglob("*{T,t}est.php", dirname(__FILE__).DIRECTORY_SEPARATOR, GLOB_BRACE) as $file) {
            $suite->addTestFile($file);
        }
        return $suite;
    }

    /**
     * Recursive <a href="http://php.net/manual/en/function.glob.php">glob()</a>.
     * @param  string $pattern the pattern passed to glob(), default is "*"
     * @param  string $path    the path to scan, default is
     *                         <a href="http://php.net/manual/en/function.getcwd.php">the current working directory</a>
     * @param  int    $flags   the flags passed to glob()
     * @return array  an array of files in the given path matching the pattern.
     */
    private static function rglob($pattern = '*', $path = '', $flags = 0) {
        $paths = glob($path.'*', GLOB_MARK | GLOB_ONLYDIR | GLOB_NOSORT) or array();
        $files = glob($path.$pattern, $flags) or array();
        foreach ($paths as $path) {
            $files = array_merge($files, self::rglob($pattern, $path, $flags));
        }
        return $files;
    }
}

?>
