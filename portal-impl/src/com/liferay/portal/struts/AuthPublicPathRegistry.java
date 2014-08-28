/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.struts;

import com.liferay.portal.kernel.concurrent.ConcurrentHashSet;
import com.liferay.portal.kernel.struts.path.AuthPublicPath;
import com.liferay.registry.Registry;
import com.liferay.registry.RegistryUtil;
import com.liferay.registry.ServiceReference;
import com.liferay.registry.ServiceRegistration;
import com.liferay.registry.ServiceTracker;
import com.liferay.registry.ServiceTrackerCustomizer;
import com.liferay.registry.collections.StringServiceRegistrationMap;

import java.util.Set;

/**
 * @author Mika Koivisto
 */
public class AuthPublicPathRegistry {

	public static boolean contains(String path) {
		return _paths.contains(path);
	}

	public static void register(String... paths) {
		Registry registry = RegistryUtil.getRegistry();

		for (String path : paths) {
			ServiceRegistration<AuthPublicPath> serviceRegistration =
				registry.registerService(
					AuthPublicPath.class, new DefaultAuthPublicPath(path));

			_serviceRegistrations.put(path, serviceRegistration);
		}
	}

	public static void unregister(String... paths) {
		for (String path : paths) {
			ServiceRegistration<AuthPublicPath> serviceRegistration =
				_serviceRegistrations.remove(path);

			if (serviceRegistration != null) {
				serviceRegistration.unregister();
			}
		}
	}

	private static Set<String> _paths = new ConcurrentHashSet<>();
	private static StringServiceRegistrationMap<AuthPublicPath>
		_serviceRegistrations = new StringServiceRegistrationMap<>();
	private static ServiceTracker<AuthPublicPath, AuthPublicPath>
		_serviceTracker;

	private static class AuthPublicTrackerCustomizer
		implements ServiceTrackerCustomizer<AuthPublicPath, AuthPublicPath> {

		@Override
		public AuthPublicPath addingService(
			ServiceReference<AuthPublicPath> serviceReference) {

			Registry registry = RegistryUtil.getRegistry();

			AuthPublicPath authPublicPath = registry.getService(
				serviceReference);

			_paths.add(authPublicPath.path());

			return authPublicPath;
		}

		@Override
		public void modifiedService(
			ServiceReference<AuthPublicPath> serviceReference,
			AuthPublicPath authPublicPath) {
		}

		@Override
		public void removedService(
			ServiceReference<AuthPublicPath> serviceReference,
			AuthPublicPath authPublicPath) {

			_paths.remove(authPublicPath.path());

			Registry registry = RegistryUtil.getRegistry();

			registry.ungetService(serviceReference);
		}

	}

	static {
		Registry registry = RegistryUtil.getRegistry();

		_serviceTracker = registry.trackServices(
			AuthPublicPath.class, new AuthPublicTrackerCustomizer());

		_serviceTracker.open();
	}

	private static class DefaultAuthPublicPath implements AuthPublicPath {

		public DefaultAuthPublicPath(String path) {
			_path = path;
		}

		@Override
		public String path() {
			return _path;
		}

		private String _path;

	}

}