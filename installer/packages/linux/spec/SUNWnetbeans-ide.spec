Name: sun-netbeans-ide
Summary: NetBeans IDE
Requires: sun-netbeans-ide5-modules >= 4.1

%description
NetBeans IDE

%files

%erpm_map /usr/lib/netbeans nb_destdir

%dir /usr/lib/netbeans

/usr/lib/netbeans/nb4.1

%dir /usr/lib/netbeans/bin

/usr/lib/netbeans/bin/netbeans

%erpm_map /usr/share nb_destdir

/usr/share/applications

/usr/share/pixmaps

%erpm_map / nb_destdir

//etc/netbeans.conf


%erpm_unmap
# %dir /usr/bin

%erpm_ln_s /usr/lib/netbeans/bin/netbeans /usr/bin/netbeans
