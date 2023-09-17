# README
## 文件管理系统
说明：此处认为读者已完成在云服务器上Hadoop、jdk及zookeeper的下载并完成了路径配置。本项目使用4台云服务器（注意开在同一区域），其职责分别为：

master2：NN（active）、ZKFC、RM（standby）、ZK （myid=1）
master1：NN（standby）、ZKFC、RM（active）、ZK （myid=2）
slave1：DN、JN、NM、ZK （myid=3）
slave2：DN、JN、NM、ZK（observer） （myid=4）

在配置文件中，约定含0的IP映射为私网，不含的为公网，如master01为私网IP，master1为公网。同时省略hosts文件中IP映射的配置及4台机器间ssh免密登录的配置过程。

### 修改Hadoop配置文件
1. 配置Hadoop所用jdk的路径
分别在hadoop-env.sh、mapred-env.sh、yarn-env.sh文件中添加jdk环境变量：
`export JAVA_HOME=/root/jdk/jdk-11.0.19`
该路径根据jdk实际存放的位置填写。

2. 修改Hadoop其他配置文件
4台机器上都相同的配置文件有slaves、core-site.xml和mapred-site.xml（mapred-site.xml需要复制mapred-site.xml.template文件并重命名为mapred-site.xml），另外2个文件hdfs-site.xml及yarn-site.xml在NN和DN上有所不同。配置文件如下：

slaves：
```
slave01
slave02
```

core-site.xml：
```xml
<configuration> 
        <!-- hdfs的nameservice名称，需与dfs.nameservices一致（在hdfs-site.xml中配置） -->  
        <!-- 如果配置了NameNode的HA,则使用fs.defaultFS;如果单一NameNode节点，使用fs.default.name -->  
        <property> 
                <name>fs.defaultFS</name>  
                <value>hdfs://ns</value> 
        </property>
        <!--hadoop临时数据存储目录,如果hdfs-site.xml不配置namenode和datanode存储位置，默认放在该目录下-->  
        <property> 
                <name>hadoop.tmp.dir</name>  
                <value>/root/hadoop/hdfs/tmp</value> 
        </property>  
        <property>
        <!-- 指定Zookeeper地址（2181端口参考zoo.cfg配置文件） -->
        <name>ha.zookeeper.quorum</name>
        <value>master:2181,master1:2181,slave1:2181,slave2:2181</value>
    </property>
</configuration>
```

mapred-site.xml
```xml
<configuration>
        <property>
                <name>mapreduce.framework.name</name>
                <value>yarn</value>
        </property>
        <-- 连接jobtrack服务器的配置项，默认不写是local，map数1，reduce数1 -->
        <property>
                <name>mapred.job.tracker</name>
                <value>http://master02:9001</value>
        </property>
        <property>
                <name>mapreduce.jobhistory.address</name>
                <value>master02:10020</value>
        </property>
        <property>
                <name>mapreduce.jobhistory.webapp.address</name>
                <value>master02:19888</value>
        </property>
        <!--mapred做本地计算的目录，可配置多块硬盘用逗号分隔-->  
        <property> 
                <name>mapred.local.dir</name>  
                <value>/root/hadoop/yarn/local</value> 
        </property> 
</configuration>
```

master1和master2上的另外2个配置文件如下：
hdfs-site.xml：
```xml
<configuration> 
        <!-- 指定hdfs的nameservice为ns，需要和core-site.xml中的保持一致 -->  
        <property> 
                <name>dfs.nameservices</name>  
                <value>ns</value> 
        </property>  
        <!-- ns下面有两个NameNode，分别是nn1，nn2 -->  
        <property> 
                <name>dfs.ha.namenodes.ns</name>  
                <value>nn1,nn2</value> 
        </property>  
        <!-- nn1的RPC通信地址 -->  
        <property> 
                <name>dfs.namenode.rpc-address.ns.nn1</name>  
                <value>master02:9000</value> 
        </property>  
        <!-- nn1的http通信地址 -->  
        <property> 
                <name>dfs.namenode.http-address.ns.nn1</name>  
                <value>master02:50070</value> 
        </property>  
        <!-- nn2的RPC通信地址 -->  
        <property> 
                <name>dfs.namenode.rpc-address.ns.nn2</name>  
                <value>master01:9000</value> 
        </property>  
        <!-- nn2的http通信地址 -->  
        <property> 
                <name>dfs.namenode.http-address.ns.nn2</name>  
                <value>master01:50070</value> 
        </property>  
        <!-- 指定NameNode的元数据在JournalNode上的存放位置 -->  
        <property> 
                <name>dfs.namenode.shared.edits.dir</name>  
                <value>qjournal://slave01:8485;slave02:8485/ns</value> 
        </property>  
        <!-- 指定JournalNode在本地磁盘存放数据的位置 -->  
        <!-- 在journalNode机器上要建立该目录 -->  
        <property> 
                <name>dfs.journalnode.edits.dir</name>  
                <value>/root/hadoop/hdfs/journalnode</value> 
        </property>  
        <!-- 开启NameNode故障时自动切换 -->  
        <property> 
                <name>dfs.ha.automatic-failover.enabled</name>  
                <value>true</value> 
        </property>  
        <!-- 配置失败自动切换实现方式 -->  
        <property> 
                <name>dfs.client.failover.proxy.provider.ns</name>  
                <value>org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider</value> 
        </property>  
        <!-- 配置隔离机制 -->  
        <property> 
                <name>dfs.ha.fencing.methods</name>  
                <value>sshfence
                shell(/bin/true)</value> 
        </property>  
        <!-- 使用隔离机制时需要ssh免登陆 -->  
        <property> 
                <name>dfs.ha.fencing.ssh.private-key-files</name>  
                <value>/root/.ssh/id_rsa</value> 
        </property>  
        <!-- namenode存储位置 -->  
        <property> 
                <name>dfs.namenode.name.dir</name>  
                <value>/root/hadoop/hdfs/name</value> 
        </property>  
        <!-- dataode存储位置 -->  
        <property> 
                <name>dfs.datanode.data.dir</name>  
                <value>/root/hadoop/hdfs/data</value> 
        </property>  
        <!-- 副本数量根据自己的需求配置，这里配置2个 -->  
        <property> 
                <name>dfs.replication</name>  
                <value>2</value> 
        </property>  
        <!-- 在dfsclient中记录慢io警告的阈值-->  
        <property> 
                <name>dfs.client.slow.io.warning.threshold.ms</name>  
                <value>90000</value> 
        </property>  
        <!-- datanode的心跳时间间隔，单位为秒-->  
        <property> 
                <name>dfs.heartbeat.interval</name>  
                <value>8</value> 
        </property>  
        <!-- 心跳检测的时间间隔，单位是毫秒-->  
        <property> 
                <name>dfs.namenode.heartbeat.recheck-interval</name>  
                <value>90000</value> 
        </property>  
        <!-- namenode的checkpoint周期，单位秒。HA部署时，每经过一个周期，standby节点进行fsimage和editlog的合并-->  
        <property> 
                <name>dfs.namenode.checkpoint.preiod</name>  
                <value>3600</value> 
        </property>
        <!-- namenode的checkpoint的最大操作次数。HA部署时，hdfs操作次数超过这个数量，standby节点进行fsimage和editlog的合并-->  
        <property> 
                <name>dfs.namenode.checkpoint.txns</name>  
                <value>1000000</value> 
        </property>
        <!--块报告的时间间隔，单位是毫秒-->  
        <property> 
                <name>dfs.blockreport.intervalMsec</name>  
                <value>1800000</value> 
        </property>  
        <!--datanode以秒为单位扫描数据目录，并协调内存块和磁盘上的块之间的差异-->  
        <property> 
                <name>dfs.datanode.directoryscan.interval</name>  
                <value>1800</value> 
        </property>  
        <property> 
                <name>dfs.datanode.max.xcievers</name>  
                <value>8000</value> 
        </property>  
</configuration>
```

yarn-site.xml：
```xml
<configuration>
    <property>
        <!-- 启用ResourceManager的HA功能 -->
        <name>yarn.resourcemanager.ha.enabled</name>
        <value>true</value>
    </property>
    <property>
        <!-- 开启ResourceManager失败自动切换 -->
        <name>yarn.resourcemanager.ha.automatic-failover.enabled</name>
        <value>true</value>
    </property>
    <property>
        <!-- 给ResourceManager HA集群命名id -->
        <name>yarn.resourcemanager.cluster-id</name>
        <value>hdcluster</value>
    </property>
    <property>
        <!-- 指定ResourceManager HA有哪些节点 -->
        <name>yarn.resourcemanager.ha.rm-ids</name>
        <value>rm1,rm2</value>
    </property>
    <property>
        <!-- 指定第一个节点在哪一台机器 -->
        <name>yarn.resourcemanager.hostname.rm1</name>
        <value>master02</value>
    </property>
    <property>
        <!-- 指定第二个节点在那一台机器 -->
        <name>yarn.resourcemanager.hostname.rm2</name>
        <value>master01</value>
    </property>
    <property>
        <!-- 指定ResourceManager HA所用的Zookeeper节点 -->
        <name>yarn.resourcemanager.zk-address</name>
        <value>master02:2181,master01:2181,slave01:2181,slave02:2181</value>
    </property>
    <!--ZKRMStateStore连接的zk地址-->  
    <property> 
        <name>yarn.resourcemanager.zk.state-store.address</name>  
        <value>master02:2181,master01:2181,slave01:2181,slave02:2181</value> 
    </property> 
    <property>
        <!-- 启用RM重启的功能，默认为false -->
        <name>yarn.resourcemanager.recovery.enabled</name>
        <value>true</value>
    </property>
    <property>
        <!-- 用于状态存储的类 -->
        <name>yarn.resourcemanager.store.class</name>
        <value>org.apache.hadoop.yarn.server.resourcemanager.recovery.ZKRMStateStore</value>
    </property>
    <property>
        <!-- NodeManager启用server的方式 -->
        <name>yarn-nodemanager.aux-services</name>
        <value>mapreduce_shuffle</value>
    </property>
    <property>
        <!-- NodeManager启用server使用算法的类 -->
        <name>yarn-nodemanager.aux-services.mapreduce.shuffle.class</name>
        <value>org.apache.hadoop.mapred.ShuffleHandler</value>
    </property>
    <property>
        <!-- 启用日志聚合功能 -->
        <name>yarn.log-aggregation-enable</name>
        <value>true</value>
    </property>
    <property>
        <!-- 聚集的日志在HDFS上保存最长的时间 -->
        <name>yarn.log-aggregation.retain-seconds</name>
        <value>106800</value>
    </property>
    <property>
        <!--NN上日志聚集的位置（聚合日志后在hdfs的存放地址）-->
        <name>yarn.nodemanager.remote-app-log-dir</name>
        <value>/yarn-logs</value>
    </property>
    <!--hdfs上集合日志后的存放地址由 ${remote-app-log-dir}/${user}/{thisParam}构成-->  
    <property> 
        <name>yarn.nodemanager.remote-app-log-dir-suffix</name>  
        <value>logs</value> 
    </property>  
    <!--存储本地化文件的目录列表。应用程序的本地化文件目录位于：$ {yarn.nodemanager.local-dirs} / usercache / $ {user} / appcache / application _ $ {appid}。单个容器的工作目录（称为container _ $ {contid}）将是其子目录-->  
    <property> 
        <name>yarn.nodemanager.local-dirs</name>  
        <value>/root/hadoop/yarn/local</value> 
    </property>  
    <!--默认为/tmp/hadoop-yarn/staging。MR作业在提交时所使用的临时目录， 是一个本地路径-->  
    <property> 
        <name>yarn.app.mapreduce.am.staging-dir</name>  
        <value>/root/hadoop/yarn/staging</value> 
    </property> 
</configuration>
```

slave1及slave2有所不同的配置如下：
hdfs-site.xml：
```xml
<!-- nn1的RPC通信地址 -->
<property>
        <name>dfs.namenode.rpc-address.ns.nn1</name>
        <value>master2:9000</value>
</property>
<!-- nn1的http通信地址 -->
<property>
        <name>dfs.namenode.http-address.ns.nn1</name>
        <value>master2:50070</value>
</property>
<!-- nn2的RPC通信地址 -->
<property>
        <name>dfs.namenode.rpc-address.ns.nn2</name>
        <value>master1:9000</value>
</property>
<!-- nn2的http通信地址 -->
<property>
        <name>dfs.namenode.http-address.ns.nn2</name>
        <value>master1:50070</value>
</property>
<!-- 指定NameNode的元数据在JournalNode上的存放位置 -->
<property>
        <name>dfs.namenode.shared.edits.dir</name>
        <value>qjournal://slave01:8485;slave02:8485/ns</value>
</property>
```

yarn-site.xml：
```xml
<property>
    <!-- 指定第一个节点在那一台机器 -->
    <name>yarn.resourcemanager.hostname.rm1</name>
    <value>master2</value>
</property>
<property>
    <!-- 指定第二个节点在那一台机器 -->
    <name>yarn.resourcemanager.hostname.rm2</name>
    <value>master1</value>
</property>
<property>
    <!-- 指定ResourceManager HA所用的Zookeeper节点 -->
    <name>yarn.resourcemanager.zk-address</name>
    <value>master02:2181,master01:2181,slave01:2181,slave02:2181</value>
</property>
<!--ZKRMStateStore连接的zk地址-->
<property>
    <name>yarn.resourcemanager.zk.state-store.address</name>
    <value>master02:2181,master01:2181,slave01:2181,slave02:2181</value>
</property>
```

3. 配置zookeeper
首先复制zoo_sample.cfg并重命名为zoo.cfg，再在各个节点的apache-zookeeper-3.7.1-bin目录中创建data和logs文件夹，随后编辑zoo.cfg文件：
```
dataDir=/root/zookeeper/apache-zookeeper-3.7.1-bin/data
dataLogDir=/root/zookeeper/apache-zookeeper-3.7.1-bin/logs
quorumListenOnAllIPs=true
server.1=master02:2888:3888
server.2=master01:2888:3888
server.3=slave01:2888:3888
server.4=slave02:2888:3888:observer 
```
上面两条路径便是创建的data和logs文件夹的路径，创建myid文件的过程不再赘述。由于slave2上的ZK为observer，故需要单独在其zoo.cfg中添加：
`peerType=observer`

4. 创建有关文件夹
在上面的xml文件中我配置了自己创建的数据存放路径，故需在/root/hadoop下创建hdfs、yarn文件夹，前者中创建data、name、tmp、pid、journalnode、logs文件夹，后者中创建logs、local、staging文件夹。

### 修改前后端
1. 修改application.yml
在`hadoop.name-node: hdfs://master1:9000`中，master1应是启动HA Hadoop后activa NN的公网IP。另外CorsConfig.java中`.allowedOrigins("http://localhost:8080")`与前一项目同理。

2. 修改前端请求地址
Home.vue及UploadFile.vue中axios请求地址的修改与前一项目同理。

### 启动项目
#### 启动Hadoop HA集群
初次启动集群时需要进行ZK及NN的初始化，全部步骤如下：

1. 启动zookeeper集群(master2、master1、slave1、slave2)
    zkServer.sh start
    #运行jps命令,对应机器多了QuorumPeerMain的进程

2. 启动journalnode(slave1、slave2)
    hadoop-daemon.sh start journalnode
    #运行jps命令可以看到多了JournalNode进程

3. 格式化namenode(master2)
    hdfs namenode -format

4. 格式化ZKFC(初始化 HA 状态到 zk)（master2）
    hdfs zkfc -formatZK 

5. 启动 namenode1（master2）
    hadoop-daemon.sh start namenode
    #运行jps命令可以看到多了NameNode进程

6. 同步 namenode（master1）
    hdfs namenode -bootstrapStandby

7. 启动 namenode2（master1）
    hadoop-daemon.sh start namenode
    #运行jps命令可以看到多了NameNode进程

8. 启动ZookeeperFailoverController（master2、master1）
    hadoop-daemon.sh start zkfc
    #运行jps命令可以看到多了DFSZKFailoverController进程.
    #哪台机器先启动zkfc，哪台就是active

9. 启动 datanode（slave1、slave2）
    hadoop-daemon.sh start datanode
    #运行jps命令，多了DataNode进程

10. 启动 resourcemanager（master1、master2）
    yarn-daemon.sh start resourcemanager
    #启动时先启动master1的rm，这样将master1的rm置为active（也可以通过命令手动切换）
    #运行jps，多了ResourceManager进程

11. 启动 nodemanager（slave1、slave2）
    yarn-daemon.sh start nodemanager
    #运行jps，多了NodeManager进程

12. 启动 historyserver（master2、master1）
    mr-jobhistory-daemon.sh start historyserver
    #运行jps，多了JobHistoryServer进程

#### 启动前后端
后端运行MyFileSystemApplication，进入前端项目文件夹后`npm run dev`，访问 http://localhost:8080 即可打开文件系统。需要注意，在Home.vue的created方法中访问的文件地址是/user，这是由于访问的地址不能为空。因此读者在初次使用时需要先上传文件才能看到页面中有数据显示，并且文件根目录应为/user（当然也可以根据自己想要的根目录名称修改created方法的访问地址）。注意上传文件时目标路径应使用HDFS上的绝对路径，下载文件时目标路径也应使用本地的绝对路径。

至此，文件管理系统便可正常使用了。

### 附录
#### 集群日常启动与关闭
##### 启动
启动 zookeeper（master2、master1、slave1、slave2）
    zkServer.sh start

启动 journalnode（slave1、slave2）
    hadoop-daemons.sh start journalnode

启动 namenode（master2、master1）
    hadoop-daemon.sh start namenode

启动ZookeeperFailoverController（master2、master1）
    hadoop-daemon.sh start zkfc
    ps： 哪台机器先启动zkfc，哪台就是active

启动 datanode（slave1、slave2）
    hadoop-daemons.sh start datanode

启动 resourcemanager（master1、master2）
    yarn-daemon.sh start resourcemanager
    #ps: 先启动的为active
启动 nodemanager（slave1、slave2）
    yarn-daemon.sh start nodemanager

启动 historyserver（master2、master1）
    mr-jobhistory-daemon.sh start historyserver

##### 关闭
关闭 historyserver（master2、master1）
    mr-jobhistory-daemon.sh stop historyserver
    
关闭 nodemanager（slave1、slave2）
    yarn-daemon.sh stop nodemanager

关闭 resourcemanager（master1、master2）
    yarn-daemon.sh stop resourcemanager
    
关闭 datanode （slave1、slave2）
    hadoop-daemons.sh stop datanode

关闭 ZookeeperFailoverController (master2、master1)
    hadoop-daemon.sh stop zkfc
    
关闭 namenode（master2、master1）
    hadoop-daemon.sh stop namenode

关闭 journalnode（slave1、slave2）
    hadoop-daemons.sh stop journalnode

关闭 zookeeper（master2、master1、slave1、slave2）
    zkServer.sh stop

##### 简化版
除了zookeeper需要另外分别手动启动外，其余命令可以简化为分别在master2和master1上执行`start-all.sh`和`stop-all.sh`。不过该方法非正规方法，比如它实际上会启动或关闭DN两次（尽管这不会出现问题，因为DN在运行时是不会再次启动的）。