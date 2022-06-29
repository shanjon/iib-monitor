#!/bin/bash

MAIN_CLASS=com.newrelic.fit.plugins.messagebroker.Main

export APP_HOME=.
export MQ_LIB=$APP_HOME/lib

# ***********************************************
# ***********************************************

ARGS="-Dnewrelic.platform.config.dir=$APP_HOME/config"

exec java $ARGS -cp "$APP_HOME/lib/plugin.jar:$APP_HOME/lib/metrics_publish-3.0.0.jar:$APP_HOME/lib/gson-2.7.jar:$APP_HOME/lib/config-1.0.1.jar:$MQ_LIB/com.ibm.mq.commonservices.jar:$MQ_LIB/com.ibm.mq.headers.jar:$MQ_LIB/com.ibm.mq.jar:$MQ_LIB/com.ibm.mq.jmqi.jar:$MQ_LIB/com.ibm.mq.jms.Nojndi.jar:$MQ_LIB/com.ibm.mq.pcf.jar:$MQ_LIB/com.ibm.mqjms.jar:$MQ_LIB/connector.jar:$MQ_LIB/dhbcore.jar:$MQ_LIB/jms.jar:$MQ_LIB/jndi.jar:$MQ_LIB/providerutil.jar" $MAIN_CLASS