package hr.fer.jmbag0036514720.seminar.mtsp;

import io.jenetics.*;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Problem;
import io.jenetics.util.ISeq;

import java.util.AbstractMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.lang.Math.*;

public class MultipleTravelingSalesmen implements Problem<ISeq<int[]>, IntegerGene, Double> {

    private int m = 1;
    private double[][] cities;

    public MultipleTravelingSalesmen(int m, List<AbstractMap.SimpleEntry<Double, Double>> cityCoordinates) {
        this.m = m;
        cities = new double[cityCoordinates.size()][cityCoordinates.size()];
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
        DoubleChromosome.of();
    }

    @Override
    public Function<ISeq<int[]>, Double> fitness() {
        return (seq) -> seq.stream()
            .mapToDouble(chromosome -> {
                int skip = 1; // cities to skip for salesman
                var total = 0.;
                for(int i = chromosome.length - m; i < chromosome.length; i++) {
                    for(int j = skip; j < skip + chromosome[i] ; j++) {
                        total += cities[chromosome[j - 1]][chromosome[j]];
                    }
                    skip += chromosome[i];
                }
                return total;
            }).sum();
    }

    @Override
    public Codec<ISeq<int[]>, IntegerGene> codec() {
        //Crossover
        // var genotype = Genotype.of(
        //     PermutationChromosome.ofInteger(IntStream.range(0, cities.length), cities.length),
        //     FixedSumChromosome.of(cities.length, m)
        // );
        // return Codec.of(
        //     genotype,
        //     gt -> {
        //         gt.get(3);
        //         double[] solution = new double];
        //         gt.gene()
        //     }
        // );
        return null;
    }

}
