%define global_product_version 7.0
%define global_product_release 0


Name: sun-netbeans-mobility
Summary: NetBeans Mobility
Requires: sun-netbeans-ide >= 4.0
Requires: sed

%description
NetBeans Mobility

%post
NBCONF=/etc/netbeans.conf
LVALUE="netbeans_extraclusters"
RVALUE="/usr/lib/netbeans/mobility"
TEMPFILE="/tmp/___temporary_file_from_script___tohle_jmeno_je_snad_dost_unikatni___"

if [ ! -e $NBCONF ]; then
  echo "$LVALUE=\"$RVALUE\"" > $NBCONF
else
  ISTHERE=`grep -c "^[[:space:]]*$LVALUE" $NBCONF`
  if [ "$ISTHERE" -eq "0" ]; then
    echo "$LVALUE=\"$RVALUE\"" >> $NBCONF
  else
    RVALUE=`printf "%s" "$RVALUE" | sed 's/\([]\/*[]\)/\\\\\1/g'`
    sed /^[[:space:]]*$LVALUE/{s/\"[[:space:]]*$/:$RVALUE\"/} $NBCONF > $TEMPFILE
    mv $TEMPFILE $NBCONF
  fi
fi

%postun
NBCONF=/etc/netbeans.conf
LVALUE="netbeans_extraclusters"
RVALUE="/usr/lib/netbeans/mobility"
TEMPFILE="/tmp/___temporary_file_from_script___tohle_jmeno_je_snad_dost_unikatni___"

if [ -e $NBCONF ]; then
  ISTHERE=`grep -c "^[[:space:]]*$LVALUE[[:space:]]*=[[:space:]]*\".*$RVALUE.*\"" $NBCONF`
  if [ "$ISTHERE" -ne "0" ]; then
    LVALUE=`printf "%s" "$LVALUE" | sed 's/\([]\/*[]\)/\\\\\1/g'`
    RVALUE=`printf "%s" "$RVALUE" | sed 's/\([]\/*[]\)/\\\\\1/g'`
    sed /^[[:space:]]*$LVALUE/{s/:*$RVALUE//g} $NBCONF > $TEMPFILE
    mv $TEMPFILE $NBCONF
  fi
fi


%files

%erpm_map /usr/lib/netbeans nb_destdir

%dir /usr/lib/netbeans

/usr/lib/netbeans/mobility
