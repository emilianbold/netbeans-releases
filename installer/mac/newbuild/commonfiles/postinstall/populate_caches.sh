#!/bin/sh
nb_dir=$1

if [ -z "$1" ]; then
    echo "usage: $0 nb_dir"
    exit
fi

echo Complete installation $nb_dir

if [ -d "$nb_dir" ]
then
    # remove tmpnb first
    rm -Rf /tmp/tmpnb

    cd "$nb_dir" 
    cd Contents/Resources/NetBeans*/bin

    #issue 209263
    #run IDE in headless mode
    echo Run IDE in headless mode
    sh netbeans -J-Dnetbeans.close=true --nosplash -J-Dorg.netbeans.core.WindowSystem.show=false -J-Dorg.netbeans.core.WindowSystem.show=false --userdir /tmp/tmpnb --modules --update-all
    exit_code=$?
    echo Run IDE returns exit code: $exit_code
    if [ ! -d /tmp/tmpnb/var/cache ]; then
        echo Warning: No caches found -> exiting
        exit
    fi

    cd /tmp/tmpnb/var/cache
    # zip -r populate.zip netigso
    zip -r -q populate.zip netigso

    # remove useless files
    # rm -r netigso
    # rm -r lastModified
    # rm splash* # if any
    rm -r netigso lastModified catalogcache splash*

    # copy into nb/var/cache
    mkdir -p "$nb_dir"/Contents/Resources/NetBeans/nb/var/cache/
    cp -v * "$nb_dir"/Contents/Resources/NetBeans/nb/var/cache/

    # copy IDE log to var/log/populate_caches.log
    mkdir -p "$nb_dir"/Contents/Resources/NetBeans/nb/var/log/
    cp -v ../log/messages.log "$nb_dir"/Contents/Resources/NetBeans/nb/var/log/populate_caches.log

    # remove tmpnb
    cd "$nb_dir" 
    rm -Rf /tmp/tmpnb

fi
