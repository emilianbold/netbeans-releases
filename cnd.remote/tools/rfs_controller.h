#ifndef _RFS_CONTROLLER_H
#define _RFS_CONTROLLER_H

static const int default_controller_port = 5555;

//enum rfs_request_kind {
//    ENSURE_SYNC
//};
//
//struct rfs_request {
//    enum rfs_request_kind kind;
//    char file_name[];
//};
//
//enum rfs_response_kind {
//    OK,
//    FAILURE
//};
//
//struct rgs_response {
//    enum rfs_response_kind kind;
//    int code;
//    char data[];
//};

enum response_kind {
    response_ok = '1',
    response_failure = '0'
};

#endif // _RFS_CONTROLLER_H
