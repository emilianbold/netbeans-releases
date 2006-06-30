#!/usr/bin/env perl
# -*- perl -*-
# The contents of this file are subject to the terms of the Common Development
# and Distribution License (the License). You may not use this file except in
# compliance with the License.
#
# You can obtain a copy of the License at http://www.netbeans.org/cddl.html
# or http://www.netbeans.org/cddl.txt.
#
# When distributing Covered Code, include this CDDL Header Notice in each file
# and include the License file at http://www.netbeans.org/cddl.txt.
# If applicable, add the following below the CDDL Header, with the fields
# enclosed by brackets [] replaced by your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
#
# The Original Software is NetBeans. The Initial Developer of the Original
# Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
# Microsystems, Inc. All Rights Reserved.

$totalwc = 0;
$totallc = 0;

printf "%7s %5s File\n", "Strings", "Words";
printf "%7s %5s %s\n", "-------", "-----", "-" x 40;

foreach $f (@ARGV) {
    open FH, "< $f" or die;
    {
        local $/ = undef;
        $all = <FH>;
    }
    close FH;

    @lines = split /\r|\n/, $all;

    $wc = 0;
    $lc = 0;

    foreach (@lines) {
        next if /^\s*#/;
        next if /^\s*$/;

        if (/^[^=]+=(.*)$/) {
            $_ = $1;
            s/^\s*//;
            s/\s*$//;
            @words = split /\s+/;
            $wc += @words ;
            $lc++;
        }
    }
    
    printf "%7d %5d %s\n", $lc, $wc, $f;
    $totalwc += $wc;
    $totallc += $lc;
}

printf "\n%7d %5d Total\n", $totallc, $totalwc;
