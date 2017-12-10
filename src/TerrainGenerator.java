

import java.util.Random;

public class TerrainGenerator {
	
	private static Random rand = new Random();
	
	/**
	 * 
	 * @param width
	 * @param height
	 * @param patchSize
	 * @param patchFrequency
	 * @param centerWeighting
	 * @return
	 */
	public static boolean[][] generateTerrain(int width, int height, int patchSize, double patchFrequency, double centerWeighting) {
		
		int centerX = width/2;
		int centerY = height/2;
		boolean[][] vals = new boolean[width][height];
		double maxDist = distance(centerX, centerY, width, centerY);
		
		for (int x = patchSize/2; x < width; x+=patchSize) {
			for (int y = patchSize/2; y < height; y+=patchSize) {				
				double distFactor = distance(centerX, centerY, x, y) / maxDist;
				double randNum = rand.nextDouble() * distFactor;
				if (randNum < centerWeighting * centerWeighting) {
					vals[x][y] = true;
					 
					for (int m = x - patchSize/2; m < x + patchSize/2; m++) {
						for (int n = y - patchSize/2; n < y + patchSize/2; n++) {
							double chance = rand.nextDouble();
							if (chance < patchFrequency && m >= 0 && m < width && n >= 0 && n < height) {
								vals[m][n] = true;
							}
						}
					}
				}				
			}
		}
		
		return vals;
		
	}
	
	private static double distance(int centerX, int centerY, int x, int y) {
		int dX = x - centerX;
		int dY = y - centerY;
		return Math.sqrt(dX*dX + dY*dY);	
	}

}
