# jsch-agent-proxy
a proxy to ssh-agent and Pageant in Java.

## Description
**jsch-agent-proxy** is a proxy program to [OpenSSH](http://www.openssh.com/)'s [ssh-agent](http://en.wikipedia.org/wiki/Ssh-agent) and [Pageant](http://en.wikipedia.org/wiki/PuTTY#Applications)
included [Putty](http://www.chiark.greenend.org.uk/~sgtatham/putty/).  It will be easily integrated into [JSch](http://www.jcraft.com/jsch/), and users
will be allowed to use those programs in authentications.
This software has been developed for JSch, but it will be easily
applicable to other ssh2 implementations in Java.
This software is licensed under [BSD style license](https://github.com/ymnk/jsch-agent-proxy/blob/master/LICENSE.txt).


## Build from Source
    $ git clone git://github.com/ymnk/jsch-agent-proxy.git
    $ mkdir jsch-agent-proxy/lib
    $ (cd jsch-agent-proxy/lib; \
       wget --no-check-certificate \
          https://github.com/downloads/twall/jna/jna.jar)
    $ (cd jsch-agent-proxy/lib; \
       wget --no-check-certificate \
          https://github.com/downloads/twall/jna/platform.jar)
    $ wget http://www.jcraft.com/jsch/jsch-0.1.46-rc1.zip
    $ unzip jsch-0.1.46-rc1.zip
    $ (cd jsch-0.1.46; ant; cp dist/lib/jsch*.jar ../jsch-agent-proxy/lib/)
    $ cd jsch-agent-proxy
    $ ant


## Examples
+ [examples/UsingPageant.java](https://github.com/ymnk/jsch-agent-proxy/blob/master/examples/UsingPageant.java)  
    This sample demonstrates how to get accesses to Pageant. 
 
		$ ant examples
		$ (cd examples; java -classpath "../lib/*:../dist/lib/*":. UsingPageant)
+ [examples/UsingSSHAent.java](https://github.com/ymnk/jsch-agent-proxy/blob/master/examples/UsingSSHAgent.java)  
    This sample demonstrates how to get accesses to ssh-agent.  

		$ ant examples
		$ (cd examples; java -classpath "../lib/*:../dist/lib/*":. UsingSSHAgent)
+ [examples/JSchWithAgentProxy.java](https://github.com/ymnk/jsch-agent-proxy/blob/master/examples/JSchWithAgentProxy.java)  
    This sample demonstrates how to integrate jsch-agent-proxy into JSch.  

		$ ant examples
		$ (cd examples; \
		   java -classpath "../lib/*:../dist/lib/*":. \
		   JSchWithAgentProxy foo@bar.com)

## Dependencies
To work as a proxy to ssh-agent and Pageant,
the current implementation depends on the following software
 
+ JNA: https://github.com/twall/jna
+ junixsocket: http://code.google.com/p/junixsocket/

As for connections to ssh-agent, unix domain sockets must be
handled, and the current implementation has been using JNA or junixsocket.  Refer to following classes,
 
+ [com.jcraft.jsch.agentproxy.usocket.JNAUSocketFactory](https://github.com/ymnk/jsch-agent-proxy/blob/master/src/main/java/com/jcraft/jsch/agentproxy/usocket/JNAUSocketFactory.java)
+ [com.jcraft.jsch.agentproxy.usocket.JUnixDomainSocketFactory](https://github.com/ymnk/jsch-agent-proxy/blob/master/src/main/java/com/jcraft/jsch/agentproxy/usocket/JUnixDomainSocketFactory.java)

If you can use JNA, the later is not needed.

As for connections to Pageant, win32 APIs on Windows must be
handled, and JNA has been used in the current implementation.  Refer to the following class,

+ [com.jcraft.jsch.agentproxy.connector.PageantConnector](https://github.com/ymnk/jsch-agent-proxy/blob/master/src/main/java/com/jcraft/jsch/agentproxy/connector/PageantConnector.java)


If you want to be free from JNA and junixsocket,
implement following interfaces without them,

+ [com.jcraft.jsch.agentproxy.Connector](https://github.com/ymnk/jsch-agent-proxy/blob/master/src/main/java/com/jcraft/jsch/agentproxy/Connector.java)
+ [com.jcraft.jsch.agentproxy.USocketFactory](https://github.com/ymnk/jsch-agent-proxy/blob/master/src/main/java/com/jcraft/jsch/agentproxy/USocketFactory.java)


