<?php

// Start of snmp v.

/**
 * Fetch an SNMP object
 * @link http://php.net/manual/en/function.snmpget.php
 * @param hostname string <p>
 * The SNMP agent.
 * </p>
 * @param community string <p>
 * The read community.
 * </p>
 * @param object_id string <p>
 * The SNMP object.
 * </p>
 * @param timeout int[optional] <p>
 * </p>
 * @param retries int[optional] <p>
 * </p>
 * @return string SNMP object value on success or false on error.
 * </p>
 */
function snmpget ($hostname, $community, $object_id, $timeout = null, $retries = null) {}

/**
 * Fetch a SNMP object
 * @link http://php.net/manual/en/function.snmpgetnext.php
 * @param host string 
 * @param community string 
 * @param object_id string 
 * @param timeout int[optional] 
 * @param retries int[optional] 
 * @return string 
 */
function snmpgetnext ($host, $community, $object_id, $timeout = null, $retries = null) {}

/**
 * Fetch all the SNMP objects from an agent
 * @link http://php.net/manual/en/function.snmpwalk.php
 * @param hostname string <p>
 * The SNMP agent.
 * </p>
 * @param community string <p>
 * The read community.
 * </p>
 * @param object_id string <p>
 * If &null;, object_id is taken as the root of
 * the SNMP objects tree and all objects under that tree are returned as
 * an array. 
 * </p>
 * <p>
 * If object_id is specified, all the SNMP objects
 * below that object_id are returned.
 * </p>
 * @param timeout int[optional] <p>
 * </p>
 * @param retries int[optional] <p>
 * </p>
 * @return array an array of SNMP object values starting from the
 * object_id as root or false on error.
 * </p>
 */
function snmpwalk ($hostname, $community, $object_id, $timeout = null, $retries = null) {}

/**
 * Return all objects including their respective object ID within the specified one
 * @link http://php.net/manual/en/function.snmprealwalk.php
 * @param host string 
 * @param community string 
 * @param object_id string 
 * @param timeout int[optional] 
 * @param retries int[optional] 
 * @return array 
 */
function snmprealwalk ($host, $community, $object_id, $timeout = null, $retries = null) {}

/**
 * Query for a tree of information about a network entity
 * @link http://php.net/manual/en/function.snmpwalkoid.php
 * @param hostname string <p>
 * The SNMP agent.
 * </p>
 * @param community string <p>
 * The read community.
 * </p>
 * @param object_id string <p>
 * If &null;, object_id is taken as the root of
 * the SNMP objects tree and all objects under that tree are returned as
 * an array. 
 * </p>
 * <p>
 * If object_id is specified, all the SNMP objects
 * below that object_id are returned.
 * </p>
 * @param timeout int[optional] <p>
 * </p>
 * @param retries int[optional] <p>
 * </p>
 * @return array an associative array with object ids and their respective
 * object value starting from the object_id
 * as root or false on error.
 * </p>
 */
function snmpwalkoid ($hostname, $community, $object_id, $timeout = null, $retries = null) {}

/**
 * Fetches the current value of the UCD library's quick_print setting
 * @link http://php.net/manual/en/function.snmp-get-quick-print.php
 * @return bool true if quick_print is on, false otherwise.
 * </p>
 */
function snmp_get_quick_print () {}

/**
 * Set the value of quick_print within the UCD SNMP library
 * @link http://php.net/manual/en/function.snmp-set-quick-print.php
 * @param quick_print bool <p>
 * </p>
 * @return void &return.void;
 * </p>
 */
function snmp_set_quick_print ($quick_print) {}

/**
 * Return all values that are enums with their enum value instead of the raw integer
 * @link http://php.net/manual/en/function.snmp-set-enum-print.php
 * @param enum_print int 
 * @return void 
 */
function snmp_set_enum_print ($enum_print) {}

/**
 * Set the OID output format
 * @link http://php.net/manual/en/function.snmp-set-oid-output-format.php
 * @param oid_format int <p>
 * Set it to SNMP_OID_OUTPUT_FULL if you want a full
 * output, SNMP_OID_OUTPUT_NUMERIC otherwise.
 * </p>
 * @return void &return.void;
 * </p>
 */
function snmp_set_oid_output_format ($oid_format) {}

/**
 * Return all objects including their respective object id within the specified one
 * @link http://php.net/manual/en/function.snmp-set-oid-numeric-print.php
 * @param oid_numeric_print int 
 * @return void 
 */
function snmp_set_oid_numeric_print ($oid_numeric_print) {}

/**
 * Set an SNMP object
 * @link http://php.net/manual/en/function.snmpset.php
 * @param hostname string <p>
 * The SNMP agent.
 * </p>
 * @param community string <p>
 * The read community.
 * </p>
 * @param object_id string <p>
 * The SNMP object.
 * </p>
 * @param type string <p>
 * </p>
 * @param value mixed <p>
 * </p>
 * @param timeout int[optional] <p>
 * </p>
 * @param retries int[optional] <p>
 * </p>
 * @return bool &return.success;
 * </p>
 */
function snmpset ($hostname, $community, $object_id, $type, $value, $timeout = null, $retries = null) {}

function snmp2_get () {}

function snmp2_getnext () {}

function snmp2_walk () {}

function snmp2_real_walk () {}

function snmp2_set () {}

function snmp3_get () {}

function snmp3_getnext () {}

function snmp3_walk () {}

function snmp3_real_walk () {}

function snmp3_set () {}

/**
 * Specify the method how the SNMP values will be returned
 * @link http://php.net/manual/en/function.snmp-set-valueretrieval.php
 * @param method int 
 * @return void 
 */
function snmp_set_valueretrieval ($method) {}

/**
 * Return the method how the SNMP values will be returned
 * @link http://php.net/manual/en/function.snmp-get-valueretrieval.php
 * @return int 
 */
function snmp_get_valueretrieval () {}

/**
 * Reads and parses a MIB file into the active MIB tree
 * @link http://php.net/manual/en/function.snmp-read-mib.php
 * @param filename string 
 * @return bool 
 */
function snmp_read_mib ($filename) {}


/**
 * As of 5.2.0
 * @link http://php.net/manual/en/snmp.constants.php
 */
define ('SNMP_OID_OUTPUT_FULL', 3);

/**
 * As of 5.2.0
 * @link http://php.net/manual/en/snmp.constants.php
 */
define ('SNMP_OID_OUTPUT_NUMERIC', 4);
define ('SNMP_VALUE_LIBRARY', 0);
define ('SNMP_VALUE_PLAIN', 1);
define ('SNMP_VALUE_OBJECT', 2);
define ('SNMP_BIT_STR', 3);
define ('SNMP_OCTET_STR', 4);
define ('SNMP_OPAQUE', 68);
define ('SNMP_NULL', 5);
define ('SNMP_OBJECT_ID', 6);
define ('SNMP_IPADDRESS', 64);
define ('SNMP_COUNTER', 66);
define ('SNMP_UNSIGNED', 66);
define ('SNMP_TIMETICKS', 67);
define ('SNMP_UINTEGER', 71);
define ('SNMP_INTEGER', 2);
define ('SNMP_COUNTER64', 70);

// End of snmp v.
?>
