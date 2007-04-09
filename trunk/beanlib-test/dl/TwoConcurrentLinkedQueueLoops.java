
/*
 * Written by Hanson Char with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TwoConcurrentLinkedQueueLoops 
{
    static final ExecutorService pool = Executors.newCachedThreadPool();
    
    static class Result {
        volatile Integer r1;
        volatile Integer r2;
        
        @Override
        public String toString() {
            return "r1=" + r1 + ", r2=" + r2;
        }
    }
    
    static abstract class Stage implements Runnable {
        final int iters;
        final Queue<Integer> q1;
        final Queue<Integer> q2;
        final CyclicBarrier barrier;
        volatile Result result;
        boolean r1;
        
        Stage (Queue<Integer> q1, Queue<Integer> q2, CyclicBarrier barrier, int iters, Result result, boolean r1) {
            this.q1 = q1; 
            this.q2 = q2; 
            this.barrier = barrier;
            this.iters = iters;
            this.result = result;
            this.r1 = r1;
        }
    }
    
    static class Runner extends Stage 
    {
        Runner(Queue<Integer> q1, Queue<Integer> q2, CyclicBarrier barrier, int iters, Result result, final boolean r1) {
            super(q1, q2, barrier, iters, result, r1);
        }
        
        public void run() {
            try {
                int value = hashCode();
                barrier.await();
                q1.offer(value);
                Integer polledValue = q2.poll();
                
                if (r1)
                    result.r1 = polledValue;
                else
                    result.r2 = polledValue;
                barrier.await();
            } catch (Exception ie) { 
                ie.printStackTrace(); 
                return; 
            }
        }
    }
    
    static void oneRun(int npairs, int iters) throws Exception {
        List<Result> results = new ArrayList<Result>(npairs);
        Queue<Integer> q1 = new ConcurrentLinkedQueue<Integer>();
        Queue<Integer> q2 = new ConcurrentLinkedQueue<Integer>();
        LoopHelpers.BarrierTimer timer = new LoopHelpers.BarrierTimer();
        CyclicBarrier barrier = new CyclicBarrier(npairs * 2 + 1, timer);
        
        for (int i = 0; i < npairs; ++i) {
            Result result = new Result();
            results.add(result);
            pool.execute(new Runner(q1, q2, barrier, iters, result, true));
            pool.execute(new Runner(q2, q1, barrier, iters, result, false));
        }
        barrier.await();
        barrier.await();

        int i=0;
        for (Result result : results) {
            if (result.r1 == null && result.r2 == null) {
//                throw new AssertionError("Got both null!");
                System.err.println("===> Assertion Failure! Got both result.r1 and result.r2 null at iteration " + i);
            }
            i++;
        }
        long time = timer.getTime();
        System.out.println("\t: " + LoopHelpers.rightJustify(time / (iters * npairs)) + " ns per transfer");
    }

    static void oneTest(int pairs, int iters) throws Exception {
        oneRun(pairs, iters);
    }

    public static void main(String[] args) throws Exception {
        int maxPairs = 100;
        int iters = 100000;

        if (args.length > 0) 
            maxPairs = Integer.parseInt(args[0]);

        System.out.println("Warmup...");
        oneTest(1, 10000);
        Thread.sleep(100);
        oneTest(2, 10000);
        Thread.sleep(100);
        oneTest(2, 10000);
        Thread.sleep(100);
        
        int k = 1;
        for (int i = 1; i <= maxPairs;) {
            System.out.println("Pairs:" + i);
            oneTest(i, iters);
            Thread.sleep(100);
            if (i == k) {
                k = i << 1;
                i = i + (i >>> 1);
            } 
            else 
                i = k;
        }
        pool.shutdown();
   }
}
