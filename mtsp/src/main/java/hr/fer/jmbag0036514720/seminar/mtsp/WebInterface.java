package hr.fer.jmbag0036514720.seminar.mtsp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static io.jenetics.engine.EvolutionResult.toBestPhenotype;
import static io.jenetics.engine.Limits.bySteadyFitness;

public class WebInterface {
    public static void main(String[] args) {
        var mapper = new ObjectMapper();
        var app = Javalin.create();
        app.ws("/statistics", ctx -> {
            ctx.onConnect(handler -> {
                int stops = 20;
                double R = 10;
                TravelingSalesman tsm = TravelingSalesman.of(stops, R);
                Engine<EnumGene<double[]>, Double> engine = Engine.builder(tsm)
                    .optimize(Optimize.MINIMUM).maximalPhenotypeAge(11)
                    .populationSize(500).alterers(
                        new SwapMutator<>(0.2),
                        new PartiallyMatchedCrossover<>(0.35))
                    .build();
                Phenotype<EnumGene<double[]>, Double> best = engine.stream()
                    .limit(bySteadyFitness(25))
                    //.limit(250)
                    .peek(new ThrottledConsumer<EvolutionResult<EnumGene<double[]>, Double>>(10, s -> {
                        System.out.println(s.bestFitness());
                        handler.send(s.bestPhenotype().toString());
                    }))
                    .collect(toBestPhenotype());
            });
        });
        // my from scratch implementation
        app.ws("/mtsp-from-scratch", ctx -> {
            ctx.onConnect(handler -> {
                var m = handler.queryParamAsClass("salesmen", Integer.class).getOrDefault(3);
                var points = handler.queryParamAsClass("cities", Integer.class).getOrDefault(20);
                var radius = handler.queryParamAsClass("radius", Integer.class).getOrDefault(10);
                var minCities = handler.queryParamAsClass("minCities", Integer.class).getOrDefault(10);
                var maxCities = handler.queryParamAsClass("maxCities", Integer.class).getOrDefault(50);
                var mtsp = new MTSP(m, MTSP.circlePoints(points, radius), minCities, maxCities);
                mtsp.run(new ThrottledConsumer<>(1, sol -> {
                    if (!handler.session.isOpen()) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Exiting since session is closed!");
                        return;
                    }
                    var datasets = new ArrayList<>();
                    int offset = 0;
                    for(int i = mtsp.cities.length - 1; i < mtsp.cities.length - 1 + mtsp.m; i++) {
                        var dataset = new ArrayList<>();
                        dataset.add(mtsp.cityCoordinates.get(0));
                        for (int j = offset; j < offset + sol[i]; j++) {
                            dataset.add(mtsp.cityCoordinates.get(sol[j]));
                        }
                        offset += sol[i];
                        dataset.add(mtsp.cityCoordinates.get(0));
                        datasets.add(dataset);
                    }
                    try {
                        Thread.sleep(1000);
                        handler.send(mapper.writeValueAsString(
                            Map.of("datasets", datasets,
                                   "fitness", mtsp.fitness(sol)
                            ))).get(); // block
                    } catch (InterruptedException | ExecutionException | JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }));
            });
        });
        app.get("/", ctx ->
            ctx.html(Files.readString(Paths.get(WebInterface.class.getResource("/index.html").toURI()))));
        app.start(7000);
    }
}
