#
#  Copyright (c) 2009, Sun Microsystems, Inc.
#  All rights reserved.
#  
#  Redistribution and use in source and binary forms, with or without 
#  modification, are permitted provided that the following conditions are met:
#  
#   * Redistributions of source code must retain the above copyright notice, 
#     this list of conditions and the following disclaimer.
#   * Redistributions in binary form must reproduce the above copyright 
#     notice, this list of conditions and the following disclaimer in 
#     the documentation and/or other materials provided with the distribution.
#   * Neither the name of Sun Microsystems, Inc. nor the names of its 
#     contributors may be used to endorse or promote products derived 
#     from this software without specific prior written permission.
# 
#  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
#  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
#  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
#  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
#  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
#  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
#  TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
#  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
#  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
#  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
#  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
#
 
INCLUDES = `pkg-config --cflags gtk+-2.0`
LINKFLAGS = `pkg-config --libs gtk+-2.0`

# The following lines make this work with both GNU and Solaris make, as
# well as Sun Studio's CC and g++
DEBUG_=-g3 -gdwarf-2
DEBUG_CC=-g
CCFLAGS = $(DEBUG_$(CCC)) $(INCLUDES)
CXXFLAGS = $(CCFLAGS)

PROGRAM = Freeway
SOURCES.cc = main.cc FreewayWindow.cc callbacks.cc traffic.cc \
	vehicle.cc vehicle_list.cc truck.cc sports.cc maniac.cc police.cc 
SOURCES.h = FreewayWindow.h traffic.h maniac.h police.h sports.h \
	truck.h vehicle.h vehicle_list.h about.h traffic.h
GUI.icon = traffic.icon
GUI.bitmap = arrow_left.bitmap arrow_over.bitmap arrow_right.bitmap \
	arrow_up.bitmap traffic.mask

OBJECTS = $(SOURCES.cc:%.cc=%.o) 

all: $(PROGRAM)

$(PROGRAM): $(SOURCES.h) $(GUI.icon) $(GUI.bitmap) $(OBJECTS)
	$(LINK.cc) -o $@ $(OBJECTS) $(LINKFLAGS)

clean:
	@-$(RM) -rf $(OBJECTS) $(PROGRAM)

