# session-bench

Session bench is a tool to test Couchbase and Cassandra using JMeter.

**The test scenario :**
* create session if none exists 
* loop untill *MAXBYTES* is reached: 
    * read session data
    * generate a random ascii string of *ADDBYTES* size
    * write session data
    * sleep 1s
    * when *MAXBYTES* is reached, delete the session
    

**Running the test :**

* Setup your machines, 3 nodes at least for the cluster and 1+ for JMeter 


* Add ip's to your /etc/hosts
Add to your desktop's /etc/hosts the ip of the machines: cb1, cb2, cb3 and jm1 (you may add more by modifying ansible's inventory).

* Build the sampler
At the project root:
```
mvn package
```

* Deploy Couchbase
From session-bench/src/main/resources/ansible :
```
ansible-playbook -i hosts couchbase.yaml
```
Go to cb1:8091 and add other nodes to the cluster.

* Deploy JMeter
From session-bench/src/main/resources/ansible :
```
ansible-playbook -i hosts jmeter.yaml
```
Add the sampler jar generated at step 3 to the JMeter service at apache-jmeter-2.0.13/lib/ext
Add the jmeter test file session-bench/src/main/resources/benchsessions.jmx to apache-jmeter-2.0.13 
Add the couchbase ip's to the jmeter /etc/hosts file.


* Run the test
From jm1, at apache-jmeter-2.0.13
```
./bin/jmeter -n -t benchsessions.jmx
```