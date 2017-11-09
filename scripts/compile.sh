#!/bin/bash
set -ev

# Build project.
./gradlew build
ls ./build/libs

# Set git variables.
export VERSION_BRANCH=$TRAVIS_BRANCH
export VERSION_COMMIT=$TRAVIS_COMMIT
export VERSION_TAG=$TRAVIS_TAG

# Push javadocs for release versions and snapshot versions.
[[ "$VERSION_BRANCH" = "develop" ]] && bash ./scripts/javadocs.sh latest
[[ "$VERSION_BRANCH" = "master" ]] && [[ "$VERSION_TAG" != "" ]] && bash ./scripts/javadocs.sh $VERSION_TAG

# Push artifacts for release versions and snapshot versions.
[[ "$VERSION_BRANCH" = "develop" ]] && ./gradlew uploadArchives
[[ "$VERSION_BRANCH" = "master" ]] && [[ "$VERSION_TAG" != "" ]] && ./gradlew uploadArchives && ./gradlew closeAndReleaseRepository

exit 0
