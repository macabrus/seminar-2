package hr.fer.jmbag0036514720.seminar.mtsp;

import io.jenetics.Chromosome;
import io.jenetics.EnumGene;
import io.jenetics.util.ISeq;

public class FixedSumChromosome implements Chromosome<EnumGene<Integer>> {
    private static int parts;
    private int sum;
    private int[] nums;

    private FixedSumChromosome(int sum, int length) {
        this.sum = sum;
        nums = new int[length];
    }

    public static FixedSumChromosome of(int sum, int length) {
        var ch = new FixedSumChromosome(sum, length);
        return ch;
    }

    @Override
    public Chromosome<EnumGene<Integer>> newInstance(ISeq<EnumGene<Integer>> iSeq) {
        var tmpSum = iSeq.stream().mapToInt(EnumGene::allele).sum();

        return null;
    }

    @Override
    public EnumGene<Integer> get(int i) {
        return null;
    }

    @Override
    public int length() {
        return 0;
    }

    @Override
    public Chromosome<EnumGene<Integer>> newInstance() {
        return null;
    }
}
