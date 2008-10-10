/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.p2.tests.core;

import java.io.File;
import java.net.*;
import org.eclipse.equinox.internal.p2.core.helpers.URIUtil;
import org.eclipse.equinox.p2.tests.AbstractProvisioningTest;

/**
 * Tests for the {@link URLUtil} class.
 */
public class URIUtilTest extends AbstractProvisioningTest {
	private static final String[] testPaths = new String[] {"abc", "with spaces", "with%percent"};

	/**
	 * Tests for {@link URLUtil#toFile(URL)}.
	 */
	public void testToFile() {
		File base = new File(System.getProperty("java.io.tmpdir"));
		for (int i = 0; i < testPaths.length; i++) {
			File original = new File(base, testPaths[i]);
			URI uri = original.toURI();
			File result = URIUtil.toFile(uri);
			assertEquals("1." + i, original, result);
		}
	}

	/**
	 * Tests for {@link URIUtil#fromString(String)}.
	 */
	public void testFromString() throws URISyntaxException {
		//spaces
		assertEquals("1.1", new URI("http://foo.bar/a%20b"), URIUtil.fromString("http://foo.bar/a b"));
		assertEquals("1.2", new URI("http://foo.bar/a#b%20c"), URIUtil.fromString("http://foo.bar/a#b c"));
		assertEquals("1.3", new URI("foo.bar/a%20b"), URIUtil.fromString("foo.bar/a b"));
		assertEquals("1.4", new URI("#a%20b"), URIUtil.fromString("#a b"));
	}

	/**
	 * Tests for {@link URIUtil#toURI(java.net.URL)}.
	 */
	public void testURLtoURI() throws MalformedURLException, URISyntaxException {
		//spaces
		assertEquals("1.1", new URI("http://foo.bar/a%20b"), URIUtil.toURI(new URL("http://foo.bar/a b")));
		assertEquals("1.2", new URI("http://foo.bar/a#b%20c"), URIUtil.toURI(new URL("http://foo.bar/a#b c")));

		//% characters
		assertEquals("1.1", new URI("http://foo.bar/a%25b"), URIUtil.toURI(new URL("http://foo.bar/a%b")));
	}

	/**
	 * Tests handling of Absolute file system paths on Windows incorrectly encoded as
	 * relative URIs (file:c:/tmp).
	 */
	public void testWindowsPaths() throws MalformedURLException, URISyntaxException {
		if (!isWindows())
			return;
		assertEquals("1.1", new URI("file:/c:/foo/bar.txt"), URIUtil.toURI(new URL("file:c:/foo/bar.txt")));
		assertEquals("1.2", new URI("file:/c:/foo/bar.txt"), URIUtil.toURI(new URL("file:/c:/foo/bar.txt")));
	}

	/**
	 * Tests handling of conversion from a File with spaces to URL and File to URI and equivalence of the resulting URI
	 */
	public void testFileWithSpaces() throws MalformedURLException, URISyntaxException {
		File fileWithSpaces = new File("/c:/with spaces/goo");
		URI correctURI = fileWithSpaces.toURI();
		URL fileURL = fileWithSpaces.toURL();
		URI fileURI = null;
		try {
			fileURI = fileURL.toURI();
			fail();
		} catch (URISyntaxException e) {
			fileURI = URIUtil.toURI(fileURL);
		}
		assertEquals("1.1", correctURI, fileURI);

		try {
			fileURI = new URI(fileURL.toString());
			fail();
		} catch (URISyntaxException e) {
			fileURI = URIUtil.fromString(fileURL.toString());
		}
		assertEquals("1.2", correctURI, fileURI);
	}

	/**
	 * Tests handling of conversion from a File with %20 to URL and File to URI and equivalence of the resulting URI
	 */
	public void testFileWithPercent20() throws MalformedURLException, URISyntaxException {
		File fileWithPercent20 = new File("/c:/with%20spaces/goo");
		URI correctURI = fileWithPercent20.toURI();

		URL fileURL = fileWithPercent20.toURL();
		assertNotSame("1.1", correctURI, fileURL.toURI());
		assertEquals("1.2", correctURI, URIUtil.toURI(fileURL));
		assertNotSame("1.3", correctURI, new URI(fileURL.toString()));
		// we expect these to not be the same because fromString assumes a decoded URL String
		assertNotSame("1.4", correctURI, URIUtil.fromString(fileURL.toString()));
	}
}