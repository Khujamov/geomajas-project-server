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

package org.geomajas.widget.utility.smartgwt.client.ribbon;

import java.util.ArrayList;
import java.util.List;

import org.geomajas.widget.utility.client.ribbon.RibbonColumn;
import org.geomajas.widget.utility.client.ribbon.RibbonGroup;
import org.geomajas.widget.utility.smartgwt.client.util.GuwLayout;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.HTMLFlow;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

/**
 * A SmartGWT ribbon group. Groups together a list of SmartGWT ribbon columns.
 * 
 * @author Pieter De Graef
 */
public class RibbonGroupLayout extends VLayout implements RibbonGroup {

	private String title;

	private boolean showTitle;

	private List<RibbonColumn> columns = new ArrayList<RibbonColumn>();

	private HLayout memberLayout;

	private HTMLFlow titleLabel;

	private String buttonBaseStyle = "ribbonButton";

	// ------------------------------------------------------------------------
	// Constructors:
	// ------------------------------------------------------------------------

	/**
	 * Initialize this group by specifying it's title.
	 * 
	 * @param title
	 *            The groups title.
	 */
	public RibbonGroupLayout(final String title) {
		this.title = title;
		setAutoWidth();
		setHeight100();

		memberLayout = new HLayout(GuwLayout.ribbonGroupInternalMargin);
		memberLayout.setStyleName("ribbonGroupBody");
		memberLayout.setAutoWidth();
		setHeight100();

		titleLabel = new HTMLFlow(title);
		titleLabel.setHeight("16px");

		titleLabel.setLayoutAlign(Alignment.CENTER);
		titleLabel.setStyleName("ribbonGroupTitle");
		titleLabel.setVisible(showTitle);

		setStyleName("ribbonGroup");
		addMember(memberLayout);
		addMember(titleLabel);
	}

	// ------------------------------------------------------------------------
	// RibbonGroup implementation:
	// ------------------------------------------------------------------------

	public RibbonColumn getColumn(int index) {
		return columns.get(index);
	}

	public void addColumn(RibbonColumn ribbonColumn) {
		if (ribbonColumn == null) {
			throw new NullPointerException("Cannot add RibbonColumn with null value.");
		}
		ribbonColumn.setButtonBaseStyle(buttonBaseStyle);
		columns.add(ribbonColumn);
		memberLayout.addMember(ribbonColumn.asWidget());
	}

	public void addColumn(RibbonColumn ribbonColumn, int index) {
		if (ribbonColumn == null) {
			throw new NullPointerException("Cannot add RibbonColumn with null value.");
		}
		ribbonColumn.setButtonBaseStyle(buttonBaseStyle);
		columns.add(ribbonColumn);
		memberLayout.addMember(ribbonColumn.asWidget(), index);
	}

	public void removeColumn(RibbonColumn ribbonColumn) {
		if (ribbonColumn == null) {
			throw new NullPointerException("Cannot remove RibbonColumn with null value.");
		}
		columns.remove(ribbonColumn);
		memberLayout.removeMember((Canvas) ribbonColumn.asWidget());
	}

	public void removeColumn(int index) {
		removeColumn(getColumn(index));
	}

	public String getId() {
		return id;
	}

	public void setShowTitle(boolean showTitle) {
		this.showTitle = showTitle;
		titleLabel.setVisible(showTitle);
	}

	public boolean isShowTitle() {
		return showTitle;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	// ------------------------------------------------------------------------
	// SmartGWT method overrides:
	// ------------------------------------------------------------------------

	@Override
	public void setStyleName(String styleName) {
		super.setStyleName(styleName);
		memberLayout.setStyleName(styleName + "Body");
		titleLabel.setStyleName(styleName + "Title");
		buttonBaseStyle = styleName.substring(0, styleName.length() - 5) + "Button";
		for (RibbonColumn column : columns) {
			column.setButtonBaseStyle(buttonBaseStyle);
		}
	}
}