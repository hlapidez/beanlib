/*
 * Written by Hanson Char and released to the public domain,
 * as explained at http://creativecommons.org/licenses/publicdomain
 */
package net.sf.beanlib.util.concurrent;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * An bounded concurrent blocking queue implemented upon 
 * {@link java.util.concurrent.ConcurrentLinkedQueue ConcurrentLinkedQueue}.
 * <p>
 * Note there is currently no such class in Java 6.
 * 
 * @author Hanson Char
 * @param <E> the type of elements held in this collection
 */
public class ConcurrentLinkedBoundedBlockingQueue<E> extends ConcurrentLinkedBlockingQueue<E>
{
    private static final long serialVersionUID = -3592325646231732466L;
    
    private final AtomicInteger capacity;
    private final ConcurrentLinkedQueue<ThreadMarker> putparkq = new ConcurrentLinkedQueue<ThreadMarker>();

    public ConcurrentLinkedBoundedBlockingQueue(int capacity) {
        if (capacity <= 0) 
            throw new IllegalArgumentException();
        this.capacity = new AtomicInteger(capacity);
    }

    public ConcurrentLinkedBoundedBlockingQueue(Collection<? extends E> c) {
        this(Integer.MAX_VALUE);
        for (E e : c)
            add(e);
    }

    @Override
    public boolean offer(E e) 
    {
        if (tryDecrementCapacity())
            return super.offer(e);
        return false;
    }
    
    private boolean tryDecrementCapacity() 
    {
        int capacity;
        do {
            capacity = this.capacity.get();
            
            if (capacity == 0)
                return false;
        } while (!this.capacity.weakCompareAndSet(capacity, capacity-1));
        
        return true;
    }

    @Override
    public E poll() {
        E e = super.poll();
        
        if (e != null) {
            this.capacity.incrementAndGet();
            unparkIfAny();
        }
        return e;
    }

    /**
     * Retrieves and removes the head of this queue, waiting if necessary until
     * an element becomes available.
     * 
     * @return the head of this queue
     * @throws InterruptedException if interrupted while waiting
     */
    @Override
    public E take() throws InterruptedException 
    {
        E e = super.take();
        this.capacity.incrementAndGet();
        unparkIfAny();
        return e;
    }
    
    private void unparkIfAny() 
    {
        for (;;)
        {
            ThreadMarker marker = putparkq.poll();
            
            if (marker == null)
                return;
            if (marker.parked) {
                LockSupport.unpark(marker.thread);
                return;
            }
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
    @Override
    public E poll(final long timeout, final TimeUnit unit) throws InterruptedException 
    {
        E e = super.poll(timeout, unit);
        
        if (e != null) {
            this.capacity.incrementAndGet();
            unparkIfAny();
        }
        return e;
    }
    
    @Override
    public void put(E e) throws InterruptedException 
    {
        for (;;) {
            if (tryDecrementCapacity()) {
                super.put(e);
                return;
            }
            ThreadMarker m = new ThreadMarker(Thread.currentThread());
            
            if (Thread.interrupted())
            {   // avoid the putparkq.offer(m) if already interrupted
                throw new InterruptedException();
            }
            putparkq.offer(m);
            // check again in case there is data race
            if (tryDecrementCapacity())
            {   // data race indeed
                m.parked = false;
                super.put(e);
                return;
            }
            LockSupport.park();
            m.parked = false;
          
            if (Thread.interrupted()) 
                throw new InterruptedException();
        }
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        if (timeout < 0) 
        {   // treat -ve timeout same as to wait forever
            this.put(e);  
            return true;
        }
        final long t1 = System.nanoTime() + unit.toNanos(timeout);
        
        for (;;) {
            if (tryDecrementCapacity())
                return super.offer(e, timeout, unit);
            final long duration = t1 - System.nanoTime();
            
            if (duration <= 0)
                return false;   // time out
            ThreadMarker m = new ThreadMarker(Thread.currentThread());
            
            if (Thread.interrupted())
            {   // avoid the putparkq.offer(m) if already interrupted
                throw new InterruptedException();
            }
            putparkq.offer(m);
            // check again in case there is data race
            if (tryDecrementCapacity())
            {   // data race indeed
                m.parked = false;
                super.offer(e);
                return true;
            }
            LockSupport.parkNanos(duration);
            m.parked = false;
          
            if (Thread.interrupted()) 
                throw new InterruptedException();
        }
    }

    @Override
    public int remainingCapacity() {
        return this.capacity.get();
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        int i = 0;
        E e;

        for (; (e=this.poll()) != null; i++)
            c.add(e);
        return i; 
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        int i = 0;
        E e;

        for (; i < maxElements && (e=this.poll()) != null; i++)
            c.add(e);
        return i; 
    }
}