#!/bin/sh
java -cp .:@classesDirName@:@bootstrapJarName@:@linuxLibClasspath@ -jar @bootstrapJarName@ $*
exit $?
