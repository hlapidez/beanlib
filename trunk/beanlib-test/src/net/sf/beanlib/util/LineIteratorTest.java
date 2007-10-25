package net.sf.beanlib.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Iterator;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

/**
 * @author Hanson Char
 */
public class LineIteratorTest
{
    private static final String EMPTY = "";
    private static final String ONE_LINER = "One line";
    private static final String TWO_LINER = ONE_LINER + "\n" + "Two line";
    
    private static final TextIterable PLACEHOLDER = new TextIterable((URL)null);
    
    @Test
    public void testClose() 
    {
        {
            ByteArrayInputStream is = new ByteArrayInputStream(TWO_LINER.getBytes());
            LineIterator itr = new LineIterator(PLACEHOLDER, is);
            assertEquals(ONE_LINER, itr.next());
            itr.closeInPrivate();
            assertNull(itr.next());
            assertFalse(itr.hasNext());
        }
        {
            ByteArrayInputStream is = new ByteArrayInputStream(TWO_LINER.getBytes());
            LineIterator itr = new LineIterator(PLACEHOLDER, is);
            assertEquals(ONE_LINER, itr.next());
            itr.closeInPrivate();
            assertFalse(itr.hasNext());
            assertNull(itr.next());
        }
    }
    
    @Test
    public void testEmpty() {
        {
            ByteArrayInputStream is = new ByteArrayInputStream(EMPTY.getBytes());
            LineIterator itr = new LineIterator(PLACEHOLDER, is);
            assertFalse(itr.hasNext());
            assertNull(itr.next());
            assertFalse(itr.hasNext());
            assertNull(itr.next());
            itr.close();
        }
        {
            ByteArrayInputStream is = new ByteArrayInputStream(EMPTY.getBytes());
            Iterator itr = new LineIterator(PLACEHOLDER, is);
            assertFalse(itr.hasNext());
            assertFalse(itr.hasNext());
            assertNull(itr.next());
            assertNull(itr.next());
        }
    }
    
    @Test
    public void testOneLiner() {
        {
            ByteArrayInputStream is = new ByteArrayInputStream(ONE_LINER.getBytes());
            Iterator itr = new LineIterator(PLACEHOLDER, is);
            assertTrue(itr.hasNext());
            assertEquals(ONE_LINER, itr.next());
            assertFalse(itr.hasNext());
            assertNull(itr.next());
        }
        {
            ByteArrayInputStream is = new ByteArrayInputStream(ONE_LINER.getBytes());
            Iterator itr = new LineIterator(PLACEHOLDER, is);
            assertTrue(itr.hasNext());
            assertTrue(itr.hasNext());
            assertEquals(ONE_LINER, itr.next());
            assertNull(itr.next());
        }
        {
            ByteArrayInputStream is = new ByteArrayInputStream(ONE_LINER.getBytes());
            Iterator itr = new LineIterator(PLACEHOLDER, is);
            assertEquals(ONE_LINER, itr.next());
            assertFalse(itr.hasNext());
        }
    }

    @Test
    public void testTwoLiner() {
        {
            ByteArrayInputStream is = new ByteArrayInputStream(TWO_LINER.getBytes());
            Iterator itr = new LineIterator(PLACEHOLDER, is);
            assertTrue(itr.hasNext());
            assertEquals(ONE_LINER, itr.next());
            assertTrue(itr.hasNext());
            assertEquals("Two line", itr.next());
            assertFalse(itr.hasNext());
            assertNull(itr.next());
        }
        {
            ByteArrayInputStream is = new ByteArrayInputStream(TWO_LINER.getBytes());
            Iterator itr = new LineIterator(PLACEHOLDER, is);
            assertTrue(itr.hasNext());
            assertTrue(itr.hasNext());
            assertEquals(ONE_LINER, itr.next());
            assertTrue(itr.hasNext());
            assertTrue(itr.hasNext());
            assertEquals("Two line", itr.next());
            assertFalse(itr.hasNext());
            assertFalse(itr.hasNext());
            assertNull(itr.next());
            assertNull(itr.next());
        }
        {
            ByteArrayInputStream is = new ByteArrayInputStream(TWO_LINER.getBytes());
            Iterator itr = new LineIterator(PLACEHOLDER, is);
            assertEquals(ONE_LINER, itr.next());
            assertEquals("Two line", itr.next());
            assertNull(itr.next());
            assertFalse(itr.hasNext());
        }
    }
    
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(LineIteratorTest.class);
    }
}
