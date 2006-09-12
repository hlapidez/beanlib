/*
 * Written by Hanson Char and released to the public domain,
 * as explained at http://creativecommons.org/licenses/publicdomain
 */
package net.sf.beanlib.util.concurrent;

import java.util.Queue;
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
    
    @Override
    protected Queue<Integer> getQueue() {
        return q;
    }
    
    @Override
    protected Callable<Void> newConumerCallable() {
        return new Callable<Void>()
        {
            public Void call() throws InterruptedException 
            {
                for (int count=0; count < TOTAL; count++)
                    q.take();
                return null;
            }
        };
    }
    
    public static void main(String[] args) throws InterruptedException, ExecutionException
    {
        new LinkedBlockingQueueTestMain().call();
        System.exit(0);
    }
}
