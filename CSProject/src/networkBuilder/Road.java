package networkBuilder;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.ArrayList;

public class Road {
	public int ID;
	public Intersection start;
	public Intersection end;
	double distance;
	double speed;
	//double ambulanceSpeed = speed * Ambulance.ambulanceSpeedFactor;
	double volume;
	ArrayList<Car> cars = new ArrayList<Car>(); // acts as a queue
	ArrayList<Ambulance> ambulances = new ArrayList<Ambulance>();

	public Road(int ID, Intersection start, Intersection end, double distance, double speed, double volume) {
		this.ID = ID;
		this.start = start;
		this.end = end;
		this.distance = distance;
		this.speed = speed;
		this.volume = volume;
	}

	@Override
	public String toString() {
		return "Road [ID=" + ID + ", start=" + start + ", end=" + end + ", distance=" + distance + ", speed=" + speed
				+ ", volume=" + volume + "]";
	}
	/**
	 * 
	 * @param distanceFraction
	 * @param time
	 */
	public void addAmbulance(double distanceFraction, double time, Network network) {
		Ambulance a = new Ambulance(time, network);
		a.start = start;
		a.destination = end;
		a.positionFraction = distanceFraction;
		a.road = this;
		a.generateDirections(1000);
		ambulances.add(a);
	}
	public void addAmbulance(double distanceFraction, Ambulance a) {
		a.start = start;
		a.destination = end;
		a.positionFraction = distanceFraction;
		a.road = this;
		a.generateDirections(99);
		ambulances.add(a);
	}

	public void addCar(double distanceFraction, double time, Network network) {
		Car c = new Car(time, network);
		c.start = start;
		c.destination = end;
		c.positionFraction = distanceFraction;
		c.road = this;
		if (cars.size() > 0) {
			c.ahead = cars.get(cars.size() - 1);
			cars.get(cars.size() - 1).behind = c;
		}
		cars.add(c);
		/*
		System.out.print("Car: start: " + c.start.lon + " " + c.start.lat);
		System.out.print(" pos: " + (c.start.lon + (c.destination.lon - c.start.lon) * distanceFraction) + " " + (c.start.lat + (c.destination.lat - c.start.lat) * distanceFraction));
		System.out.print(" end: " + c.destination.lon + " " + c.destination.lat);
		System.out.println();*/
		
		c.update(0);
		
	}

	public double distanceBetween(Car a, Car b) {
		return Math.abs(a.positionFraction * distance - b.positionFraction * distance);
	}
}
