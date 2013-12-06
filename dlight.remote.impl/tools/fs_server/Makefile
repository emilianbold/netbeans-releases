MAKEFILE = $(shell echo gmake)
MAKEFILE:sh = echo make
ROOT_DIR = $(shell pwd)
ROOT_DIR:sh = pwd
OS = $(shell uname -s)
OS:sh = uname -s

ifeq (Darwin,$(OS))
  OS = MacOSX
endif

ifdef 64BITS
    CONF=$(OS)-x86_64
else
    CONF=$(OS)-x86
endif


PNAME=fs_server

CC_COMMON=gcc
CC_Linux-x86      = $(CC_COMMON)
CC_Linux-x86_64   = $(CC_COMMON)
CC_MacOSX-x86     = clang 
CC_MacOSX-x86_64  = clang 
CC_SunOS-sparc    = $(CC_COMMON)
CC_SunOS-sparc_64 = $(CC_COMMON)
CC_SunOS-x86      = $(CC_COMMON)
CC_SunOS-x86_64   = $(CC_COMMON)
CC_Windows-x86    = $(CC_COMMON)
CC_Windows-x86_64 = $(CC_COMMON)
CC_FreeBSD-x86    = $(CC_COMMON)
CC_FreeBSD-x86_64 = $(CC_COMMON)
CC=$(CC_$(CONF))

ifdef PARFAIT
   CC=parfait-gcc
endif

SOURCES=\
	$(ROOT_DIR)/src/fs_server.c \
	$(ROOT_DIR)/src/queue.c \
	$(ROOT_DIR)/src/blocking_queue.c \
	$(ROOT_DIR)/src/util.c \
	$(ROOT_DIR)/src/dirtab.c \
	$(ROOT_DIR)/src/array.c

SRC_DIRS=\
	$(ROOT_DIR)/src

MKDIR=mkdir -p
OBJ_DIR=$(ROOT_DIR)/build/$(CONF)
DIST_DIR=$(ROOT_DIR)/../../release/bin/$(CONF)
INSTALL_DIR=../../../nbbuild/netbeans/dlight/bin$(CONF)
LINE=sh $(ROOT_DIR)/make/line

EXEC=$(DIST_DIR)/$(PNAME)


#CF_COMMON         = --std=c99 -s -O2
CF_COMMON         = --std=c99 -g -Wall -Werror
CF_Linux-x86      = $(CF_COMMON) -DLINUX   -D_GNU_SOURCE -D_XOPEN_SOURCE=700 -m32
CF_Linux-x86_64   = $(CF_COMMON) -DLINUX   -D_GNU_SOURCE -D_XOPEN_SOURCE=700 -m64
CF_MacOSX-x86     = $(CF_COMMON) -DMAXOSX  -D_DARWIN_C_SOURCE -m32
CF_MacOSX-x86_64  = $(CF_COMMON) -DMAXOSX  -D_DARWIN_C_SOURCE -m64
CF_SunOS-sparc    = $(CF_COMMON) -DSOLARIS -D__EXTENSIONS__ -D_POSIX_PTHREAD_SEMANTICS  -m32
CF_SunOS-sparc_64 = $(CF_COMMON) -DSOLARIS -D__EXTENSIONS__ -D_POSIX_PTHREAD_SEMANTICS -m64
CF_SunOS-x86      = $(CF_COMMON) -DSOLARIS -D__EXTENSIONS__ -D_POSIX_PTHREAD_SEMANTICS -m32
CF_SunOS-x86_64   = $(CF_COMMON) -DSOLARIS -D__EXTENSIONS__ -D_POSIX_PTHREAD_SEMANTICS -m64
CF_Windows-x86    = $(CF_COMMON) -DWINDOWS -m32
CF_Windows-x86_64 = $(CF_COMMON) -DWINDOWS -m32
CF_FreeBSD-x86    = $(CF_COMMON) -D_GNU_SOURCE -m32
CF_FreeBSD-x86_64 = $(CF_COMMON) -D_GNU_SOURCE -m64

LF_Windows-x86    = --static-libgcc
LF_Windows-x86_64 = --static-libgcc
LF_Linux-x86      = $(LF_COMMON)
LF_Linux-x86_64   = $(LF_COMMON)

CFLAGS=$(CF_$(CONF))
LDFLAGS=$(LF_$(CONF)) -lpthread

all: $(EXEC)
	@$(LINE) =
	@(cd `dirname $(EXEC)`; echo "`pwd`/`basename $(EXEC)`"); echo
	@#echo $(EXEC); echo
	@ls -la $(EXEC) | sed "s#$(EXEC)##"
	@FILE=`(ls /bin/file || ls /usr/bin/file) 2>/dev/null`; \
	   if [ -n "$${FILE}" ]; then $${FILE} ${EXEC} | sed "s#$(EXEC):.##"; fi
	@$(LINE) =

parfait: all
	@echo Processing parfait output. This can take a while...
	@parfait --enable=all $(OBJ_DIRS)/*.bc

include $(ROOT_DIR)/make/Makefile.$(MAKEFILE)

$(EXEC): $(DIST_DIR) $(OBJS)
	@$(LINE) -
	$(LINK.c) -o $@ $(OBJS) -lpthread

$(OBJ_DIRS) $(DIST_DIR):
	$(MKDIR) $@

clean: clean_deps
	$(RM) -r $(OBJ_DIR) $(DIST_DIR)

clean-all:
	$(RM) -r $(ROOT_DIR)/build
	$(RM) -r $(ROOT_DIR)/dist
	$(RM) .make.state*

install:
	cp $(EXEC) $(INSTALL_DIR)

