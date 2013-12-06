#ifndef FS_SERVER_H
#define	FS_SERVER_H

#ifndef FS_COMMON_H
#error fs_common.h MUST be included before any other file
#endif

#include "util.h"

#ifdef	__cplusplus
extern "C" {
#endif

#if defined __CYGWIN__ && !defined WCONTINUED
    //added for compatibility with cygwin 1.5
#define WCONTINUED 0
#endif
    
enum fs_request_kind {
    FS_REQ_LS = 'l',
    FS_REQ_RECURSIVE_LS = 'r',
    FS_REQ_STAT = 'S',
    FS_REQ_LSTAT = 's',
    FS_REQ_CANCEL = 'c',
    FS_REQ_QUIT = 'q',
    FS_REQ_SLEEP = 'P',
    FS_REQ_ADD_WATCH = 'W',
    FS_REQ_REMOVE_WATCH = 'w'
};

enum fs_response_kind {
    FS_RSP_LS = 'l',
    FS_RSP_RECURSIVE_LS = 'r',
    FS_RSP_ENTRY = 'e',
    FS_RSP_END = 'x',
    FS_RSP_CHANGE = 'c',
    FS_RSP_ERROR = 'E'
};

typedef struct fs_request {
    int size;
    /** request kind */
    enum fs_request_kind kind : 8;
    /** request ID */
    int id;
    /** file name length */
    int len;
    /** zero-terminated absoulte file path */
    char path[];

} fs_request;

/**
 * Response is textual.
 * 
 * Responses can be single and multiple.
 * Single response describes one file (e.g. result of 'stat' request)
 * Multiple response describes list of files (e.g. result of 'ls' request)
 * 
 * First fields (kind, id and length) are space separated
 * All figures are in decimal human readable format (e.g. "234")
 * 
 * kind         response kind (1 char); for values, see see fs_response_kind
 *              1-character gap
 * id           the id of the request this response responds to
 *              1-character gap
 * count        number of elements in response
 *              1-character gap
 * length       file absolute path length
 *              1-character gap
 * abspath      absoulte path of the file this response refers to
 * 
 * LF           line feed character
 * 
 * Each response element has the following format:
 * 
 * file_type    1-character file type
 *              1-character gap
 * access       9-character access (e.g. "rwxr-xr-x")
 *              1-character gap
 * user         numeric user id
 *              1-character gap
 * group        numeric group id
 *              1-character gap
 * length       file size
 *              1-character gap
 * modified     modification time in milliseconds since 01/01/1970, 00:00:00 GMT
 *              1-character gap
 * name_len     filename length (short name - without dir)
 *              1-character gap
 * name         file name
 *              1-character gap
 * link         symbolic link
 * LF           line feed character
 * 
 * response format example
 * CORRECT: l 123 l my_link
 * INCORRECT: tmp rwxrwxrwx d 3 0 1380101169000 629500
 * ls -ln
 * drwxrwxrwx  79 0        3         632606 Sep 30 06:05 tmp
 * 
 * 
 * 
 */
typedef struct fs_response {
    /** request kind */
    enum fs_response_kind kind : 8;
    char data[];
} fs_response;


#ifdef	__cplusplus
}
#endif

#endif	/* FS_SERVER_H */

