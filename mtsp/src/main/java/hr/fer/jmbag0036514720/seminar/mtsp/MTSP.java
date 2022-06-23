package hr.fer.jmbag0036514720.seminar.mtsp;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.*;

public class MTSP {

    int minCities; // per salesman
    int maxCities; // per salesman
    int m;
    Map<Integer, Map<String, Double>> cityCoordinates;
    double[][] cities;
    List<int[]> population = new ArrayList<>();
    Random rand = new Random();

    // home depot is assumed to be 0th city
    public MTSP(int m, List<AbstractMap.SimpleEntry<Double, Double>> cityCoordinates, int minCities, int maxCities) {
        this.m = m;
        cities = new double[cityCoordinates.size()][cityCoordinates.size()];
        this.cityCoordinates = IntStream.range(0, cityCoordinates.size())
            .boxed().collect(Collectors.toMap(i -> i, i -> {
                var pair = cityCoordinates.get(i);
                return Map.of("x", pair.getKey(), "y", pair.getValue());
            }));
        this.minCities = minCities;
        this.maxCities = maxCities;
        // Initialize all distances between cities
        for(int i = 0; i < cityCoordinates.size(); i++) {
            for(int j = 0; j < cityCoordinates.size(); j++) {
                var pair1 = cityCoordinates.get(i);
                var x1 = pair1.getKey();
                var y1 = pair1.getValue();
                var pair2 = cityCoordinates.get(j);
                var x2 = pair2.getKey();
                var y2 = pair2.getValue();
                cities[i][j] = hypot(x1 - x2, y1 - y2);
            }
        }
    }

    private boolean isOnSegment(int p, int q, int r) {
        double px, py, qx, qy, rx, ry;
        px = cityCoordinates.get(p).get("x");
        py = cityCoordinates.get(p).get("y");
        qx = cityCoordinates.get(q).get("x");
        qy = cityCoordinates.get(q).get("y");
        rx = cityCoordinates.get(r).get("x");
        ry = cityCoordinates.get(r).get("y");
        return qx <= Math.max(px, rx) && qx >= Math.min(px, rx) &&
            qy <= Math.max(py, ry) && qy >= Math.min(py, ry);
    }

    private int getOrientation(int p, int q, int r) {
        double px, py, qx, qy, rx, ry;
        px = cityCoordinates.get(p).get("x");
        py = cityCoordinates.get(p).get("y");
        qx = cityCoordinates.get(q).get("x");
        qy = cityCoordinates.get(q).get("y");
        rx = cityCoordinates.get(r).get("x");
        ry = cityCoordinates.get(r).get("y");
        double val = (qy - py) * (rx - qx) -
            (qx - px) * (ry - qy);
        if (val == 0) return 0; // collinear
        return (val > 0)? 1: 2;
    }

    private boolean doesIntersect(int p1, int q1, int p2, int q2) {
        int o1 = getOrientation(p1, q1, p2);
        int o2 = getOrientation(p1, q1, q2);
        int o3 = getOrientation(p2, q2, p1);
        int o4 = getOrientation(p2, q2, q1);

        // General case
        if (o1 != o2 && o3 != o4)
            return true;
        // Special Cases
        // p1, q1 and p2 are collinear and p2 lies on segment p1q1
        if (o1 == 0 && isOnSegment(p1, p2, q1)) return true;
        // p1, q1 and q2 are collinear and q2 lies on segment p1q1
        if (o2 == 0 && isOnSegment(p1, q2, q1)) return true;
        // p2, q2 and p1 are collinear and p1 lies on segment p2q2
        if (o3 == 0 && isOnSegment(p2, p1, q2)) return true;
        // p2, q2 and q1 are collinear and q1 lies on segment p2q2
        if (o4 == 0 && isOnSegment(p2, q1, q2)) return true;
        return false; // Doesn't fall in any of the above cases
    }

    public static List<AbstractMap.SimpleEntry<Double, Double>> circlePoints(int stops, int radius) {
        List<AbstractMap.SimpleEntry<Double, Double>> points = new ArrayList<>();
        final double delta = 2.0 * PI / stops;
        for (int i = 0; i < stops; ++i) {
            final double alpha = delta * i;
            final double x = cos(alpha) * radius + radius;
            final double y = sin(alpha) * radius + radius;
            points.add(new AbstractMap.SimpleEntry<>(x, y));
        }
        return points;
    }

    // generates one random solution
    public int[] generate() {
        var routesList = IntStream.range(1, cities.length).boxed().collect(Collectors.toList());
        Collections.shuffle(routesList);

        var routes = routesList.stream().mapToInt(i -> i).toArray();
        int[] splits = new int[m]; Arrays.fill(splits, minCities);
        for (int i = 0; i < cities.length - 1 - minCities * m; i++) {
            splits[(int)(Math.random() * m)]++;
        }
        var solution = new int[m + cities.length - 1];
        System.arraycopy(routes, 0, solution, 0, routes.length);
        System.arraycopy(splits, 0, solution, routes.length, splits.length);
        return solution;
    }

    public List<int[]> sample(List<int[]> population, int n) {
        population = new ArrayList<>(population);
        int length = population.size();
        if (length < n) return null;
        //We don't need to shuffle the whole list
        for (int i = length - 1; i >= length - n; --i) {
            Collections.swap(population, i, rand.nextInt(i + 1));
        }
        return population.subList(length - n, length);
    }

    public int[] crossover(int[] p1, int[] p2) {
        int pt1, pt2;
        pt1 = rand.nextInt(cities.length - 1);
        pt2 = rand.nextInt(cities.length - 1);
        while (pt1 == pt2) {
            pt2 = rand.nextInt(cities.length - 1);
        }
        var tmpMin = min(pt1, pt2);
        var tmpMax = max(pt1, pt2);
        pt1 = tmpMin;
        pt2 = tmpMax;
        var p1Burst = IntStream.range(pt1, pt2)
            .boxed()
            .map(i -> p1[i]) // get all cities in parent 1
            .collect(Collectors.toUnmodifiableSet());
        var c = new int[p1.length];
        int j = 0; // second parent iter
        // copy burst from first parent between pt1 and pt2
        // other elements should be copied as-is from second parent
        var end = cities.length - 1;
        for (int i = 0; i < end; i++) {
            if (i >= pt1 && i < pt2) { // if we are in first parents burst, just copy it
                c[i] = p1[i];
            } else { // otherwise, skip every element contained in p1's burst
                while(p1Burst.contains(p2[j % end])) { j++; }
                c[i] = p2[j++ % end];
            }
        }
        // copy tour lengths randomly from any of parents
        if (rand.nextBoolean()) {
            System.arraycopy(p1, end, c, end, m);
        } else {
            System.arraycopy(p2, end, c, end, m);
        }
        return c;
    }

    public int[] mutate(int[] solution) {
        int pt1, pt2;
        pt1 = rand.nextInt(cities.length - 1);
        pt2 = rand.nextInt(cities.length - 1);
        var tmpMin = min(pt1, pt2);
        var tmpMax = max(pt1, pt2);
        pt1 = tmpMin;
        pt2 = tmpMax;

        // reverse part of tour
        for (int i = pt1; i < pt2 / 2; i++) {
            var tmp = solution[i];
            solution[i] = solution[pt2 - i];
            solution[pt2 - i] = tmp;
        }

        // mutate tour lengths (sometimes left intact)
        for(int i = 0; i + 1 < m; i += 2) {
            var move = rand.nextBoolean() ? 1 : -1; // -1 or 1
            var tmpMove1 = solution[cities.length - 1 + i] + move;
            var tmpMove2 = solution[cities.length - 1 + i + 1] - move;
            if (tmpMove1 >= minCities && tmpMove1 < maxCities &&
                tmpMove2 >= minCities && tmpMove2 < maxCities) {
                solution[cities.length - 1 + i] += move;
                solution[cities.length - 1 + i + 1] -= move;
            }
        }
        return solution;
    }

    // fix crossing points
    public int[] twoOptSwap(int[] solution) {
        // for(int i = 1; i < cities.length - 1; i++) {
        //     if(doesIntersect(solution[0], solution[1]))
        // }
        return solution;
    }

    public double fitness(int[] solution) {
        return -cost(solution);
    }

    private double cost(int[] solution) {
        double cost = 0;
        int offset = 0;
        for (int i = 0; i < m; i++) {
            if (solution[cities.length - 1 + i] == 0) continue;
            // from home depot to first city
            var salesmanCost = cities[0][solution[offset]];
            // for every pair of neighbouring cities in current salesman's tour
            for (int j = 0; j < solution[cities.length - 1 + i] - 1; j++) {
                salesmanCost += cities[solution[offset + j]][solution[offset + j + 1]];
            }
            offset += solution[cities.length - 1 + i];
            // accounting for returning to home depot
            salesmanCost += cities[0][solution[offset - 1]];
            cost += salesmanCost;
        }
        return cost;
    }

    public List<int[]> topBest(int n, Collection<int[]> pop) {
        return pop.stream()
            .sorted(Comparator.comparing(this::fitness).reversed())
            .limit(n)
            .toList();
    }

    public void run(Consumer<int[]> observer) {
        // initial population
        for(int i = 0; i < 150; i++) {
            population.add(generate());
        }
        // 100 generations
        while(true) {
            var newPop = new ArrayList<int[]>();
            var topBest = topBest(1, population);
            var best = topBest.get(0);
            observer.accept(best);
            newPop.addAll(topBest);

            while(newPop.size() < population.size()) {
                var fiveTournament = sample(population, 5);
                var p1 = fiveTournament.stream()
                    .max(Comparator.comparing(this::fitness)).get();
                fiveTournament = sample(population, 5);
                var p2 = fiveTournament.stream()
                    .max(Comparator.comparing(this::fitness)).get();
                var c = crossover(p1, p2);
                c = rand.nextDouble() < 0.2 ? mutate(c) : c;
                newPop.addAll(topBest(1, List.of(c, p1, p2)));
            }
            population = newPop;
        }
    }



    public static void main(String[] args) {
        var mtsp = new MTSP(2, circlePoints(10, 10), 3, 6);
        mtsp.run(sol -> {
            System.out.printf("Current best: %s%n", Arrays.toString(sol));
            System.out.printf("With cost:    %s%n", mtsp.cost(sol));
        });
    }
}
