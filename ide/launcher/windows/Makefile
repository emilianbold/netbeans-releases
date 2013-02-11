#  Main targets can be executed directly, and they are:
#  
#     build                    build a specific configuration
#     clean                    remove built files from a configuration
#     clobber                  remove all built files
#     all                      build all configurations
#     help                     print help mesage
#  
#  Targets .build-impl, .clean-impl, .clobber-impl, .all-impl, and
#  .help-impl are implemented in nbproject/makefile-impl.mk.
#
# NOCDDL



# Environment 
MKDIR=mkdir
CP=cp
CCADMIN=CCadmin
RANLIB=ranlib



# build
build: .build-post-$(CONF)

.build-pre: .build-pre-$(CONF)
# Add your pre 'build' code here...

.build-pre-netbeans.exe: netbeans.rc
	windres -DMANIFEST_FILE="netbeans.exe.manifest" -onetbeans.res -Ocoff netbeans.rc

.build-pre-netbeans64.exe: netbeans.rc
	x86_64-w64-mingw32-windres.exe -DMANIFEST_FILE="netbeans64.exe.manifest" -onetbeans64.res -Ocoff netbeans.rc

.build-post-netbeans.exe: .build-impl
	cp netbeans.exe ../../../nbbuild/netbeans/bin/netbeans.exe
	
.build-post-netbeans64.exe: .build-impl
	cp netbeans64.exe ../../../nbbuild/netbeans/bin/netbeans64.exe

											

# clean
clean: .clean-post

.clean-pre:
# Add your pre 'clean' code here...

.clean-post: .clean-impl
# Add your post 'clean' code here...



# clobber
clobber: .clobber-post

.clobber-pre:
# Add your pre 'clobber' code here...

.clobber-post: .clobber-impl
# Add your post 'clobber' code here...



# all
all: .all-post

.all-pre:
# Add your pre 'all' code here...

.all-post: .all-impl
# Add your post 'all' code here...



# help
help: .help-post

.help-pre:
# Add your pre 'help' code here...

.help-post: .help-impl
# Add your post 'help' code here...



# include project implementation makefile
include nbproject/Makefile-impl.mk
