#!/usr/bin/env perl
# -*- perl -*-

require 5.005;
use File::Find;

my @files = ();

if ($#ARGV < 0) {
    die "usage: $0 [-f] file | directory ...\n";
}

foreach my $name (@ARGV) {
    if (-f $name) {
	push @files, $name;
    } elsif (-d $name) {
        find(sub {
                 if (-f && m,\.java$, || -f && m,\bBundle.properties$,) {
                     push @files, $File::Find::name;
                 }
             },
             $name);
    }
}

#
# read Bundle.properties
#

@props = ();

foreach $f (@files) {
    next if $f !~ m,\bBundle.properties$,i;
      
    open FH, "< $f" or die;
    {
        local $/ = undef;
        $all = <FH>;
    }
    close FH;

    @lines = split /\r\n|\n|\r/, $all;
    for ($lineno = 0; $lineno <= $#lines; $lineno++) {
	$_ = $lines[$lineno];
        
        next if /^\s*#/;
        next if /^\s*$/;

        if (m,^([^=]+)=(.*)$,) {
            $k = $1;
            $k =~ s,\\ , ,g;
            push @props, { key => $k,
                           file => $f,
                           lineno => $lineno + 1,
                           line => $_,
                           used => 0
                         };
        }

        while (m,\\$, && $lineno <= $#lines) {
            $lineno++;
            $_ = $lines[$lineno];
        }
    }
}

#
# go through *.java
#
  
  
foreach $f (@files) {
    next if $f =~ m,\bBundle.properties$,i;

    print STDERR "*** $f\n";
    
    open FH, "< $f" or die;
    {
        local $/ = undef;
        $all = <FH>;
    }
    close FH;

    foreach $p (@props) {
        next if $p->{used} > 0;
        $pat = $p->{key};
        $pat = quotemeta $pat;
        $p->{used}++ if $all =~ m,\"$pat\",;
    }
}

foreach $p (@props) {
    print "$p->{file}:$p->{lineno}: $p->{line}\n" if $p->{used} == 0;
}
