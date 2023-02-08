package net.pterodactylus.fcp;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Test;

import static net.pterodactylus.fcp.AddPeer.Trust.HIGH;
import static net.pterodactylus.fcp.AddPeer.Trust.LOW;
import static net.pterodactylus.fcp.AddPeer.Trust.NORMAL;
import static net.pterodactylus.fcp.AddPeer.Visibility.NAME_ONLY;
import static net.pterodactylus.fcp.AddPeer.Visibility.NO;
import static net.pterodactylus.fcp.AddPeer.Visibility.YES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

/**
 * Unit test for {@link AddPeer}.
 */
public class AddPeerTest extends AbstractFcpMessageTest {

	@Test
	public void canCreateAddPeerWithFile() throws Exception {
		AddPeer addPeer = new AddPeer(LOW, NO, "file");
		assertThat(encodeMessage(addPeer), isMessage("AddPeer",
				"File=file",
				"Trust=LOW",
				"Visibility=NO"
		));
	}

	@Test
	public void canCreateAddPeerWithIdentifierAndFile() throws Exception {
		AddPeer addPeer = new AddPeer(LOW, NO, "identifier", "file");
		assertThat(encodeMessage(addPeer), isMessage("AddPeer",
				"File=file",
				"Identifier=identifier",
				"Trust=LOW",
				"Visibility=NO"
		));
	}

	@Test
	public void canCreateAddPeerWithUrl() throws Exception {
		AddPeer addPeer = new AddPeer(NORMAL, NAME_ONLY, new URL("http://url"));
		assertThat(encodeMessage(addPeer), isMessage("AddPeer",
				"URL=http://url",
				"Trust=NORMAL",
				"Visibility=NAME_ONLY"
		));
	}

	@Test
	public void canCreateAddPeerWithIdentifierAndUrl() throws Exception {
		AddPeer addPeer = new AddPeer(NORMAL, NAME_ONLY, "identifier", new URL("http://url"));
		assertThat(encodeMessage(addPeer), isMessage("AddPeer",
				"URL=http://url",
				"Identifier=identifier",
				"Trust=NORMAL",
				"Visibility=NAME_ONLY"
		));
	}

	@Test
	public void canCreateAddPeerWithNodeRef() throws Exception {
		NodeRef nodeRef = createNodeRef();
		AddPeer addPeer = new AddPeer(HIGH, YES, nodeRef);
		assertThat(encodeMessage(addPeer), isMessage("AddPeer",
				"lastGoodVersion=test,1.2.3,4.5,678",
				"opennet=true",
				"identity=identity",
				"myName=test-node",
				"location=0.1234",
				"testnet=false",
				"version=test,2.3.4,5.6,789",
				"physical.udp=10.11.12.13",
				"ark.pubURI=ark-public-uri",
				"ark.number=1",
				"dsaPubKey.y=dsa-public-key",
				"dsaGroup.g=dsa-base",
				"dsaGroup.p=dsa-prime",
				"dsaGroup.q=dsa-subprime",
				"auth.negTypes=1;2;3",
				"sig=signature",
				"Trust=HIGH",
				"Visibility=YES"
		));
	}

	@Test
	public void canCreateAddPeerWithIdentifierAndNodeRef() throws Exception {
		NodeRef nodeRef = createNodeRef();
		AddPeer addPeer = new AddPeer(HIGH, YES, "identifier", nodeRef);
		assertThat(encodeMessage(addPeer), isMessage("AddPeer",
				"Identifier=identifier",
				"lastGoodVersion=test,1.2.3,4.5,678",
				"opennet=true",
				"identity=identity",
				"myName=test-node",
				"location=0.1234",
				"testnet=false",
				"version=test,2.3.4,5.6,789",
				"physical.udp=10.11.12.13",
				"ark.pubURI=ark-public-uri",
				"ark.number=1",
				"dsaPubKey.y=dsa-public-key",
				"dsaGroup.g=dsa-base",
				"dsaGroup.p=dsa-prime",
				"dsaGroup.q=dsa-subprime",
				"auth.negTypes=1;2;3",
				"sig=signature",
				"Trust=HIGH",
				"Visibility=YES"
		));
	}

	private static NodeRef createNodeRef() {
		NodeRef nodeRef = new NodeRef();
		nodeRef.setLastGoodVersion(new Version("test,1.2.3,4.5,678"));
		nodeRef.setOpennet(true);
		nodeRef.setIdentity("identity");
		nodeRef.setName("test-node");
		nodeRef.setLocation(0.1234);
		nodeRef.setTestnet(false);
		nodeRef.setVersion(new Version("test,2.3.4,5.6,789"));
		nodeRef.setPhysicalUDP("10.11.12.13");
		nodeRef.setARK(new ARK("ark-public-uri", "1"));
		nodeRef.setDSAPublicKey("dsa-public-key");
		nodeRef.setDSAGroup(new DSAGroup("dsa-base", "dsa-prime", "dsa-subprime"));
		nodeRef.setNegotiationTypes(new int[]{1, 2, 3});
		nodeRef.setSignature("signature");
		return nodeRef;
	}

	private static Matcher<Iterable<String>> isMessage(String name, String... lines) {
		return new TypeSafeDiagnosingMatcher<Iterable<String>>() {
			@Override
			protected boolean matchesSafely(Iterable<String> encodedMessage, Description mismatchDescription) {
				Iterator<String> messageLines = encodedMessage.iterator();
				String messageName = messageLines.next();
				if (!messageName.equals(name)) {
					mismatchDescription.appendText("name is ").appendValue(messageName);
					return false;
				}
				List<String> parameterLines = new ArrayList<>();
				while (true) {
					String actualLine = messageLines.next();
					if (actualLine.contains("=")) {
						parameterLines.add(actualLine);
					} else {
						if (!containsInAnyOrder(lines).matches(parameterLines)) {
							containsInAnyOrder(lines).describeMismatch(parameterLines, mismatchDescription);
							return false;
						}
						break;
					}
				}
				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("is message named ").appendValue(name)
						.appendText(" with parameters ").appendValue(lines);
			}
		};
	}

}
