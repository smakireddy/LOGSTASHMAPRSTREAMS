cat logstash-syslog.conf

input {
  tcp {
    port => 5000
    type => syslog
  }
  udp {
    port => 5000
    type => syslog
  }
}

filter {
  if [type] == "syslog" {
    grok {
      match => { "message" => "%{SYSLOGTIMESTAMP:syslog_timestamp} %{SYSLOGHOST:syslog_hostname} %{DATA:syslog_program}(?:\[%{POSINT:syslog_pid}\])?: %{GREEDYDATA:syslog_message}" }
      add_field => [ "received_at", "%{@timestamp}" ]
      add_field => [ "received_from", "%{host}" ]
    }
    date {
      match => [ "syslog_timestamp", "MMM  d HH:mm:ss", "MMM dd HH:mm:ss" ]
    }
  }
}
output {
 kafka {
   topic_id => "/user/mapr/stream1:test"
 }
}



#to start logstash -- its not needed to start as logstash service already running
/usr/share/logstash/bin/logstash -f logstash-syslog.conf


#start consumer
/opt/mapr/kafka/kafka-1.1.1/bin/kafka-console-consumer.sh --topic /user/mapr/stream1:test --new-consumer --bootstrap-server foo:1 --from-beginning


maprcli stream cursor list -path /user/mapr/stream1

maprcli stream topic list -path /user/mapr/stream1



#send netcat message

echo -n "message25" | nc -4u -w1 localhost 5000


#check the port is already using
lsof -i:5000

netstat -ntlp | grep 514

journalctl --unit logstash


rpm -qc logstash

view /etc/logstash/logstash.yml

journalctl --unit logstash

systemctl stop logstash


ssh root@10.20.30.96 ps | grep syslogd

ssh root@10.20.30.96 kill 733



Elastic Search


https://mapr.com/docs/60/AdvancedInstallation/InstallLogging.html


/opt/mapr/server/configure.sh -OT psnode96.ps.lab,psnode97.ps.lab,psnode98.ps.lab -ES psnode96.ps.lab,psnode97.ps.lab,psnode98.ps.lab -R -EPelasticsearch -genESKeys



curl -X GET http://10.250.51.119:9200

curl -gk -X DELETE http://10.250.51.119:9200/index-name

