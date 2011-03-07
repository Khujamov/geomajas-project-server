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

package org.geomajas.plugin.caching.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Random;

import com.vividsolutions.jts.geom.Geometry;
import org.geomajas.global.CacheableObject;
import org.geomajas.global.GeomajasException;
import org.geomajas.service.pipeline.PipelineContext;
import org.jboss.serial.io.JBossObjectOutputStream;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.twmacinta.util.MD5;

/**
 * Implementation of {@link CacheKeyService}.
 *
 * @author Joachim Van der Auwera
 */
@Component
public class CacheKeyServiceImpl implements CacheKeyService {

	private static final int BASE_KEY_LENGTH = 512;

	private static final char[] CHARACTERS = {
			'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
			'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
			'u', 'v', 'w', 'x', 'y', 'z',
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

	private static final String ENCODING = "UTF-8";

	private final Logger log = LoggerFactory.getLogger(CacheKeyServiceImpl.class);

	private Random random = new Random();

	public String getCacheKey(CacheContext context) {
		try {
			MD5 md5 = new MD5();
			StringBuilder toHash = new StringBuilder(BASE_KEY_LENGTH);
			if (context instanceof CacheContextImpl) {
				CacheContextImpl cci = (CacheContextImpl) context;
				for (Map.Entry<String, Object> entry : cci.entries()) {
					md5.Update(entry.getKey(), ENCODING);
					md5.Update(":");
					if (log.isDebugEnabled()) {
						toHash.append(entry.getKey());
						toHash.append(":");
					}
					Object value = entry.getValue();
					if (null != value) {
						String cid = getCacheId(value);
						md5.Update(cid, ENCODING);
						if (log.isDebugEnabled()) {
							toHash.append(cid);
						}
					}
					md5.Update("-");
					if (log.isDebugEnabled()) {
						toHash.append("-");
					}
				}
			} else {
				String cid = getCacheId(context);
				md5.Update(cid, ENCODING);
				if (log.isDebugEnabled()) {
					toHash.append(cid);
				}
			}
			String key = md5.asHex();
			log.debug("key for context {} which is a hash for {}", key, toHash.toString());
			return key;
		} catch (UnsupportedEncodingException uee) {
			log.error("Impossible error, UTF-8 should be supported:" + uee.getMessage(), uee);
			return null;
		}
	}

	private String getCacheId(Object value) {
		if (value instanceof CacheableObject) {
			return ((CacheableObject) value).getCacheId();
		} else if (value instanceof CoordinateReferenceSystem) {
			return ((CoordinateReferenceSystem) value).toWKT();
		} else if (value instanceof Geometry) {
			return ((Geometry) value).toText();
		} else {
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
				JBossObjectOutputStream serialize = new JBossObjectOutputStream(baos);
				serialize.smartClone(value);
				serialize.flush();
				serialize.close();
				return baos.toString("UTF-8");
			} catch (IOException ioe) {
				String fallback = value.toString();
				log.error("Could not serialize {}, falling back to toString() which may cause problems.", value);
				return fallback;
			}
		}
	}

	public CacheContext getCacheContext(PipelineContext pipelineContext, String[] keys) {
		CacheContext res = new CacheContextImpl();
		for (String key : keys) {
			try {
				res.put(key, pipelineContext.get(key));
			} catch (GeomajasException ge) {
				log.error(ge.getMessage(), ge);
			}
		}
		return res;
	}

	public String makeUnique(String duplicateKey) {
		log.debug("Need to make key {} unique.", duplicateKey);
		return duplicateKey + CHARACTERS[random.nextInt(CHARACTERS.length)];
	}
}
