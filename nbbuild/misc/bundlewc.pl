#!/usr/bin/env perl
# -*- perl -*-
#                 Sun Public License Notice
# 
# The contents of this file are subject to the Sun Public License
# Version 1.0 (the "License"). You may not use this file except in
# compliance with the License. A copy of the License is available at
# http://www.sun.com/
# 
# The Original Code is NetBeans. The Initial Developer of the Original
# Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
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
