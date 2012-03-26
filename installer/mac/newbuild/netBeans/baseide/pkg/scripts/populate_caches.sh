#!/bin/sh -x
nb_dir=$1

if [ -z "$1" ]; then
    echo "usage: $0 nb_dir"
    exit
fi

echo Complete installation $nb_dir

if [ -d "$nb_dir" ]
then
    cd "$nb_dir" 
    cd Contents/Resources/NetBeans*/bin

    #issue 209263
    #run IDE in headless mode
    sh netbeans -J-Dnetbeans.close=true --nosplash -J-Dorg.netbeans.core.WindowSystem.show=false -J-Dorg.netbeans.core.WindowSystem.show=false --userdir /tmp/tmpnb
    if [ ! -d /tmp/tmpnb/var/cache ]; then
        exit
    fi

    cd /tmp/tmpnb/var/cache
    # zip -r populate.zip netigso
    zip -r populate.zip netigso

    # remove useless files
    # rm -r netigso
    # rm -r lastModified
    # rm splash* # if any
    rm -r netigso lastModified splash*

    # copy into nb/var/cache
    mkdir -p "$nb_dir"/Contents/Resources/NetBeans/nb/var/cache/
    cp -v * "$nb_dir"/Contents/Resources/NetBeans/nb/var/cache/

    # remove tmpnb
    cd "$nb_dir" 
    rm -Rf /tmp/tmpnb

fi
