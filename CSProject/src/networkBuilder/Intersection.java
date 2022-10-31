package networkBuilder;

import java.util.ArrayList;

public class Intersection {
	public int ID;
	public double lat;
	public double lon;
	public boolean trafficLight;
	public ArrayList<Road> roadsIn = new ArrayList<Road>();
	public ArrayList<Road> roadsOut = new ArrayList<Road>();
	public boolean green;
	public double nextGreen;
	public double nextRed;
	public double count = 0;
	double time = 0;
	public double greenDuration = 150;
	public static int numGreenAtOnce = 2;
	public ArrayList<Road> greenLights = new ArrayList<Road>();
	public Road roadWithAmbulance;
	
	public static int clearIntersectionThreshold = 1;
	
	public Integer blocksUntilAmbulance = null; //apparently ints dont like being set to null

	public Intersection(int id, double lat, double lon, boolean trafficLight) {
		this.ID = id;
		this.lat = lat;
		this.lon = lon;
		this.trafficLight = trafficLight;
	}
	
	public void initializeGreenLights() {
		for(int i = 0; i < numGreenAtOnce; i++) {
			Road r = roadsIn.get((int) (Math.random() * roadsIn.size()));
			if(greenLights.contains(r)) {
				i--;
			} else {
				greenLights.add(r);
			}
		}
	}

	public String toString() {
		String str = ID + "," + trafficLight + "," + lat + "," + lon + "," + roadsIn.size() + "," + roadsOut.size()
				+ ",";
		for (Road r : roadsIn) {
			str = str + r.ID + ",";
		}
		for (Road r : roadsOut) {
			str = str + r.ID + ",";
		}
		return str;

	}

	public double distSQ(Intersection n) {

		return (lat - n.lat) * (lat - n.lat) + (lon - n.lon) * (lon - n.lon);
	}

	public double dist(Intersection n) {
		return Math.sqrt((lat - n.lat) * (lat - n.lat) + (lon - n.lon) * (lon - n.lon));
	}

	public double distHaversine(Intersection n) {
		// distance between latitudes and longitudes
		double dLat = Math.toRadians(n.lat - lat);
		double dLon = Math.toRadians(n.lon - lon);

		// convert to radians
		lat = Math.toRadians(lat);
		n.lat = Math.toRadians(n.lat);

		// apply formulae
		double a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat) * Math.cos(n.lat);
		double rad = 6371;
		double c = 2 * Math.asin(Math.sqrt(a));
		return rad * c;
	}

	public void count() {
		count++;
		// System.out.println("Count");
	}

	public void update(double timestep) {
		time += timestep;
		if ((int) ((time - timestep) / greenDuration) < (int) (time / greenDuration)) {
			//System.out.println("Light Change");
			ArrayList<Road> lastGreen = (ArrayList<Road>) greenLights.clone();
			greenLights.clear();
			for (Road r : lastGreen) {
				greenLights.add(roadsIn.get((roadsIn.indexOf(r) + 1) % roadsIn.size())); // adds the next road in the
																							// roadsIn array list to the
																							// green lights
			}
		}

	}

}
