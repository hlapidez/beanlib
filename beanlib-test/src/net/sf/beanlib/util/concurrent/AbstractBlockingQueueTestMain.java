/*
 * Written by Hanson Char and released to the public domain,
 * as explained at http://creativecommons.org/licenses/publicdomain
 */
package net.sf.beanlib.util.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Base class for testing the performance of 
 * ConcurrentLinkedBlockingQueue vs LinkedBlockingQueue.
 * 
 * @author Hanson Char
 */
public abstract class AbstractBlockingQueueTestMain implements Callable<Void>
{
    protected static final int BATCH_SIZE = 100000;
    protected static final int PRODUCER_THREAD_COUNT = 10;
    protected static final int TOTAL = BATCH_SIZE * PRODUCER_THREAD_COUNT;
    
    private static Integer[] data = new Integer[TOTAL];
    
    static 
    {
        for (int i=0; i < data.length; i++)
            data[i] = i;
    }
        
    protected static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors() + 1;
    protected static final int REPEAT = 10;
    
    protected abstract Queue<Integer> getQueue();
    protected abstract Callable<Void> newConumerCallable();
    
    public Void call() throws InterruptedException, ExecutionException
    {
        long totalDuration = 0;
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        
        for (int i=REPEAT-1; i > -1; i--) 
        {
            long duration = this.test();
            System.out.println("Test#" + i + ", duration: "+ duration + " ms");
            
            if (duration < min)
                min = duration;
            if (duration > max)
                max = duration;
            totalDuration += duration;
        }
        long average = totalDuration / REPEAT;
        System.out.println();
        System.out.println(getClass().getName());
        System.out.println("Thread pool size is " + THREAD_POOL_SIZE);
        System.out.println("Total items per test: " + TOTAL
                        + ", Tested: " + REPEAT + " times"
                        + "\nAvg: " + average + " ms"
                        + "\nmin: " + min + " ms" 
                        + "\nmax: " + max + " ms"
                        );
        System.out.println();
        return null;
    }
    
    public long test() throws InterruptedException, ExecutionException 
    {
        Collection<Callable<Void>> producers = new ArrayList<Callable<Void>>();
        
        for (int i=PRODUCER_THREAD_COUNT-1; i > -1; i--)
            producers.add(newProducer(i * BATCH_SIZE));
        
        final long t0 = System.nanoTime();
        // start the consumer
        ExecutorService consumerExecutorService = Executors.newSingleThreadExecutor();
        Future<Void> consumerFuture = consumerExecutorService.submit(newConumerCallable());
        // start all producers
        ExecutorService producerExecutorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        List<Future<Void>> producerFutures = producerExecutorService.invokeAll(producers);
        // wait for all producers to complete
        for (Future<Void> producerFuture : producerFutures)
            producerFuture.get();
        // wait for consumer to complete
        consumerFuture.get();
        // Calculate the duration
        long duration = (System.nanoTime() - t0)/1000000;
        // Shutdown all thread pools
        producerExecutorService.shutdownNow();
        consumerExecutorService.shutdownNow();
        return duration;
    }
    
    /** 
     * Returns a new producer, 
     * producing BATCH_SIZE number of items from the given start number. 
     */
    private Callable<Void> newProducer(final int start) {
        return new Callable<Void>() 
        {
            public Void call() throws InterruptedException {
                Queue<Integer> q= getQueue();
                
                for (int i=start,end=start+BATCH_SIZE; i < end; i++) 
                    q.offer(data[i]);
                return null;
            }
        };
    }
}