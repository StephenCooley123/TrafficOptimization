package networkBuilder;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class Car {
	Color color;
	Road road;
	double time;
	double nextEventTime;
	Car ahead;
	Car behind;

	// double reactionTime = 0.1;
	Intersection destination;
	Intersection start;
	double positionFraction = 0;
	double followDistance = 0.1; // distance until it becomes a following car. This can just be done using
	// regular distance, as roads are straight on this scale
	static final boolean STOPPED = true;
	static final boolean NOT_STOPPED = false;
	boolean isStopped = STOPPED;
	Network network;

	public Car(double time, Network network) {
		color = new Color((int) (Math.random() * 256), (int) (Math.random() * 256), (int) (Math.random() * 256));
		this.network = network;
	}

	public void update(double timestep) {
		
		calculateAhead();
		// NEED TO DO CAR AHEAD AND BEHIND
		

		
		time += timestep;

		if (!isStopped) {
			move(road.speed * timestep);
		}

		isStopped = false;
		for (Ambulance a : road.ambulances) {
			if (road.distanceBetween(a, this) < Network.ambulanceStopDistance) {
				isStopped = true;
				//System.out.println("Close to ambulance");
			}
		}

		// check for green light (ie. if red, break from the method)
		if ((!destination.greenLights.contains(road) && destination.roadWithAmbulance != road)
				&& Network.dist2D(getX(), getY(), destination.lon, destination.lat) < followDistance) {
			isStopped = true;
			//System.out.println("Traffic Light");
		}
		
		if (ahead != null) {
			if (Network.dist2D(getX(), getY(), ahead.getX(), ahead.getY()) < followDistance && ahead.positionFraction > this.positionFraction) {
				isStopped = true;
				//System.out.println("Car Ahead");
			}
		}
		

		if (positionFraction >= 1) {
			// pick a random road from the intersection that isnt this one
			destination.count();
			Road next = destination.roadsOut.get((int) (Math.random() * destination.roadsOut.size()));
			if (next.cars.size() > 0) {
				ahead = next.cars.get(next.cars.size() - 1);
				ahead.behind = this;
			} else {
				ahead = null;
			}
			road.cars.remove(this);
			road = next;
			next.cars.add(this);
			positionFraction = 0;
			start = destination;
			destination = road.end;
			if (behind != null) {
				behind.ahead = null;
			}

		}
	}

	public void calculateAhead() {
		// TODO Auto-generated method stub
		if(road.cars.size() == 1) {
			ahead = null;
			return;
		}
		Car best = road.cars.get(0);
		if(best == this) {
			best = road.cars.get(1);
		}
		for(Car c : road.cars) {
			if(c.positionFraction > positionFraction && c.positionFraction < best.positionFraction) {
				best = c;
			}
		}
		
		ahead = best;
		ahead.behind = this;
	}

	public double getX() {
		return start.lon + (destination.lon - start.lon) * positionFraction;
	}

	public double getY() {
		return start.lat + (destination.lat - start.lat) * positionFraction;
	}

	public void move(double distance) {
		double roadDist = Network.dist2D(start.lon, start.lat, destination.lon, destination.lat);
		positionFraction += distance / roadDist;
		// System.out.println("moved");
	}

}
