/*
 * Written by Hanson Char and released to the public domain,
 * as explained at http://creativecommons.org/licenses/publicdomain
 */
package net.sf.beanlib.util.concurrent;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

/**
 * JUnit test for {@link ConcurrentSkipListSet}.
 *  
 * @author Hanson Char
 */
public class ConcurrentSkipListSetTest 
{
    // Special thanks to Anthony Baker for pointing out the problem with 
    // the "java.lang.SecurityException: Unsafe" 
    // when the ConcurrentSkipListSet is not loaded via the -Xbootclasspath
    @Test
    public void testUnsafeCheck()  
    {
        ConcurrentSkipListSet<Integer> stuff = new ConcurrentSkipListSet<Integer>();
        stuff.add(1);
        stuff.add(2);
        stuff.add(3);
        System.out.println(stuff);
    }

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(ConcurrentSkipListSetTest.class);
    }
}
