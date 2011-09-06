/*
 * This is part of Geomajas, a GIS framework, http://www.geomajas.org/.
 *
 * Copyright 2008-2011 Geosparc nv, http://www.geosparc.com/, Belgium.
 *
 * The program is available in open source according to the GNU Affero
 * General Public License. All contributions in this program are covered
 * by the Geomajas Contributors License Agreement. For full licensing
 * details, see LICENSE.txt in the project root.
 */

package org.geomajas.puregwt.client.map.event;

import org.geomajas.annotation.FutureApi;
import org.geomajas.global.UserImplemented;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent.Type;

/**
 * Handler that catches changes in layer style.
 * 
 * @author Pieter De Graef
 * @since 1.0.0
 */
@FutureApi(allMethods = true)
@UserImplemented
public interface LayerStyleChangedHandler extends EventHandler {

	Type<LayerStyleChangedHandler> TYPE = new Type<LayerStyleChangedHandler>();

	/**
	 * Called when the style of a layer has changed.
	 * 
	 * @param event
	 *            event
	 */
	void onLayerStyleChanged(LayerStyleChangedEvent event);
}