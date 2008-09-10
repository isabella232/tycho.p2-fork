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

import org.eclipse.equinox.p2.publisher.actions.MergeResultsAction;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.p2.publisher.*;
import org.eclipse.equinox.p2.publisher.eclipse.BundlesAction;
import org.eclipse.equinox.p2.publisher.eclipse.FeaturesAction;

/**
 * A publishing action that processes a local (File-based) update site and generates
 * metadata and artifacts for the features, bundles and site index (categories etc).
 */
public class LocalUpdateSiteAction implements IPublisherAction {
	protected String source;
	private UpdateSite updateSite;

	protected LocalUpdateSiteAction() {
	}

	public LocalUpdateSiteAction(String source) {
		this.source = source;
	}

	public LocalUpdateSiteAction(UpdateSite updateSite) {
		this.updateSite = updateSite;
	}

	public IStatus perform(IPublisherInfo info, IPublisherResult results) {
		IPublisherAction[] actions = createActions();
		for (int i = 0; i < actions.length; i++)
			actions[i].perform(info, results);
		return Status.OK_STATUS;
	}

	protected IPublisherAction[] createActions() {
		createAdvice();
		ArrayList result = new ArrayList();
		// create an action that just publishes the raw bundles and features
		IPublisherAction action = new MergeResultsAction(new IPublisherAction[] {createFeaturesAction(), createBundlesAction()}, IPublisherResult.MERGE_ALL_NON_ROOT);
		result.add(action);
		result.add(createSiteXMLAction());
		return (IPublisherAction[]) result.toArray(new IPublisherAction[result.size()]);
	}

	private IPublisherAction createSiteXMLAction() {
		if (updateSite != null)
			return new SiteXMLAction(updateSite);
		if (source != null) {
			try {
				return new SiteXMLAction(new File(source, "site.xml").toURL()); //$NON-NLS-1$
			} catch (MalformedURLException e) {
				// never happens
				return null;
			}
		}
		return null;
	}

	private void createAdvice() {
	}

	protected IPublisherAction createFeaturesAction() {
		return new FeaturesAction(new File[] {new File(source, "features")}); //$NON-NLS-1$
	}

	protected IPublisherAction createBundlesAction() {
		return new BundlesAction(new File[] {new File(source, "plugins")}); //$NON-NLS-1$
	}

}