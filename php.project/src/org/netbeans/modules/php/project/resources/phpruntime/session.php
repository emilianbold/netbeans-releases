<?php

// Start of session v.

/**
 * Get and/or set the current session name
 * @link http://php.net/manual/en/function.session-name.php
 * @param name string[optional] <p>
 * The session name references the session id in cookies and URLs. It
 * should contain only alphanumeric characters; it should be short and
 * descriptive (i.e. for users with enabled cookie warnings).
 * If name is specified, the name of the current
 * session is changed to its value.
 * </p>
 * <p>
 * <p>
 * The session name can't consist of digits only, at least one letter
 * must be present. Otherwise a new session id is generated every time.
 * </p>
 * </p>
 * @return string the name of the current session.
 * </p>
 */
function session_name ($name = null) {}

/**
 * Get and/or set the current session module
 * @link http://php.net/manual/en/function.session-module-name.php
 * @param module string[optional] <p>
 * If module is specified, that module will be
 * used instead.
 * </p>
 * @return string the name of the current session module.
 * </p>
 */
function session_module_name ($module = null) {}

/**
 * Get and/or set the current session save path
 * @link http://php.net/manual/en/function.session-save-path.php
 * @param path string[optional] <p>
 * Session data path. If specified, the path to which data is saved will
 * be changed. session_save_path needs to be called
 * before session_start for that purpose.
 * </p>
 * <p>
 * <p>
 * On some operating systems, you may want to specify a path on a
 * filesystem that handles lots of small files efficiently. For example,
 * on Linux, reiserfs may provide better performance than ext2fs.
 * </p>
 * </p>
 * @return string the path of the current directory used for data storage.
 * </p>
 */
function session_save_path ($path = null) {}

/**
 * Get and/or set the current session id
 * @link http://php.net/manual/en/function.session-id.php
 * @param id string[optional] <p>
 * If id is specified, it will replace the current
 * session id. session_id needs to be called before
 * session_start for that purpose. Depending on the
 * session handler, not all characters are allowed within the session id.
 * For example, the file session handler only allows characters in the
 * range a-z, A-Z and 0-9!
 * </p>
 * When using session cookies, specifying an id
 * for session_id will always send a new cookie
 * when session_start is called, regardless if the
 * current session id is identical to the one being set.
 * @return string session_id returns the session id for the current
 * session or the empty string ("") if there is no current
 * session (no current session id exists).
 * </p>
 */
function session_id ($id = null) {}

/**
 * Update the current session id with a newly generated one
 * @link http://php.net/manual/en/function.session-regenerate-id.php
 * @param delete_old_session bool[optional] <p>
 * Whether to delete the old associated session file or not. Defaults to
 * false.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function session_regenerate_id ($delete_old_session = null) {}

/**
 * Decodes session data from a string
 * @link http://php.net/manual/en/function.session-decode.php
 * @param data string <p>
 * The encoded data to be stored.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function session_decode ($data) {}

/**
 * Register one or more global variables with the current session
 * @link http://php.net/manual/en/function.session-register.php
 * @param name mixed <p>
 * A string holding the name of a variable or an array consisting of
 * variable names or other arrays.
 * </p>
 * @param _ mixed[optional] 
 * @return bool &return.success;
 * </p>
 */
function session_register ($name, $_ = null) {}

/**
 * Unregister a global variable from the current session
 * @link http://php.net/manual/en/function.session-unregister.php
 * @param name string <p>
 * The variable name.
 * </p>
 * @return bool &return.success;
 * </p>
 */
function session_unregister ($name) {}

/**
 * Find out whether a global variable is registered in a session
 * @link http://php.net/manual/en/function.session-is-registered.php
 * @param name string <p>
 * The variable name.
 * </p>
 * @return bool session_is_registered returns true if there is a
 * global variable with the name name registered in
 * the current session, false otherwise.
 * </p>
 */
function session_is_registered ($name) {}

/**
 * Encodes the current session data as a string
 * @link http://php.net/manual/en/function.session-encode.php
 * @return string the contents of the current session encoded.
 * </p>
 */
function session_encode () {}

/**
 * Initialize session data
 * @link http://php.net/manual/en/function.session-start.php
 * @return bool This function always returns true.
 * </p>
 */
function session_start () {}

/**
 * Destroys all data registered to a session
 * @link http://php.net/manual/en/function.session-destroy.php
 * @return bool &return.success;
 * </p>
 */
function session_destroy () {}

/**
 * Free all session variables
 * @link http://php.net/manual/en/function.session-unset.php
 * @return void &return.void;
 * </p>
 */
function session_unset () {}

/**
 * Sets user-level session storage functions
 * @link http://php.net/manual/en/function.session-set-save-handler.php
 * @param open callback <p>
 * </p>
 * @param close callback <p>
 * </p>
 * @param read callback <p>
 * Read function must return string value always to make save handler
 * work as expected. Return empty string if there is no data to read.
 * Return values from other handlers are converted to boolean expression.
 * true for success, false for failure.
 * </p>
 * @param write callback <p>
 * <p>
 * The "write" handler is not executed until after the output stream is
 * closed. Thus, output from debugging statements in the "write"
 * handler will never be seen in the browser. If debugging output is
 * necessary, it is suggested that the debug output be written to a
 * file instead.
 * </p>
 * </p>
 * @param destroy callback <p>
 * </p>
 * @param gc callback <p>
 * </p>
 * @return bool &return.success;
 * </p>
 */
function session_set_save_handler ($open, $close, $read, $write, $destroy, $gc) {}

/**
 * Get and/or set the current cache limiter
 * @link http://php.net/manual/en/function.session-cache-limiter.php
 * @param cache_limiter string[optional] <p>
 * If cache_limiter is specified, the name of the
 * current cache limiter is changed to the new value.
 * </p>
 * @return string the name of the current cache limiter. 
 * </p>
 */
function session_cache_limiter ($cache_limiter = null) {}

/**
 * Return current cache expire
 * @link http://php.net/manual/en/function.session-cache-expire.php
 * @param new_cache_expire int[optional] <p>
 * If new_cache_expire is given, the current cache
 * expire is replaced with new_cache_expire.
 * </p>
 * <p>
 * Setting new_cache_expire is of value only, if
 * session.cache_limiter is set to a value
 * different from nocache.
 * </p>
 * @return int the current setting of session.cache_expire.
 * The value returned should be read in minutes, defaults to 180. 
 * </p>
 */
function session_cache_expire ($new_cache_expire = null) {}

/**
 * Set the session cookie parameters
 * @link http://php.net/manual/en/function.session-set-cookie-params.php
 * @param lifetime int <p>
 * </p>
 * @param path string[optional] <p>
 * </p>
 * @param domain string[optional] <p>
 * </p>
 * @param secure bool[optional] <p>
 * </p>
 * @param httponly bool[optional] <p>
 * </p>
 * @return void &return.void;
 * </p>
 */
function session_set_cookie_params ($lifetime, $path = null, $domain = null, $secure = null, $httponly = null) {}

/**
 * Get the session cookie parameters
 * @link http://php.net/manual/en/function.session-get-cookie-params.php
 * @return array an array with the current session cookie information, the array
 * contains the following items:
 * "lifetime" - The lifetime of the cookie in seconds.
 * "path" - The path where information is stored.
 * "domain" - The domain of the cookie.
 * "secure" - The cookie should only be sent over secure connections.
 * "httponly" - The cookie can only be accessed through the HTTP protocol.
 * </p>
 */
function session_get_cookie_params () {}

/**
 * Write session data and end session
 * @link http://php.net/manual/en/function.session-write-close.php
 * @return void &return.void;
 * </p>
 */
function session_write_close () {}

/**
 * &Alias; <function>session_write_close</function>
 * @link http://php.net/manual/en/function.session-commit.php
 */
function session_commit () {}

// End of session v.
?>
