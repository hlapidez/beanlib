/*
 * Written by Hanson Char and released to the public domain,
 * as explained at http://creativecommons.org/licenses/publicdomain
 */
package net.sf.beanlib.util.concurrent;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Used to test the performance of LinkedBlockingQueue.
 * 
 * @see ConcurrentLinkedBlockingQueueTestMain
 * @author Hanson Char
 */
public class LinkedBlockingQueueTestMain extends AbstractBlockingQueueTestMain 
{
    private final LinkedBlockingQueue<Integer> q = new LinkedBlockingQueue<Integer>();

    public LinkedBlockingQueueTestMain(float wcRatio, int numConsumer, int numProducer) 
    {
        super(wcRatio, numConsumer, numProducer);
    }
    
    public LinkedBlockingQueueTestMain()
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
    protected BlockingQueue<Runnable> newThreadPoolBlockingQueue() {
        return new LinkedBlockingQueue<Runnable>();
    }
    
    public static void main(String[] args) throws InterruptedException, ExecutionException
    {
        new LinkedBlockingQueueTestMain().call();
        System.exit(0);
    }
}
