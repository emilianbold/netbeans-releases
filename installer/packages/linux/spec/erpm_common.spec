# This should be the same as the Solaris package version. #
%define global_product_version 4.1

# This is an RPM-specific thing.  The RPM manual says that this is the
# package's version (rather than the package's content's version).  So
# this should be 1 unless a package is redelivered with the same
# Version, in which case the Release should be incremented each time
# the package is redelivered.  I.e. if the Version of the RPM's is the
# same for EA and FCS, then the Release should be incremented for FCS.
%define global_product_release 1

%define _prefix /usr/lib

Version: %{global_product_version}
Release: %{global_product_release}
Group: Applications
Copyright: commercial
Vendor: Sun Microsystems, Inc.
URL: http://www.sun.com/
Prefix: %_prefix
AutoReqProv: no
