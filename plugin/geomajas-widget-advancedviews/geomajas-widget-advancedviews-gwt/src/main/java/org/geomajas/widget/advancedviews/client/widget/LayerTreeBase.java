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

package org.geomajas.widget.advancedviews.client.widget;

import org.geomajas.configuration.client.ClientLayerTreeInfo;
import org.geomajas.configuration.client.ClientLayerTreeNodeInfo;
import org.geomajas.configuration.client.ClientToolInfo;
import org.geomajas.global.Api;
import org.geomajas.gwt.client.action.ToolbarBaseAction;
import org.geomajas.gwt.client.action.layertree.LayerTreeAction;
import org.geomajas.gwt.client.action.layertree.LayerTreeModalAction;
import org.geomajas.gwt.client.action.layertree.LayerTreeRegistry;
import org.geomajas.gwt.client.i18n.I18nProvider;
import org.geomajas.gwt.client.map.MapModel;
import org.geomajas.gwt.client.map.event.LayerDeselectedEvent;
import org.geomajas.gwt.client.map.event.LayerSelectedEvent;
import org.geomajas.gwt.client.map.event.LayerSelectionHandler;
import org.geomajas.gwt.client.map.event.MapModelEvent;
import org.geomajas.gwt.client.map.event.MapModelHandler;
import org.geomajas.gwt.client.map.layer.Layer;
import org.geomajas.gwt.client.widget.MapWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.types.SelectionType;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.HTMLFlow;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.LayoutSpacer;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.tree.Tree;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.TreeNode;
import com.smartgwt.client.widgets.tree.events.FolderClickEvent;
import com.smartgwt.client.widgets.tree.events.FolderClickHandler;
import com.smartgwt.client.widgets.tree.events.LeafClickEvent;
import com.smartgwt.client.widgets.tree.events.LeafClickHandler;

/**
 * The LayerTree shows a tree resembling the available layers for the map
 * Several actions can be executed on the layers (make them invisible, ...).
 * 
 * TODO This is a copy from LayerTree, should make original properties protected, of add get/setters
 * 
 * @author Kristof Heirwegh
 * @author Frank Wynants
 * @author Pieter De Graef
 * @since 1.0.0 (1.6.0)
 */
@Api
public abstract class LayerTreeBase extends Canvas implements LeafClickHandler, FolderClickHandler,
		LayerSelectionHandler {

	protected static final int LAYERTREEBUTTON_SIZE = 24;

	protected static final int DEFAULT_ICONSIZE = 18;

	protected final HTMLFlow htmlSelectedLayer = new HTMLFlow(I18nProvider.getLayerTree().activeLayer(
			I18nProvider.getLayerTree().none()));

	protected ToolStrip toolStrip;

	protected LayerTreeTreeNode selectedLayerTreeNode;

	protected TreeGrid treeGrid;

	protected RefreshableTree tree;

	protected MapModel mapModel;

	protected boolean initialized;

	// -------------------------------------------------------------------------
	// Constructor:
	// -------------------------------------------------------------------------

	/**
	 * Initialize the LayerTree, using a MapWidget as base reference. It will
	 * display the map's layers, as configured in the XML configuration, and
	 * select/deselect the layer as the user clicks on them in the tree.
	 * 
	 * @param mapWidget
	 *            map widget this layer tree is connected to
	 * @since 1.0.0
	 */
	@Api
	public LayerTreeBase(final MapWidget mapWidget) {
		super();
		setHeight100();
		mapModel = mapWidget.getMapModel();
		htmlSelectedLayer.setWidth100();
		treeGrid = new TreeGrid();
		treeGrid.setSelectionType(SelectionStyle.SINGLE);

		// Wait for the MapModel to be loaded
		mapModel.addMapModelHandler(new MapModelHandler() {
			public void onMapModelChange(MapModelEvent event) {
				if (!initialized) {
					buildTree();
					// toolStrip = buildToolstrip(mapWidget);
					//
					// // display the toolbar and the tree
					// VLayout vLayout = new VLayout();
					// vLayout.setSize("100%", "100%");
					// vLayout.addMember(toolStrip);
					htmlSelectedLayer.setBackgroundColor("#cccccc");
					htmlSelectedLayer.setAlign(Alignment.CENTER);
					// vLayout.addMember(htmlSelectedLayer);
					// vLayout.addMember(treeGrid);
					treeGrid.markForRedraw();
					LayerTreeBase.this.addChild(treeGrid);
					LayerTreeBase.this.markForRedraw();
				}
				initialized = true;
			}
		});
		mapModel.addLayerSelectionHandler(this);
	}

	// -------------------------------------------------------------------------
	// LayerSelectionHandler implementation:
	// -------------------------------------------------------------------------

	/**
	 * When a layer deselection event comes in, the LayerTree must also deselect
	 * the correct node in the tree, update the selected layer text, and update
	 * all buttons icons.
	 * 
	 * @since 1.0.0
	 */
	@Api
	public void onDeselectLayer(LayerDeselectedEvent event) {
		ListGridRecord selected = treeGrid.getSelectedRecord();
		if (selected != null) {
			treeGrid.deselectRecord(selected);
		}
		selectedLayerTreeNode = null;
		htmlSelectedLayer.setContents(I18nProvider.getLayerTree().activeLayer(I18nProvider.getLayerTree().none()));

		// Canvas[] toolStripMembers = toolStrip.getMembers();
		// updateButtonIconsAndStates(toolStripMembers);
	}

	/**
	 * When a layer selection event comes in, the LayerTree must also select the
	 * correct node in the tree, update the selected layer text, and update all
	 * buttons icons.
	 * 
	 * @since 1.0.0
	 */
	@Api
	public void onSelectLayer(LayerSelectedEvent event) {
		for (TreeNode node : tree.getAllNodes()) {
			if (node.getName().equals(event.getLayer().getLabel())) {
				selectedLayerTreeNode = (LayerTreeTreeNode) node;
				treeGrid.selectRecord(selectedLayerTreeNode);
				htmlSelectedLayer.setContents(I18nProvider.getLayerTree().activeLayer(
						selectedLayerTreeNode.getLayer().getLabel()));

				// Canvas[] toolStripMembers = toolStrip.getMembers();
				// updateButtonIconsAndStates(toolStripMembers);
			}
		}
	}

	// -------------------------------------------------------------------------
	// LeafClickHandler, FolderClickHandler
	// -------------------------------------------------------------------------

	/**
	 * When the user clicks on a folder nothing gets selected.
	 */
	public void onFolderClick(FolderClickEvent event) {
		mapModel.selectLayer(null);
	}

	/**
	 * When the user clicks on a leaf the headertext of the treetable is changed
	 * to the selected leaf and the toolbar buttons are updated to represent the
	 * correct state of the buttons.
	 */
	public void onLeafClick(LeafClickEvent event) {
		LayerTreeTreeNode layerTreeNode = (LayerTreeTreeNode) event.getLeaf();
		if (null != selectedLayerTreeNode
				&& layerTreeNode.getLayer().getId().equals(selectedLayerTreeNode.getLayer().getId())) {
			mapModel.selectLayer(null);
		} else {
			mapModel.selectLayer(layerTreeNode.getLayer());
		}
	}

	// -------------------------------------------------------------------------
	// Getters:
	// -------------------------------------------------------------------------

	/**
	 * Get the currently selected tree node.
	 * 
	 * @return selected node
	 */
	public LayerTreeTreeNode getSelectedLayerTreeNode() {
		return selectedLayerTreeNode;
	}

	// -------------------------------------------------------------------------
	// Private methods:
	// -------------------------------------------------------------------------

	/**
	 * Builds the toolbar
	 * 
	 * @param mapWidget
	 *            The mapWidget containing the layerTree
	 * @return {@link com.smartgwt.client.widgets.toolbar.ToolStrip} which was
	 *         built
	 */
	protected ToolStrip buildToolstrip(MapWidget mapWidget) {
		toolStrip = new ToolStrip();
		toolStrip.setWidth100();
		toolStrip.setPadding(3);

		ClientLayerTreeInfo layerTreeInfo = mapModel.getMapInfo().getLayerTree();
		if (layerTreeInfo != null) {
			for (ClientToolInfo tool : layerTreeInfo.getTools()) {
				String id = tool.getId();
				IButton button = null;
				ToolbarBaseAction action = LayerTreeRegistry.getToolbarAction(id, mapWidget);
				if (action instanceof LayerTreeAction) {
					button = new LayerTreeButton(this, (LayerTreeAction) action);
				} else if (action instanceof LayerTreeModalAction) {
					button = new LayerTreeModalButton(this, (LayerTreeModalAction) action);
				}
				if (button != null) {
					toolStrip.addMember(button);
					LayoutSpacer spacer = new LayoutSpacer();
					spacer.setWidth(2);
					toolStrip.addMember(spacer);
				}
			}
		}
		final Canvas[] toolStripMembers = toolStrip.getMembers();
		// delaying this fixes an image 'undefined' error
		Timer t = new Timer() {

			@Override
			public void run() {
				updateButtonIconsAndStates(toolStripMembers);
			}

		};
		t.schedule(10);
		return toolStrip;
	}

	/**
	 * Builds up the tree showing the layers.
	 */
	protected void buildTree() {
		treeGrid.setWidth100();
		treeGrid.setHeight100();
		treeGrid.setShowHeader(false);
		treeGrid.setOverflow(Overflow.AUTO);
		tree = new RefreshableTree();
		final TreeNode nodeRoot = new TreeNode("ROOT");
		tree.setRoot(nodeRoot); // invisible ROOT node (ROOT node is required)

		ClientLayerTreeInfo layerTreeInfo = mapModel.getMapInfo().getLayerTree();
		if (layerTreeInfo != null) {
			ClientLayerTreeNodeInfo treeNode = layerTreeInfo.getTreeNode();
			processNode(treeNode, nodeRoot, false);
		}

		treeGrid.setData(tree);
		treeGrid.addLeafClickHandler(this);
		treeGrid.addFolderClickHandler(this);
		tree.openFolder(nodeRoot);
		syncNodeState(nodeRoot);
	}

	/**
	 * Processes a treeNode (add it to the TreeGrid)
	 * 
	 * @param treeNode
	 *            The treeNode to process
	 * @param nodeRoot
	 *            The root node to which the treeNode has te be added
	 * @param tree
	 *            The tree to which the node has to be added
	 * @param mapModel
	 *            map model
	 * @param refresh
	 *            True if the tree is refreshed (causing it to keep its expanded
	 *            state)
	 */
	protected abstract void processNode(final ClientLayerTreeNodeInfo treeNode, final TreeNode nodeRoot,
			final boolean refresh);

	protected abstract void syncNodeState(final TreeNode treeNode);
	
	/**
	 * Updates the icons and the state of the buttons in the toolbar based upon
	 * the currently selected layer
	 * 
	 * @param toolStripMembers
	 *            data for the toolbar
	 */
	private void updateButtonIconsAndStates(Canvas[] toolStripMembers) {
		for (Canvas toolStripMember : toolStripMembers) {
			if (toolStripMember instanceof LayerTreeModalButton) {
				((LayerTreeModalButton) toolStripMember).update();
			} else if (toolStripMember instanceof LayerTreeButton) {
				((LayerTreeButton) toolStripMember).update();
			}
		}
	}

	/**
	 * General definition of an action button for the layer tree.
	 * 
	 * @author Frank Wynants
	 * @author Pieter De Graef
	 */
	protected class LayerTreeButton extends IButton {

		private LayerTreeBase tree;

		private LayerTreeAction action;

		public LayerTreeButton(final LayerTreeBase tree, final LayerTreeAction action) {
			this.tree = tree;
			this.action = action;
			setWidth(LAYERTREEBUTTON_SIZE);
			setHeight(LAYERTREEBUTTON_SIZE);
			setIconSize(LAYERTREEBUTTON_SIZE - 8);
			setIcon(action.getIcon());
			setTooltip(action.getTooltip());
			setActionType(SelectionType.BUTTON);
			setShowDisabledIcon(false);
			addClickHandler(new ClickHandler() {

				public void onClick(ClickEvent event) {
					try {
						action.onClick(tree.getSelectedLayerTreeNode().getLayer());
						update();
					} catch (Throwable t) {
						GWT.log("LayerTreeButton onClick error", t);
					}
				}
			});
		}

		public void update() {
			LayerTreeTreeNode selected = tree.getSelectedLayerTreeNode();
			if (selected != null && action.isEnabled(selected.getLayer())) {
				setDisabled(false);
				setIcon(action.getIcon());
				setTooltip(action.getTooltip());
			} else {
				setDisabled(true);
				GWT.log("LayerTreeButton" + action.getDisabledIcon());
				setIcon(action.getDisabledIcon());
				setTooltip("");
			}
		}
	}

	/**
	 * General definition of a modal button for the layer tree.
	 * 
	 * @author Frank Wynants
	 * @author Pieter De Graef
	 */
	protected class LayerTreeModalButton extends IButton {

		private LayerTreeBase tree;

		private LayerTreeModalAction modalAction;

		/**
		 * Constructor
		 * 
		 * @param tree
		 *            The currently selected layer
		 * @param modalAction
		 *            The action coupled to this button
		 */
		public LayerTreeModalButton(final LayerTreeBase tree, final LayerTreeModalAction modalAction) {
			this.tree = tree;
			this.modalAction = modalAction;
			setWidth(LayerTreeBase.LAYERTREEBUTTON_SIZE);
			setHeight(LayerTreeBase.LAYERTREEBUTTON_SIZE);
			setIconSize(LayerTreeBase.LAYERTREEBUTTON_SIZE - 8);
			setIcon(modalAction.getDeselectedIcon());
			setActionType(SelectionType.CHECKBOX);
			setTooltip(modalAction.getDeselectedTooltip());
			setShowDisabledIcon(false);

			this.addClickHandler(new ClickHandler() {

				public void onClick(ClickEvent event) {
					LayerTreeTreeNode selectedLayerNode = tree.getSelectedLayerTreeNode();
					if (LayerTreeModalButton.this.isSelected()) {
						modalAction.onSelect(selectedLayerNode.getLayer());
					} else {
						modalAction.onDeselect(selectedLayerNode.getLayer());
					}
					selectedLayerNode.updateIcon();
					update();
				}
			});
		}

		public void update() {
			LayerTreeTreeNode selected = tree.getSelectedLayerTreeNode();
			if (selected != null && modalAction.isEnabled(selected.getLayer())) {
				setDisabled(false);
			} else {
				setSelected(false);
				setDisabled(true);
				GWT.log("LayerTreeModalButton" + modalAction.getDisabledIcon());
				setIcon(modalAction.getDisabledIcon());
				setTooltip("");
			}
			if (selected != null && modalAction.isSelected(selected.getLayer())) {
				setIcon(modalAction.getSelectedIcon());
				setTooltip(modalAction.getSelectedTooltip());
				select();
			} else if (selected != null) {
				setIcon(modalAction.getDeselectedIcon());
				setTooltip(modalAction.getDeselectedTooltip());
				deselect();
			}
		}
	}

	/**
	 * A SmartGWT Tree with one extra method 'refresh'. This is needed to update
	 * icons on the fly in a tree
	 * 
	 * @author Frank Wynants
	 */
	protected class RefreshableTree extends Tree {
		/**
		 * Refreshes the icons in the tree, this is done by closing and
		 * reopening all nodes A dirty solution but no other option was found at
		 * the time
		 */
		public void refreshIcons() {
			TreeNode[] openNodes = this.getOpenList(this.getRoot());

			this.closeAll();
			for (TreeNode openNode : openNodes) {
				this.openFolder(openNode);
			}
		}
	}

	/**
	 * A node inside the LayerTree.
	 * 
	 * @author Frank Wynants
	 * @author Pieter De Graef
	 */
	public class LayerTreeTreeNode extends TreeNode {

		protected RefreshableTree tree;

		protected Layer<?> layer;

		/**
		 * Constructor creates a TreeNode with layer.getLabel as label.
		 * 
		 * @param tree
		 *            tree for node
		 * @param layer
		 *            The layer object
		 */
		public LayerTreeTreeNode(RefreshableTree tree, Layer<?> layer) {
			super(layer.getLabel());
			this.layer = layer;
			this.tree = tree;
			updateIcon();
		}

		/**
		 * Causes the node to check its status (visible, showing labels, ...)
		 * and to update its icon to match its status.
		 */
		public void updateIcon() {
			if (getLayer().isShowing()) {
				if (getLayer().isLabeled()) {
					// show icon labeled and showing
					setIcon("[ISOMORPHIC]/geomajas/widget/layertree/layer-show-labeled.png");
				} else {
					// show showing icon
					setIcon("[ISOMORPHIC]/geomajas/widget/layertree/layer-show.png");
				}
			} else {
				// show not showing
				setIcon("[ISOMORPHIC]/geomajas/widget/layertree/layer-hide.png");
			}
			tree.refreshIcons();
		}

		public Layer<?> getLayer() {
			return layer;
		}
	}
}