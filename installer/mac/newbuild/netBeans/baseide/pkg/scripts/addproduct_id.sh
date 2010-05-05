#!/bin/sh -x

product_id=$1

script_dir=`dirname "$0"`

. "$script_dir"/env.sh

cd "$NETBEANS_INSTALL_DIR"
cd Contents/Resources/NetBeans*/${NB_CLUSTER_DIR}/config
if [[ $product_id == NB* ]] ; then 
    rm -rf productid #just in case
    printf $product_id >> productid
elif [ -e productid ] &&  ! cat productid | grep -q "$product_id" ; then    
      printf "_$product_id" >> productid     
fi   