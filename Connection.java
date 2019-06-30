package ads.set4.airports;

import java.util.Comparator;
import java.util.Objects;

/**
 * Represents a connection between two airports. Connections can be taken in either direction. They
 * have an associated cost and can be compared with one another (and thus sorted) based on that
 * cost. Two connections are considered equal if they connect the same airports, even if they differ
 * in cost. This is because we don't allow more than one connection between two airports.
 * 
 * <p>This class offers a comparator to sort lists of connections with. If you have a list of
 * connections called {@code list}, you can sort them by cost using the following code:</p>
 * 
 * <pre>
 * list.sort(Connection.COST_COMPARATOR);
 * </pre>
 */
public class Connection {
	
	/** A comparator you can use to sort lists of connectors. See class comment for details. */
	public static final Comparator<Connection> COST_COMPARATOR =
			(conn1, conn2) -> conn1.getCost() - conn2.getCost();
	
	/** One of the two incident airports. */
	private final int airport1;
	/** Other of the two incident airports. */
	private final int airport2;
	/** The connection's cost. */
	private final int cost;
	
	
	/**
	 * Creates a new connection between the two airports with the given cost.
	 */
	public Connection(final int airport1, final int airport2, final int cost) {
		if (airport1 < 0 || airport2 < 0) {
			throw new IllegalArgumentException("Airports must be >= 0.");
		}
		
		if (airport1 == airport2) {
			throw new IllegalArgumentException("Connections must connect different airports.");
		}
		
		if (cost < 1) {
			throw new IllegalArgumentException("Cost must be >= 1");
		}
		
		// To be properly comparable, we always save the airport with the lower index in airport1
		if (airport1 < airport2) {
			this.airport1 = airport1;
			this.airport2 = airport2;
		} else {
			this.airport1 = airport2;
			this.airport2 = airport1;
		}
		
		this.cost = cost;
	}
	
	
	/**
	 * Returns the first of the two airports the connection connects.
	 */
	public int getAirport1() {
		return airport1;
	}
	
	/**
	 * Returns the second of the two airports the connection connects.
	 */
	public int getAirport2() {
		return airport2;
	}
	
	/**
	 * Returns the cost associated with this connection.
	 */
	public int getCost() {
		return cost;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Connection) {
			Connection c = (Connection) obj;
			return c.airport1 == this.airport1
					&& c.airport2 == this.airport2;
			
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		// We build the hash code based on our fields
		return Objects.hash(airport1, airport2);
	}
	
	@Override
	public String toString() {
		return airport1 + " <-> " + airport2 + " (cost " + cost + ")";
	}

}
