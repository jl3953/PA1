----------------------------------------------------------------------------
                            Programming Assignment #1 

                                Jennifer Lam jl3953
                            Professor Augustin Chaintreau
                                    CSEE W4119
                                  March 14, 2015
---------------------------------------------------------------------------

===========================================================================
                                FILES INCLUDED
===========================================================================
Client.java
ClientListener.java
ClientObject.java
CommandObject.java
ConnectionHandler.java
HeartbeatChecker.java
HeartBeat.java
MailNode.java
MessageObject.java
Server.java

credentials.txt
Makefile
READEME.txt

===========================================================================
                                 SPECIALTIES
===========================================================================
- The underlying sockets follow TCP/IP protocol. Thus, the messages sent
  should guarantee delievery (see "Important Notes" section for more detail).

===========================================================================
                            COMPILATION/RUNNING PROGRAM
===========================================================================
To compile everything, simply type "make". The Makefile will take care of the
rest for you.
>> make

To start server:
>> java Server <port>

To connect with multiple clients, type the following command for each.
You immediately be prompted for a login. Type in one of the usernames/password
combinations from "credentials.txt". The login assumes that the user will not
type in a blank username or password:
>> java Client <server-host> <server-port>

You're ready to start chatting! If you already have a client logged in, that
client will see that a broadcasted message that a new user has logged in.

Notes (further elaborated on in "Important Notes" section)
---------------------------------------------------------
- The login assumes that the user will not type in a blank username or password.
- If, immediately after the login, a BindException occurs, this means that the
  Client tried to listen on a port that was already taken by another program
  on the clic machine. Simply restart the server and clients.
- The program runs well when both Server and clients are running on the same
  machine. However, running a client on another machine produces connectivity
  issues that I have diagnosed but do not have a solution to.

==============================================================================
                            SAMPLE COMMANDS
==============================================================================
- Client Jennifer>>message Pokemon Be a master!
This will result in Pokemon receiving >>Jennifer: Be a master!
- Client Jennifer>>broadcast Come to Networks!
This will result in both Augustin and Pokemon receiving the message "Come to
Networks!"
- Client Pokemon>>online
This will result in all online clients being displayed
- Client Augustin>>block Jennifer
This will result in Jennifer not being able to send messages or broadcasts to
Augustin. When Client Jennifer tries, both the message and broadcast will tell
her that she has been blocked.
- Client Augustin>>unblock Jennifer
This works in the opposite fashion as "block", described above
- Client Pokemon>>private Jennifer my secret
This results in client Pokemon attaining Jennifer's IP and port address.
Pokemon will then use this information to directly send a message to Jennifer
instead of going through the server. If Jennifer is offline when the message
goes out, a printed line will notify Pokemon that he can send Jennifer an
offline message through the server.

Example:


Client: Jennifer             Client: Pokemon             Client: Augustin
--------------               ---------------            ----------------
>>broadcast hey all!         
                             >>Jennifer: hey all!       >>Jennifer: hey all!
                             >>online
                             >>Jennifer
                             >>Augustin
                                                        >>block Jennifer
                                                        >>User Jennifer has been blocked
>>message Augustin hi!
>>User has blocked you.
                                                        >>unblock Jennifer
>>message Augustin you're back!                         
                                                        >>Jennifer: you're back!
                             >>private Jennifer my secret
>>Pokemon: my secret

==============================================================================
                    PROGRAM DESCRIPTION AND STRUCTURE
==============================================================================

Server
------------------------------
Server.java
ClientObject.java
ConnectionHandler.java
HeartbeatChecker.java
MailNode.java
MessageObject.java

In order to 1) handle multiple, non-permanent connections, and
2) periodically update clients that are online/offline via heartbeats, the
server (Server.java) spawns several threads. One thread
(HeartbeatChecker.java) is solely in charge of periodically checking which clients have not sent a heartbeat for over ~30
seconds (this actual time interval is more like 35 seconds. This mismatch was
implemented on purpose to avoid a race condition in which the server checks
for the most recent heartbeat as a heartbeat is coming in. If the operating
system always schedules the server to check for heartbeats first right before
scheduling the heartbeats, the server is theoretically missing them all). The
main thread (Server.java) runs a listening server socket, and its sole job is
just to authenticate and accept (or refuse, if the authentication is wrong)
clients. For every connection that comes in, a new thread is spawned to serve
the client's request. To implement non-permanent TCP connection, this new
thread is immediately terminated as soon as the client has been served. To
make this non-permanent connection clear to the user, the server will print out the protocol message
when it receives a request from a client, and it will print out a termination
message when the connection has been terminated.

To keep track of all clients and their states, the server creates
and initiates a ConcurrentHashMap that maps a username to a ClientObject. A
ClientObject (ClientObject.java) is defined in order to keep track of a
Client's current state (username, password, whether or not client is online,
etc). The ClientObject also implements a mailbox that stores all messages that
it received when it was offline. The mailbox is implemented by a linked list
of MailNode objects (MailNode.java). Each MailNode contains the sender of the
offline message and the message itself. As soon as the client logs back on,
the server immediately checks to see if it has any stored mail and sends
everything in its mailbox.

Most of the client request processing happens in ConnectionHandler.java.
ConnectionHandler uses MessageObject to represent the request and help parse
the line. It then checks against the possible list of actions (message,
logout, broadcast, private, etc), and takes the appropriate course of action
depending on the match.

Client
-----------------------------
Client.java
ClientListner.java
CommandObject.java
HeartBeat.java

The client needs to both listen for incoming communications AND send requests
to the server AND periodically send heartbeats. Thus, it runs three
threads--one to listen for server messages (ClientListener.java), one to send
heartbeats ever 30 seconds (HeartBeat.java), and one to process commands from
the user (Client.java). When processing commands from the user, Client.java
parses and stores strings with CommandObject.java, which represents different
fields of a command. It will then formate each command according to the
communication protocol and send that message to the server. The thread listening for server communication will print
out to terminal anything the server sends to the client, and it will terminate
the client when instructed to do so by the server (see "Communication
Protocols" section for when this happens). When a client chooses to log out

==============================================================================
                        COMMUNICATION PROTOCOLS
==============================================================================

Authentication
---------------
When a user first types in her username and password 1) it's right on the
first try. In this case, the server sends back "OK," and client proceeds to be
logged on. 2) It isn't right, server sends back "TRY_AGAIN", and client tries
again twice. 3) Password is wrong on the the third time, server sends back
"THIRD_TIME", and user is notified that she's blocked for some time. 4) The
user has already been blocked. Server sends back "BLOCKED", and the user is
notified. 5) A user is logged on with the same username from another
terminal--server sends "EXIT_NOW" to the other user, and that user immediately
terminates with a explanatory message.

Client-Server communication
---------------------------
When client communicates to the server, it first sends an initial message with
only her name--the server uses this name to authenticate that this user is
indeed online. After that, the message looks like this:

sender:<username> action:<send or serveraction> field3:<variable> field4:<variable>

<username> is the username of the sending client. The action field is to
indicate to the server whether it needs to send a message to (an)other
client(s), or whether it simply needs to modify the state of the sending
client. "send" means that a message needs to be sent, "serveraction" means
that the server only needs to modify client state. field3 and field4 are
variable depending on the actual command. ("nope" is used as a placeholder for
commands that do not have more than one parameter)

Client Jennifer
>>message <recipient> <payload>
sender:Jennifer action:send field3:<recipient> field4:<payload>
>>broadcast <payload>
sender:Jennifer action:send field3:ALL field4:<payload>
>>online
sender:Jennifer action:serveraction field3:nope field4:nope
>>block <target>
sender:Jennifer action:serveraction field3:block field4:<target>
>>unblock <target>
sender:Jennifer action:serveraction field3:unblock field4:<target>
>>logout
sender:Jennifer action:serveraction field3:logout field4:nope
>>getaddress <target>
sender:Jennifer action:serveraction field3:getaddress field4:<target>
>>private <recipient> <payload>
sender:Jennifer action:serveraction field3:private field4:<target>
(The protocol request for private looks similar to getaddress)
**Heartbeat**
sender:Jennifer action:send field3:HEARTBEAT field4:<client's host machine>/<client's port>
(Server uses information from the heartbeat to update a client's server,
port, and state. This was a design decision, because it makes sense that the
heartbeat notifies the server of the client's current state.)

===============================================================================
                            IMPORTANT NOTES
==============================================================================
- The login assumes that the user is smart enough not to input blank usernames
  or passwords.
- Sometimes, Client.java will generate a BindException immediately after
  login. This is because clients randomly choose a port number to listen on.
  This is a design decision I made to avoid hardcoding a port number and
  potentially having clients on the same machine clash with each other.
- The BindException issue means that if you try to connect to the server on
  the same machine immediately after you call "logout", you also see
  BindException errors. The only way to solve this is to wait for the machine
  to stop binding and releases its resources.
- The message system works well when both Server and clients are running on
  the same machine. However, running a client on another machine produces
  connectivity issues. Through thorough debugging, I found that when Server
  and Client run on different machines, the Server is incapable of reading from
  the socket's InputStream (lines 64-64 in ConnectionHandler.java). Because this
  issue does not arise when Server and Client run on the same machine, I
  concluded that perhaps there is a communication barrier outside of my program
  that is related to what kinds of communication methods the clic machines will
  not allow.

RACE CONDITIONS
----------------------------------------------------------
RACE CONDITIONS ARE THE ROOT OF ALMOST EVERY BUG THAT OCCURS IN THIS
ASSIGNMENT. These bugs cannot be reliably reproduced, and they to
occur--albeit somewhat infrequently--at random. I have run the server many
times with the same input. Sometimes, it will go through 4-5 consecutive runs
without a problem. Other times, the server will crash on a single test. 

One bug that I see now and then is when only part of a client request actually
arrives to the server. The server
sees only a partial request that it cannot process correctly, so it terminates the connection with an
exception, and simply moves on. From the client's side however, it
looks as if the server is not getting its request, when in reality, the
request arrived but was not processed. I diagnosed this bug by going through
the server's logging of all client request; some lines are
cut off. Usually, a single missing character has no effect on performance, but when an entire field is
lost from the message, the server cannot read it properly. I
categorized this as a race condition, because this usually happens when a
heartbeat and a request are competing to communicate with the server, and the
bug pops up a little more when you send more heartbeats in a shorter interval.
In the latter situation, the logging shows me that heartbeats seem to be
fighting with each other to talk to the server. The good thing is that this bug usually fixes
itself--if you wait for a period when there is less traffic on the server (aka
not everyone is trying to send a heartbeat at the same time)--your command
usually gets through.

Two rarer bugs that occur unpredictably are that sometimes, the server will
crash on an ArrayOutOfBoundsException, or the listening part of a Client
will. I do not know how to reproduce these bugs. However, I noticed that they
become there are many clients (instead of
just a few) trying to communicate, and you are more likely to see them when
the server has been running for a while. These bugs do not always
occur--they're fairly rare, and often, the server will run fine. But when they do occur, the entire server
crashes, and nothing can get through anymore. Due to my observations, I have
categorized these bugs as race conditions.
