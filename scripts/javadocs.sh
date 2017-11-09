#!/bin/bash
set -ev
VERSION=$1
TARGET='Xemiru/General.git'

# Prepare our deployment key.
chmod 600 gh_pages
eval `ssh-agent -s`
ssh-add gh_pages

# Clone the current gh-pages branch.
cd "$TRAVIS_BUILD_DIR/../"
git clone -b gh-pages -- "https://github.com/$TARGET" gh-pages
cd gh-pages

# Clean out our version directory.
rm -rf ./$VERSION
mkdir $VERSION
cd $VERSION

# Generate javadocs.
javadoc -sourcepath $TRAVIS_BUILD_DIR/src/main/java/ -link https://docs.oracle.com/javase/8/docs/api/ -subpackages com.github.xemiru.general

# Push to Git.
git config --global user.name "Travis CI"
git config --global user.email "xemiruk@gmail.com"
git add -A
git commit -m "Update javadocs for version $VERSION: $VERSION_COMMIT"
git push "git@github.com:$TARGET" gh-pages
