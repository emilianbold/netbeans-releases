
package org.netbeans.modules.cnd.repository.testbench;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.modules.cnd.repository.api.CacheLocation;
import org.netbeans.modules.cnd.repository.api.RepositoryAccessor;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.support.RepositoryStatistics;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author vkvashin
 */
@ServiceProvider(service = RepositoryStatistics.class)
public class RepositoryStatisticsImpl extends RepositoryStatistics {

    private final ConcurrentHashMap<String, Impl> instances =
            new ConcurrentHashMap<String, Impl>();

    public RepositoryStatisticsImpl() {
    }

    private Impl getImpl(String id) {
        Impl instance = instances.get(id);
        if (instance == null) {
            instance = new Impl(id);
            Impl old = instances.putIfAbsent(id, instance);
            if (old != null) {
                instance = old;
            }
        }
        return instance;
    }

    public static RepositoryStatisticsImpl getInstance() {
        return (RepositoryStatisticsImpl) Lookup.getDefault().lookup(RepositoryStatistics.class);
    }

    public void init(String id, String displayName) {
        Impl instance = getImpl(id);
        instance.init(displayName);
    }

    public void logIndexRead(int unitId, int indexSize) {
        if (RepositoryStatistics.ENABLED) {
            Impl impl = getImpl(unitId);
            impl.incrementIndex(unitId, indexSize);
        }
    }

    public void logDataRead(Key key, int dataSize) {
        if (RepositoryStatistics.ENABLED) {
            Impl impl = getImpl(key.getUnitId());
            impl.incrementData(key, dataSize);
        }
    }

    private Impl getImpl(int unitId) {
        CacheLocation cacheLoc = RepositoryAccessor.getTranslator().getCacheLocation(unitId);
        String path = cacheLoc.getLocation().getAbsolutePath();
        Impl impl = getImpl(path);
        return impl;
    }

    private String getCategory(Key key) {
        return key.getClass().getSimpleName(); //NOI18N
    }

    @Override
    public void clearImpl() {
        for (Impl impl : instances.values()) {
            impl.clear();
        }
        RepositoryAccessor.getRepository().debugClear();
    }

    @Override
    public void reportImpl(PrintWriter pw, String title) {
        PrintWrapper wrapper = new PrintWrapper(pw);
        reportImpl(wrapper, title);
    }

    @Override
    public void reportImpl(PrintStream ps, String title) {
        PrintWrapper wrapper = new PrintWrapper(ps);
        reportImpl(wrapper, title);
    }

    private void reportImpl(PrintWrapper wrapper, String title) {
        for (Impl stats : instances.values()) {
            stats.report(wrapper, title);
        }
    }

    private class Impl {

        private final String instanceId;
        private final ConcurrentHashMap<String, Entry> buckets = new ConcurrentHashMap<String, Entry>();
        private volatile String displayName;

        private static final String TOTAL = "TOTAL"; // NOI18N

        private Impl(String id) {
            this.instanceId = id;
        }
        
        private void init(String displayName) {
            this.displayName = displayName;
        }

        public void incrementData(Key key, int size) {
            String category = getCategory(key);
            getOrCreateEntry(Group.BY_KEY, category).increment(size, key);
            getOrCreateEntry(Group.SUMMARY, TOTAL).increment(size);
            getOrCreateEntry(Group.SUMMARY, "DATA").increment(size); // NOI18N
            getOrCreateEntry(Group.COUNT, "CALLS").increment(1); // NOI18N
        }

        public void incrementIndex(int unitId, int size) {
            getOrCreateEntry(Group.SUMMARY, "UNIT INDEXES").increment(size); // NOI18N
            getOrCreateEntry(Group.SUMMARY, TOTAL).increment(size); // NOI18N
        }

        public void report(String title) {
            report(new PrintWrapper(System.out), title);
        }    
        
        private void report(PrintWrapper ps, String title) {
            ps.printf("\n\n%s for %s\n", title, getDisplayName()); // NOI18N
            TreeSet<Entry> sorted = new TreeSet<Entry>(new Comparator<Entry>() {
                @Override
                public int compare(Entry e1, Entry e2) {
                    int delta;
                    delta = e1.group.sortWeight - e2.group.sortWeight;
                    if (delta == 0) {
                        delta = e2.getCount() - e1.getCount();
                        if (delta == 0) {
                            return e1.getDisplayName().compareTo(e2.getDisplayName());
                        }
                    }
                    return delta;
                }
            });
            sorted.addAll(buckets.values());
            for (Entry entry : sorted) {
                entry.report(ps);
            }
            ps.printf("\n"); // NOI18N
            ps.flush();
        }        
        
        private Entry getOrCreateEntry(Group group,  String category) {
            Entry result = buckets.get(category);
            if (result == null) {
                result = new Entry(group, category);
                Entry old = buckets.putIfAbsent(category, result);
                if (old != null) {
                    result = old;
                }
            }
            return result;
        }
        
        private String getDisplayName() {
            return (displayName == null) ? instanceId : displayName;
        }

        private void clear() {
            buckets.clear();
        }    
    }

    private static enum Group {

        SUMMARY(0),
        COUNT(1),
        BY_KEY(2);

        public final int sortWeight;

        private Group(int sortWeight) {
            this.sortWeight = sortWeight;
        }
    }

    private static class Entry {

        private final Group group;
        private final String id;
        private final AtomicInteger size = new AtomicInteger();
        private final AtomicInteger count = new AtomicInteger();
        private final ConcurrentHashMap<Key, AtomicInteger> byKey;

        public Entry(Group group, String id) {
            this.group = group;
            this.id = id;
            this.byKey = (RepositoryStatistics.ENHANCED) ?
                    new ConcurrentHashMap<Key, AtomicInteger>() : null;
        }

        public void increment(int increment) {
            count.incrementAndGet();
            size.addAndGet(increment);
        }

        public void increment(int increment, Key key) {
            count.incrementAndGet();
            size.addAndGet(increment);
            if (RepositoryStatistics.ENHANCED) {
                AtomicInteger cnt = byKey.get(key);
                if (cnt == null) {
                    cnt = new AtomicInteger();
                    AtomicInteger prev = byKey.putIfAbsent(key, cnt);
                    if (prev != null) {
                        cnt = prev;
                    }
                }
                cnt.addAndGet(increment);
            }
        }

        private void report(PrintWrapper ps) {
            if (RepositoryStatistics.ENHANCED) {
                String unique = byKey.isEmpty() ?
                        "                   " : //NOI18N
                        String.format("%,12d unique", byKey.size()); //NOI18N
                ps.printf("\t%,12d bytes %,12d times %s %s\n", size.get(), count.get(), unique, getDisplayName()); //NOI18N
            } else {
                ps.printf("\t%,12d bytes %,12d times %s\n", size.get(), count.get(), getDisplayName()); //NOI18N
            }
        }

        public int getCount() {
            return size.get();
        }

        private String getDisplayName() {
            return id;
        }

        @Override
        public String toString() {
            return id;
        }
    }

    private static class PrintWrapper {

        private final Object delegate;

        public PrintWrapper(PrintWriter delegate) {
            this.delegate = delegate;
        }

        public PrintWrapper(PrintStream delegate) {
            this.delegate = delegate;
        }

        public void printf(String format, Object ... args) {
            if (delegate instanceof PrintStream) {
                ((PrintStream) delegate).printf(format, args);
            } else if (delegate instanceof PrintWriter) {
                ((PrintWriter) delegate).printf(format, args);
            }
        }

        public void flush() {
            if (delegate instanceof PrintStream) {
                ((PrintStream) delegate).flush();
            } else if (delegate instanceof PrintWriter) {
                ((PrintWriter) delegate).flush();
            }
        }

        public void close() {
            if (delegate instanceof PrintStream) {
                ((PrintStream) delegate).flush();
            } else if (delegate instanceof PrintWriter) {
                ((PrintWriter) delegate).flush();
            }
        }
    }

}
