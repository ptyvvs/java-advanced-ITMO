package info.kgeorgiy.ja.boguslavskaya.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;


/**
 * implementation of {@link ScalarIP}. Makes iterative parallelism for methods to work with {@link List}.
 *
 * @author Tatiana Boguslavskaya
 */

public class IterativeParallelism implements ScalarIP {

    private ParallelMapper mapper;

    /**
     * Constructor without arguments.
     */
    public IterativeParallelism(){
        this.mapper = null;
    }

    /**
     * Constructor with {@link ParallelMapper} as argument.
     * @param mapper {@link ParallelMapper}
     */
    public IterativeParallelism(ParallelMapper mapper){
        this.mapper = mapper;
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        checkIfIsEmpty(values);
        return makeThreads(threads, values,
                st -> st.stream().max(comparator).orElseThrow(IllegalArgumentException::new)).stream().max(comparator).orElseThrow(IllegalArgumentException::new);
    }

    private <T> void checkIfIsEmpty(List<? extends T> values) throws IllegalArgumentException{
        if (values.isEmpty()){
            throw new IllegalArgumentException("null comparator");
        }
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, comparator.reversed());
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        checkIfIsEmpty(values);
        return makeThreads(threads, values, st -> st.stream().allMatch(predicate)).stream().allMatch(v -> v);
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        checkIfIsEmpty(values);
        return makeThreads(threads, values, st -> st.stream().anyMatch(predicate)).stream().anyMatch(v -> v);
    }

    private <T, R> List<R> makeThreads(int threads, List<? extends T> values, Function<List<? extends T>, R> func) throws InterruptedException{
        if (values == null || threads < 1){
            throw new IllegalArgumentException("problems with arguments");
        }
        List<List<? extends T>> listWithArgs = new ArrayList<>();
        threads = Math.min(threads, values.size());
        int elementsInThread = values.size() / threads;
        int restOfElements = values.size() % threads;
        List<R> res = new ArrayList<>(Collections.nCopies(threads, null));
        List<Thread> threadList = new ArrayList<>();
        int tmpLb = 0;
        for (int i = 0; i < threads; i++){
            int lb = tmpLb;
            int rb = lb + elementsInThread + (restOfElements > 0 ? 1 : 0);
            restOfElements--;
            listWithArgs.add(values.subList(lb, rb));
            tmpLb = rb;
        }
        if (this.mapper != null){
            return mapper.map(func, listWithArgs);
        }
        for (int i = 0; i < threads; i++){
            int k = i;
            threadList.add(new Thread(() -> res.set(k, func.apply(listWithArgs.get(k)))));
            threadList.get(i).start();
        }
        for (Thread thread : threadList){
            thread.join();
        }
        return res;
    }
}
