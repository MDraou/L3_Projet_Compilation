#!/bin/bash
for VARIABLE in ../test/input/*
do
	echo "$VARIABLE"
	java Compiler $VARIABLE
done
