package tests;
/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2023 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this project.
 ******************************************************************************/


import java.io.IOException;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.Collection;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.config.keys.loader.KeyPairResourceLoader;
import org.apache.sshd.common.keyprovider.KeyIdentityProvider;
import org.apache.sshd.common.util.security.SecurityUtils;

public class TestSSHClient {

	public static void main(String[] args) throws IOException {
		SshClient client = SshClient.setUpDefaultClient();
		KeyPairResourceLoader loader = SecurityUtils.getKeyPairResourceParser();
		Collection<KeyPair> keys = null;
		try {
			keys = loader.loadKeyPairs(null, Paths.get(
				"/Users/kozusznikj/.ssh/keys/it4i_rsa-np"), ($1, $2, $3) -> "");
		}
		catch (IOException | GeneralSecurityException exc) {
			throw new RuntimeException(exc);
		}
		client.setKeyIdentityProvider(KeyIdentityProvider.wrapKeyPairs(keys));
		client.start();
		client.setServerKeyVerifier(($1, $2, $3) -> true);
		ClientSession session = client.connect("koz01", "barbora.it4i.cz", 22)
			.verify().getSession();

		session.auth().verify();
		String result = session.executeRemoteCommand("pwd");
		System.out.println(result);
	}
}
