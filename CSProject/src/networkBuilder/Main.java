package networkBuilder;

public class Main {
	static Network network;
	static double timestep = 1;

	static double endTime = 10000;

	static int numSimulations = 10;
	
	static int stepsBetweenImages = 10;

	public static void main(String[] args) {
		for (int n = 0; n < numSimulations; n++) {
			network = new Network();
			network.time = 0;
			Intersection.clearIntersectionThreshold = n;
			
			// default files: "C:\\Users\\SCool\\Documents\\TestRoadNetwork.csv",
			// "C:\\Users\\SCool\\Documents\\TestIntersectionNetwork.csv"
			network.generateTestNetwork(100);
			// network.printNetwork("C:\\Users\\SCool\\Documents\\TestRoadNetwork.csv",
			// "C:\\Users\\SCool\\Documents\\TestIntersectionNetwork.csv");
			// network.readNetworkFromFile("C:\\Users\\SCool\\Documents\\TestRoadNetwork.csv",
			// "C:\\Users\\SCool\\Documents\\TestIntersectionNetwork.csv");
			network.populateCars();
			network.populateAmbulances();

			int lastImageTime = 0;
			while (network.time <= endTime) {
				int numCars = 0;
				for (int i = 0; i < network.roads.size(); i++) {
					for (int j = 0; j < network.roads.get(i).cars.size(); j++) {
						Car c = network.roads.get(i).cars.get(j);
						c.update(timestep);
						numCars++;
						//System.out.println("here");
					}
					for(int j = 0; j < network.roads.get(i).ambulances.size(); j++) {
						Ambulance a = network.roads.get(i).ambulances.get(j);
						a.update(timestep);
					}
					for(int j = 0; j < network.intersections.size(); j++) {
						Intersection s = network.intersections.get(j);
						s.update(timestep);
					}
				}
				// System.out.println(numCars);
				network.time += timestep;
				
				//image generation code for debugging
				/*
				if (equalsEpsilon(network.time, lastImageTime + stepsBetweenImages)) {
					// System.out.println("About to Generate Image");
					lastImageTime = (int) network.time;
					network.generateImage("NetworkImage" + ((int) network.time));
				}*/
			}

			double totalCount = 0;
			for (Intersection s : network.intersections) {
				totalCount += s.count;

			}
			System.out.println("Count:\t" + totalCount);
			System.out.println("Throughput:\t" + (totalCount / endTime));
			System.out.println("Threshold:\t" + Intersection.clearIntersectionThreshold);
		}

	}

	private static boolean equalsEpsilon(double a, double b) {
		double epsilon = 0.00001;
		return Math.abs(a - b) < epsilon;
	}
}
