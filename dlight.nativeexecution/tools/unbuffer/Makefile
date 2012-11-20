MAKEFILE = $(shell echo gmake)
MAKEFILE:sh = echo make
ROOT_DIR = $(shell pwd)
ROOT_DIR:sh = pwd
OS = $(shell uname -s)
OS:sh = uname -s

CONF=$(OS)-x86

CC=gcc

SOURCES=\
	$(ROOT_DIR)/src/unbuffer.c

SRC_DIRS=\
	$(ROOT_DIR)/src

MKDIR=mkdir -p
OBJ_DIR=$(ROOT_DIR)/build/$(CONF)
DIST_DIR=$(ROOT_DIR)/dist/$(CONF)
INSTALL_DIR=../../release/bin/nativeexecution/$(CONF)
LINE=sh $(ROOT_DIR)/make/line

CF_COMMON         = -s -O2
CF_Linux-x86      = $(CF_COMMON) -DLINUX -shared -fPIC -m32
EX_Linux-x86      = unbuffer.so
CF_Linux-x86_64   = $(CF_COMMON) -DLINUX -shared -fPIC -m64
EX_Linux-x86_64   = unbuffer.so
CF_MacOSX-x86     = $(CF_COMMON) -DMAXOSX  -shared -m32
EX_MacOSX-x86     = unbuffer.dylib
CF_MacOSX-x86_64  = $(CF_COMMON) -DMAXOSX  -shared -m64
EX_MacOSX-x86_64  = unbuffer.dylib
CF_SunOS-sparc    = $(CF_COMMON) -DSOLARIS -G -fPIC -m32
EX_SunOS-sparc    = unbuffer.so
CF_SunOS-sparc_64 = $(CF_COMMON) -DSOLARIS -G -fPIC -m64
EX_SunOS-sparc_64 = unbuffer.so
CF_SunOS-x86      = $(CF_COMMON) -DSOLARIS -G -fPIC -m32
EX_SunOS-x86      = unbuffer.so
CF_SunOS-x86_64   = $(CF_COMMON) -DSOLARIS -G -fPIC -m64
EX_SunOS-x86_64   = unbuffer.so
CF_Windows-x86    = $(CF_COMMON) -DWINDOWS -shared -m32
EX_Windows-x86    = unbuffer.dll
CF_Windows-x86_64 = $(CF_COMMON) -DWINDOWS -shared -m32
EX_Windows-x86_64 = unbuffer.dll

LF_Windows-x86    = --static-libgcc
LF_Windows-x86_64 = --static-libgcc

CFLAGS=$(CF_$(CONF))
LDFLAGS=$(LF_$(CONF))

PNAME=$(EX_$(CONF))

EXEC=$(DIST_DIR)/$(PNAME)

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

