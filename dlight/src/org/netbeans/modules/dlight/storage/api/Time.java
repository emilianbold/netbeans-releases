package org.netbeans.modules.dlight.storage.api;

import java.beans.PropertyEditorManager;
import org.netbeans.modules.dlight.storage.impl.TimeEditor;

/**
 * Metric value class for time interval in nanoseconds. Immutable.
 *
 * @author Alexey Vladykin
 */
public class Time implements Comparable<Time> {

    private final long nanos;

    /**
     * Creates new instance.
     *
     * @param nanos  time in nanoseconds
     */
    public Time(long nanos) {
        this.nanos = nanos;
    }

    /**
     * @return time in nanoseconds
     */
    public long getNanos() {
        return nanos;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(nanos).append(" nanoseconds");
        return buf.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Time) {
            return this.nanos == ((Time) obj).nanos;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 29 + (int) (nanos ^ (nanos >>> 32));
    }

    public int compareTo(Time that) {
        if (this.nanos < that.nanos) {
            return -1;
        } else if (this.nanos == that.nanos) {
            return 0;
        } else {
            return 1;
        }
    }

    static {
        PropertyEditorManager.registerEditor(Time.class, TimeEditor.class);
    }

}
