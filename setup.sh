export KAFKA_LOG4J_LOGGERS="kafka.controller=INFO,kafka.producer.async.DefaultEventHandler=INFO,state.change.logger=INFO"
export KAFKA_LOG4J_ROOT_LOGLEVEL=INFO
export KAFKA_HEAP_OPTS="-Xmx12g -Xms12g"
export KAFKA_JVM_PERFORMANCE_OPTS="-XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:InitiatingHeapOccupancyPercent=35 -XX:+ExplicitGCInvokesConcurrent -XX:+ParallelRefProcEnabled"
export KAFKA_JVM_OPTS="-Xmx12g -Xms12g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:InitiatingHeapOccupancyPercent=35 -XX:+ExplicitGCInvokesConcurrent -XX:+ParallelRefProcEnabled"

echo "KAFKA_LOG4J_LOGGERS=$KAFKA_LOG4J_LOGGERS"
echo "KAFKA_LOG4J_ROOT_LOGLEVEL=$KAFKA_LOG4J_ROOT_LOGLEVEL"
echo "KAFKA_HEAP_OPTS=$KAFKA_HEAP_OPTS"
echo "KAFKA_JVM_PERFORMANCE_OPTS=$KAFKA_JVM_PERFORMANCE_OPTS"
echo "KAFKA_JVM_OPTS=$KAFKA_JVM_OPTS"