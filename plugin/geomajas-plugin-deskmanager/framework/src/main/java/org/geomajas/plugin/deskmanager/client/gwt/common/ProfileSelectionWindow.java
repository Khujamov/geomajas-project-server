/*
 * This is part of Geomajas, a GIS framework, http://www.geomajas.org/.
 *
 * Copyright 2008-2012 Geosparc nv, http://www.geosparc.com/, Belgium.
 *
 * The program is available in open source according to the GNU Affero
 * General Public License. All contributions in this program are covered
 * by the Geomajas Contributors License Agreement. For full licensing
 * details, see LICENSE.txt in the project root.
 */
package org.geomajas.plugin.deskmanager.client.gwt.common;

import org.geomajas.plugin.deskmanager.client.gwt.common.RolesWindow.AskRoleCallback;

/**
 * @author Oliver May
 *
 */
public interface ProfileSelectionWindow {

	/**
	 * Ask for the role. This message will actually open a window asking for the roll.
	 * 
	 * @param callback
	 *            callback for when a role is selected.
	 */
	void askRole(String geodeskId, final AskRoleCallback callback);

}