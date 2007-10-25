package net.sf.beanlib.util;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.jcip.annotations.ThreadSafe;

/**
 * @author Hanson Char
 */
@ThreadSafe
public class TextIterable implements Iterable<String>, Closeable {
    private final URL url;

    private final List<LineIterator> openedIterators = new ArrayList<LineIterator>();

    public TextIterable(File file) throws MalformedURLException {
        this(file.toURI().toURL());
    }

    public TextIterable(URL url) {
        this.url = url;
    }

    public TextIterable(String resourcePath) {
        this(
            Thread.currentThread()
                  .getContextClassLoader()
                  .getResource(resourcePath));
    }

    public LineIterator iterator() {
        try {
            LineIterator ret = new LineIterator(this, url.openStream());

            synchronized (openedIterators) {
                openedIterators.add(ret);
            }
            return ret;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void close() {
        LineIterator[] lineIterators;

        synchronized (openedIterators) {
            lineIterators = new LineIterator[openedIterators.size()];
            lineIterators = openedIterators.toArray(lineIterators);

            for (Iterator<LineIterator> itr=openedIterators.iterator(); itr.hasNext();)
            {
                itr.next();
                itr.remove();
            }
        }
        for (LineIterator li : lineIterators)
            li.closeInPrivate();
    }

    public int numberOfopenedIterators() {
        return openedIterators.size();
    }

    void removeLineIterator(LineIterator li) {
        synchronized (openedIterators) {
            openedIterators.remove(li);
        }
    }
}
