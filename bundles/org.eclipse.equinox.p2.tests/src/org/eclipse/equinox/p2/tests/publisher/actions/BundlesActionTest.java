/*******************************************************************************
 * Copyright (c) 2008 Code 9 and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Code 9 - initial API and implementation
 *   IBM - ongoing development
 ******************************************************************************/
package org.eclipse.equinox.p2.tests.publisher.actions;

import static org.easymock.EasyMock.expect;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.zip.ZipInputStream;
import org.easymock.EasyMock;
import org.eclipse.core.runtime.*;
import org.eclipse.equinox.internal.p2.metadata.ArtifactKey;
import org.eclipse.equinox.internal.provisional.p2.metadata.*;
import org.eclipse.equinox.p2.publisher.IPublisherInfo;
import org.eclipse.equinox.p2.publisher.IPublisherResult;
import org.eclipse.equinox.p2.publisher.actions.ICapabilityAdvice;
import org.eclipse.equinox.p2.publisher.eclipse.*;
import org.eclipse.equinox.p2.tests.TestActivator;
import org.eclipse.equinox.p2.tests.TestData;
import org.eclipse.equinox.p2.tests.publisher.TestArtifactRepository;
import org.eclipse.equinox.spi.p2.publisher.PublisherHelper;
import org.eclipse.osgi.service.resolver.VersionRange;
import org.osgi.framework.Version;

@SuppressWarnings( {"restriction", "unchecked"})
public class BundlesActionTest extends ActionTest {
	private static final String OSGI = PublisherHelper.OSGI_BUNDLE_CLASSIFIER;
	private static final String JAVA_PACKAGE = "java.package";//$NON-NLS-1$

	private static final String TEST1_IUD_NAME = "iud";//$NON-NLS-1$
	private static final String TEST1_PROVZ_NAME = "iuz";//$NON-NLS-1$
	private static final String TEST1_PROVBUNDLE_NAME = "test1";//$NON-NLS-1$
	private static final String TEST2_REQA_NAME = "iua";//$NON-NLS-1$
	private static final String TEST2_REQB_NAME = "iub";//$NON-NLS-1$
	private static final String TEST2_REQC_NAME = "iuc";//$NON-NLS-1$
	private static final String TEST2_PROVZ_NAME = "iuz";//$NON-NLS-1$
	private static final String TEST2_PROVY_NAME = "iuy";//$NON-NLS-1$
	private static final String TEST2_PROVX_NAME = "iux";//$NON-NLS-1$
	private static final String TEST2_PROVBUNDLE_NAME = "test2";//$NON-NLS-1$

	private static final File TEST_BASE = new File(TestActivator.getTestDataFolder(), "BundlesActionTest");//$NON-NLS-1$
	private static final File TEST_FILE1 = new File(TEST_BASE, TEST1_PROVBUNDLE_NAME);
	private static final File TEST_FILE2 = new File(TEST_BASE, TEST2_PROVBUNDLE_NAME + ".jar");//$NON-NLS-1$

	private static final String PROVBUNDLE_NAMESPACE = "org.eclipse.equinox.p2.iu";//$NON-NLS-1$
	private static final String TEST2_IUA_NAMESPACE = OSGI;
	private static final String TEST2_IUB_NAMESPACE = JAVA_PACKAGE;
	private static final String TEST2_IUC_NAMESPACE = JAVA_PACKAGE;
	private static final String TEST1_IUD_NAMESPACE = JAVA_PACKAGE;
	private static final String TEST2_PROVZ_NAMESPACE = JAVA_PACKAGE;
	private static final String TEST2_PROVY_NAMESPACE = JAVA_PACKAGE;
	private static final String TEST2_PROVX_NAMESPACE = JAVA_PACKAGE;
	private static final String TEST1_PROVZ_NAMESPACE = JAVA_PACKAGE;

	private final Version TEST1_BUNDLE_VERSION = new Version("0.1.0");//$NON-NLS-1$
	private final Version BUNDLE2_VERSION = new Version("1.0.0.qualifier");//$NON-NLS-1$
	private final Version PROVBUNDLE2_VERSION = BUNDLE2_VERSION;
	private final Version TEST2_PROVZ_VERSION = Version.emptyVersion;
	private final Version TEST2_PROVY_VERSION = Version.emptyVersion;
	private final Version TEST2_PROVX_VERSION = Version.emptyVersion;
	private final VersionRange TEST2_IUA_VERSION_RANGE = VersionRange.emptyRange;
	private final VersionRange TEST2_IUB_VERSION_RANGE = VersionRange.emptyRange;
	private final VersionRange TEST2_IUC_VERSION_RANGE = new VersionRange(new Version("1.0.0"), true, new Version(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE), true);//$NON-NLS-1$
	private final VersionRange TEST1_IUD_VERSION_RANGE = new VersionRange(new Version("1.3.0"), true, new Version(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE), true);//$NON-NLS-1$

	protected TestArtifactRepository artifactRepository = new TestArtifactRepository();

	public void testAll() throws Exception {
		File[] files = TEST_BASE.listFiles();
		testAction = new BundlesAction(files);
		setupPublisherResult();
		setupPublisherInfo();

		assertEquals(Status.OK_STATUS, testAction.perform(publisherInfo, publisherResult, new NullProgressMonitor()));
		verifyBundlesAction();
		cleanup();
		debug("Completed BundlesActionTest.");//$NON-NLS-1$
	}

	private void verifyBundlesAction() throws Exception {
		// verify publisher result
		verifyBundle1();
		verifyBundle2();

		verifyArtifactRepository();
	}

	private void verifyArtifactRepository() throws Exception {
		IArtifactKey key2 = ArtifactKey.parse("osgi.bundle,test2,1.0.0.qualifier");//$NON-NLS-1$
		ZipInputStream actual = artifactRepository.getZipInputStream(key2);
		ZipInputStream expected = new ZipInputStream(new FileInputStream(TEST_FILE2));
		TestData.assertEquals(expected, actual);

		IArtifactKey key1 = ArtifactKey.parse("osgi.bundle,test1,0.1.0");//$NON-NLS-1$
		ZipInputStream zis = artifactRepository.getZipInputStream(key1);
		Map fileMap = getFileMap(new HashMap(), new File[] {TEST_FILE1}, new Path(TEST_FILE1.getAbsolutePath()));
		TestData.assertContains(fileMap, zis, true);
	}

	private void verifyBundle1() {
		ArrayList ius = new ArrayList(publisherResult.getIUs(TEST1_PROVBUNDLE_NAME, IPublisherResult.ROOT));
		assertTrue(ius.size() == 1);
		IInstallableUnit bundle1IU = (IInstallableUnit) ius.get(0);

		assertNotNull(bundle1IU);
		assertEquals(bundle1IU.getVersion(), TEST1_BUNDLE_VERSION);

		// check required capabilities
		RequiredCapability[] requiredCapability = bundle1IU.getRequiredCapabilities();
		verifyRequiredCapability(requiredCapability, TEST1_IUD_NAMESPACE, TEST1_IUD_NAME, TEST1_IUD_VERSION_RANGE);
		assertTrue(requiredCapability.length == 1 /*num of tested elements*/);

		// check provided capabilities
		ProvidedCapability[] providedCapabilities = bundle1IU.getProvidedCapabilities();
		verifyProvidedCapability(providedCapabilities, PROVBUNDLE_NAMESPACE, TEST1_PROVBUNDLE_NAME, TEST1_BUNDLE_VERSION);
		verifyProvidedCapability(providedCapabilities, OSGI, TEST1_PROVBUNDLE_NAME, TEST1_BUNDLE_VERSION);
		verifyProvidedCapability(providedCapabilities, TEST1_PROVZ_NAMESPACE, TEST1_PROVZ_NAME, TEST2_PROVZ_VERSION);
		verifyProvidedCapability(providedCapabilities, PublisherHelper.NAMESPACE_ECLIPSE_TYPE, "source", new Version("1.0.0"));//$NON-NLS-1$//$NON-NLS-2$
		assertTrue(providedCapabilities.length == 4 /*num of tested elements*/);
	}

	private void verifyBundle2() {
		ArrayList ius = new ArrayList(publisherResult.getIUs(TEST2_PROVBUNDLE_NAME, IPublisherResult.ROOT));
		assertTrue(ius.size() == 1);
		IInstallableUnit bundle2IU = (IInstallableUnit) ius.get(0);

		assertNotNull(bundle2IU);
		assertEquals(bundle2IU.getVersion(), BUNDLE2_VERSION);

		// check required capabilities
		RequiredCapability[] requiredCapabilities = bundle2IU.getRequiredCapabilities();
		verifyRequiredCapability(requiredCapabilities, TEST2_IUA_NAMESPACE, TEST2_REQA_NAME, TEST2_IUA_VERSION_RANGE);
		verifyRequiredCapability(requiredCapabilities, TEST2_IUB_NAMESPACE, TEST2_REQB_NAME, TEST2_IUB_VERSION_RANGE);
		verifyRequiredCapability(requiredCapabilities, TEST2_IUC_NAMESPACE, TEST2_REQC_NAME, TEST2_IUC_VERSION_RANGE);
		assertTrue(requiredCapabilities.length == 3 /*number of tested elements*/);

		// check provided capabilities
		ProvidedCapability[] providedCapabilities = bundle2IU.getProvidedCapabilities();
		verifyProvidedCapability(providedCapabilities, PROVBUNDLE_NAMESPACE, TEST2_PROVBUNDLE_NAME, PROVBUNDLE2_VERSION);
		verifyProvidedCapability(providedCapabilities, OSGI, TEST2_PROVBUNDLE_NAME, BUNDLE2_VERSION);
		verifyProvidedCapability(providedCapabilities, TEST2_PROVZ_NAMESPACE, TEST2_PROVZ_NAME, TEST2_PROVZ_VERSION);
		verifyProvidedCapability(providedCapabilities, TEST2_PROVY_NAMESPACE, TEST2_PROVY_NAME, TEST2_PROVY_VERSION);
		verifyProvidedCapability(providedCapabilities, TEST2_PROVX_NAMESPACE, TEST2_PROVX_NAME, TEST2_PROVX_VERSION);
		verifyProvidedCapability(providedCapabilities, PublisherHelper.NAMESPACE_ECLIPSE_TYPE, "bundle", new Version("1.0.0"));//$NON-NLS-1$//$NON-NLS-2$
		assertTrue(providedCapabilities.length == 6 /*number of tested elements*/);

		// check %bundle name is correct
		Map prop = bundle2IU.getProperties();
		assertTrue(prop.get("org.eclipse.equinox.p2.name").toString().equalsIgnoreCase("%bundleName"));//$NON-NLS-1$//$NON-NLS-2$
		assertTrue(prop.get("org.eclipse.equinox.p2.provider").toString().equalsIgnoreCase("%providerName"));//$NON-NLS-1$//$NON-NLS-2$
	}

	public void cleanup() {
		super.cleanup();
		if (artifactRepository != null) {
			artifactRepository.removeAll();
			artifactRepository = null;
		}
	}

	protected void insertPublisherInfoBehavior() {
		Properties sarProperties = new Properties();
		sarProperties.put("key1", "value1");//$NON-NLS-1$//$NON-NLS-2$
		sarProperties.put("key2", "value2");//$NON-NLS-1$//$NON-NLS-2$

		Properties sdkProperties = new Properties();
		sdkProperties.put("key1", "value1");//$NON-NLS-1$//$NON-NLS-2$
		sdkProperties.put("key2", "value2");//$NON-NLS-1$//$NON-NLS-2$

		IBundleAdvice bundleAdvice = EasyMock.createMock(IBundleAdvice.class);
		expect(bundleAdvice.getArtifactProperties(TEST_FILE1)).andReturn(sarProperties).anyTimes();
		expect(bundleAdvice.getArtifactProperties(TEST_FILE2)).andReturn(sdkProperties).anyTimes();
		expect(bundleAdvice.getInstructions((File) EasyMock.anyObject())).andReturn(new HashMap()).anyTimes();
		expect(bundleAdvice.getIUProperties((File) EasyMock.anyObject())).andReturn(new Properties()).anyTimes();

		EasyMock.replay(bundleAdvice);
		ArrayList adviceCollection = new ArrayList();
		adviceCollection.add(bundleAdvice);
		expect(publisherInfo.getAdvice(null, false, null, null, IBundleAdvice.class)).andReturn(adviceCollection).anyTimes();
		expect(publisherInfo.getArtifactRepository()).andReturn(artifactRepository).anyTimes();
		expect(publisherInfo.getAdvice(null, true, TEST1_PROVBUNDLE_NAME, new Version("0.1.0"), IBundleShapeAdvice.class)).andReturn(null); //$NON-NLS-1$
		expect(publisherInfo.getAdvice(null, true, TEST2_PROVBUNDLE_NAME, new Version("1.0.0.qualifier"), IBundleShapeAdvice.class)).andReturn(null);//$NON-NLS-1$
		expect(publisherInfo.getArtifactOptions()).andReturn(IPublisherInfo.A_INDEX | IPublisherInfo.A_OVERWRITE | IPublisherInfo.A_PUBLISH).anyTimes();
		expect(publisherInfo.getAdvice(null, false, null, null, ICapabilityAdvice.class)).andReturn(new ArrayList()).anyTimes();
	}
}