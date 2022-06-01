# Gradle Wrapper 7.2

Provides a common configuration for gradle/gradle-wrapper, so we don't have to have multiple gradlew
and gradle deps through the whole repo.


## To run this in dev

~~~{.sh}
export PATH="common/gradle/gradlew-7.2:$PATH"
~~~

and then go into your project root and run

~~~{.sh}
gradlew $TASK
~~~

as normal


## To run this in an app build

~~~{.Dockerfile}
FROM $REPO/base/java_17:latest-gradle_7.2
~~~

See base/java\_17 for more details
