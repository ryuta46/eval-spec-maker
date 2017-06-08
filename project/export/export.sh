#! /bin/sh

pushd ..
./gradlew clean exportJar --info
popd
