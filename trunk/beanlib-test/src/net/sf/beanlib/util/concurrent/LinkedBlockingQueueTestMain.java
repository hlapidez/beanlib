/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
