

struct addrinfo
{
    int ai_flags;			/* Input flags.  */
    struct addrinfo *ai_next;	/* Pointer to next in list.  */
};

struct sockaddr
{
    char sa_data[14];		/* Address data.  */
};
