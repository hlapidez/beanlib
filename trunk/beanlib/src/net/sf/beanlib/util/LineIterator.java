package net.sf.beanlib.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Iterator;

import net.jcip.annotations.NotThreadSafe;

/**
 * @author Hanson Char
 */
@NotThreadSafe
class LineIterator implements Iterator<String>, Closeable {
    private boolean hasNextExecuted;

    private String line;

    private LineNumberReader lnr;

    private final TextIterable textIterable;

    LineIterator(TextIterable textIterable, InputStream is) {
        this.textIterable = textIterable;
        InputStreamReader isr = null;

        try {
            isr = new InputStreamReader(is);
            lnr = new LineNumberReader(isr);
        } catch (Exception ex) {
            try {
                if (lnr != null)
                    lnr.close();
                else if (isr != null)
                    isr.close();
                else if (is != null)
                    is.close();
            } catch (Throwable ignore) {
            }
        }
    }

    public boolean hasNext() {
        if (hasNextExecuted)
            return line != null;
        try {
            hasNextExecuted = true;

            if (lnr != null) {
                line = lnr.readLine();

                if (line == null)
                    close();
            }
            return line != null;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public String next() {
        if (hasNextExecuted) {
            hasNextExecuted = false;
            return line;
        }
        return hasNext() ? next() : null;
    }

    public void close() {
        if (lnr != null) {
            textIterable.removeLineIterator(this);
            closeInPrivate();
        }
    }

    void closeInPrivate() {
        if (lnr != null) {
            try {
                lnr.close();
            } catch (IOException ignore) {
            }
            line = null;
            lnr = null;
        }
    }

    public void remove() {
        throw new UnsupportedOperationException("remove not supported");
    }

    @Override
    public void finalize() {
        try {
            super.finalize();
        } catch (Throwable ex) {
        }
        close();
    }
}