Name: sun-netbeans-ide
Summary: NetBeans IDE
Requires: sun-netbeans-ide4-modules >= 4.0

%description
NetBeans IDE

%files

%erpm_map /usr/netbeans nb_destdir

%dir /usr/netbeans

/usr/netbeans/nb4.0

%dir /usr/netbeans/bin

/usr/netbeans/bin/netbeans

%erpm_map /usr/share nb_destdir

/usr/share/applications

/usr/share/pixmaps

%erpm_map / nb_destdir

//etc/netbeans.conf


%erpm_unmap
# %dir /usr/bin

%erpm_ln_s /usr/netbeans/bin/netbeans /usr/bin/netbeans
