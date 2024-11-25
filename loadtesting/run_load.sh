#!/bin/bash
# run jmeter with the test script and the output file, should set the JMETER_HOME variable first
export PATH=$JMETER_HOME:$PATH
jmeter -n -t ./high_competing_transaction.jmx -l ./high_competing_transaction.jtl -e -o ./reports