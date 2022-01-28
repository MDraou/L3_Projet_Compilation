#!/bin/bash
for VARIABLE in ../test/input/*.l
do
	echo "$VARIABLE"
	java Compiler -v 2  $VARIABLE
done
