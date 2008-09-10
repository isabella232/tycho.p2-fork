/*******************************************************************************
 * Copyright (c) 2008 Code 9 and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Code 9 - initial API and implementation
 ******************************************************************************/
package org.eclipse.equinox.internal.p2.updatesite;

import java.util.Dictionary;
import java.util.Properties;
import org.eclipse.equinox.internal.provisional.p2.metadata.IArtifactKey;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.publisher.IPublisherInfo;
import org.eclipse.equinox.p2.publisher.IPublisherResult;
import org.eclipse.equinox.p2.publisher.eclipse.*;
import org.eclipse.equinox.spi.p2.publisher.PublisherHelper;
import org.eclipse.osgi.service.resolver.BundleDescription;

public class RemoteFeaturesAction extends FeaturesAction {

	public RemoteFeaturesAction(Feature[] features) {
		super(features);
	}

	protected void generateFeatureIUs(Feature[] features, IPublisherResult result, IPublisherInfo info) {
		Properties extraProperties = new Properties();
		extraProperties.put(IInstallableUnit.PROP_PARTIAL_IU, Boolean.TRUE.toString());
		for (int i = 0; i < features.length; i++) {
			Feature feature = features[i];
			FeatureEntry[] featureEntries = feature.getEntries();
			for (int j = 0; j < featureEntries.length; j++) {
				FeatureEntry entry = featureEntries[j];
				if (entry.isPlugin() && !entry.isRequires()) {
					Dictionary mockManifest = new Properties();
					mockManifest.put("Manifest-Version", "1.0"); //$NON-NLS-1$ //$NON-NLS-2$
					mockManifest.put("Bundle-ManifestVersion", "2"); //$NON-NLS-1$ //$NON-NLS-2$
					mockManifest.put("Bundle-SymbolicName", entry.getId()); //$NON-NLS-1$
					mockManifest.put("Bundle-Version", entry.getVersion()); //$NON-NLS-1$
					BundleDescription bundleDescription = BundlesAction.createBundleDescription(mockManifest, null);
					IArtifactKey key = BundlesAction.createBundleArtifactKey(entry.getId(), entry.getVersion());
					IInstallableUnit[] bundleIUs = PublisherHelper.createEclipseIU(bundleDescription, null, entry.isUnpack(), key, extraProperties);
					for (int n = 0; n < bundleIUs.length; n++)
						result.addIU(bundleIUs[n], IPublisherResult.ROOT);
				}
			}
			IInstallableUnit featureIU = createFeatureJarIU(feature, null, null);
			IInstallableUnit groupIU = createGroupIU(feature, featureIU, null);
			result.addIU(featureIU, IPublisherResult.ROOT);
			result.addIU(groupIU, IPublisherResult.ROOT);
		}
	}
}