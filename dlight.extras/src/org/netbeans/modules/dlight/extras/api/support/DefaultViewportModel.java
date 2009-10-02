package org.netbeans.modules.dlight.extras.api.support;

import javax.swing.event.ChangeListener;
import org.netbeans.modules.dlight.util.Range;
import org.netbeans.modules.dlight.extras.api.ViewportModel;
import org.netbeans.modules.dlight.extras.api.ViewportModelState;
import org.openide.util.ChangeSupport;

/**
 * Basic {@link ViewportModel} implementation.
 * Assumes that limits never shrink.
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
    private long minViewportSize;
    private boolean autoscroll;

    public DefaultViewportModel(Range<Long> limits, Range<Long> viewport) {
        this.changeSupport = new ChangeSupport(this);
        this.lowerLimit = limits.getStart();
        this.upperLimit = limits.getEnd();
        this.viewportStart = viewport.getStart();
        this.viewportEnd = viewport.getEnd();
        this.minViewportSize = 0;
        this.autoscroll = true;
    }

    public synchronized boolean getAutoscroll() {
        return autoscroll;
    }

    public synchronized void setAutoscroll(boolean autoscroll) {
        this.autoscroll = autoscroll;
    }

    public synchronized long getMinViewportSize() {
        return minViewportSize;
    }

    public synchronized void setMinViewportSize(long minViewportSize) {
        this.minViewportSize = minViewportSize;
    }

    public synchronized Range<Long> getLimits() {
        return new Range<Long>(lowerLimit, upperLimit);
    }

    public synchronized void setLimits(Range<Long> limits) {
        long newLowerLimit;
        long newUpperLimit;
        if (limits.getStart() != null) {
            newLowerLimit = Math.min(limits.getStart(), lowerLimit);
        } else {
            newLowerLimit = lowerLimit;
        }
        if (limits.getEnd() != null) {
            newUpperLimit = Math.max(limits.getEnd(), upperLimit);
        } else {
            newUpperLimit = upperLimit;
        }
        if (newLowerLimit < lowerLimit || upperLimit < newUpperLimit) {
            if (newUpperLimit - newLowerLimit < 0) {
                // attempt to set negative-length limits
                return;
            }
            boolean scroll = autoscroll && viewportStart <= upperLimit && upperLimit <= viewportEnd;
            long extent = viewportEnd - viewportStart;
            lowerLimit = newLowerLimit;
            upperLimit = newUpperLimit;
            if (scroll) {
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
        long newViewportStart;
        long newViewportEnd;
        if (viewport.getStart() != null) {
            newViewportStart = viewport.getStart();
        } else {
            newViewportStart = viewportStart;
        }
        if (viewport.getEnd() != null) {
            newViewportEnd = viewport.getEnd();
        } else {
            newViewportEnd = viewportEnd;
        }
        if (newViewportStart != viewportStart || newViewportEnd != viewportEnd) {
            if (newViewportEnd - newViewportStart < minViewportSize) {
                // attempt to set too small viewport
                return;
            }
            viewportStart = newViewportStart;
            viewportEnd = newViewportEnd;
            changeSupport.fireChange();
        }
    }

    public synchronized ViewportModelState getState() {
        return new StateImpl(getLimits(), getViewport());
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    private static class StateImpl implements ViewportModelState {

        private final Range<Long> limits;
        private final Range<Long> viewport;

        public StateImpl(Range<Long> limits, Range<Long> viewport) {
            this.limits = limits;
            this.viewport = viewport;
        }

        public Range<Long> getLimits() {
            return limits;
        }

        public Range<Long> getViewport() {
            return viewport;
        }
    }
}
