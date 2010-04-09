package org.netbeans.core.ui.sampler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.management.ThreadInfo;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPOutputStream;
import javax.management.openmbean.CompositeData;

/**
 *
 * @author Tomas Hurka
 */
class SamplesOutputStream {

    private static final String[][] methods = new String[][]{
        {"sun.management.ThreadInfoCompositeData", "toCompositeData"}, // NOI18N Sun JVM
        {"com.ibm.lang.management.ManagementUtils", "toThreadInfoCompositeData"} // NOI18N IBM J9
    };
    static final String ID = "NPSS"; // NetBeans Profiler samples stream
    static final String FILE_EXT = ".npss"; // NOI18N
    static byte version = 1;
    private static Method toCompositeDataMethod;

    static {
        for (String[] method : methods) {
            String className = method[0];
            String methodName = method[1];
            try {
                Class clazz = Class.forName(className);
                toCompositeDataMethod = clazz.getMethod(methodName, ThreadInfo.class);
                if (toCompositeDataMethod != null) {
                    break;
                }
            } catch (ClassNotFoundException ex) {
            } catch (NoSuchMethodException ex) {
            } catch (SecurityException ex) {
            }
        }
    }
    OutputStream outStream;
    Map<Long, ThreadInfo> lastThreadInfos;
    Map<StackTraceElement, StackTraceElement> steCache;
    List<Sample> samples;

    static boolean isSupported() {
        return toCompositeDataMethod != null;
    }

    SamplesOutputStream(OutputStream os) throws IOException {
        outStream = os;
        writeHeader(os);
//        out = new ObjectOutputStream(os);
        lastThreadInfos = new HashMap();
        steCache = new HashMap(8*1024);
        samples = new ArrayList(1024);
    }

    SamplesOutputStream(File file) throws IOException {
        this(new FileOutputStream(file));
    }

    void writeSample(ThreadInfo[] infos, long time, long selfThreadId) throws IOException {
        List<Long> sameT = new ArrayList();
        List<ThreadInfo> newT = new ArrayList();
        List<Long> tids = new ArrayList();

        for (ThreadInfo tinfo : infos) {
            long id = tinfo.getThreadId();

            if (id != selfThreadId) { // ignore sampling thread
                Long tid = Long.valueOf(tinfo.getThreadId());
                ThreadInfo lastThread = lastThreadInfos.get(tid);

                tids.add(tid);
                if (lastThread != null) {
                    if (lastThread.getThreadState().equals(tinfo.getThreadState())) {
                        StackTraceElement[] lastStack = lastThread.getStackTrace();
                        StackTraceElement[] stack = tinfo.getStackTrace();

                        if (Arrays.deepEquals(lastStack, stack)) {
                            sameT.add(tid);
                            continue;
                        }
                    }
                }
                internStackTrace(tinfo);
                newT.add(tinfo);
                lastThreadInfos.put(tid, tinfo);
            }
        }
        samples.add(new Sample(time, sameT, newT));
        // remove dead threads
        Set<Long> ids = new HashSet(lastThreadInfos.keySet());
        ids.removeAll(tids);
        lastThreadInfos.keySet().removeAll(ids);
    }

    private static CompositeData toCompositeData(ThreadInfo tinfo) {
        try {
            return (CompositeData) toCompositeDataMethod.invoke(null, tinfo);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    void close() throws IOException {
        steCache = null;
        GZIPOutputStream stream = new GZIPOutputStream(outStream, 64 * 1024);
        ObjectOutputStream out = new ObjectOutputStream(stream);
        for (int i=0; i<samples.size();i++) {
            Sample s = samples.get(i);
            samples.set(i, null);
            s.writeToStream(out);
        }
        out.close();
    }

    private void writeHeader(OutputStream os) throws IOException {
        os.write(ID.getBytes());
        os.write(version);
    }

    private void internStackTrace(ThreadInfo tinfo) {
        StackTraceElement[] stack = tinfo.getStackTrace();

        for (int i = 0; i < stack.length; i++) {
            StackTraceElement ste = stack[i];
            StackTraceElement oldStack = steCache.get(ste);

            if (oldStack != null) {
                stack[i] = oldStack;
            } else {
                steCache.put(ste, ste);
            }
        }
    }

    private static class Sample {

        final private long time;
        final private List<Long> sameThread;
        final private List<ThreadInfo> newThreads;

        Sample(long t, List<Long> sameT, List<ThreadInfo> newT) {
            time = t;
            sameThread = sameT;
            newThreads = newT;
        }

        private long getTime() {
            return time;
        }

        private List<Long> getSameThread() {
            return sameThread;
        }

        private List<ThreadInfo> getNewThreads() {
            return newThreads;
        }

        private void writeToStream(ObjectOutputStream out) throws IOException {
            out.writeLong(time);
            out.writeInt(sameThread.size());
            for (Long tid : sameThread) {
                out.writeLong(tid.longValue());
            }
            out.writeInt(newThreads.size());
            for (ThreadInfo tic : newThreads) {
                out.writeObject(toCompositeData(tic));
            }
        }
    }
}
