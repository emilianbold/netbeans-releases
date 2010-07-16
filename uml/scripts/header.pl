# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
#
# Oracle and Java are registered trademarks of Oracle and/or its affiliates.
# Other names may be trademarks of their respective owners.
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
# nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
# particular file as subject to the "Classpath" exception as provided
# by Oracle in the GPL Version 2 section of the License file that
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

use strict;
use warnings;

use Fcntl qw(SEEK_SET);

my $header = <<'FILEHEADER';
//*****************************************************************************
// $Workfile:: ClassifierTestCase.java                                         $
// Desc     :
//
// $Revision$
//   $Author$
//     $Date$
//
//*****************************************************************************
FILEHEADER

chomp(my @files = qx[dir *.java /s /b]);

add_header($header, $_) for @files;

sub add_header {
    my ($header, $file) = @_;

    open my $inf, '+<', $file or do {
        warn "Couldn't open $file: $!\n";
        return;
    };

    # Discard first line, grab second.
    my $text = do { local $/; <$inf> };
    close $inf;

    return if $text =~ /Revision:/;   # Already has our header

    print STDERR "Adding header to $file\n"; 

    open my $outf, '>', $file or do {
        warn "Couldn't write to $file: $!\n";
        return;
    };

    $header =~ s/(?<=\$Workfile:: ).*\$/pad_workfile($file, 65)/e;
    print $outf $header;

    use Regexp::Common;
    $text =~ s/^(?:\s*$RE{comment}{Java}\s*)//g;

    print $outf $text;
    close $outf;
}

sub pad_workfile {
    my ($file, $length) = @_;

    $file =~ tr:\\:/:;
    my ($name) = $file =~ m[.*/(.*)];
    $name ||= '';

    my $pad = $length - length($name) - 1;
    $pad = 0 if $pad < 0;

    return $name . (' ' x $pad) . '$';
}
