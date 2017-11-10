#!/bin/bash
set -ev

# Set git variables.
VERSION_BRANCH=$TRAVIS_BRANCH
VERSION_COMMIT=$TRAVIS_COMMIT
VERSION_TAG=$TRAVIS_TAG

# Initialize gradle properties.
openssl aes-256-cbc -K $encrypted_61ecede1ea13_key -iv $encrypted_61ecede1ea13_iv -in encrypted.tar.enc -out encrypted.tar -d
tar xvf encrypted.tar

printf "
onTravis=true
travisBuild=$TRAVIS_COMMIT
travisBranch=$TRAVIS_BRANCH
" >> gradle.properties
