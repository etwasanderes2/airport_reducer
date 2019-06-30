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

import static org.junit.jupiter.api.Assertions.*;

class AirportReducerTestNY {

    Set<Connection> createConnections(String configFile) {
        Set<Connection> test = new HashSet<Connection>();
        try {
            URL tmp = getClass().getResource(configFile);
            Stream<String> lines = Files.lines(Paths.get(tmp.toURI()));
            lines.forEach(line -> {
                if (line.charAt(0) == 'a') {
                    String[] tokens = line.split(" ");
                    test.add(new Connection(Integer.parseInt(tokens[1])-1, Integer.parseInt(tokens[2])-1, Integer.parseInt(tokens[3])));
                }
            });
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return test;
    }


    @Test
    void newYork() {
        final Set<Connection> connections = createConnections("road_ny");
        final int nodes = 264346;
        System.out.println("start");
        Set<Connection> result = AirportReducer.minimalSpanningTree(nodes, connections);
        System.out.println(result);
    }

    @Test
    void florida() {
        final Set<Connection> connections = createConnections("road_florida");
        final int nodes = 1070376;
        System.out.println("start");
        Set<Connection> result = AirportReducer.minimalSpanningTree(nodes, connections);
        System.out.println(result);
    }

    @Test
    void westernUsa() {
        final Set<Connection> connections = createConnections("road_wusa");
        final int nodes = 6262104;
        System.out.println("start");
        Set<Connection> result = AirportReducer.minimalSpanningTree(nodes, connections);
        System.out.println(result);
    }

    @Test
    void usa() {
        final Set<Connection> connections = createConnections("road_usa");
        final int nodes = 23947347;
        System.out.println("start");
        Set<Connection> result = AirportReducer.minimalSpanningTree(nodes, connections);
        System.out.println(result);
    }


}