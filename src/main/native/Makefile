# Copyright (c) 2010 Sean C. Rhea <sean.c.rhea@gmail.com>
# All rights reserved.
#
# See the file LICENSE included in this distribution for details.

ifeq ($(shell uname),Darwin)
LIBEXT=dylib
else
LIBEXT=so
endif

TARGET=libscalaqlite.$(LIBEXT)

all: $(TARGET)

clean:
	rm -f Sqlite3C.h $(TARGET)

.PHONY: all, clean

libscalaqlite.so: Sqlite3C.cc Sqlite3C.h
	g++ -O2 -o $@ -shared -Wl,-soname,libscalaqlite.so \
	        -I/usr/lib/jvm/java-6-sun-1.6.0.26/include \
	        -I/usr/lib/jvm/java-6-sun-1.6.0.26/include/linux \
		$< -static -lc -lsqlite3

libscalaqlite.dylib: Sqlite3C.cc Sqlite3C.h
	g++ -O2 -dynamiclib -o $@ \
		-I /System/Library/Frameworks/JavaVM.framework/Versions/Current/Headers \
		-L/opt/local/lib \
		$< -lsqlite3

Sqlite3C.h: ../../../target/scala-2.9.1/classes/org/srhea/scalaqlite/Sqlite3C.class
	javah -classpath ../../../target/scala-2.9.1/classes org.srhea.scalaqlite.Sqlite3C
