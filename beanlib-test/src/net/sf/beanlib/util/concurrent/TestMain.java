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
        final float wcRatio = floatValue("wcRatio", "0.0");
        final int numConsumer = intValue("numConsumer", "1");
        final int numProducer = intValue("numProducer", "10");
        
        for (int i=0; i < 10; i++)
        {
            new ConcurrentLinkedBlockingQueueTestMain(wcRatio, numConsumer, numProducer).call();
            // try to minimize residual memory effect
            System.gc();
            new LinkedBlockingQueueTestMain(wcRatio, numConsumer, numProducer).call();
            // try to minimize residual memory effect
            System.gc();
        }
        System.exit(0);
    }
    
    private static int intValue(String key, String def) {
        String val = System.getProperty(key, def);
        return Integer.parseInt(val);
    }
    
    private static float floatValue(String key, String def) {
        String val = System.getProperty(key, def);
        return Float.parseFloat(val);
    }
}
