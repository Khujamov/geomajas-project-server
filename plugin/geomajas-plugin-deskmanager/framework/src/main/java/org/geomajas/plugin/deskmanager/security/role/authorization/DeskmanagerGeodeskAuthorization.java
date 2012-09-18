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
package org.geomajas.plugin.deskmanager.security.role.authorization;

import org.geomajas.annotation.Api;

/**
 * Geodesk authorization for the deskmanager plugin. This interface handles the access rights to geodesks.
 * 
 * @author Kristof Heirwegh
 * @author Oliver May
 * 
 * @since 1.0.0
 */
@Api
public interface DeskmanagerGeodeskAuthorization {

	/**
	 * Checks if the user can use a geodesk.
	 * 
	 * @param publicGeodeskId the public id of the geodesk.
	 * @return true if the user may use the geodesk.
	 */
	boolean isLoketUseAllowed(String publicGeodeskId);

}