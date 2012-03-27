package org.apache.whirr.service.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.CommandResult;
import java.io.IOException;
import java.util.Set;
import java.util.ArrayList;
import java.util.Iterator;
import java.net.InetAddress;
import org.apache.commons.configuration.Configuration;
import org.apache.whirr.Cluster;
import org.apache.whirr.ClusterSpec;
import org.apache.whirr.service.ClusterActionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.apache.whirr.RolePredicates.role;
import static org.apache.whirr.RolePredicates.anyRoleIn;
import static org.apache.whirr.service.FirewallManager.Rule;
import static org.jclouds.scriptbuilder.domain.Statements.call;
import com.google.common.collect.Sets;


public class ReplicaSetInitializer implements Runnable{


	private static final Logger LOG =
	  LoggerFactory.getLogger(ReplicaSetInitializer.class);
	private final DBObject replicaSetConfig;
	private final Cluster.Instance replSetLeader;
	private final String authPassword;
	private final String authUsername;
	private int arbiterPort = MongoDBShardArbiterClusterActionHandler.PORT;
	private int port = MongoDBReplSetMemberClusterActionHandler.PORT;

	public ReplicaSetInitializer(DBObject replicaSetConfig, Cluster.Instance replSetLeader){
		this.replicaSetConfig = replicaSetConfig;
		this.replSetLeader = replSetLeader;
		this.authPassword = null;
		this.authUsername = null;
	}

	public ReplicaSetInitializer(DBObject replicaSetConfig, Cluster.Instance replSetLeader, String authUsername, String authPassword){
		this.replicaSetConfig = replicaSetConfig;
		this.replSetLeader = replSetLeader;
		this.authPassword = authPassword;
		this.authUsername = authUsername;
	}

	public void run(){
		Mongo mongo;
		DB db;

		try{
			LOG.info("Shard ReplicaSet - Connecting to " + this.replSetLeader.getPublicAddress().getHostAddress() + " to initiate replica set.");
			mongo = new Mongo(this.replSetLeader.getPublicAddress().getHostAddress(), this.port); 
			db = mongo.getDB("admin");
			if(this.authPassword != null && this.authUsername != null){
				LOG.info("authenticating");
				boolean authresult = db.authenticate(this.authUsername, this.authPassword.toCharArray());
				LOG.info("authenticated: " + authresult);
			}
		}catch(Exception e){
			LOG.error("Unable to get public host address of replica set leader, " + e.getMessage());
			return;
		}

		BasicDBObject commandInfo = new BasicDBObject("replSetInitiate", this.replicaSetConfig);
		LOG.info("Sending rs.initiate() command");
		CommandResult initiateResult = db.command(commandInfo);
		LOG.info("Command Result: " + initiateResult.toString());
	}

	public void setArbiterPort(int arbiterPort){
		this.arbiterPort = arbiterPort;
	}

	public void setPort(int port){
		this.port = port;
	}

}
