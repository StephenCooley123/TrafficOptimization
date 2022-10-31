package networkBuilder;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class Network {
	ArrayList<Intersection> intersections = new ArrayList<Intersection>();
	ArrayList<Road> roads = new ArrayList<Road>();
	static double ambulanceStopDistance = 5;
	static double numAmbulances = 10;
	double time;

	public void readNetworkFromFile(String roadFile, String intersectionFile) {
		ArrayList<String> roadLines = new ArrayList<String>();
		ArrayList<String> intersectionLines = new ArrayList<String>();

		try {
			BufferedReader rr = new BufferedReader(new FileReader(roadFile));
			BufferedReader ir = new BufferedReader(new FileReader(intersectionFile));

			String line = rr.readLine();
			while (line != null) {
				roadLines.add(line);
				line = rr.readLine();
			}
			rr.close();
			roadLines.remove(0); // remove the column headers

			line = ir.readLine();
			while (line != null) {
				intersectionLines.add(line);
				line = ir.readLine();
			}
			ir.close();
			intersectionLines.remove(0); // remove the column headers

		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(intersectionLines.size());
		for (String line : intersectionLines) {
			System.out.println("Intersection ID: " + Integer.parseInt(getIndexCSV(0, line)));

			intersections.add(
					new Intersection(Integer.parseInt(getIndexCSV(0, line)), Double.parseDouble(getIndexCSV(2, line)),
							Double.parseDouble(getIndexCSV(3, line)), Boolean.parseBoolean(getIndexCSV(1, line))));

		}

		for (String line : roadLines) {
			// Road ID, Distance, Speed, Volume, Start Intersection ID, End Intersection ID
			int ID = Integer.parseInt(getIndexCSV(0, line));
			double distance = Double.parseDouble(getIndexCSV(1, line));
			double speed = Double.parseDouble(getIndexCSV(2, line));
			double volume = Double.parseDouble(getIndexCSV(3, line));
			int startID = Integer.parseInt(getIndexCSV(4, line));
			Intersection start = intFromID(startID);
			int endID = Integer.parseInt(getIndexCSV(5, line));
			Intersection end = intFromID(endID);
			System.out.println("Start: " + startID + " End: " + endID);
			addRoad(ID, start, end, distance, speed, volume);
		}
		generateImage();

	}

	public void generateTestNetwork(int nodes) {

		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				intersections.add(new Intersection(i, i, j, true));
			}

		}

		/*
		 * for (int i = 0; i < 50; i++) { intersections.add(new Intersection(i,
		 * Math.random() * 10, Math.random() * 10, true)); }
		 */

		for (Intersection n : intersections) {
			for (Intersection m : intersections) {
				if (n.dist(m) <= 1 && n != m) { // if it is within 0.1 of the other node
					roads.add(addRoad(roads.size(), n, m, n.dist(m), 0.01, 4000));
					// System.out.println("Road");
				}
			}
		}

		int longPaths = 0;
		int shortcutSize = 5;
		for (int numShortcuts = 0; numShortcuts < longPaths; numShortcuts++) {
			Intersection last = intersections.get((int) (intersections.size() * Math.random()));
			for (int i = 0; i < shortcutSize; i++) {
				Intersection second = intersections.get((int) (intersections.size() * Math.random()));
				roads.add(addRoad(roads.size(), last, second, last.dist(second), 20, 4000));
				last = second;
			}
		}

		for (Intersection n : intersections) {
			n.initializeGreenLights();
		}

		// generateImage();
	}

	public void populateCars() {
		// System.out.println("Populate Cars");
		for (Road r : roads) {
			// System.out.println("Road");
			double traversalTime = r.distance / r.speed;
			double numOnRoad = (traversalTime / 24.0) * r.distance * 0.5;
			double distanceInterval = r.distance / numOnRoad;
			// System.out.println("DistanceInterval: " + distanceInterval + " Distance: " +
			// r.distance);
			double totalDistance = 0;
			while (totalDistance < r.distance) {
				// System.out.println("Car Added");
				r.addCar(totalDistance / r.distance, 0, this);
				// r.cars.add(new Car(0));
				// r.cars.get(r.cars.size() - 1).positionFraction = totalDistance / r.distance;

				totalDistance += distanceInterval + (Math.random() - 0.5) * 0.25 * distanceInterval;
			}
			// System.out.println("Cars on road: " + r.cars.size());
		}
		for (Road r : roads) {
			for (int i = 1; i < r.cars.size(); i++) {
				r.cars.get(i - 1).ahead = r.cars.get(i);
				r.cars.get(i).behind = r.cars.get(i - 1);
			}
		}
		for (Road r : roads) {
			for (Car c : r.cars) {
				c.calculateAhead();
			}
		}
		// System.out.println("Cars populated");
		// generateImage();
	}

	public void populateAmbulances() {
		// System.out.println("Populate Cars");
		for (int i = 0; i < numAmbulances; i++) {
			Road toAddAmbulance = roads.get((int) (Math.random() * roads.size()));
			Ambulance a = new Ambulance(0, this);

			toAddAmbulance.addAmbulance(0.5, 0, this);

		}
	}

	public static String getIndexCSV(int index, String line) {
		try {
			// index starts at 0
			for (int i = 0; i < index; i++) {
				line = line.substring(line.indexOf(",") + 1, line.length());

			}
			if (line.contains(",")) {
				line = line.substring(0, line.indexOf(","));
			}
			return line;
		} catch (Exception e) {
			return null;
		}
	}

	public void generateImage() {
		generateImage("CarNetwork");
	}

	public void generateImage(String filename) {
		filename = filename + ".png";
		double minLat = Double.MAX_VALUE;
		double minLon = Double.MAX_VALUE;
		double maxLat = Double.MIN_VALUE;
		double maxLon = Double.MIN_VALUE;

		for (Intersection i : intersections) {
			if (i.lat > maxLat) {
				maxLat = i.lat;
			}
			if (i.lon > maxLon) {
				maxLon = i.lon;
			}
			if (i.lat < minLat) {
				minLat = i.lat;
			}
			if (i.lon < minLon) {
				minLon = i.lon;
			}
		}
		// lat is y, lon is x
		int xScalar = 150;
		int yScalar = 150;
		int xOffset = (int) (-1 * minLon * xScalar);
		int yOffset = (int) (-1 * minLat * yScalar);
		int dotSize = 5;

		BufferedImage bi = new BufferedImage((int) ((maxLon - minLon) * xScalar * 1.1),
				(int) ((maxLat - minLat) * yScalar * 1.1), BufferedImage.TYPE_INT_RGB);

		Color color = new Color(255, 255, 255);
		Graphics2D g = bi.createGraphics();
		g.setColor(color);

		Color carColor = new Color(255, 128, 0);
		g.setColor(carColor);
		Color ambulanceColor = new Color(255, 0, 0);
		for (Road r : roads) {
			Intersection i1 = r.start;
			Intersection i2 = r.end;
			g.setColor(color);
			g.drawLine((int) (i1.lon * xScalar + xOffset), (int) (i1.lat * yScalar + yOffset),
					(int) (i2.lon * xScalar + xOffset), (int) (i2.lat * yScalar + yOffset));
			g.setColor(
					new Color((int) (Math.random() * 256), (int) (Math.random() * 256), (int) (Math.random() * 256)));
			int dotsize = 8;
			for (Car c : r.cars) {
				g.setColor(c.color);
				// System.out.println(c.positionFraction);
				g.fillOval(
						(int) ((c.start.lon + (c.destination.lon - c.start.lon) * c.positionFraction) * xScalar
								+ xOffset) - dotSize / 2,
						(int) ((c.start.lat + (c.destination.lat - c.start.lat) * c.positionFraction) * yScalar
								+ yOffset) - dotSize / 2,
						dotsize, dotsize);
			}
		}
		for (Road r : roads) {
			Intersection i1 = r.start;
			Intersection i2 = r.end;
			g.setColor(color);
			g.drawLine((int) (i1.lon * xScalar + xOffset), (int) (i1.lat * yScalar + yOffset),
					(int) (i2.lon * xScalar + xOffset), (int) (i2.lat * yScalar + yOffset));
			g.setColor(
					new Color((int) (Math.random() * 256), (int) (Math.random() * 256), (int) (Math.random() * 256)));

			for (Ambulance c : r.ambulances) {
				// System.out.println("Drew Ambulance");
				g.setColor(ambulanceColor);
				// System.out.println(c.positionFraction);
				dotSize = 35;
				g.fillOval(
						(int) ((c.start.lon + (c.destination.lon - c.start.lon) * c.positionFraction) * xScalar
								+ xOffset) - dotSize / 2,
						(int) ((c.start.lat + (c.destination.lat - c.start.lat) * c.positionFraction) * yScalar
								+ yOffset) - dotSize / 2,
						dotSize, dotSize);
				dotSize = 8;
			}
		}

		File imageFile = new File(filename);
		try {
			ImageIO.write(bi, "PNG", imageFile);
			System.out.println("Image Written");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Image Generated");

	}

	public Intersection intFromID(int id) {
		for (Intersection i : intersections) {
			if (i.ID == id) {
				return i;
			}
		}
		return null;
	}

	public Road roadFromID(int id) {
		for (Road r : roads) {
			if (r.ID == id) {
				return r;
			}
		}
		return null;
	}

	public Road addRoad(int ID, Intersection start, Intersection end, double distance, double speed, double volume) {
		Road r = new Road(ID, start, end, distance, speed, volume);
		end.roadsIn.add(r);
		start.roadsOut.add(r);
		// System.out.println(start.roadsOut.size());
		return r;
	}

	public void printNetwork(String roadPath, String intersectionPath) {
		// default files: "C:\\Users\\SCool\\Documents\\TestRoadNetwork.csv",
		// "C:\\Users\\SCool\\Documents\\TestIntersectionNetwork.csv"

		try {
			BufferedWriter roadWriter = new BufferedWriter(new FileWriter(new File(roadPath)));

			BufferedWriter intersectionWriter = new BufferedWriter(new FileWriter(new File(intersectionPath)));
			roadWriter.write("Road ID, Distance, Speed, Volume, Start Intersection ID, End Intersection ID \n");
			for (Road r : roads) {
				roadWriter.write(r.ID + "," + r.distance + "," + r.speed + "," + r.volume + "," + r.start.ID + ","
						+ r.end.ID + '\n');
			}
			roadWriter.close();

			intersectionWriter.write("Intersection ID, isTrafficLight, lat, lon, Num In, Num Out\n");

			for (Intersection n : intersections) {
				// intersectionWriter.write(n.ID + "," + n.trafficLight + "," + n.roadsIn.size()
				// + "," + n.roadsOut.size());
				intersectionWriter.write(n.toString());
				intersectionWriter.write("\n");
			}
			roadWriter.close();
			intersectionWriter.close();

			// BufferedReader br = new BufferedReader(new FileReader(new File(filePath)));

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static double dist2D(double x1, double y1, double x2, double y2) {
		return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
	}
}
