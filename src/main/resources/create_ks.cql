DROP KEYSPACE bench_ks;

CREATE KEYSPACE bench_ks
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

CREATE TABLE bench_ks.sessions ( 
	id TEXT,
	json TEXT, 
	PRIMARY KEY (id) 
);