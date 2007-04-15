package net.sf.beanlib.util.concurrent;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/*
 * Written by Hanson Char and released to the public domain,
 * as explained at http://creativecommons.org/licenses/publicdomain
 */
public class UnboundedLinkedBlockingQueue<E> extends AbstractQueue<E>
        implements BlockingQueue<E>, java.io.Serializable 
{
    private static final long serialVersionUID = -6903933977591709194L;
    
    private final ConcurrentLinkedQueue<E> q;

    /** Lock held by take, poll, etc */
    private final ReentrantLock takeLock = new ReentrantLock();

    /** Wait queue for waiting takes */
    private final Condition notEmpty = takeLock.newCondition();

    /**
     * Signals a waiting take. Called only from put/offer (which do not
     * otherwise ordinarily lock takeLock.)
     */
    private void signalNotEmpty() {
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            notEmpty.signal();
        } finally {
            takeLock.unlock();
        }
    }

    /**
     * Creates a <tt>LinkedBlockingQueue</tt> with a capacity of
     * {@link Integer#MAX_VALUE}.
     */
    public UnboundedLinkedBlockingQueue() {
        q = new ConcurrentLinkedQueue<E>();
    }

    /**
     * Creates a <tt>LinkedBlockingQueue</tt> with a capacity of
     * {@link Integer#MAX_VALUE}, initially containing the elements of the
     * given collection,
     * added in traversal order of the collection's iterator.
     *
     * @param c the collection of elements to initially contain
     * @throws NullPointerException if the specified collection or any
     *         of its elements are null
     */
    public UnboundedLinkedBlockingQueue(Collection<? extends E> c) {
        q = new ConcurrentLinkedQueue<E>(c);
    }

    /**
     * Inserts the specified element at the tail of this queue, waiting if
     * necessary for space to become available.
     *
     * @throws InterruptedException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    public void put(E e) throws InterruptedException {
        offer(e);
    }

    /**
     * Inserts the specified element at the tail of this queue, waiting if
     * necessary up to the specified wait time for space to become available.
     *
     * @return <tt>true</tt> if successful, or <tt>false</tt> if
     *         the specified waiting time elapses before space is available.
     * @throws InterruptedException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    public boolean offer(E e, long timeout, TimeUnit unit)
        throws InterruptedException {
        return offer(e);
    }

    /**
     * Inserts the specified element at the tail of this queue if it is
     * possible to do so immediately without exceeding the queue's capacity,
     * returning <tt>true</tt> upon success and <tt>false</tt> if this queue
     * is full.
     * When using a capacity-restricted queue, this method is generally
     * preferable to method {@link BlockingQueue#add add}, which can fail to
     * insert an element only by throwing an exception.
     *
     * @throws NullPointerException if the specified element is null
     */
    public boolean offer(E e) {
        q.offer(e);
        signalNotEmpty();
        return true;
    }


    public E take() throws InterruptedException {
        E e = q.poll();

        if (e != null)
            return e;

        final ReentrantLock takeLock = this.takeLock;
        takeLock.lockInterruptibly();
        try {
            try {
                for (;;)
                {
                    e = q.poll();
                    if (e != null)
                        return e;
                    notEmpty.await();
                }
            } catch (InterruptedException ie) {
                notEmpty.signal(); // propagate to a non-interrupted thread
                throw ie;
            }
        } finally {
            takeLock.unlock();
        }
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        if (timeout < 0)
            return take();  // treat -ve timeout same as to wait forever
        E e = q.poll();

        if (e != null)
            return e;
        long nanos = unit.toNanos(timeout);
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lockInterruptibly();
        try {
            for (;;) {
                e = q.poll();

                if (e != null)
                    return e;
                if (nanos <= 0)
                    return null;
                try {
                    nanos = notEmpty.awaitNanos(nanos);
                } catch (InterruptedException ie) {
                    notEmpty.signal(); // propagate to a non-interrupted thread
                    throw ie;
                }
            }
        } finally {
            takeLock.unlock();
        }
    }

    public E poll() {
        return q.poll();    
    }

    public int drainTo(Collection<? super E> c) {
        int i = 0;
        E e;

        for (; (e=q.poll()) != null; i++)
            c.add(e);
        return i; 
    }

    public int drainTo(Collection<? super E> c, int maxElements) {
        int i = 0;
        E e;

        for (; i < maxElements && (e=q.poll()) != null; i++)
            c.add(e);
        return i; 
    }

    @Override
    public Iterator<E> iterator() {
        return q.iterator();
    }

    @Override
    public int size() {
        return q.size();
    }

    public int remainingCapacity() {
        return Integer.MAX_VALUE;
    }

    public E peek() {
        return q.peek();
    }
}
