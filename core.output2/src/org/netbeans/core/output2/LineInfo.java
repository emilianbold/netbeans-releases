/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.core.output2;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import org.openide.windows.IOColors;
import org.openide.windows.OutputListener;

/**
 *
 * @author Tomas Holy
 */
public class LineInfo {

    ArrayList<Segment> segments = new ArrayList<Segment>(1);
    final Lines parent;

    LineInfo(Lines parent) {
        this.parent = parent;
    }

    LineInfo(Lines parent, int end) {
        this(parent, end, false, null, null, false);
    }

    LineInfo(Lines parent, int end, boolean err, OutputListener l, Color c, boolean important) {
        this.parent = parent;
        addSegment(end, err, l, c, important);
    }

    int getEnd() {
        return segments.isEmpty() ? 0 : segments.get(segments.size() - 1).getEnd();
    }

    void addSegment(int end, boolean err, OutputListener l, Color c, boolean important) {
        Segment s = null;
        if (!segments.isEmpty()) {
            s = segments.get(segments.size() - 1);
            if (s.isErr() == err && s.getListener() == l && (s.getCustomColor() == c || (c != null && c.equals(s.getCustomColor())))) {
                // the same type of segment, prolong last one
                s.end = end;
                return;
            }
        }
        if (l != null) {
            s = c != null ? new ColorListenerSegment(end, l, important, c) : new ListenerSegment(end, l, important);
        } else if (err) {
            s = c != null ? new ColorErrSegment(end, c) : new ErrSegment(end);
        } else {
            s = c != null ? new ColorSegment(end, c) : new Segment(end);
        }
        segments.add(s);
    }

    OutputListener getListenerAfter(int pos, int[] range) {
        int start = 0;
        for (Segment s : segments) {
            if (s.getEnd() < pos) {
                continue;
            }
            if (s.getListener() != null) {
                if (range != null) {
                    range[0] = start;
                    range[1] = s.getEnd();
                }
                return s.getListener();
            }
            start = s.getEnd();
        }
        return null;
    }

    OutputListener getListenerBefore(int pos, int[] range) {
        for (int i = segments.size() - 1; i >= 0; i--) {
            int startPos = i == 0 ? 0 : segments.get(i-1).getEnd();
            if (startPos > pos) {
                continue;
            }
            if (segments.get(i).getListener() != null) {
                if (range != null) {
                    range[0] = startPos;
                    range[1] = segments.get(i).getEnd();
                }
                return segments.get(i).getListener();
            }
        }
        return null;
    }

    OutputListener getFirstListener(int[] range) {
        int pos = 0;
        for (Segment s : segments) {
            if (s.getListener() != null) {
                if (range != null) {
                    range[0] = pos;
                    range[1] = s.getEnd();
                }
                return s.getListener();
            }
            pos = s.getEnd();
        }
        return null;
    }

    OutputListener getLastListener(int[] range) {
        for (int i = segments.size() - 1; i >= 0; i--) {
            Segment s = segments.get(i);
            if (s.getListener() != null) {
                if (range != null) {
                    range[0] = i == 0 ? 0 : segments.get(i-1).getEnd();
                    range[1] = s.getEnd();
                }
                return s.getListener();
            }
        }
        return null;
    }

    Collection<Segment> getLineSegments() {
        return segments;
    }

    Collection<OutputListener> getListeners() {
        ArrayList<OutputListener> ol = new ArrayList<OutputListener>();
        for (Segment s : segments) {
            OutputListener l = s.getListener();
            if (l != null) {
                ol.add(l);
            }
        }
        return ol;
    }

    public class Segment {

        int end;

        public Segment(int end) {
            this.end = end;
        }

        int getEnd() {
            return end;
        }

        OutputListener getListener() {
            return null;
        }

        boolean isErr() {
            return false;
        }

        Color getColor() {
            return parent.getDefColor(IOColors.OutputType.OUTPUT);
        }

        Color getCustomColor() {
            return null;
        }
    }

    private class ColorSegment extends Segment {

        Color color;

        public ColorSegment(int end, Color color) {
            super(end);
            this.color = color;
        }

        @Override
        Color getColor() {
            return color;
        }

        @Override
        Color getCustomColor() {
            return color;
        }
    }

    private class ErrSegment extends Segment {

        public ErrSegment(int end) {
            super(end);
        }

        @Override
        boolean isErr() {
            return true;
        }

        @Override
        Color getColor() {
            return parent.getDefColor(IOColors.OutputType.ERROR);
        }
    }

    private class ColorErrSegment extends ErrSegment {

        Color color;

        public ColorErrSegment(int end, Color color) {
            super(end);
            this.color = color;
        }

        @Override
        Color getColor() {
            return color;
        }

        @Override
        Color getCustomColor() {
            return color;
        }
    }

    private class ListenerSegment extends Segment {

        OutputListener listener;
        boolean important;

        public ListenerSegment(int end, OutputListener l, boolean important) {
            super(end);
            this.listener = l;
            this.important = important;
        }

        @Override
        OutputListener getListener() {
            return listener;
        }

        @Override
        Color getColor() {
            return parent.getDefColor(important ? IOColors.OutputType.HYPERLINK_IMPORTANT
                    : IOColors.OutputType.HYPERLINK);
        }
    }

    private class ColorListenerSegment extends ListenerSegment {

        Color color;

        public ColorListenerSegment(int end, OutputListener l, boolean important, Color color) {
            super(end, l, important);
            this.color = color;
        }

        @Override
        Color getColor() {
            return color;
        }

        @Override
        Color getCustomColor() {
            return color;
        }
    }
}
