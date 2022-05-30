package info.kgeorgiy.ja.boguslavskaya.concurrent;


import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

public class ParallelMapperImpl implements ParallelMapper {

    private static class Counter{
        int count = 0;

        public void inc(){
            count++;
        }

        public int getCount(){
            return count;
        }
    }

    private final List<Thread> listOfThreads;
    private final Queue<Runnable> queue;

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        Counter count = new Counter();
        List<R> res = new ArrayList<>();
        for (int i = 0; i < args.size(); i++){
            // :NOTE: Collection.nCopies()
            res.add(null);
            final int k = i;
            Runnable taskForThread = () -> {
                res.set(k, f.apply(args.get(k)));
                synchronized (count){
                    count.inc();
                    if (count.getCount() == args.size()){
                        count.notify();
                    }
                }
            };
            synchronized (queue){
                queue.add(taskForThread);
                queue.notify();
            }
        }
        synchronized (count){
            while (count.getCount() < args.size()){
                count.wait();
            }
        }
        return res;
    }


    /**
     * Constructor with given number of threads.
     * @param threads given number of threads to create.
     */
    public ParallelMapperImpl(int threads){
        listOfThreads = new ArrayList<>();
        // :NOTE: ArrayDeque - fixed
        queue = new ArrayDeque<>();
        for (int i = 0; i < threads; i++){
            listOfThreads.add(new Thread(() -> {
                Runnable goal;
                while (!Thread.currentThread().isInterrupted()){
                    synchronized (queue){
                        while (queue.isEmpty()){
                            try {
                                queue.wait();
                            } catch (InterruptedException e) {
                                // :NOTE: ignore
                                return;
                            }
                        }
                        goal = queue.poll();
                    }
                    goal.run();
                }
            }));
            listOfThreads.get(i).start();
        }
    }



    @Override
    public void close() {
        for (Thread thread : listOfThreads){
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e){
                // :NOTE: ignore
            }
        }
    }
}

