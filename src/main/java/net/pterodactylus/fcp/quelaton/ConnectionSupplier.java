package net.pterodactylus.fcp.quelaton;

import java.io.IOException;
import java.util.function.Supplier;

import net.pterodactylus.fcp.FcpConnection;

/**
 * Extension of the {@link Supplier} interfaces that declares an {@link IOException}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
@FunctionalInterface
interface ConnectionSupplier {

	FcpConnection get() throws IOException;

}
