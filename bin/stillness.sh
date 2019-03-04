#! /bin/sh

VHOME=/home/claude/git/stillness

CLASSPATH=$VHOME/target/classes
for lib in $VHOME/target/lib/*.jar; do CLASSPATH=$CLASSPATH:$lib; done;

#hack
CLASSPATH=$CLASSPATH:/usr/share/java/slf4j-simple-1.7.25.jar

#echo CLASSPATH=$CLASSPATH

java -classpath $CLASSPATH com.republicate.stillness.Stillness $*

