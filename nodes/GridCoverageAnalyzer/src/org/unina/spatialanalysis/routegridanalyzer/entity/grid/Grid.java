package org.unina.spatialanalysis.routegridanalyzer.entity.grid;

import java.io.IOException;
import java.util.Iterator;

public class Grid implements Iterable<GridSlot> {
	
	/**
	 * The Grid associated with the on-going execution.
	 */
	private final GridSlot[][] grid;
	
	/**
	 * The GridManager associated with the current execution.
	 */
	private final GridManager manager;
	
	
	public Grid(double minLat, double maxLat, double minLon, double maxLon, int numberOfRows, int numberOfColumns) {
		this.manager = new GridManager(minLat, maxLat, minLon, maxLon, numberOfRows, numberOfColumns);
		this.grid = manager.makeGrid();
	}
	
	public int[] getGridSlotPosition(Hit h) {
		return this.manager.getGridSlotPosition(h.getLongitude(), h.getLatitude());
	}
	
	public boolean addHitToGrid(Hit h) {
		if(this.manager.checkIfValidPosition(h.getLongitude(), h.getLatitude())) {
			int[] loc = this.manager.getGridSlotPosition(h.getLongitude(), h.getLatitude());
			grid[loc[0]][loc[1]].addVisit(h.getTime());
			return true;
		}else {
			return false;
		}
	}
	
	public boolean checkIfValidPosition(Hit h) {
		return this.manager.checkIfValidPosition(h.getLongitude(), h.getLatitude());
	}
	
	private class GridManager {
		
		/**
		 * The field minLat identifies the minimum value of latitude for this Grid Manager and its Grid.
		 */
		private final double minLat;
		
		/**
		 * The field minLon identifies the minimum value of longitude for this Grid Manager and its Grid.
		 */
		private final double minLon;
		
		/**
		 * The field numberOfRows identifies the number of rows of this GridManager and its Grid
		 */
		private final int numberOfRows;
		private final int numberOfColumns;
		
		private final double deltaOnLongitude;
		private final double deltaOnLatitude;
		
		private GridSlot[][] makeGrid(){
		
			GridSlot[][] res = new GridSlot[numberOfRows][numberOfColumns];
		
			double [] minimumLongitudes = new double[numberOfRows];
			double [] maximumLongitudes = new double[numberOfRows];
			
			for(int i = 0; i<numberOfRows; i++) {
				minimumLongitudes[i] = minLon + i*deltaOnLongitude/numberOfRows;
				maximumLongitudes[i] = minLon + (i+1)*deltaOnLongitude/numberOfRows;
			}
			
			double [] minimumLatitudes = new double[numberOfColumns];
			double [] maximumLatitudes = new double[numberOfColumns];
			for(int i = 0; i<numberOfRows; i++) {
				minimumLatitudes[i] = minLat + i*deltaOnLatitude/numberOfColumns;
				maximumLatitudes[i] = minLat + (i+1)*deltaOnLatitude/numberOfColumns;
			}
			
			for(int i=0; i<numberOfRows; i++) {
				for(int j=0; j<numberOfColumns; j++) {
					res[i][j] = new GridSlot(minimumLatitudes[j], maximumLatitudes[j], minimumLongitudes[i], maximumLongitudes[i]);
				}
			}
			
			
			return res;
		}
		
		private int[] getGridSlotPosition(double longitude, double latitude) {
			int row = ((Double)(Math.floor((Math.abs(longitude-manager.minLon))/(manager.deltaOnLongitude/manager.numberOfRows)))).intValue();
			int column = ((Double)(Math.floor((Math.abs(latitude-manager.minLat))/(manager.deltaOnLatitude/manager.numberOfColumns)))).intValue();
			return new int [] {row,column};
		}

		public boolean checkIfValidPosition(double longitude, double latitude) {
			int [] loc = this.getGridSlotPosition(longitude, latitude);
			return 0<=loc[0] && loc[0]<this.numberOfRows && 0<=loc[1] && loc[1]<this.numberOfColumns ;
		}
		
		
		private GridManager(double minLat, double maxLat, double minLon, double maxLon, int numberOfRows, int numberOfColumns) {
			super();
			this.minLat = minLat;
			this.minLon = minLon;
			this.numberOfColumns = numberOfColumns;
			this.numberOfRows = numberOfRows;
			this.deltaOnLatitude = Math.abs(maxLat-minLat);
			this.deltaOnLongitude = Math.abs(maxLon-minLon);
		}
	}
	
	public class MyGridIterator implements Iterator<GridSlot>{
		
		private int currentRow = 0;
		private int currentColumn = 0;
		private int numberOfRows;
		private int numberOfColumns;
		
		public MyGridIterator(int numberOfRows2, int numberOfColumns2) {
			this.numberOfRows = numberOfRows2;
			this.numberOfColumns = numberOfColumns2;
		}

		@Override
		public boolean hasNext() {
			return currentRow<numberOfRows && currentColumn<numberOfColumns;
		}

		@Override
		public GridSlot next() {
			GridSlot res = grid[currentRow][currentColumn];
			if(currentColumn!=numberOfColumns-1) {
				currentColumn++;
			}else {
				currentRow++;
				currentColumn = 0;
			}
			return res;
		}
		
	}

	@Override
	public Iterator<GridSlot> iterator() {
		return new Grid.MyGridIterator(this.manager.numberOfRows, this.manager.numberOfColumns);
	}
}
