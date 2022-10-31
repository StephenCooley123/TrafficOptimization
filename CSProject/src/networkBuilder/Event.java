package networkBuilder;

public class Event implements Comparable<Event> {
	int type;
	long time;
	
	public static final int START_MOVING = 0;
	public static final int	STOP_AT_CAR = 1;
	public static final int ARRIVE_GREEN = 2;
	public static final int ARRIVE_RED = 3;
	public static final int AMBULANCE_IN_RADIUS = 4;

	public Event(long time, int type) {
		this.time = time;
		this.type = type;
	}

	@Override
	/**
	 * compares the times. in a.compareTo(b), if a is before b, -1 is returned. 0 if
	 * same, and 1 if a later than b
	 */
	public int compareTo(Event e) {
		if (time < ((Event) e).time) {
			return -1;
		} else if (time == ((Event) e).time) {
			return 0;
		} else {
			return 1;
		}
	}
}
