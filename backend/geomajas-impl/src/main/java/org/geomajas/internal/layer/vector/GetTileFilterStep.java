/*
 * This file is part of Geomajas, a component framework for building
 * rich Internet applications (RIA) with sophisticated capabilities for the
 * display, analysis and management of geographic information.
 * It is a building block that allows developers to add maps
 * and other geographic data capabilities to their web applications.
 *
 * Copyright 2008-2010 Geosparc, http://www.geosparc.com, Belgium
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.geomajas.internal.layer.vector;

import org.geomajas.global.ExceptionCode;
import org.geomajas.global.GeomajasException;
import org.geomajas.layer.VectorLayer;
import org.geomajas.layer.tile.InternalTile;
import org.geomajas.layer.tile.TileMetadata;
import org.geomajas.service.FilterService;
import org.geomajas.service.GeoService;
import org.geomajas.service.pipeline.PipelineCode;
import org.geomajas.service.pipeline.PipelineContext;
import org.geomajas.service.pipeline.PipelineStep;
import org.geotools.geometry.jts.JTS;
import org.opengis.filter.Filter;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.TransformException;
import org.springframework.beans.factory.annotation.Autowired;

import com.vividsolutions.jts.geom.Envelope;


/**
 * Determine the filter which needs to be applied to get the features.
 *
 * @author Joachim Van der Auwera
 */
public class GetTileFilterStep implements PipelineStep<InternalTile> {

	private String id;

	@Autowired
	private FilterService filterService;

	@Autowired
	private GeoService geoService;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void execute(PipelineContext context, InternalTile response) throws GeomajasException {
		VectorLayer layer = context.get(PipelineCode.LAYER_KEY, VectorLayer.class);
		TileMetadata metadata = context.get(PipelineCode.TILE_METADATA_KEY, TileMetadata.class);
		MathTransform maptoLayer;
		try {
			maptoLayer = context.get(PipelineCode.CRS_TRANSFORM_KEY, MathTransform.class).inverse();
		} catch (NoninvertibleTransformException e) {
			throw new GeomajasException(ExceptionCode.CRS_TRANSFORMATION_NOT_POSSIBLE, e);
		}

		String geomName = layer.getLayerInfo().getFeatureInfo().getGeometryType().getName();

		String epsg = Integer.toString(geoService.getSridFromCrs(layer.getCrs()));
		// transform tile bounds back to layer coordinates
		// TODO: for non-affine transforms this is not accurate enough !
		Envelope bounds;
		try {
			bounds = JTS.transform(response.getBounds(), maptoLayer);
			Filter filter = filterService.createBboxFilter(epsg, bounds, geomName);
			if (null != metadata.getFilter()) {
				filter = filterService.createAndFilter(filterService.parseFilter(metadata.getFilter()), filter);
			}

			context.put(PipelineCode.FILTER_KEY, filter);
		} catch (TransformException e) {
			throw new GeomajasException(ExceptionCode.CRS_TRANSFORMATION_NOT_POSSIBLE, e);
		}
	}
}