#!/bin/bash

BASE_DIR=$1
DEBS=(wildfly_8.1.0 wildfly-mysql_8.1.0)
MAIN_ARCH=amd64
ARCH_DIR=dists/trusty/main/binary-$MAIN_ARCH
LINK_ARCHS=(i386)

echo "Building debs $DEBS"
mkdir -p dist
for deb in ${DEBS[@]}
do
  dpkg-deb --build $deb ./dist
done

echo "Installing debs into $BASE_DIR/$ARCH_DIR"

for deb in ${DEBS[@]}
do
  deb_file=${deb}_all.deb
  echo "Install $deb_file into $BASE_DIR/$ARCH_DIR"
  cp dist/$deb_file $BASE_DIR/$ARCH_DIR
  for link_arch in ${LINK_ARCHS[@]}
  do
    link_dir=$BASE_DIR/dists/trusty/main/binary-$link_arch
    echo "Link $BASE_DIR/$ARCH_DIR/$deb_file into $link_dir"
    rm -f $link_dir/$deb_file
    ln -s $BASE_DIR/$ARCH_DIR/$deb_file $link_dir
  done
done

echo "Scanning packages for $MAIN_ARCH"
cd $BASE_DIR
dpkg-scanpackages $ARCH_DIR /dev/null | gzip -9c > $ARCH_DIR/Packages.gz

for link_arch in ${LINK_ARCHS[@]}
do
  link_dir=dists/trusty/main/binary-$link_arch
  echo "Scanning packages for $link_arch"
  dpkg-scanpackages $ARCH_DIR /dev/null | gzip -9c > $link_dir/Packages.gz
done
