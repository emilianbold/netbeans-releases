package org.netbeans.api.visual.vmd;

import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

/**
 * @author David Kaspar
 */
public class VMDNodeAnchor extends Anchor {

    private boolean requiresRecalculation = true;

    private HashMap<Entry, Result> results = new HashMap<Entry, Result> ();

    public VMDNodeAnchor (Widget widget) {
        super (widget);
        assert widget != null;
    }

    protected void notifyEntryAdded (Entry entry) {
        requiresRecalculation = true;
    }

    protected void notifyEntryRemoved (Entry entry) {
        results.remove (entry);
        requiresRecalculation = true;
    }

    private void recalculate () {
        if (! requiresRecalculation)
            return;

        Widget widget = getRelatedWidget ();
        Point relatedLocation = getRelatedSceneLocation ();

        HashMap<Entry, Float> topmap = new HashMap<Entry, Float> ();
        HashMap<Entry, Float> bottommap = new HashMap<Entry, Float> ();

        for (Entry entry : getEntries ()) {
            Point oppositeLocation = getOppositeSceneLocation (entry);
            int dy = oppositeLocation.y - relatedLocation.y;
            int dx = oppositeLocation.x - relatedLocation.x;

            if (dy > 0)
                bottommap.put (entry, (float) dx / (float) dy);
            else if (dy < 0)
                topmap.put (entry, (float) - dx / (float) dy);
            else
                topmap.put (entry, dx < 0 ? Float.MAX_VALUE : Float.MIN_VALUE);
        }

        Entry[] topList = toArray (topmap);
        Entry[] bottomList = toArray (bottommap);

        Rectangle bounds = widget.convertLocalToScene (widget.getBounds ());

        int y = bounds.y;
        int len = topList.length;

        for (int a = 0; a < len; a ++) {
            Entry entry = topList[a];
            int x = bounds.x + (a + 1)  * bounds.width / (len + 1);
            results.put (entry, new Result (new Point (x, y), Direction.TOP));
        }

        y = bounds.y + bounds.height;
        len = bottomList.length;

        for (int a = 0; a < len; a ++) {
            Entry entry = bottomList[a];
            int x = bounds.x + (a + 1) * bounds.width / (len + 1);
            results.put (entry, new Result (new Point (x, y), Direction.BOTTOM));
        }
    }

    private Entry[] toArray (final HashMap<Entry, Float> map) {
        Set<Entry> keys = map.keySet ();
        Entry[] entries = keys.toArray (new Entry[keys.size ()]);
        Arrays.sort (entries, new Comparator<Entry>() {
            public int compare (Entry o1, Entry o2) {
                float f = map.get (o1) - map.get (o2);
                if (f > 0.0f)
                    return 1;
                else if (f < 0.0f)
                    return -1;
                else
                    return 0;
            }
        });
        return entries;
    }

    public Result compute (Entry entry) {
        recalculate ();
        return results.get (entry);
    }

}
