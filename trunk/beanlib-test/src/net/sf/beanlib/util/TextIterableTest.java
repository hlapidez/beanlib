package net.sf.beanlib.util;

import static org.junit.Assert.assertTrue;
import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

/**
 * @author Hanson Char
 */
public class TextIterableTest 
{
    @Test
    public void testIteration() {
        TextIterable ti = new TextIterable("testing_text_iterable.txt");

        int count = 0;
        
        for (String line : ti) {
            assertTrue(ti.numberOfopenedIterators() == 1);
            count++;
            System.out.println(line);
        }
        assertTrue(count == 10);
        assertTrue(ti.numberOfopenedIterators() == 0);
    }
    
    @Test
    public void testMultipleIterators() {
        TextIterable ti = new TextIterable("testing_text_iterable.txt");

        int count = 0;
        
        ti.iterator();  // 1st
        ti.iterator();  // 2nd
        
        for (String line : ti) {
            assertTrue(ti.numberOfopenedIterators() == 3);
            count++;
            System.out.println(line);
        }
        assertTrue(count == 10);
        assertTrue(ti.numberOfopenedIterators() == 2);
        ti.close();
        assertTrue(ti.numberOfopenedIterators() == 0);
    }
    
    @Test
    public void testCloseInMiddle() {
        TextIterable ti = new TextIterable("testing_text_iterable.txt");
        int count = 0;
        
        for (String line : ti) {
            count++;
            assertTrue(ti.numberOfopenedIterators() == 1);
            System.out.println(line);

            if (count == 5)
                break;
        }
        assertTrue(count == 5);
        assertTrue(ti.numberOfopenedIterators() == 1);
        ti.close();
        assertTrue(ti.numberOfopenedIterators() == 0);
        ti.close();
    }
   
    
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(TextIterableTest.class);
    }
}
