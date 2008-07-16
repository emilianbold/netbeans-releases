<?php

// Start of interbase v.

/**
 * Open a connection to an InterBase database
 * @link http://php.net/manual/en/function.ibase-connect.php
 * @param database string[optional] <p>
 * The database argument has to be a valid path to
 * database file on the server it resides on. If the server is not local,
 * it must be prefixed with either 'hostname:' (TCP/IP), '//hostname/'
 * (NetBEUI) or 'hostname@' (IPX/SPX), depending on the connection
 * protocol used. 
 * </p>
 * @param username string[optional] <p>
 * The user name. Can be set with the
 * ibase.default_user &php.ini; directive.
 * </p>
 * @param password string[optional] <p>
 * The password for username. Can be set with the
 * ibase.default_password &php.ini; directive.
 * </p>
 * @param charset string[optional] <p>
 * charset is the default character set for a
 * database.
 * </p>
 * @param buffers int[optional] <p>
 * buffers is the number of database buffers to
 * allocate for the server-side cache. If 0 or omitted, server chooses
 * its own default.
 * </p>
 * @param dialect int[optional] <p>
 * dialect selects the default SQL dialect for any
 * statement executed within a connection, and it defaults to the highest
 * one supported by client libraries. Functional only with InterBase 6
 * and up.
 * </p>
 * @param role string[optional] <p>
 * Functional only with InterBase 5 and up.
 * </p>
 * @param sync int[optional] <p>
 * </p>
 * @return resource an InterBase link identifier on success, or false on error.
 * </p>
 */
function ibase_connect ($database = null, $username = null, $password = null, $charset = null, $buffers = null, $dialect = null, $role = null, $sync = null) {}

/**
 * Open a persistent connection to an InterBase database
 * @link http://php.net/manual/en/function.ibase-pconnect.php
 * @param database string[optional] <p>
 * The database argument has to be a valid path to
 * database file on the server it resides on. If the server is not local,
 * it must be prefixed with either 'hostname:' (TCP/IP), '//hostname/'
 * (NetBEUI) or 'hostname@' (IPX/SPX), depending on the connection
 * protocol used. 
 * </p>
 * @param username string[optional] <p>
 * The user name. Can be set with the
 * ibase.default_user &php.ini; directive.
 * </p>
 * @param password string[optional] <p>
 * The password for username. Can be set with the
 * ibase.default_password &php.ini; directive.
 * </p>
 * @param charset string[optional] <p>
 * charset is the default character set for a
 * database.
 * </p>
 * @param buffers int[optional] <p>
 * buffers is the number of database buffers to
 * allocate for the server-side cache. If 0 or omitted, server chooses
 * its own default.
 * </p>
 * @param dialect int[optional] <p>
 * dialect selects the default SQL dialect for any
 * statement executed within a connection, and it defaults to the highest
 * one supported by client libraries. Functional only with InterBase 6
 * and up.
 * </p>
 * @param role string[optional] <p>
 * Functional only with InterBase 5 and up.
 * </p>
 * @param sync int[optional] <p>
 * </p>
 * @return resource an InterBase link identifier on success, or false on error.
 * </p>
 */
function ibase_pconnect ($database = null, $username = null, $password = null, $charset = null, $buffers = null, $dialect = null, $role = null, $sync = null) {}

/**
 * Close a connection to an InterBase database
 * @link http://php.net/manual/en/function.ibase-close.php
 * @param connection_id resource[optional] <p>
 * An InterBase link identifier returned from
 * ibase_connect. If omitted, the last opened link
 * is assumed.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function ibase_close ($connection_id = null) {}

/**
 * Drops a database
 * @link http://php.net/manual/en/function.ibase-drop-db.php
 * @param connection resource[optional] <p>
 * An InterBase link identifier. If omitted, the last opened link is
 * assumed.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function ibase_drop_db ($connection = null) {}

/**
 * Execute a query on an InterBase database
 * @link http://php.net/manual/en/function.ibase-query.php
 * @param link_identifier resource[optional] <p>
 * An InterBase link identifier. If omitted, the last opened link is
 * assumed.
 * </p>
 * @param query string <p>
 * An InterBase query.
 * </p>
 * @param bind_args int[optional] <p>
 * </p>
 * @return resource If the query raises an error, returns false. If it is successful and
 * there is a (possibly empty) result set (such as with a SELECT query),
 * returns a result identifier. If the query was successful and there were
 * no results, returns true.
 * </p>
 * <p>
 * In PHP 5.0.0 and up, this function will return the number of rows
 * affected by the query for INSERT, UPDATE and DELETE statements. In order
 * to retain backward compatibility, it will return true for these
 * statements if the query succeeded without affecting any rows.
 * </p>
 */
function ibase_query ($link_identifier = null, $query, $bind_args = null) {}

/**
 * Fetch a row from an InterBase database
 * @link http://php.net/manual/en/function.ibase-fetch-row.php
 * @param result_identifier resource <p>
 * An InterBase result identifier.
 * </p>
 * @param fetch_flag int[optional] <p>
 * fetch_flag is a combination of the constants
 * IBASE_TEXT and IBASE_UNIXTIME
 * ORed together. Passing IBASE_TEXT will cause this
 * function to return BLOB contents instead of BLOB ids. Passing
 * IBASE_UNIXTIME will cause this function to return
 * date/time values as Unix timestamps instead of as formatted strings.
 * </p>
 * @return array an array that corresponds to the fetched row, or false if there
 * are no more rows. Each result column is stored in an array offset,
 * starting at offset 0.
 * </p>
 */
function ibase_fetch_row ($result_identifier, $fetch_flag = null) {}

/**
 * Fetch a result row from a query as an associative array
 * @link http://php.net/manual/en/function.ibase-fetch-assoc.php
 * @param result resource <p>
 * The result handle.
 * </p>
 * @param fetch_flag int[optional] <p>
 * fetch_flag is a combination of the constants
 * IBASE_TEXT and IBASE_UNIXTIME
 * ORed together. Passing IBASE_TEXT will cause this
 * function to return BLOB contents instead of BLOB ids. Passing
 * IBASE_UNIXTIME will cause this function to return
 * date/time values as Unix timestamps instead of as formatted strings.
 * </p>
 * @return array an associative array that corresponds to the fetched row.
 * Subsequent calls will return the next row in the result set, or false if
 * there are no more rows.
 * </p>
 */
function ibase_fetch_assoc ($result, $fetch_flag = null) {}

/**
 * Get an object from a InterBase database
 * @link http://php.net/manual/en/function.ibase-fetch-object.php
 * @param result_id resource <p>
 * An InterBase result identifier obtained either by
 * ibase_query or ibase_execute.
 * </p>
 * @param fetch_flag int[optional] <p>
 * fetch_flag is a combination of the constants
 * IBASE_TEXT and IBASE_UNIXTIME
 * ORed together. Passing IBASE_TEXT will cause this
 * function to return BLOB contents instead of BLOB ids. Passing
 * IBASE_UNIXTIME will cause this function to return
 * date/time values as Unix timestamps instead of as formatted strings.
 * </p>
 * @return object an object with the next row information, or false if there are
 * no more rows.
 * </p>
 */
function ibase_fetch_object ($result_id, $fetch_flag = null) {}

/**
 * Free a result set
 * @link http://php.net/manual/en/function.ibase-free-result.php
 * @param result_identifier resource <p>
 * A result set created by ibase_query or
 * ibase_execute.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function ibase_free_result ($result_identifier) {}

/**
 * Assigns a name to a result set
 * @link http://php.net/manual/en/function.ibase-name-result.php
 * @param result resource <p>
 * An InterBase result set.
 * </p>
 * @param name string <p>
 * The name to be assigned.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function ibase_name_result ($result, $name) {}

/**
 * Prepare a query for later binding of parameter placeholders and execution
 * @link http://php.net/manual/en/function.ibase-prepare.php
 * @param query string <p>
 * An InterBase query.
 * </p>
 * @return resource a prepared query handle, or false on error.
 * </p>
 */
function ibase_prepare ($query) {}

/**
 * Execute a previously prepared query
 * @link http://php.net/manual/en/function.ibase-execute.php
 * @param query resource <p>
 * An InterBase query prepared by ibase_prepare.
 * </p>
 * @param bind_arg mixed[optional] <p>
 * </p>
 * @param _ mixed[optional] 
 * @return resource If the query raises an error, returns false. If it is successful and
 * there is a (possibly empty) result set (such as with a SELECT query),
 * returns a result identifier. If the query was successful and there were
 * no results, returns true.
 * </p>
 * <p>
 * In PHP 5.0.0 and up, this function returns the number of rows affected by
 * the query (if > 0 and applicable to the statement type). A query that
 * succeeded, but did not affect any rows (e.g. an UPDATE of a non-existent
 * record) will return true.
 * </p>
 */
function ibase_execute ($query, $bind_arg = null, $_ = null) {}

/**
 * Free memory allocated by a prepared query
 * @link http://php.net/manual/en/function.ibase-free-query.php
 * @param query resource <p>
 * A query prepared with ibase_prepare.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function ibase_free_query ($query) {}

/**
 * Increments the named generator and returns its new value
 * @link http://php.net/manual/en/function.ibase-gen-id.php
 * @param generator string 
 * @param increment int[optional] 
 * @param link_identifier resource[optional] 
 * @return mixed new generator value as integer, or as string if the value is too big.
 * </p>
 */
function ibase_gen_id ($generator, $increment = null, $link_identifier = null) {}

/**
 * Get the number of fields in a result set
 * @link http://php.net/manual/en/function.ibase-num-fields.php
 * @param result_id resource <p>
 * An InterBase result identifier.
 * </p>
 * @return int the number of fields as an integer.
 * </p>
 */
function ibase_num_fields ($result_id) {}

/**
 * Return the number of parameters in a prepared query
 * @link http://php.net/manual/en/function.ibase-num-params.php
 * @param query resource <p>
 * The prepared query handle.
 * </p>
 * @return int the number of parameters as an integer.
 * </p>
 */
function ibase_num_params ($query) {}

/**
 * Return the number of rows that were affected by the previous query
 * @link http://php.net/manual/en/function.ibase-affected-rows.php
 * @param link_identifier resource[optional] <p>
 * A transaction context. If link_identifier is a
 * connection resource, its default transaction is used.
 * </p>
 * @return int the number of rows as an integer.
 * </p>
 */
function ibase_affected_rows ($link_identifier = null) {}

/**
 * Get information about a field
 * @link http://php.net/manual/en/function.ibase-field-info.php
 * @param result resource <p>
 * An InterBase result identifier.
 * </p>
 * @param field_number int <p>
 * Field offset.
 * </p>
 * @return array an array with the following keys: name,
 * alias, relation,
 * length and type.
 * </p>
 */
function ibase_field_info ($result, $field_number) {}

/**
 * Return information about a parameter in a prepared query
 * @link http://php.net/manual/en/function.ibase-param-info.php
 * @param query resource <p>
 * An InterBase prepared query handle.
 * </p>
 * @param param_number int <p>
 * Parameter offset.
 * </p>
 * @return array an array with the following keys: name,
 * alias, relation,
 * length and type.
 * </p>
 */
function ibase_param_info ($query, $param_number) {}

/**
 * Begin a transaction
 * @link http://php.net/manual/en/function.ibase-trans.php
 * @param trans_args int[optional] <p>
 * trans_args can be a combination of
 * IBASE_READ,
 * IBASE_WRITE,
 * IBASE_COMMITTED, 
 * IBASE_CONSISTENCY,
 * IBASE_CONCURRENCY, 
 * IBASE_REC_VERSION, 
 * IBASE_REC_NO_VERSION,
 * IBASE_WAIT and 
 * IBASE_NOWAIT.
 * </p>
 * @param link_identifier resource[optional] <p>
 * An InterBase link identifier. If omitted, the last opened link is
 * assumed.
 * </p>
 * @return resource a transaction handle, or false on error.
 * </p>
 */
function ibase_trans ($trans_args = null, $link_identifier = null) {}

/**
 * Commit a transaction
 * @link http://php.net/manual/en/function.ibase-commit.php
 * @param link_or_trans_identifier resource[optional] <p>
 * If called without an argument, this function commits the default
 * transaction of the default link. If the argument is a connection
 * identifier, the default transaction of the corresponding connection
 * will be committed. If the argument is a transaction identifier, the
 * corresponding transaction will be committed.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function ibase_commit ($link_or_trans_identifier = null) {}

/**
 * Roll back a transaction
 * @link http://php.net/manual/en/function.ibase-rollback.php
 * @param link_or_trans_identifier resource[optional] <p>
 * If called without an argument, this function rolls back the default
 * transaction of the default link. If the argument is a connection
 * identifier, the default transaction of the corresponding connection
 * will be rolled back. If the argument is a transaction identifier, the
 * corresponding transaction will be rolled back.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function ibase_rollback ($link_or_trans_identifier = null) {}

/**
 * Commit a transaction without closing it
 * @link http://php.net/manual/en/function.ibase-commit-ret.php
 * @param link_or_trans_identifier resource[optional] <p>
 * If called without an argument, this function commits the default
 * transaction of the default link. If the argument is a connection
 * identifier, the default transaction of the corresponding connection
 * will be committed. If the argument is a transaction identifier, the
 * corresponding transaction will be committed. The transaction context
 * will be retained, so statements executed from within this transaction
 * will not be invalidated.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function ibase_commit_ret ($link_or_trans_identifier = null) {}

/**
 * Roll back a transaction without closing it
 * @link http://php.net/manual/en/function.ibase-rollback-ret.php
 * @param link_or_trans_identifier resource[optional] <p>
 * If called without an argument, this function rolls back the default
 * transaction of the default link. If the argument is a connection
 * identifier, the default transaction of the corresponding connection
 * will be rolled back. If the argument is a transaction identifier, the
 * corresponding transaction will be rolled back. The transaction context
 * will be retained, so statements executed from within this transaction
 * will not be invalidated.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function ibase_rollback_ret ($link_or_trans_identifier = null) {}

/**
 * Return blob length and other useful info
 * @link http://php.net/manual/en/function.ibase-blob-info.php
 * @param link_identifier resource <p>
 * An InterBase link identifier. If omitted, the last opened link is
 * assumed.
 * </p>
 * @param blob_id string <p>
 * A BLOB id.
 * </p>
 * @return array an array containing information about a BLOB. The information returned
 * consists of the length of the BLOB, the number of segments it contains, the size
 * of the largest segment, and whether it is a stream BLOB or a segmented BLOB.
 * </p>
 */
function ibase_blob_info ($link_identifier, $blob_id) {}

/**
 * Create a new blob for adding data
 * @link http://php.net/manual/en/function.ibase-blob-create.php
 * @param link_identifier resource[optional] <p>
 * An InterBase link identifier. If omitted, the last opened link is
 * assumed.
 * </p>
 * @return resource a BLOB handle for later use with
 * ibase_blob_add or false on failure.
 * </p>
 */
function ibase_blob_create ($link_identifier = null) {}

/**
 * Add data into a newly created blob
 * @link http://php.net/manual/en/function.ibase-blob-add.php
 * @param blob_handle resource <p>
 * A blob handle opened with ibase_blob_create.
 * </p>
 * @param data string <p>
 * The data to be added.
 * </p>
 * @return void &return.void;
 * </p>
 */
function ibase_blob_add ($blob_handle, $data) {}

/**
 * Cancel creating blob
 * @link http://php.net/manual/en/function.ibase-blob-cancel.php
 * @param blob_handle resource <p>
 * A BLOB handle opened with ibase_create_blob.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function ibase_blob_cancel ($blob_handle) {}

/**
 * Close blob
 * @link http://php.net/manual/en/function.ibase-blob-close.php
 * @param blob_handle resource <p>
 * A BLOB handle opened with ibase_create_blob or
 * ibase_open_blob.
 * </p>
 * @return mixed If the BLOB was being read, this function returns true on success, if
 * the BLOB was being written to, this function returns a string containing
 * the BLOB id that has been assigned to it by the database. On failure, this
 * function returns false.
 * </p>
 */
function ibase_blob_close ($blob_handle) {}

/**
 * Open blob for retrieving data parts
 * @link http://php.net/manual/en/function.ibase-blob-open.php
 * @param link_identifier resource <p>
 * An InterBase link identifier. If omitted, the last opened link is
 * assumed.
 * </p>
 * @param blob_id string <p>
 * A BLOB id.
 * </p>
 * @return resource a BLOB handle for later use with 
 * ibase_blob_get or false on failure.
 * </p>
 */
function ibase_blob_open ($link_identifier, $blob_id) {}

/**
 * Get len bytes data from open blob
 * @link http://php.net/manual/en/function.ibase-blob-get.php
 * @param blob_handle resource <p>
 * A BLOB handle opened with ibase_blob_open.
 * </p>
 * @param len int <p>
 * Size of returned data.
 * </p>
 * @return string at most len bytes from the BLOB, or false
 * on failure.
 * </p>
 */
function ibase_blob_get ($blob_handle, $len) {}

/**
 * Output blob contents to browser
 * @link http://php.net/manual/en/function.ibase-blob-echo.php
 * @param link_identifier resource[optional] <p>
 * An InterBase link identifier. If omitted, the last opened link is
 * assumed.
 * </p>
 * @param blob_id string <p>
 * </p>
 * @return bool &return.success;
 * </p>
 */
function ibase_blob_echo ($link_identifier = null, $blob_id) {}

/**
 * Create blob, copy file in it, and close it
 * @link http://php.net/manual/en/function.ibase-blob-import.php
 * @param link_identifier resource <p>
 * An InterBase link identifier. If omitted, the last opened link is
 * assumed.
 * </p>
 * @param file_handle resource <p>
 * The file handle is a handle returned by fopen.
 * </p>
 * @return string the BLOB id on success, or false on error.
 * </p>
 */
function ibase_blob_import ($link_identifier, $file_handle) {}

/**
 * Return error messages
 * @link http://php.net/manual/en/function.ibase-errmsg.php
 * @return string the error message as a string, or false if no error occured.
 * </p>
 */
function ibase_errmsg () {}

/**
 * Return an error code
 * @link http://php.net/manual/en/function.ibase-errcode.php
 * @return int the error code as an integer, or false if no error occured.
 * </p>
 */
function ibase_errcode () {}

/**
 * Add a user to a security database (only for IB6 or later)
 * @link http://php.net/manual/en/function.ibase-add-user.php
 * @param service_handle resource 
 * @param user_name string 
 * @param password string 
 * @param first_name string[optional] 
 * @param middle_name string[optional] 
 * @param last_name string[optional] 
 * @return bool &return.success;
 * </p>
 */
function ibase_add_user ($service_handle, $user_name, $password, $first_name = null, $middle_name = null, $last_name = null) {}

/**
 * Modify a user to a security database (only for IB6 or later)
 * @link http://php.net/manual/en/function.ibase-modify-user.php
 * @param service_handle resource 
 * @param user_name string 
 * @param password string 
 * @param first_name string[optional] 
 * @param middle_name string[optional] 
 * @param last_name string[optional] 
 * @return bool &return.success;
 * </p>
 */
function ibase_modify_user ($service_handle, $user_name, $password, $first_name = null, $middle_name = null, $last_name = null) {}

/**
 * Delete a user from a security database (only for IB6 or later)
 * @link http://php.net/manual/en/function.ibase-delete-user.php
 * @param service_handle resource 
 * @param user_name string 
 * @return bool &return.success;
 * </p>
 */
function ibase_delete_user ($service_handle, $user_name) {}

/**
 * Connect to the service manager
 * @link http://php.net/manual/en/function.ibase-service-attach.php
 * @param host string 
 * @param dba_username string 
 * @param dba_password string 
 * @return resource 
 */
function ibase_service_attach ($host, $dba_username, $dba_password) {}

/**
 * Disconnect from the service manager
 * @link http://php.net/manual/en/function.ibase-service-detach.php
 * @param service_handle resource 
 * @return bool &return.success;
 * </p>
 */
function ibase_service_detach ($service_handle) {}

/**
 * Initiates a backup task in the service manager and returns immediately
 * @link http://php.net/manual/en/function.ibase-backup.php
 * @param service_handle resource 
 * @param source_db string 
 * @param dest_file string 
 * @param options int[optional] 
 * @param verbose bool[optional] 
 * @return mixed 
 */
function ibase_backup ($service_handle, $source_db, $dest_file, $options = null, $verbose = null) {}

/**
 * Initiates a restore task in the service manager and returns immediately
 * @link http://php.net/manual/en/function.ibase-restore.php
 * @param service_handle resource 
 * @param source_file string 
 * @param dest_db string 
 * @param options int[optional] 
 * @param verbose bool[optional] 
 * @return mixed 
 */
function ibase_restore ($service_handle, $source_file, $dest_db, $options = null, $verbose = null) {}

/**
 * Execute a maintenance command on the database server
 * @link http://php.net/manual/en/function.ibase-maintain-db.php
 * @param service_handle resource 
 * @param db string 
 * @param action int 
 * @param argument int[optional] 
 * @return bool &return.success;
 * </p>
 */
function ibase_maintain_db ($service_handle, $db, $action, $argument = null) {}

/**
 * Request statistics about a database
 * @link http://php.net/manual/en/function.ibase-db-info.php
 * @param service_handle resource 
 * @param db string 
 * @param action int 
 * @param argument int[optional] 
 * @return string 
 */
function ibase_db_info ($service_handle, $db, $action, $argument = null) {}

/**
 * Request information about a database server
 * @link http://php.net/manual/en/function.ibase-server-info.php
 * @param service_handle resource 
 * @param action int 
 * @return string 
 */
function ibase_server_info ($service_handle, $action) {}

/**
 * Wait for an event to be posted by the database
 * @link http://php.net/manual/en/function.ibase-wait-event.php
 * @param event_name1 string <p>
 * The event name.
 * </p>
 * @param event_name2 string[optional] <p>
 * </p>
 * @param _ string[optional] 
 * @return string the name of the event that was posted.
 * </p>
 */
function ibase_wait_event ($event_name1, $event_name2 = null, $_ = null) {}

/**
 * Register a callback function to be called when events are posted
 * @link http://php.net/manual/en/function.ibase-set-event-handler.php
 * @param event_handler callback <p>
 * The callback is called with the event name and the link resource as
 * arguments whenever one of the specified events is posted by the
 * database.
 * </p>
 * <p>
 * The callback must return false if the event handler should be
 * canceled. Any other return value is ignored. This function accepts up
 * to 15 event arguments.
 * </p>
 * @param event_name1 string <p>
 * An event name.
 * </p>
 * @param event_name2 string[optional] <p>
 * </p>
 * @param _ string[optional] 
 * @return resource The return value is an event resource. This resource can be used to free
 * the event handler using ibase_free_event_handler.
 * </p>
 */
function ibase_set_event_handler ($event_handler, $event_name1, $event_name2 = null, $_ = null) {}

/**
 * Cancels a registered event handler
 * @link http://php.net/manual/en/function.ibase-free-event-handler.php
 * @param event resource <p>
 * An event resource, created by
 * ibase_set_event_handler.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function ibase_free_event_handler ($event) {}

function fbird_connect () {}

function fbird_pconnect () {}

function fbird_close () {}

function fbird_drop_db () {}

function fbird_query () {}

function fbird_fetch_row () {}

function fbird_fetch_assoc () {}

function fbird_fetch_object () {}

function fbird_free_result () {}

function fbird_name_result () {}

function fbird_prepare () {}

function fbird_execute () {}

function fbird_free_query () {}

function fbird_gen_id () {}

function fbird_num_fields () {}

function fbird_num_params () {}

function fbird_affected_rows () {}

function fbird_field_info () {}

function fbird_param_info () {}

function fbird_trans () {}

function fbird_commit () {}

function fbird_rollback () {}

function fbird_commit_ret () {}

function fbird_rollback_ret () {}

function fbird_blob_info () {}

function fbird_blob_create () {}

function fbird_blob_add () {}

function fbird_blob_cancel () {}

function fbird_blob_close () {}

function fbird_blob_open () {}

function fbird_blob_get () {}

function fbird_blob_echo () {}

function fbird_blob_import () {}

function fbird_errmsg () {}

function fbird_errcode () {}

function fbird_add_user () {}

function fbird_modify_user () {}

function fbird_delete_user () {}

function fbird_service_attach () {}

function fbird_service_detach () {}

function fbird_backup () {}

function fbird_restore () {}

function fbird_maintain_db () {}

function fbird_db_info () {}

function fbird_server_info () {}

function fbird_wait_event () {}

function fbird_set_event_handler () {}

function fbird_free_event_handler () {}

define ('IBASE_DEFAULT', 0);
define ('IBASE_CREATE', 0);
define ('IBASE_TEXT', 1);
define ('IBASE_FETCH_BLOBS', 1);
define ('IBASE_FETCH_ARRAYS', 2);
define ('IBASE_UNIXTIME', 4);
define ('IBASE_WRITE', 1);
define ('IBASE_READ', 2);
define ('IBASE_COMMITTED', 8);
define ('IBASE_CONSISTENCY', 16);
define ('IBASE_CONCURRENCY', 4);
define ('IBASE_REC_VERSION', 64);
define ('IBASE_REC_NO_VERSION', 32);
define ('IBASE_NOWAIT', 256);
define ('IBASE_WAIT', 128);
define ('IBASE_BKP_IGNORE_CHECKSUMS', 1);
define ('IBASE_BKP_IGNORE_LIMBO', 2);
define ('IBASE_BKP_METADATA_ONLY', 4);
define ('IBASE_BKP_NO_GARBAGE_COLLECT', 8);
define ('IBASE_BKP_OLD_DESCRIPTIONS', 16);
define ('IBASE_BKP_NON_TRANSPORTABLE', 32);

/**
 * Options to ibase_backup
 * @link http://php.net/manual/en/ibase.constants.php
 */
define ('IBASE_BKP_CONVERT', 64);
define ('IBASE_RES_DEACTIVATE_IDX', 256);
define ('IBASE_RES_NO_SHADOW', 512);
define ('IBASE_RES_NO_VALIDITY', 1024);
define ('IBASE_RES_ONE_AT_A_TIME', 2048);
define ('IBASE_RES_REPLACE', 4096);
define ('IBASE_RES_CREATE', 8192);

/**
 * Options to ibase_restore
 * @link http://php.net/manual/en/ibase.constants.php
 */
define ('IBASE_RES_USE_ALL_SPACE', 16384);
define ('IBASE_PRP_PAGE_BUFFERS', 5);
define ('IBASE_PRP_SWEEP_INTERVAL', 6);
define ('IBASE_PRP_SHUTDOWN_DB', 7);
define ('IBASE_PRP_DENY_NEW_TRANSACTIONS', 10);
define ('IBASE_PRP_DENY_NEW_ATTACHMENTS', 9);
define ('IBASE_PRP_RESERVE_SPACE', 11);
define ('IBASE_PRP_RES_USE_FULL', 35);
define ('IBASE_PRP_RES', 36);
define ('IBASE_PRP_WRITE_MODE', 12);
define ('IBASE_PRP_WM_ASYNC', 37);
define ('IBASE_PRP_WM_SYNC', 38);
define ('IBASE_PRP_ACCESS_MODE', 13);
define ('IBASE_PRP_AM_READONLY', 39);
define ('IBASE_PRP_AM_READWRITE', 40);
define ('IBASE_PRP_SET_SQL_DIALECT', 14);
define ('IBASE_PRP_ACTIVATE', 256);
define ('IBASE_PRP_DB_ONLINE', 512);
define ('IBASE_RPR_CHECK_DB', 16);
define ('IBASE_RPR_IGNORE_CHECKSUM', 32);
define ('IBASE_RPR_KILL_SHADOWS', 64);
define ('IBASE_RPR_MEND_DB', 4);
define ('IBASE_RPR_VALIDATE_DB', 1);
define ('IBASE_RPR_FULL', 128);

/**
 * Options to ibase_maintain_db
 * @link http://php.net/manual/en/ibase.constants.php
 */
define ('IBASE_RPR_SWEEP_DB', 2);
define ('IBASE_STS_DATA_PAGES', 1);
define ('IBASE_STS_DB_LOG', 2);
define ('IBASE_STS_HDR_PAGES', 4);
define ('IBASE_STS_IDX_PAGES', 8);

/**
 * Options to ibase_db_info
 * @link http://php.net/manual/en/ibase.constants.php
 */
define ('IBASE_STS_SYS_RELATIONS', 16);
define ('IBASE_SVC_SERVER_VERSION', 55);
define ('IBASE_SVC_IMPLEMENTATION', 56);
define ('IBASE_SVC_GET_ENV', 59);
define ('IBASE_SVC_GET_ENV_LOCK', 60);
define ('IBASE_SVC_GET_ENV_MSG', 61);
define ('IBASE_SVC_USER_DBPATH', 58);
define ('IBASE_SVC_SVR_DB_INFO', 50);

/**
 * Options to ibase_server_info
 * @link http://php.net/manual/en/ibase.constants.php
 */
define ('IBASE_SVC_GET_USERS', 68);

// End of interbase v.
?>
