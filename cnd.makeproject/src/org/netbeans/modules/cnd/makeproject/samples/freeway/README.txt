#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common
# Development and Distribution License("CDDL") (collectively, the
# "License"). You may not use this file except in compliance with the
# License. You can obtain a copy of the License at
# http://www.netbeans.org/cddl-gplv2.html
# or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
# specific language governing permissions and limitations under the
# License.  When distributing the software, include this License Header
# Notice in each file and include the License file at
# nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
# particular file as subject to the "Classpath" exception as provided
# by Sun in the GPL Version 2 section of the License file that
# accompanied this code. If applicable, add the following below the
# License Header, with the fields enclosed by brackets [] replaced by
# your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
#
# Contributor(s):
#
# The Original Software is NetBeans. The Initial Developer of the Original
# Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
# Microsystems, Inc. All Rights Reserved.
#
# If you wish your version of this file to be governed by only the CDDL
# or only the GPL Version 2, indicate your decision by adding
# "[Contributor] elects to include this software in this distribution
# under the [CDDL or GPL Version 2] license." If you do not indicate a
# single choice of license, a recipient has the option to distribute
# your version of this file under either the CDDL, the GPL Version 2 or
# to extend the choice of license to its licensees as provided above.
# However, if you add GPL Version 2 code and therefore, elected the GPL
# Version 2 license, then the option applies only if the new code is
# made subject to such option by the copyright holder.
#
 
Description:
	Freeway simulates traffic flow on a typical California highway.
	Individual vehicles make decisions about how fast and where to
	drive based on the conditions around them.

	The user can control some of the simulation parameters:
	* Speed limit in the three zones
	* Distance between cars
	* Simulation speed

Supported Platforms:
    Solaris:
        The primary platform during the GTK port was Solaris 10. A verification
        build was done on a prerelease version of Solaris 11. All required include
        directories and libraries should be included in Solaris.

        GtkFreeway builds with either Sun Studio or GNU tool chains.

    Linux:
        Linux development was done on Ubuntu 7.10.

    Windows:
        Currently only tested on Windows XP using MinGW toolchain and the all-in-one
        GTK bundle from http://www.gtk.org/download-windows.html. Note that this
        bundle explicitly states it *only* compiles with the MinGW toolchain (it
        specifically does *not* compile with the Cygwin toolchain).

        While its likely that this demo will compile with Cygwin libraries and
        the toolchain, it has not been verified and is not officially supported.

Unsupported Platforms:
    Currently, the most important platform we'd like to support (and don't) is Mac
    OSX. We don't support the Mac because there is no supported version of the GTK+
    libraries for the Mac. There is a project in-progress to provide GTK+ on the Mac.
    
    In general, the gating factor is GTK+. If you can supply the correct set of
    libraries and tools (including pkg-config and the gtk+-2.0 packages) then its
    unlikely the GtkFreeway demo won't build.

Disclaimer:
        Freeway was written in 1991 as an XView application, converted to a Motif
        application and has subsequently been included in all Visual WorkShop and
        Sun Studio releases. In 2008 it was ported from Motif to GTK+ 2 in an
        effort to modernize it. However, while it was converted to GTK, no effort
        was made to complete partially implemented features. There appear to be
        hooks for things not currently working. Since the demo program is so old,
        its original authors and plans are long gone. Much of the C++ programming
        is also based on the original 1991 development and does not make use of
        more modern keywords and constructs (such as templates and consts).
