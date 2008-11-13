package org.eclipse.equinox.p2.tests.planner;

import java.io.File;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.internal.p2.engine.SimpleProfileRegistry;
import org.eclipse.equinox.internal.provisional.p2.director.*;
import org.eclipse.equinox.internal.provisional.p2.engine.IProfile;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.InstallableUnitQuery;
import org.eclipse.equinox.internal.provisional.p2.metadata.repository.IMetadataRepository;
import org.eclipse.equinox.internal.provisional.p2.query.Collector;
import org.eclipse.equinox.p2.tests.AbstractProvisioningTest;

public class Bug254481dataSet1 extends AbstractProvisioningTest {
	IProfile profile = null;
	IMetadataRepository repo = null;

	protected void setUp() throws Exception {
		super.setUp();
		File reporegistry1 = getTestData("test data bug 254481", "testData/bug254481/dataSet1/p2/org.eclipse.equinox.p2.engine/profileRegistry");
		SimpleProfileRegistry registry = new SimpleProfileRegistry(reporegistry1, null, false);
		profile = registry.getProfile("bootProfile");
		assertNotNull(profile);
		repo = getMetadataRepositoryManager().loadRepository(getTestData("test data bug 254481", "testData/bug254481/dataSet1/repo").toURI(), null);
		assertNotNull(repo);
	}

	protected void tearDown() throws Exception {
		getMetadataRepositoryManager().removeRepository(getTestData("test data bug 254481", "testData/bug254481/dataSet1/repo").toURI());
		super.tearDown();
	}

	public void testInstallFeaturePatch() {
		Collector c = repo.query(new InstallableUnitQuery("RPT_ARM_TEST.feature.group"), new Collector(), new NullProgressMonitor());
		assertEquals(1, c.size());
		IInstallableUnit patch = (IInstallableUnit) c.iterator().next();
		ProfileChangeRequest request = new ProfileChangeRequest(profile);
		request.addInstallableUnits(new IInstallableUnit[] {patch});
		request.setInstallableUnitInclusionRules(patch, PlannerHelper.createOptionalInclusionRule(patch));
		IPlanner planner = createPlanner();
		ProvisioningPlan plan = planner.getProvisioningPlan(request, null, new NullProgressMonitor());
		assertInstallOperand(plan, patch);
		//[[R]com.ibm.rational.test.lt.arm 7.0.250.v200810021504 --> [R]com.ibm.rational.test.lt.arm 7.0.300.200811041300, 
		assertEquals(1, plan.getAdditions().query(new InstallableUnitQuery("com.ibm.rational.test.lt.arm"), new Collector(), null).size());
		//[R]com.ibm.rational.test.lt.armbroker 7.0.250.v200810021504 --> [R]com.ibm.rational.test.lt.armbroker 7.0.300.200811041300, 
		assertEquals(1, plan.getAdditions().query(new InstallableUnitQuery("com.ibm.rational.test.lt.armbroker"), new Collector(), null).size());
		//[R]com.ibm.rational.test.lt.kernel 7.2.151.v200810021605 --> [R]com.ibm.rational.test.lt.kernel 7.2.200.200811041300, 
		assertEquals(1, plan.getAdditions().query(new InstallableUnitQuery("com.ibm.rational.test.lt.kernel"), new Collector(), null).size());
	}
}