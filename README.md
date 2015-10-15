# jCFPlib – an FCPv2 client library
Developers of Freenet client applications have two possibilities to integrate their software with Freenet: they can write a plugin for Freenet, or they can use FCPv2 to talk to the Freenet node.

## Freenet Plugins
Plugins for Freenet have the advantage of always running on the same machine and being “closer” to Freenet itself, making some of the interactions with the node easier. However, the plugin competes with the node for common resources, most notably CPU time and memory. Also, plugins need to be written in Java.

## Client Applications
Client applications on the other hand are free to use whatever language they want. The applications can run on different machines, allowing users to run their node on a different machine than the client (the most common scenario would probably be the node running on a remote machine and the client application running on the local machine), and resources used by the client are not counted towards Freenet’s resource usage.

# Using jFCPlib
jFCPlib is a library that helps with the creation of client applications. It offers a low-level FCP implementation, and a high-level client implementation that allows quick access to most of the features available in FCPv2. The difference between the two APIs is outlined below; as an example, a file is to be inserted from an `InputStream`.

## Low-Level API
The low-level API is centered around the class `FcpConnection` and the `FcpListener` interface. It requires you to be familiar with all the gritty details of FCPv2 but offers you the most control and information over what’s happening, such as detailed error codes.

<pre>
FcpConnection fcpConnection = new FcpConnection("node", 9481);
fcpConnection.addListener(fcpListener);
fcpConnection.connect();
fcpConnection.sendMessage(new ClientHello("ClientName"));
</pre>

In your `FcpListener` implementation you have to implement the `receivedNodeHello` method to receive the node’s reply message. Now you can create the `ClientPut` command and send it to the node.

<pre>
InputStream inputStream = createInputStream();
ClientPut clientPut = new ClientPut("CHK@", "client-insert-identifier");
clientPut.setTargetFilename("test.html");
clientPut.setPayloadInputStream(inputStream);
clientPut.setDataLength(12345);
fcpConnection.sendMessage(clientPut);
</pre>

Now, in your `FcpListener` implementation you implement the `receivedPutSuccessful` method to get notified when your insert is finished. There are multiple other methods you might want to implement, too, such as

* `receivedSimpleProgress` (sent by the node to inform you of inserted blocks, requires a call to `clientPut.setVerbosity(Verbosity.PROGRESS)`),
* `receivedPutFetchable` (sent by the node when the data is fetchable by other nodes),
* `receivedURIGenerated` (sent by the node once the final URI of the insert has been calculated),
* `receivedPutFailed` (sent when the operation failed),
* `receivedClosedConnectionDuplicateClientName` (if some other connection with the same client name was established), or
* `receivedProtocolError` (if something else went wrong).

In all methods you have to remember to check the identifier of the incoming message against the identifier of your original `ClientPut` message. In case of a connection failure you have to reconnect to the node and re-register your listener on the new `FcpConnection`.

So, using the low-level API requires detailed knowledge about all the dirty FCPv2 internals.

## High-Level API
The high-level API (code-named “Quelaton”) requires only very little knowledge of the FCP protocol internals. It offers a Fluent-style API that uses functional interfaces to reduce the number of errors the user can make, uses Java’s `Future` object to allow running all commands asynchronously in a background thread, and declares many return types as `Optional` to signify the possibility of an operation not succeeding (like a `ClientGet` for a non-existing key).

<pre>
FcpClient fcpClient = new DefaultFcpClient(threadPool, "localhost", 9481, () -> "ClientName");
Future&lt;Optional&lt;Key&gt;&gt; keyFuture = fcpClient.clientPut()
	.named("test.html")
	.from(inputStream)
	.length(12345)
	.uri("CHK@")
	.execute();
if (keyFuture.get().isPresent()) {
	// a key was generated, the insert succeeded.
	...
</pre>

In case of a connection failure the `Future.get()` will throw an exception in which case you can simply repeat the `FcpClient.clientPut()` operation.

# Compiling jFCPlib

jFCPlib uses [Maven](http://maven.apache.org/) as a build tool. And because it does, installing Maven, checking out the source and calling

<pre>
mvn clean package
</pre>

should be enough to create a usable JAR file in the `target/` directory. If you use a build system like Maven or Gradle, you can call

<pre>
mvn install
</pre>

to install jFCPlib to your local repository and use `net.pterodactylus:jFCPlib:<version>` as dependency.