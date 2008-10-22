

struct addrinfo
{
    int ai_flags;			/* Input flags.  */
    struct sockaddr *ai_addr;	/* Socket address for socket.  */
    char *ai_canonname;		/* Canonical name for service location.  */
    struct addrinfo *ai_next;	/* Pointer to next in list.  */
};

struct sockaddr
{
    char sa_data[14];		/* Address data.  */
};
