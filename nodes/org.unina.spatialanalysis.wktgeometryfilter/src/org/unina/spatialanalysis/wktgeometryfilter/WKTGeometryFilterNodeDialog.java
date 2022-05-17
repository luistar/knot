package org.unina.spatialanalysis.wktgeometryfilter;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;
import javax.swing.event.MouseInputListener;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.cache.FileBasedLocalCache;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.knime.core.data.StringValue;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentButton;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.unina.spatialanalysis.wktgeometryfilter.map.SelectionAdapter;
import org.unina.spatialanalysis.wktgeometryfilter.map.SelectionPainter;

/**
 * This is an example implementation of the node dialog of the "RouteFilter"
 * node.
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}. In general, one can create an
 * arbitrary complex dialog using Java Swing.
 * 
 * @author Roland Varriale
 */
public class WKTGeometryFilterNodeDialog extends DefaultNodeSettingsPane {

	// private static final NodeLogger LOGGER = NodeLogger.getLogger(WKTGeometryFilterNodeDialog.class);
	
	private static final String PAIR_TYPE_TOOLTIP = "<html>"
			  + "This setting is used to specify the coordinate input format to be used."
			  + "<br>Both (lat, lon) and (lon, lat) formats are supported</br>"
			  + "</html>";
	
	/**
	 * New dialog pane for configuring the node. The dialog created here will show
	 * up when double clicking on a node in KNIME Analytics Platform.
	 */
	@SuppressWarnings("unchecked")
	protected WKTGeometryFilterNodeDialog() {
		super();

		this.createNewGroup("Column Selector");

		// Column Geometry
		SettingsModelColumnName geomtryColSettingModel = WKTGeometryFilterNodeModel.createGeometryColModel();
		DialogComponentColumnNameSelection heomtryColumnSelection = new DialogComponentColumnNameSelection(
				geomtryColSettingModel, "Geometry Column", 0, false, true, StringValue.class);
		addDialogComponent(heomtryColumnSelection);

		SettingsModelString coordinatePairType = WKTGeometryFilterNodeModel.createCoordinatePairTypeSettings();
		DialogComponentStringSelection pairTypeSelector = new DialogComponentStringSelection(coordinatePairType, "Select the coordinates pair type of the input data:","{lat,lon}","{lon,lat}") ;
		pairTypeSelector.setToolTipText(PAIR_TYPE_TOOLTIP);
		coordinatePairType.setEnabled(true);
		addDialogComponent(pairTypeSelector);	
		
		this.createNewGroup("Bounding Box Coordinates");
		
		// PUNTO IN ALTO A SINISTRA X
		SettingsModelDouble topLeftLon = WKTGeometryFilterNodeModel.createTopLeftCornerLonModel();
		DialogComponentNumber topLeftLonSelector = new DialogComponentNumber(topLeftLon, "Min Y", 1, 12);
		addDialogComponent(topLeftLonSelector);

		// PUNTO IN ALTO A SINISTRA Y
		SettingsModelDouble topLeftLat = WKTGeometryFilterNodeModel.createTopLeftCornerLatModel();
		DialogComponentNumber topLeftLatSelector = new DialogComponentNumber(topLeftLat, "Max Y", 1, 12);
		addDialogComponent(topLeftLatSelector);

		// PUNTO IN BASSOA DESTRA Y
		SettingsModelDouble bottomRightLat = WKTGeometryFilterNodeModel.createBottomRightCornerLatModel();
		DialogComponentNumber bottomRightLatSelector = new DialogComponentNumber(bottomRightLat, "Min X", 1, 12);
		addDialogComponent(bottomRightLatSelector);
		
		// PUNTO IN BASSO A DESTRA X
		SettingsModelDouble bottomRightLon = WKTGeometryFilterNodeModel.createBottomRightCornerLonModel();
		DialogComponentNumber bottomRightLonSelector = new DialogComponentNumber(bottomRightLon, "Max X", 1, 12);
		addDialogComponent(bottomRightLonSelector);


		DialogComponentButton button = new DialogComponentButton("Select Bounding Box From Map");
		button.addActionListener(e -> createWindow(topLeftLon, topLeftLat, bottomRightLon, bottomRightLat));
		addDialogComponent(button);

	}

	private static void createWindow(SettingsModelDouble topLeftLon, SettingsModelDouble topLeftLat, SettingsModelDouble bottomRightLon, SettingsModelDouble bottomRightLat) {
		// Create a TileFactoryInfo for OpenStreetMap
		TileFactoryInfo info = new OSMTileFactoryInfo();
		DefaultTileFactory tileFactory = new DefaultTileFactory(info);

		// Setup local file cache
		File cacheDir = new File(System.getProperty("java.io.tmpdir") + File.separator + ".jxmapviewer2");
		tileFactory.setLocalCache(new FileBasedLocalCache(cacheDir, false));

		// Setup JXMapViewer
		final JXMapViewer mapViewer = new JXMapViewer();
		mapViewer.setTileFactory(tileFactory);

		GeoPosition startPos = new GeoPosition(0, 0);

		// Set the focus
		mapViewer.setZoom(17);
		mapViewer.setAddressLocation(startPos);

		// Add interactions
		MouseInputListener mia = new PanMouseInputListener(mapViewer);
		mapViewer.addMouseListener(mia);
		mapViewer.addMouseMotionListener(mia);

		mapViewer.addMouseListener(new CenterMapListener(mapViewer));

		mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));

		mapViewer.addKeyListener(new PanKeyListener(mapViewer));

		// Add a selection painter
		SelectionAdapter sa = new SelectionAdapter(mapViewer);
		SelectionPainter sp = new SelectionPainter(sa);
		mapViewer.addMouseListener(sa);
		mapViewer.addMouseMotionListener(sa);
		mapViewer.setOverlayPainter(sp);

		// Display the viewer in a JFrame
		final JFrame frame = new JFrame();
		frame.setLayout(new BorderLayout());

		JLabel jLabel = new JLabel("Click and drag with left mouse button to pan, use the mouse wheel to zoom in/out, and click and drag the right mouse button to draw a bounding box");
		jLabel.setFont(new Font("Serif", Font.PLAIN, 14));

		JButton button = new JButton("Save bounding box and close");
		
		frame.add(jLabel, BorderLayout.NORTH);
		frame.add(button, BorderLayout.SOUTH);
		
		frame.add(mapViewer);
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setVisible(true);

		mapViewer.addPropertyChangeListener("zoom", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				updateWindowTitle(frame, mapViewer);

			}
		});

		mapViewer.addPropertyChangeListener("center", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				updateWindowTitle(frame, mapViewer);
			}
		});
		
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GeoPosition topLeftCorner = sa.getStartPos();
				
				topLeftLon.setDoubleValue(topLeftCorner.getLongitude());
				topLeftLat.setDoubleValue(topLeftCorner.getLatitude());

				GeoPosition bottomRightCorner = sa.getEndPos();
				
				bottomRightLon.setDoubleValue(bottomRightCorner.getLongitude());
				bottomRightLat.setDoubleValue(bottomRightCorner.getLatitude());	
				
				// Close the window
				frame.dispose();
			}
		});

		updateWindowTitle(frame, mapViewer);

	}

	protected static void updateWindowTitle(JFrame frame, JXMapViewer mapViewer) {
		double lat = mapViewer.getCenterPosition().getLatitude();
		double lon = mapViewer.getCenterPosition().getLongitude();
		int zoom = mapViewer.getZoom();

		frame.setTitle(String.format("Map Area Selector (%.2f / %.2f) - Zoom: %d", lat, lon, zoom));
	}

}
