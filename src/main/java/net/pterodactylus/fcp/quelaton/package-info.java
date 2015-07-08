/**
 * Quelaton – a high-level FCP client with an easy-to-use fluent interface.
 * <p>
 * Quelaton can be used by FCP applications to easily perform the most commons tasks, such as retrieving or inserting
 * data, or generating keys. The main entry class is {@link net.pterodactylus.fcp.quelaton.FcpClient} which can create
 * FCP commands which can then be fed arguments or data and which can be executed, yielding a result in form of a
 * {@link java.util.concurrent.Future}.
 * <pre>
 * FcpClient fcpClient = ...
 * Optional&lt;Data&gt; data = fcpClient.clientGet().uri("KSK@gpl.txt").{@link java.util.concurrent.Future#get()
 * get()};
 * if (data.isPresent()) {
 *     ... process data ...
 * }
 * </pre>
 * <p>
 * As is the case with {@link java.util.concurrent.Future}’s, the call to {@link java.util.concurrent.Future#get()}
 * will block until the request has finished. This allows starting the request (by calling
 * {@link net.pterodactylus.fcp.quelaton.ClientGetCommand#uri(String)}) and waiting for it to finish in a different
 * thread. Using Guava’s {@link com.google.common.util.concurrent.ListenableFuture} allows client applications to
 * register listeners that will automatically be run once the request finishes.
 *
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
package net.pterodactylus.fcp.quelaton;
