package networkBuilder;

import java.util.ArrayList;

public class Ambulance extends Car {
	ArrayList<Integer> directionList = new ArrayList<Integer>();
	static double ambulanceSpeedFactor = 2;

	// double speedFactor = 1;

	public Ambulance(double time, Network network) {
		super(time, network);
		// speedFactor = ambulanceSpeedFactor;
		// TODO Auto-generated constructor stub
	}

	public void generateDirections(int numTurns) {
		for (int i = 0; i < numTurns; i++) {
			directionList.add((int) (Math.random() * 1000) + 1000); // depending on num of ways into an intersection,
																	// which one
			// // is picked will be determined by mod
		}
	}

	public void update(double timestep) {
		// NEED TO DO CAR AHEAD AND BEHIND
		time += timestep;

		if (timestep != 1) {
			System.out.println("Timestep error: " + timestep);
		}
		move(road.speed * ambulanceSpeedFactor * timestep);

		if (positionFraction >= 1) {
			// System.out.println("directions: " + directionList.size());
			// pick a random road from the intersection that isnt this one
			// destination.count();
			// generateDirections(1);
			updateIntersections();
			if (directionList.size() == 0) {
				for (Road r : this.network.roads) {
					r.ambulances.remove(this);
				}
				return;
			}
			road.ambulances.remove(this);

			destination.roadWithAmbulance = null;
			int nextDirection = directionList.remove(0);
			Road next = destination.roadsOut.get((int) (nextDirection % destination.roadsOut.size()));
			// System.out.println("New Road");
			road = next;

			positionFraction = 0.0001;
			road.ambulances.add(this);
			start = road.start;
			destination = road.end;
			// speedDivisor++;

		}
	}

	private void updateIntersections() {
		ArrayList<Integer> virtualDirList = (ArrayList<Integer>) directionList.clone();
		updateIntersection(virtualDirList, destination, 0);

	}

	// recursively notifies each intersection of when the soonest ambulance is
	// coming
	private void updateIntersection(ArrayList<Integer> virtualDirList, Intersection dest, int i) {
		if (virtualDirList.size() == 0) {
			return;
		}
		Road next = dest.roadsOut.get((int) (virtualDirList.remove(0) % dest.roadsOut.size()));
		Intersection nextDest = next.end;
		if (nextDest.blocksUntilAmbulance == null || i < nextDest.blocksUntilAmbulance) {
			nextDest.blocksUntilAmbulance = i;
			if (i <= Intersection.clearIntersectionThreshold) {
				nextDest.roadWithAmbulance = next;
				//further extensions include making it automatically pick the closest ambulance to flush the roads for
			}
		}
		updateIntersection(virtualDirList, nextDest, i + 1);
	}

}
