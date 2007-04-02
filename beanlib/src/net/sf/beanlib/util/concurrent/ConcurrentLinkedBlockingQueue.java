/*
 * Written by Hanson Char and released to the public domain,
 * as explained at http://creativecommons.org/licenses/publicdomain
 */
package net.sf.beanlib.util.concurrent;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * A {@link java.util.concurrent.ConcurrentLinkedQueue ConcurrentLinkedQueue} 
 * that additionally supports operations
 * that wait for the queue to become non-empty when retrieving an element.
 * <p>
 * Note there is currently no such class in Java 6.
 * <p>
 * In contrast to {@link java.util.concurrent.LinkedBlockingQueue LinkedBlockingQueue}
 * which is always bounded, a ConcurrentLinkedBlockingQueue is unbounded.
 * 
 * @author Hanson Char
 * @param <E> the type of elements held in this collection
 */
public class ConcurrentLinkedBlockingQueue<E> extends AbstractQueue<E>
        implements java.io.Serializable 
{
    private static final long serialVersionUID = -191767472599610115L;

    private static class ThreadMarker {
        final Thread thread;
        // assumed parked until found otherwise.
        volatile boolean parked = true;
        
        ThreadMarker(Thread thread)
        {
            this.thread = thread;
        }
    }
    
    private final ConcurrentLinkedQueue<ThreadMarker> parkq = new ConcurrentLinkedQueue<ThreadMarker>();
    
    private final ConcurrentLinkedQueue<E> q;

    public ConcurrentLinkedBlockingQueue() {
        q = new ConcurrentLinkedQueue<E>();
    }

    public ConcurrentLinkedBlockingQueue(Collection<? extends E> c) {
        q = new ConcurrentLinkedQueue<E>(c);
    }

    @Override
    public Iterator<E> iterator() {
        return q.iterator();
    }

    @Override
    public int size() {
        return q.size();
    }

    public boolean offer(E e) {
        boolean b = q.offer(e);
        
        for (;;)
        {
            ThreadMarker marker = parkq.poll();
            
            if (marker == null)
                return b;
            if (marker.parked) 
            {
                LockSupport.unpark(marker.thread);
                return b;
            }
        }
    }

    public E peek() {
        return q.peek();
    }

    public E poll() {
        return q.poll();
    }

    /**
     * Retrieves and removes the head of this queue, waiting if necessary until
     * an element becomes available.
     * 
     * @return the head of this queue
     * @throws InterruptedException if interrupted while waiting
     */
    public E take() throws InterruptedException 
    {
        for (;;) {
            E e = q.poll();

            if (e != null)
                return e;
            ThreadMarker m = new ThreadMarker(Thread.currentThread());
            
            if (Thread.interrupted())
            {   // avoid the parkq.offer(m) if already interrupted
                throw new InterruptedException();
            }
            parkq.offer(m);
            // check again in case there is data race
            e = q.poll();

            if (e != null)
            {   // data race indeed
                m.parked = false;
                return e;
            }
            LockSupport.park();
            m.parked = false;
            
            if (Thread.interrupted()) 
                throw new InterruptedException();
        }
    }
    
    /**
     * Retrieves and removes the head of this queue, waiting up to the specified
     * wait time if necessary for an element to become available.
     * 
     * @param timeout
     *            how long to wait before giving up, in units of <tt>unit</tt>.
     *            A negative timeout is treated the same as to wait forever.
     * @param unit
     *            a <tt>TimeUnit</tt> determining how to interpret the
     *            <tt>timeout</tt> parameter
     * @return the head of this queue, or <tt>null</tt> if the specified
     *         waiting time elapses before an element is available
     * @throws InterruptedException if interrupted while waiting
     */
    public E poll(final long timeout, final TimeUnit unit) throws InterruptedException 
    {
        if (timeout < 0)
            return take();  // treat -ve timeout same as to wait forever
        final long t1 = System.nanoTime() + unit.toNanos(timeout);
        
        for (;;) {
            E e = q.poll();

            if (e != null)
                return e;
            final long duration = t1 - System.nanoTime();
            
            if (duration <= 0)
                return null;    // time out
            ThreadMarker m = new ThreadMarker(Thread.currentThread());
            
            if (Thread.interrupted())
            {   // avoid the parkq.offer(m) if already interrupted
                throw new InterruptedException();
            }
            parkq.offer(m);
            // check again in case there is data race
            e = q.poll();

            if (e != null)
            {   // data race indeed
                m.parked = false;
                return e;
            }
            LockSupport.parkNanos(duration);
            m.parked = false;
            
            if (Thread.interrupted()) 
                throw new InterruptedException();
        }
    }
}