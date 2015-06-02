#! /bin/sh

VHOME=/home/claude/projects/stillness

CLASSPATH=$VHOME/classes
for lib in $VHOME/lib/*.jar; do CLASSPATH=$CLASSPATH:$lib; done;

echo CLASSPATH=$CLASSPATH

java -classpath $CLASSPATH stillness.Stillness $*

