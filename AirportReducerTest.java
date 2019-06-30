package ads.set4.airports;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class AirportReducerTest {

	@Test
	public void testRandomGraph() {
		// 1
		Set<Connection> input = createConnections("test1");
		Set<Connection> out = AirportReducer.minimalSpanningTree(320, input);
		Set<Connection> correct = createConnections("test1_solution");
		assertEquals(correct, out);
	}

	@Test
	public void testSingleAirport() {
		// 2
		Set<Connection> input = new HashSet<Connection>();
		Set<Connection> out = AirportReducer.minimalSpanningTree(1, input);
		assertEquals(input, out);
	}

	@Test
	public void testPath() {
		// 3
		Set<Connection> input = createConnections("test3");
		Set<Connection> out = AirportReducer.minimalSpanningTree(10, input);
		Set<Connection> correct = createConnections("test3_solution");
		assertEquals(correct, out);
	}

	@Test
	public void testTree() {
		// 4
		Set<Connection> input = createConnections("test4");
		Set<Connection> out = AirportReducer.minimalSpanningTree(10, input);
		Set<Connection> correct = createConnections("test4_solution");
		assertEquals(correct, out);
	}

	@Test
	public void testTwoAirports() {
		// 5
		Set<Connection> input = createConnections("test5");
		Set<Connection> out = AirportReducer.minimalSpanningTree(2, input);
		Set<Connection> correct = createConnections("test5_solution");
		assertEquals(correct, out);
	}

	@Test
	public void testCircle() {
		Set<Connection> input = createConnections("test6");
		Set<Connection> out = AirportReducer.minimalSpanningTree(10, input);
		Set<Connection> correct = createConnections("test6_solution");
		assertEquals(correct, out);
	}

	@Test
	public void testCompleteGraph() {
		Set<Connection> input = createConnections("test7");
		Set<Connection> out = AirportReducer.minimalSpanningTree(500, input);
		Set<Connection> correct = createConnections("test7_solution");
		assertEquals(correct, out);
	}

	/**
	 * Parses config files.
	 * 
	 * @param config
	 * @return
	 */
	private Set<Connection> createConnections(String config) {
		Set<Connection> test = new HashSet<Connection>();
		try {
			URL tmp = getClass().getResource(config);
			Stream<String> lines = Files.lines(Paths.get(tmp.toURI()));
			lines.forEach(line -> {
				String[] tokens = line.split(";");
				test.add(new Connection(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]),
						Integer.parseInt(tokens[2])));

			});
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}

		return test;
	}
}

