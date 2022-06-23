package hr.fer.jmbag0036514720.seminar.mtsp;

import io.jenetics.*;
import io.jenetics.engine.*;
import io.jenetics.util.ISeq;
import io.jenetics.util.MSeq;
import io.jenetics.util.RandomRegistry;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

import static io.jenetics.engine.EvolutionResult.toBestPhenotype;
import static io.jenetics.engine.Limits.bySteadyFitness;
import static java.lang.Math.*;
import static java.util.Objects.requireNonNull;

public class TravelingSalesman implements Problem<ISeq<double[]>, EnumGene<double[]>, Double> {

    private final ISeq<double[]> _points;

    // Create new TSP problem instance with given way points .
    public TravelingSalesman(ISeq<double[]> points) {
        _points = requireNonNull(points);
    }

    @Override
    public Function<ISeq<double[]>, Double> fitness() {
        return p -> IntStream.range(0, p.length())
            .mapToDouble(i -> {
                final double[] p1 = p.get(i);
                final double[] p2 = p.get((i + 1) % p.size());
                return hypot(p1[0] - p2[0], p1[1] - p2[1]);
            }).sum();
    }

    public Codec<ISeq<double[]>, EnumGene<double[]>> codec() {
        return Codecs.ofPermutation(_points);
    }

    public static <T> Consumer<T> throttledConsumer(int every, Consumer<T> consumer) {
        return new ThrottledConsumer<>(every, consumer);
    }


    // Create a new TSM example problem with the given number
    // of stops. All stops lie on a circle with the given radius.
    public static TravelingSalesman of(int stops, double radius) {
        final MSeq<double[]> points = MSeq.ofLength(stops);
        final double delta = 2.0 * PI / stops;
        for (int i = 0; i < stops; ++i) {
            final double alpha = delta * i;
            final double x = cos(alpha) * radius + radius;
            final double y = sin(alpha) * radius + radius;
            points.set(i, new double[]{x, y});
        }
        // Shuffling of the created points .
        final var random = RandomRegistry.random();
        for (int j = points.length() - 1; j > 0; --j) {
            final int i = random.nextInt(j + 1);
            final double[] tmp = points.get(i);
            points.set(i, points.get(j));
            points.set(j, tmp);
        }
        return new TravelingSalesman(points.toISeq());
    }

    public static void main(String[] args) {
        int stops = 20;
        double R = 10;
        double minPathLength = 2.0 * stops * R * sin(PI / stops);
        TravelingSalesman tsm = TravelingSalesman.of(stops, R);
        Engine<EnumGene<double[]>, Double> engine = Engine.builder(tsm)
            .optimize(Optimize.MINIMUM).maximalPhenotypeAge(11)
            .populationSize(500).alterers(
                new SwapMutator<>(0.2),
                new PartiallyMatchedCrossover<>(0.35))
            .build();
        // Create evolution statistics consumer.
        EvolutionStatistics<Double, ?>
            statistics = EvolutionStatistics.ofNumber();
        Phenotype<EnumGene<double[]>, Double> best = engine.stream()
            // Truncate the evolution stream after 25 " steady "
            // generations .
            .limit(bySteadyFitness(25))
            // The evolution will stop after maximal 250
            // generations .
            .limit(250)
            // Update the evaluation statistics after // each generation
            .peek(statistics)
            .peek(throttledConsumer(10, s -> {
                System.out.println(s.bestFitness());
                s.bestPhenotype();
            }))
            // Collect (reduce) the evolution stream to
            // its best phenotype.
            .collect(toBestPhenotype());
        System.out.println(statistics);
        System.out.println("Known min path length: " + minPathLength);
        System.out.println("Found min path length: " + best.fitness());
    }
}
