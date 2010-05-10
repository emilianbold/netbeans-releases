set -x

DIRNAME=`dirname $0`
cd ${DIRNAME}
source init.sh


pack_component() 
{
    dist=$1
    base_name=$2
    component=$3
    filter=$4
    zip -q -r $dist/$base_name-$component.zip $filter
}

mkdir $DIST/zip/moduleclusters

cd $NB_ALL/nbbuild
ant zip-cluster-config -Dcluster.config=basic -Dzip.name=$DIST/zip/$BASENAME-javase.zip || exit 1
cd $NB_ALL/nbbuild/netbeans
pack_component $DIST/zip/moduleclusters $BASENAME ergonomics "ergonomics*"

cp -r $DIST/zip /net/smetiste.czech/space/${BASE_FOR_JAVAFX}
touch /net/smetiste.czech/space/${BASE_FOR_JAVAFX}/ready

