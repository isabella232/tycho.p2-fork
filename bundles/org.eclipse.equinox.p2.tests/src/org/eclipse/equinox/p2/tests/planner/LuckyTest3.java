/*******************************************************************************
 * Copyright (c) 2011 Sonatype, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * Sonatype, Inc. - initial implementation and ideas 
 ******************************************************************************/
package org.eclipse.equinox.p2.tests.planner;

import org.eclipse.equinox.internal.p2.director.ProfileChangeRequest;
import org.eclipse.equinox.p2.engine.*;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.planner.IPlanner;
import org.eclipse.equinox.p2.tests.*;

public class LuckyTest3 extends AbstractProvisioningTest {
	@IUDescription(content = "package: sdk \n" + "singleton: true\n" + "version: 1 \n" + "depends: platform = 1")
	public IInstallableUnit sdk1;

	@IUDescription(content = "package: platform \n" + "singleton: true\n" + "version: 1 \n")
	public IInstallableUnit platform1;

	@IUDescription(content = "package: sdk \n" + "singleton: true\n" + "version: 2 \n" + "depends: platform = 2")
	public IInstallableUnit sdk2;

	@IUDescription(content = "package: platform \n" + "singleton: true\n" + "version: 2 \n")
	public IInstallableUnit platform2;

	@IUDescription(content = "package: egit \n" + "singleton: true\n" + "version: 1 \n")
	public IInstallableUnit egit1;

	IProfile profile = createProfile("TestProfile." + getName());

	private IPlanner planner;

	private IEngine engine;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		IULoader.loadIUs(this);
		createTestMetdataRepository(new IInstallableUnit[] {sdk1, platform1, sdk2, platform2, egit1});
		planner = createPlanner();
		engine = createEngine();

		assertOK(install(profile, new IInstallableUnit[] {sdk1}, true, planner, engine));
		assertOK(install(profile, new IInstallableUnit[] {egit1}, false, planner, engine));
		assertEquals("STRICT", profile.getInstallableUnitProperty(sdk1, "org.eclipse.equinox.p2.internal.inclusion.rules"));
		assertEquals("OPTIONAL", profile.getInstallableUnitProperty(egit1, "org.eclipse.equinox.p2.internal.inclusion.rules"));
	}

	public void testInstallSDK2() {
		assertNotOK(install(profile, new IInstallableUnit[] {platform2}, true, planner, engine));

		ProfileChangeRequest res = new LuckyHelper().computeProfileChangeRequest(profile, planner, null, new ProvisioningContext(getAgent()), getMonitor());
		assertEquals(1, res.getAdditions().size());
		assertTrue(res.getAdditions().contains(sdk2));
		assertEquals(1, res.getRemovals().size());
		assertTrue(res.getRemovals().contains(sdk1));

		assertOK(install(res, planner, engine));
		assertProfileContains("validate new profile", profile, new IInstallableUnit[] {sdk2, platform2, egit1});
		assertEquals("STRICT", profile.getInstallableUnitProperty(sdk2, "org.eclipse.equinox.p2.internal.inclusion.rules"));
		assertEquals(null, profile.getInstallableUnitProperty(platform2, "org.eclipse.equinox.p2.internal.inclusion.rules"));
		assertEquals("OPTIONAL", profile.getInstallableUnitProperty(egit1, "org.eclipse.equinox.p2.internal.inclusion.rules"));
	}
}
