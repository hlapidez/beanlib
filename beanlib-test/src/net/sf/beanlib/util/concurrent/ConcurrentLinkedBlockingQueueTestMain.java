/*
 * Written by Hanson Char and released to the public domain,
 * as explained at http://creativecommons.org/licenses/publicdomain
 */
package net.sf.beanlib.util.concurrent;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * Used to test the performance of ConcurrentLinkedBlockingQueue.
 * 
 * @see LinkedBlockingQueueTestMain
 * @author Hanson Char
 */
public class ConcurrentLinkedBlockingQueueTestMain extends AbstractBlockingQueueTestMain 
{
    private final ConcurrentLinkedBlockingQueue<Integer> q = 
        new ConcurrentLinkedBlockingQueue<Integer>();
    
    public ConcurrentLinkedBlockingQueueTestMain(float wcRatio, int numConsumer, int numProducer, Integer capacity) 
    {
        super(wcRatio, numConsumer, numProducer, capacity);
    }
    
    public ConcurrentLinkedBlockingQueueTestMain()
    {
    }
    
    @Override
    protected Queue<Integer> getQueue() {
        return q;
    }
    
    @Override
    protected Callable<Void> newConumerCallable(final int max) {
        return new Callable<Void>()
        {
            public Void call() throws InterruptedException 
            {
                for (int count=0; count < max; count++)
                    q.take();
                return null;
            }
        };
    }

    @Override
    protected BlockingQueue<Runnable> newThreadPoolBlockingQueue(Integer capacity) {
        return capacity == null 
             ? new ConcurrentLinkedBlockingQueue<Runnable>() 
             : new ConcurrentLinkedBoundedBlockingQueue<Runnable>(capacity)
             ;
    }
    
    public static void main(String[] args) 
        throws InterruptedException, ExecutionException
    {
        new ConcurrentLinkedBlockingQueueTestMain().call();
        System.exit(0);
    }
}