package org.netbeans.modules.dlight.extras.api.support;

import javax.swing.event.ChangeListener;
import org.netbeans.modules.dlight.util.Range;
import org.netbeans.modules.dlight.extras.api.ViewportModel;
import org.openide.util.ChangeSupport;

/**
 * Basic {@link ViewportModel} implementation.
 * Assumes that limits never shrink and makes viewport follow the upper limit.
 *
 * Class is thread-safe.
 *
 * @author Alexey Vladykin
 */
public final class DefaultViewportModel implements ViewportModel {

    private final ChangeSupport changeSupport;
    private long lowerLimit;
    private long upperLimit;
    private long viewportStart;
    private long viewportEnd;

    public DefaultViewportModel() {
        this.changeSupport = new ChangeSupport(this);
    }

    public synchronized Range<Long> getLimits() {
        return new Range<Long>(lowerLimit, upperLimit);
    }

    public synchronized void setLimits(Range<Long> limits) {
        boolean changed = false;
        boolean autoscroll = viewportStart <= upperLimit && upperLimit <= viewportEnd;
        long extent = viewportEnd - viewportStart;
        if (limits.getStart() != null) {
            long newLowerLimit = limits.getStart();
            if (newLowerLimit < lowerLimit) {
                lowerLimit = newLowerLimit;
                changed = true;
            }
        }
        if (limits.getEnd() != null) {
            long newUpperLimit = limits.getEnd();
            if (upperLimit < newUpperLimit) {
                upperLimit = newUpperLimit;
                changed = true;
            }
        }
        if (changed) {
            if (autoscroll) {
                viewportStart = Math.max(0, upperLimit - extent);
                viewportEnd = viewportStart + extent;
            }
            changeSupport.fireChange();
        }
    }

    public synchronized Range<Long> getViewport() {
        return new Range<Long>(viewportStart, viewportEnd);
    }

    public synchronized void setViewport(Range<Long> viewport) {
        boolean changed = false;
        if (viewport.getStart() != null) {
            long newViewportStart = viewport.getStart();
            if (viewportStart != newViewportStart) {
                viewportStart = newViewportStart;
                changed = true;
            }
        }
        if (viewport.getEnd() != null) {
            long newViewportEnd = viewport.getEnd();
            if (viewportEnd != newViewportEnd) {
                viewportEnd = newViewportEnd;
                changed = true;
            }
        }
        if (changed) {
            changeSupport.fireChange();
        }
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }
}
