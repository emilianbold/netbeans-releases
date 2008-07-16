<?php

// Start of SQLITE3 v.1.0

function sqlite3_libversion () {}

function sqlite3_open () {}

function sqlite3_close () {}

function sqlite3_error () {}

function sqlite3_exec () {}

function sqlite3_query () {}

function sqlite3_changes () {}

function sqlite3_bind_int () {}

function sqlite3_bind_double () {}

function sqlite3_bind_text () {}

function sqlite3_bind_blob () {}

function sqlite3_bind_null () {}

function sqlite3_query_exec () {}

function sqlite3_fetch () {}

function sqlite3_fetch_array () {}

function sqlite3_column_count () {}

function sqlite3_column_name () {}

function sqlite3_column_type () {}

function sqlite3_query_close () {}

function sqlite3_last_insert_rowid () {}

function sqlite3_create_function () {}


/**
 * Represents the SQLite3 INTEGER storage class.
 * @link http://php.net/manual/en/sqlite3.constants.php
 */
define ('SQLITE3_INTEGER', 1);

/**
 * Represents the SQLite3 REAL (FLOAT) storage class.
 * @link http://php.net/manual/en/sqlite3.constants.php
 */
define ('SQLITE3_FLOAT', 2);

/**
 * Represents the SQLite3 TEXT storage class.
 * @link http://php.net/manual/en/sqlite3.constants.php
 */
define ('SQLITE3_TEXT', 3);

/**
 * Represents the SQLite3 BLOB storage class.
 * @link http://php.net/manual/en/sqlite3.constants.php
 */
define ('SQLITE3_BLOB', 4);

/**
 * Represents the SQLite3 NULL storage class.
 * @link http://php.net/manual/en/sqlite3.constants.php
 */
define ('SQLITE3_NULL', 5);

// End of SQLITE3 v.1.0
?>
