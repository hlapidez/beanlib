/*
 * Written by Hanson Char and released to the public domain,
 * as explained at http://creativecommons.org/licenses/publicdomain
 */
package net.sf.beanlib.util.concurrent;

import java.util.concurrent.ExecutionException;

/**
 * Used to test the performance of 
 * ConcurrentLinkedBlockingQueue vs LinkedBlockingQueue.
 * 
 * @see LinkedBlockingQueueTestMain
 * @see BlockingQueueTestMain
 * 
 * @author Hanson Char
 */
public class TestMain 
{
    public static void main(String[] args) 
        throws InterruptedException, ExecutionException
    {
        for (int i=0; i < 10; i++)
        {
            new ConcurrentLinkedBlockingQueueTestMain().call();
            // try to minimize residual memory effect
            System.gc();
            new LinkedBlockingQueueTestMain().call();
            // try to minimize residual memory effect
            System.gc();
        }
        System.exit(0);
    }
}
