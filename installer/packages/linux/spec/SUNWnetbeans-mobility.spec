%define global_product_version 7.1
%define global_product_release 0

Name: sun-netbeans-mobility
Summary: NetBeans Mobility
Requires: sun-netbeans-ide >= 4.1

%description
NetBeans Mobility

%files

%erpm_map /usr/lib/netbeans nb_destdir

%dir /usr/lib/netbeans

/usr/lib/netbeans/mobility
