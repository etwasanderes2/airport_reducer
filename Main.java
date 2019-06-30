package ads.set4.airports;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Main {

    private static int AIRPORT_COUNT=10000;

    public static void main(String[] args) {
        Random rng = new Random(42);
        HashSet<Connection> test = new HashSet<>();

        //generating Connections
        for (int i = 0; i < AIRPORT_COUNT/10; i++) {
            System.out.println(i);
            for (int j = i+1; j < AIRPORT_COUNT; j++) {
                test.add(new Connection(i, j, rng.nextInt(100000)+1));
            }
        }

        System.out.println("starting calculation");
        Set<Connection> result = AirportReducer.minimalSpanningTree(AIRPORT_COUNT, test);
        System.out.println(result);
    }

}
