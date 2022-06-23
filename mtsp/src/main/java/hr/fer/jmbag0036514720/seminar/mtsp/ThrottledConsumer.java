package hr.fer.jmbag0036514720.seminar.mtsp;


import java.util.function.Consumer;

public class ThrottledConsumer<T> implements Consumer<T> {
    private final int every;
    private final Consumer<T> consumer;

    public ThrottledConsumer(int every, Consumer<T> consumer) {
        this.every = every;
        this.consumer = consumer;
    }
    private int i = 0;

    @Override
    public void accept(T t) {
        if (i % every == 0) {
            consumer.accept(t);
        }
        i++;
    }
};
