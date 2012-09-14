MAKEFILE = $(shell echo gmake)
MAKEFILE:sh = echo make
ROOT_DIR = $(shell pwd)
ROOT_DIR:sh = pwd

CONF=SunOS-x86

PNAME=pty
OBJFILES=error.o loop.o pty_fork.o pty.o

CC=gcc

include $(ROOT_DIR)/make/$(CONF).inc

MKDIR=mkdir -p
SRC_DIR=$(ROOT_DIR)/src
OBJ_DIR=$(ROOT_DIR)/build/$(CONF)
DIST_DIR=$(ROOT_DIR)/dist/$(CONF)

EXEC=$(DIST_DIR)/$(PNAME)

all: $(EXEC)
	@echo ================================================================================
	@FILE=`(ls /bin/file || ls /usr/bin/file) 2>/dev/null`; \
	 if [ -n "$${FILE}" ]; then $${FILE} ${EXEC}; fi
	@ls -la $(EXEC)
	@echo ================================================================================

include $(ROOT_DIR)/make/Makefile.$(MAKEFILE)

$(EXEC): $(DIST_DIR) $(OBJS)
	$(LINK.c) -o $@ $(OBJS)

$(OBJ_DIR) $(DIST_DIR):
	$(MKDIR) $@

clean: clean_deps
	$(RM) -r $(OBJ_DIR) $(DIST_DIR)

clean-all:
	$(RM) -r $(ROOT_DIR)/build
	$(RM) -r $(ROOT_DIR)/dist
	$(RM) .make.state*

