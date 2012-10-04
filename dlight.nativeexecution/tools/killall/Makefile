MAKEFILE = $(shell echo gmake)
MAKEFILE:sh = echo make
ROOT_DIR = $(shell pwd)
ROOT_DIR:sh = pwd
OS = $(shell uname -s)
OS:sh = uname -s

CONF=$(OS)-x86

PNAME=killall
CC=gcc

SOURCES=\
	$(ROOT_DIR)/src/error.c \
	$(ROOT_DIR)/src/killall.c \
	$(ROOT_DIR)/src/options.c

SRC_DIRS=\
	$(ROOT_DIR)/src

MKDIR=mkdir -p
OBJ_DIR=$(ROOT_DIR)/build/$(CONF)
DIST_DIR=$(ROOT_DIR)/dist/$(CONF)
INSTALL_DIR=../../release/bin/nativeexecution/$(CONF)
LINE=sh $(ROOT_DIR)/make/line

EXEC=$(DIST_DIR)/$(PNAME)

CF_COMMON         = -s -O2
CF_Linux-x86      = $(CF_COMMON) -DLINUX -m32
CF_MacOSX-x86     = $(CF_COMMON) -DMACOSX  -D_DARWIN_C_SOURCE -m32
CF_SunOS-sparc    = $(CF_COMMON) -DSOLARIS -D__EXTENSIONS__ -m32
CF_SunOS-x86      = $(CF_COMMON) -DSOLARIS -D__EXTENSIONS__ -m32
CF_Windows-x86    = $(CF_COMMON) -DWINDOWS -m32

CF_Linux-x86_64   = $(CF_COMMON) -DLINUX -m64
CF_MacOSX-x86_64  = $(CF_COMMON) -DMACOSX  -m64
CF_SunOS-sparc_64 = $(CF_COMMON) -DSOLARIS -D__EXTENSIONS__ -m64
CF_SunOS-x86_64   = $(CF_COMMON) -DSOLARIS -D__EXTENSIONS__ -m64
CF_Windows-x86_64 = $(CF_COMMON) -DWINDOWS -m32

LF_Windows-x86    = --static-libgcc
LF_Windows-x86_64 = --static-libgcc
LF_SunOS-x86      = -lrt
LF_SunOS-x86_64   = -lrt
LF_SunOS-sparc    = -lrt
LF_SunOS-sparc_64 = -lrt

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

