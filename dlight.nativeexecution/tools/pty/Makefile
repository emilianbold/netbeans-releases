MAKEFILE = $(shell echo gmake)
MAKEFILE:sh = echo make
ROOT_DIR = $(shell pwd)
ROOT_DIR:sh = pwd
OS = $(shell uname -s)
OS:sh = uname -s

CONF=$(OS)-x86

PNAME=pty
CC=gcc

SOURCES=\
	$(ROOT_DIR)/src/env.c \
	$(ROOT_DIR)/src/error.c \
	$(ROOT_DIR)/src/loop.c \
	$(ROOT_DIR)/src/options.c \
	$(ROOT_DIR)/src/pty_fork.c \
	$(ROOT_DIR)/src/pty.c \
	$(ROOT_DIR)/src/util.c

SRC_DIRS=\
	$(ROOT_DIR)/src

MKDIR=mkdir -p
OBJ_DIR=$(ROOT_DIR)/build/$(CONF)
DIST_DIR=$(ROOT_DIR)/dist/$(CONF)
INSTALL_DIR=../../release/bin/nativeexecution/$(CONF)
LINE=sh $(ROOT_DIR)/make/line

EXEC=$(DIST_DIR)/$(PNAME)


CF_COMMON         = --std=c99 -s -O2
CF_Linux-x86      = $(CF_COMMON) -DLINUX   -D_GNU_SOURCE -D_XOPEN_SOURCE=700 -m32
CF_Linux-x86_64   = $(CF_COMMON) -DLINUX   -D_GNU_SOURCE -D_XOPEN_SOURCE=700 -m64
CF_MacOSX-x86     = $(CF_COMMON) -DMAXOSX  -D_DARWIN_C_SOURCE -m32
CF_MacOSX-x86_64  = $(CF_COMMON) -DMAXOSX  -D_DARWIN_C_SOURCE -m64
CF_SunOS-sparc    = $(CF_COMMON) -DSOLARIS -D__EXTENSIONS__ -m32
CF_SunOS-sparc_64 = $(CF_COMMON) -DSOLARIS -D__EXTENSIONS__ -m64
CF_SunOS-x86      = $(CF_COMMON) -DSOLARIS -D__EXTENSIONS__ -m32
CF_SunOS-x86_64   = $(CF_COMMON) -DSOLARIS -D__EXTENSIONS__ -m64
CF_Windows-x86    = $(CF_COMMON) -DWINDOWS -m32
CF_Windows-x86_64 = $(CF_COMMON) -DWINDOWS -m32

LF_Windows-x86    = --static-libgcc
LF_Windows-x86_64 = --static-libgcc

CFLAGS=$(CF_$(CONF))
LDFLAGS=$(LF_$(CONF))

all: $(EXEC)
	@$(LINE) =
	@echo $(EXEC); echo
	@ls -la $(EXEC) | sed "s#$(EXEC)##"
	@FILE=`(ls /bin/file || ls /usr/bin/file) 2>/dev/null`; \
	   if [ -n "$${FILE}" ]; then $${FILE} ${EXEC} | sed "s#$(EXEC):.##"; fi
	@$(LINE) =

include $(ROOT_DIR)/make/Makefile.$(MAKEFILE)

$(EXEC): $(DIST_DIR) $(OBJS)
	@$(LINE) -
	$(LINK.c) -o $@ $(OBJS)

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

