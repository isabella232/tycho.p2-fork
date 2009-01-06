/******************************************************************************* 
* Copyright (c) 2008 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
******************************************************************************/
package org.eclipse.equinox.internal.provisional.p2.metadata;

import org.eclipse.equinox.internal.provisional.p2.core.VersionRange;

/**
 * A required capability represents some external constraint on an {@link IInstallableUnit}.
 * Each capability represents something an {@link IInstallableUnit} needs that
 * it expects to be provided by another {@link IInstallableUnit}. Capabilities are
 * entirely generic, and are intended to be capable of representing anything that
 * an {@link IInstallableUnit} may need either at install time, or at runtime.
 * <p>
 * Capabilities are segmented into namespaces.  Anyone can introduce new 
 * capability namespaces. Some well-known namespaces are introduced directly
 * by the provisioning framework.
 * 
 * @see IInstallableUnit#NAMESPACE_IU_ID
 */
public interface IRequiredCapability {

	public String getFilter();

	public String getName();

	public String getNamespace();

	/**
	 * Returns the range of versions that satisfy this required capability. Returns
	 * an empty version range ({@link VersionRange#emptyRange} if any version
	 * will satisfy the capability.
	 * @return the range of versions that satisfy this required capability.
	 */
	public VersionRange getRange();

	/**
	 * Returns the properties to use for evaluating required capability filters 
	 * downstream from this capability. For example, if the selector "doc"
	 * is provided, then a downstream InstallableUnit with a required capability
	 * filtered with "doc=true" will be included.
	 */
	public String[] getSelectors();

	public boolean isMultiple();

	public boolean isOptional();

	/**
	 * TODO This object shouldn't be mutable since it makes equality unstable, and
	 * introduces lifecycle issues (how are the changes persisted, etc)
	 */
	public void setFilter(String filter);

	/**
	 * TODO This object shouldn't be mutable since it makes equality unstable, and
	 * introduces lifecycle issues (how are the changes persisted, etc)
	 */
	public void setSelectors(String[] selectors);

	public boolean isGreedy();

	/**
	 * Returns whether this required capability is equal to the given object.
	 * 
	 * This method returns <i>true</i> if:
	 * <ul>
	 *  <li> Both this object and the given object are of type IRequiredCapability
	 *  <li> The result of <b>getFilter()</b> on both objects are equal
	 *  <li> The result of <b>isMultiple()</b> on both objects are equal
	 *  <li> The result of <b>getName()</b> on both objects are equal
	 *  <li> The result of <b>geNamespace()</b> on both objects are equal
	 *  <li> The result of <b>isOptional()</b> on both objects are equal
	 *  <li> The result of <b>getRange()</b> on both objects are equal
	 * </ul> 
	 */
	public boolean equals(Object other);

}