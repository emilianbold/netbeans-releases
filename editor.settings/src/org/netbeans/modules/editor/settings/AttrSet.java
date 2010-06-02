/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.editor.settings;

import java.awt.Color;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.StyleConstants;
import javax.swing.text.AttributeSet;
import org.netbeans.api.editor.settings.EditorStyleConstants;

/**
 * Implementation of immutable {@link AttributeSet} that performs sharing
 * of commonly used attribute sets.
 * <br/>
 * The attributes are divided into ones that are shareable (like foreground, background
 * or font-related properties) and non-shareable (everything else). Attribute sets
 * with shareable attributes only are put into a weak set and shared eventually.
 * <br/>
 * Each attribute set can create a weak cache for composite sets optimization.
 * <br/>
 * In addition the implementation also implements {@link Iterable}
 * allowing the clients to subsequently get all keys and values (key1, value1, key2, value2 etc.).
 * <br/>
 * Parenting through {@link AttributeSet#ResolveAttribute} is currently not supported
 * and the implementations should use <code>AttributeUtilities.createImmutable(parentSet,childSet)</code> instead.
 * <br/>
 *
 * Attributes (keys) are divided into shareable (e.g. foreground or background and non-shareable (all that are not marked as shareable).
 * <br/>
 * It is assumed that attribute sets mostly contain the shareable attributes
 * and also that there is not many shareable attributes (e.g. less than 10) so the pairs
 * are currently all held in an array (extra attributes are held separately).
 *
 * @author Miloslav Metelka
 */
public final class AttrSet implements AttributeSet, Iterable<Object> {

    private static final Map<Object,KeyWrapper> sharedKeys =
            new HashMap<Object,KeyWrapper>(64, 0.4f); // Intentional low load-factor
    private static final List<KeyWrapper> sortedSharedKeys = new ArrayList<KeyWrapper>(16);

    private static final SimpleWeakSet<AttrSet> cache = new SimpleWeakSet<AttrSet>();

    private static final Object[] EMPTY_ARRAY = new Object[0];

    private static final AttrSet EMPTY = new AttrSet(EMPTY_ARRAY, null, 0);

    // -J-Dorg.netbeans.modules.editor.settings.AttrSet.level=FINE
    // -J-Dorg.netbeans.modules.editor.settings.AttrSet.level=FINER - also dump attr contents
    private static final Logger LOG = Logger.getLogger(AttrSet.class.getName());
    private static int opCount;
    private static int nextDumpOpCount;

    // Cache statistics
    private static int cacheGets;
    private static int cacheMisses;
    private static int overrideGets;
    private static int overrideMisses;

    /**
     * Get attribute set for the given key-value pairs.
     *
     * @param keyValuePairs
     * @return
     */
    public static synchronized AttrSet get(Object... keyValuePairs) {
        if (keyValuePairs.length == 0) {
            return EMPTY;
        }
        AttrSetBuilder builder = new AttrSetBuilder(keyValuePairs.length);
        for (int i = keyValuePairs.length; i > 0;) {
            Object value = keyValuePairs[--i];
            Object key = keyValuePairs[--i];
            if (key != null && value != null) {
                if (key == AttributeSet.ResolveAttribute) {
                    throw new IllegalStateException("AttributeSet.ResolveAttribute key not supported"); // NOI18N
                }
                builder.add(key, value);
            }
        }
        AttrSet attrSet = builder.toAttrSet();

        if (LOG.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder(100);
            sb.append("AttrSet.get():\n"); // NOI18N
            for (int i = 0; i < keyValuePairs.length;) {
                Object key = keyValuePairs[i++];
                if (sharedKeys.containsKey(key)) {
                    sb.append("  S ");
                } else {
                    sb.append("    ");
                }
                sb.append(key).append(" => ").append(keyValuePairs[i++]).append('\n');
            }
            sb.append("=> ").append(attrSet).append("; cacheSize=").append(cacheSize()).append('\n'); // NOI18N
            dumpCache(sb);
            LOG.fine(sb.toString());
        }
        opCount++;
        return attrSet;
    }

    /**
     * Merge the given sets so that an attribute in earlier attribute set overrides the same attribute in latter one.
     * @param sets
     * @return non-null merged attribute set.
     */
    public static synchronized AttrSet merge(AttributeSet... sets) {
        AttrSet attrSet = null;
        for (int i = sets.length - 1; i >= 0; i--) {
            AttributeSet set = sets[i];
            if (set == null) { // Skip null attribute set e.g. from html.editor's navigation sidebar
                continue;
            }
            if (attrSet == null) {
                attrSet = toAttrSet(set);
            } else if (set == EMPTY) {
                continue; // Skip this one since it won't make any change
            } else {
                attrSet = attrSet.findOverride(toAttrSet(set));
            }
        }
        if (LOG.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder(100);
            sb.append("AttrSet.merge():\n");
            for (int i = 0; i < sets.length; i++) {
                sb.append("    ").append(sets[i]).append('\n');
            }
            sb.append("=> ").append(attrSet).append("; cacheSize=").append(cacheSize()).append('\n');
            dumpCache(sb);
            LOG.fine(sb.toString());
        }
        opCount++;
        return attrSet;
    }

    private static void dumpCache(StringBuilder sb) {
        if (opCount >= nextDumpOpCount) {
            nextDumpOpCount = opCount + 100;
            sb.append("AttrSet CACHE DUMP START -------------------------\n"); // NOI18N
            List<AttrSet> cacheAsList = cache.asList();
            int i = 0;
            for (AttrSet dumpSet : cacheAsList) {
                appendSpaces(sb, 4);
                sb.append("[").append(i++).append("] ");
                dumpSet.appendInfo(sb, true, true, 4).append('\n');
            }
            sb.append("Actual CACHE SIZE is " + cacheAsList.size() + '\n'); // NOI18N
            sb.append("  Cache gets/misses: " + cacheGets + '/' + cacheMisses + '\n'); // NOI18N
            sb.append("  Override cache gets/misses: " + overrideGets + '/' + overrideMisses + '\n'); // NOI18N
            sb.append("AttrSet CACHE DUMP END ---------------------------\n"); // NOI18N
        }
    }

    /**
     * Register a shareable attribute's key which means that implementation
     * will attempt to share attribute sets instances containing this attribute.
     *
     * @param key non-null key.
     * @param valueType class that values should be of (used for debugging purposes only
     *  and may be null).
     */
    private static synchronized void registerSharedKey(Object key, Class valueType) {
        if (!sharedKeys.containsKey(key)) {
            KeyWrapper keyWrapper = new KeyWrapper(key, valueType);
            sharedKeys.put(key, keyWrapper);
            sortedSharedKeys.add(keyWrapper);
        }
    }

    static {
        // Registrations must be done at the begining before any AttrSet instances get created.
        registerSharedKey(StyleConstants.Background, Color.class);
        registerSharedKey(StyleConstants.Foreground, Color.class);
        registerSharedKey(StyleConstants.FontFamily, String.class);
        registerSharedKey(StyleConstants.FontSize, Integer.class);
        registerSharedKey(StyleConstants.Bold, Boolean.class);
        registerSharedKey(StyleConstants.Italic, Boolean.class);
        registerSharedKey(StyleConstants.Underline, Boolean.class);
        registerSharedKey(StyleConstants.StrikeThrough, Boolean.class);
        registerSharedKey(StyleConstants.Superscript, Boolean.class);
        registerSharedKey(StyleConstants.Subscript, Boolean.class);
        registerSharedKey(StyleConstants.Alignment, Boolean.class);
        registerSharedKey(StyleConstants.NameAttribute, String.class);
        // EditorStyleConstants
        registerSharedKey(EditorStyleConstants.WaveUnderlineColor, Color.class);
        registerSharedKey(EditorStyleConstants.DisplayName, String.class);
        registerSharedKey(EditorStyleConstants.Default, String.class);
        registerSharedKey(EditorStyleConstants.TopBorderLineColor, Color.class);
        registerSharedKey(EditorStyleConstants.BottomBorderLineColor, Color.class);
        registerSharedKey(EditorStyleConstants.LeftBorderLineColor, Color.class);
        registerSharedKey(EditorStyleConstants.RightBorderLineColor, Color.class);
        // From HighlightsContainer:
        registerSharedKey("org.netbeans.spi.editor.highlighting.HighlightsContainer.ATTR_EXTENDS_EOL", Boolean.class); // NOI18N
        registerSharedKey("org.netbeans.spi.editor.highlighting.HighlightsContainer.ATTR_EXTENDS_EMPTY_LINE", Boolean.class); // NOI18N
    }

    static int cacheSize() { // For debugging purposes
        return cache.size();
    }

    /**
     * Shared keyWrapper-value pairs ordered by the KeyWrapper.order.
     * Always non-null.
     */
    private final Object[] sharedPairs; // 8-super + 4 = 12 bytes

    /**
     * Extra non-shared key-value pairs.
     * Can be null.
     */
    private final Object[] extraPairs; // 12 + 4 = 16 bytes

    /**
     * Cached hash code.
     */
    private final int hashCode; // 16 + 4 = 20 bytes

    /**
     * Merge of this attribute set with an attribute set being a key in the cache.
     * The value of the entry in the cache is the target overriding attribute set.
     */
    private Map<AttrSet,WeakReference<AttrSet>> overrideCache; // 20 + 4 = 24 bytes

    private AttrSet(Object[] shareablePairs, Object[] extraPairs, int hashCode) {
        this.sharedPairs = shareablePairs;
        this.extraPairs = extraPairs;
        this.hashCode = hashCode;
    }

    @Override
    public int getAttributeCount() {
        return (sharedPairs.length + extraPairsLength()) >> 1;
    }

    private int extraPairsLength() {
        return (extraPairs != null ? extraPairs.length : 0);
    }

    @Override
    public boolean isDefined(Object key) {
        KeyWrapper keyWrapper = sharedKeys.get(key);
        if (keyWrapper != null) {
            return (findKeyWrapperIndex(keyWrapper) >= 0);
        } else {
            return (findExtraPairsIndex(key) >= 0);
        }
    }

    @Override
    public boolean isEqual(AttributeSet attrs) {
        return isEqual(toAttrSet(attrs));
    }

    boolean isEqual(AttrSet attrs) {
        return isEqualExtraPairs(attrs.extraPairs) && isEqualSharedPairs(attrs.sharedPairs);
    }

    boolean isEqualSharedPairs(Object[] sharedPairs2) {
        return isEqualSharedPairs(sharedPairs2, sharedPairs2.length);
    }

    boolean isEqualSharedPairs(Object[] sharedPairs2, int sharedPairs2Length) {
        if (sharedPairs.length != sharedPairs2Length)
            return false;
        // Since keys are ordered it's possible to compare arrays by traversing
        for (int i = sharedPairs.length - 2; i >= 0; i -= 2) {
            if (sharedPairs[i] != sharedPairs2[i]) // Keys must ==
                return false;
            if (!sharedPairs[i + 1].equals(sharedPairs2[i + 1]))
                return false;
        }
        return true;
    }

    boolean isEqualExtraPairs(Object[] extraPairs2) {
        if (extraPairs2 == null)
            return (extraPairs == null);
        return isEqualExtraPairs(extraPairs2, extraPairs2.length);
    }

    boolean isEqualExtraPairs(Object[] extraPairs2, int extraPairs2Length) {
        if (extraPairs == null) {
            return (extraPairs2Length == 0);
        }
        if (extraPairs.length != extraPairs2Length) {
            return false;
        }
        for (int i = extraPairs2Length - 2; i >= 0; i -= 2) {
            int index = findExtraPairsIndex(extraPairs2[i]);
            if (index < 0)
                return false;
            if (!extraPairs[i + 1].equals(extraPairs2[i + 1]))
                return false;
        }
        return true;
    }

    @Override
    public AttributeSet copyAttributes() {
        return this; // Immutable
    }

    @Override
    public Object getAttribute(Object key) {
        KeyWrapper keyWrapper = sharedKeys.get(key);
        int keyIndex;
        if (keyWrapper != null) {
            if ((keyIndex = findKeyWrapperIndex(keyWrapper)) >= 0) {
                return sharedPairs[keyIndex + 1];
            }
        } else {
            if ((keyIndex = findExtraPairsIndex(key)) >= 0) {
                return extraPairs[keyIndex + 1];
            }
        }
        return null;
    }

    @Override
    public Enumeration<?> getAttributeNames() {
        return new KeysEnumeration();
    }

    @Override
    public boolean containsAttribute(Object key, Object value) {
        return value.equals(getAttribute(key));
    }

    @Override
    public boolean containsAttributes(AttributeSet attrs) {
        if (attrs instanceof AttrSet) {
            Iterator<?> it = ((AttrSet)attrs).iterator();
            while (it.hasNext()) {
                Object key = it.next();
                Object value = it.next(); // hasNext() should always return true here
                if (!containsAttribute(key, value))
                    return false;
            }
        } else { // nonAttrSet
            Enumeration<?> en = attrs.getAttributeNames();
            while (en.hasMoreElements()) {
                Object key = en.nextElement();
                Object value = attrs.getAttribute(key);
                if (!containsAttribute(key, value)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public AttributeSet getResolveParent() {
        return null;
    }

    @Override
    public Iterator<Object> iterator() {
        return new KeyValueIterator();
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj instanceof AttrSet) {
            return isEqual((AttrSet)obj);
        } else if (obj instanceof AttrSetBuilder) {
            return ((AttrSetBuilder)obj).isEqual(this);
        }
        return false;
    }

    AttrSet findOverride(AttrSet override) {
        AttrSet ret;
        if (overrideCache == null) {
            overrideCache = new WeakHashMap<AttrSet,WeakReference<AttrSet>>(4);
            ret = null;
        } else {
            WeakReference<AttrSet> ref = overrideCache.get(override);
            ret = (ref != null) ? ref.get() : null;
        }
        overrideGets++;
        if (ret == null) {
            overrideMisses++;
            int extrasLength = override.extraPairsLength();
            int i = override.sharedPairs.length;
            AttrSetBuilder builder = new AttrSetBuilder(sharedPairs, extraPairs, hashCode,
                    i, extrasLength);
            while (i > 0) {
                Object value = override.sharedPairs[--i];
                KeyWrapper keyWrapper = (KeyWrapper) override.sharedPairs[--i];
                builder.addShared(keyWrapper, value);
            }
            for (i = extrasLength; i > 0;) {
                Object value = override.extraPairs[--i];
                Object key = override.extraPairs[--i];
                builder.addExtra(key, value);
            }
            ret = builder.toAttrSet();
            overrideCache.put(override, new WeakReference<AttrSet>(ret));
        }
        return ret;
    }

    /**
     * Use bin-search for finding index of the given key wrapper or return an insert index;
     *
     * @param keyWrapper non-null keyWrapper to search for.
     * @return index of the keyWrapper or -(insertIndex+1).
     */
    private int findKeyWrapperIndex(KeyWrapper keyWrapper) {
        return findKeyWrapperIndex(sharedPairs, sharedPairs.length, keyWrapper);
    }

    static int findKeyWrapperIndex(Object[] pairs, int pairsLength, KeyWrapper keyWrapper) {
        int high = pairsLength - 2;
        int low = 0;
        while (low <= high) {
            int mid = ((low + high) >>> 1) & (~1);
            KeyWrapper kw = (KeyWrapper) pairs[mid]; // already <<1
            int cmp = (kw.order - keyWrapper.order);

            if (cmp < 0) {
                low = mid + 2;
            } else if (cmp > 0) {
                high = mid - 2;
            } else {
                return mid; // key found
            }
        }
        return -(low + 1);  // key not found
    }

    private int findExtraPairsIndex(Object key) {
        if (extraPairs != null) {
            for (int i = extraPairs.length - 2; i >= 0; i -= 2) {
                if (extraPairs[i].equals(key)) {
                    return i;
                }
            }
        }
        return -1;
    }

    static AttrSet toAttrSet(AttributeSet attrs) {
        if (attrs instanceof AttrSet) {
            return (AttrSet) attrs;
        }
        return toAttrSetBuilder(attrs).toAttrSet();
    }

    static AttrSetBuilder toAttrSetBuilder(AttributeSet attrs) {
        AttrSetBuilder builder = new AttrSetBuilder(attrs.getAttributeCount() << 1);
        Enumeration<?> en = attrs.getAttributeNames();
        while (en.hasMoreElements()) {
            Object key = en.nextElement();
            Object value = attrs.getAttribute(key);
            builder.add(key, value);
        }
        return builder;
    }

    static Object[] trimArray(Object[] array, int size) {
        Object[] ret = new Object[size];
        while (--size >= 0) {
            ret[size] = array[size];
        }
        return ret;
    }

    void checkIntegrity() {
        String error = findIntegrityError();
        if (error != null) {
            throw new IllegalStateException(error);
        }
    }

    String findIntegrityError() {
        int lastOrder = -1;
        for (int i = 0; i < sharedPairs.length;) {
            Object key = sharedPairs[i];
            if (key == null) {
                return "[" + i + "] is null"; // NOI18N
            }
            if (!(key instanceof KeyWrapper)) {
                return "[" + i + "]=" + key + " not KeyWrapper"; // NOI18N
            }
            KeyWrapper keyWrapper = (KeyWrapper) key;
            if (keyWrapper.order <= lastOrder) {
                return "[" + i + "] KeyWrapper.order=" + keyWrapper.order + " <= lastOrder=" + lastOrder; // NOI18N
            }
            lastOrder = keyWrapper.order;
            Object value = sharedPairs[++i];
            if (value == null) {
                return "[" + i + "] is null"; // NOI18N
            }
            i++;
        }
        if (extraPairs != null) {
            for (int i = 0; i < extraPairs.length;) {
                Object key = extraPairs[i];
                if (key == null) {
                    return "[" + i + "] is null"; // NOI18N
                }
                if (sharedKeys.containsKey(key)) {
                    return "[" + i + "]: KeyWrapper-like key in extraPairs: " + key; // NOI18N
                }
                Object value = extraPairs[++i];
                if (value == null) {
                    return "[" + i + "] is null"; // NOI18N
                }
                i++;
            }
        }
        return null;
    }


    @Override
    public String toString() {
        return appendInfo(new StringBuilder(100), false, false, 0).toString();
    }

    StringBuilder appendInfo(StringBuilder sb, boolean attrs, boolean overrides, int indent) {
        sb.append("AttrSet[").append(sharedPairs.length >> 1);
        sb.append(",").append(extraPairsLength() >> 1).append("]@");
        sb.append(System.identityHashCode(this));
        if (attrs) {
            for (int i = 0; i < sharedPairs.length;) {
                sb.append('\n');
                Object key = ((KeyWrapper)sharedPairs[i++]).key;
                Object value = sharedPairs[i++];
                appendSpaces(sb, indent + 4);
                sb.append("S ").append(key).append(" => ").append(value);
            }
            if (extraPairs != null) {
                for (int i = 0; i < extraPairs.length;) {
                    sb.append('\n');
                    Object key = extraPairs[i++];
                    Object value = extraPairs[i++];
                    appendSpaces(sb, indent + 4);
                    sb.append("E ").append(key).append(" => ").append(value);
                }
            }
            if (overrides && overrideCache != null) {
                sb.append('\n');
                appendSpaces(sb, indent + 2);
                sb.append("Overrides:");
                for (Map.Entry<AttrSet,WeakReference<AttrSet>> entry : overrideCache.entrySet()) {
                    sb.append('\n');
                    AttrSet key = entry.getKey();
                    AttrSet value = entry.getValue().get();
                    appendSpaces(sb, indent + 4);
                    key.appendInfo(sb, true, false, indent + 4).append('\n');
                    appendSpaces(sb, indent + 6);
                    sb.append("=> ");
                    if (value != null) {
                        value.appendInfo(sb, true, false, indent + 8);
                    } else {
                        sb.append("NULL");
                    }
                }
            }
        }
        return sb;
    }

    private static void appendSpaces(StringBuilder sb, int spaceCount) {
        while (--spaceCount >= 0) {
            sb.append(' ');
        }
    }



    private class KeysEnumeration implements Enumeration<Object> {

        private boolean extras;

        private int index;

        private Object nextKey;

        KeysEnumeration() {
            fetchNextKey();
        }

        @Override
        public boolean hasMoreElements() {
            return (nextKey != null);
        }

        @Override
        public Object nextElement() {
            if (nextKey == null) {
                throw new NoSuchElementException();
            }
            Object next = nextKey;
            fetchNextKey();
            return next;
        }

        private void fetchNextKey() {
            if (extras) {
                if (extraPairs != null) {
                    if (index < extraPairs.length) {
                        nextKey = extraPairs[index];
                        index += 2; // skip value
                    } else {
                        nextKey = null;
                    }
                } else {
                    nextKey = null;
                }
            } else {
                if (index < sharedPairs.length) {
                    nextKey = ((KeyWrapper)sharedPairs[index]).key;
                    index += 2;
                } else {
                    extras = true;
                    index = 0;
                    fetchNextKey();
                }
            }
        }

    }

    private class KeyValueIterator implements Iterator<Object> {

        private boolean extras;

        private int index;

        private Object nextKeyOrValue;

        KeyValueIterator() {
            fetchNext();
        }

        @Override
        public boolean hasNext() {
            return (nextKeyOrValue != null);
        }

        @Override
        public Object next() {
            if (nextKeyOrValue == null) {
                throw new NoSuchElementException();
            }
            Object next = nextKeyOrValue;
            fetchNext();
            return next;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Remove not allowed.");
        }

        private void fetchNext() {
            if (extras) {
                if (extraPairs != null) {
                    if (index < extraPairs.length) {
                        nextKeyOrValue = extraPairs[index++];
                    } else {
                        nextKeyOrValue = null;
                    }
                } else {
                    nextKeyOrValue = null;
                }
            } else {
                if (index < sharedPairs.length) {
                    if ((index & 1) == 0) { // key
                        nextKeyOrValue = ((KeyWrapper)sharedPairs[index]).key;
                    } else {
                        nextKeyOrValue = sharedPairs[index];
                    }
                    index++;
                } else {
                    extras = true;
                    index = 0;
                    fetchNext();
                }
            }
        }

    }

    /**
     * Wrapper around a shareable key.
     */
    private static final class KeyWrapper {

        private static int orderCounter;
        
        final Object key;

        final Class valueType;
        
        final int order; // used for ordering in shareablePairs array

        final int keyHashCode;

        KeyWrapper(Object key, Class valueType) {
            this.key = key;
            this.valueType = valueType;
            this.order = orderCounter++;
            this.keyHashCode = key.hashCode();
        }

        @Override
        public String toString() {
            return "key=" + key + // ", valueType=" + valueType + // NOI18N
                    ", order=" + order + ", hash=" + keyHashCode; // NOI18N
        }

    }

    private static final class AttrSetBuilder implements SimpleWeakSet.ElementProvider<AttrSet> {

        Object[] shared;

        Object[] extras;

        int sharedLength;

        int extrasLength;

        int hashCode;

        AttrSetBuilder(int attrsLength) {
            shared = new Object[attrsLength];
            extras = new Object[attrsLength];
        }

        AttrSetBuilder(Object[] sharedSrc, Object[] extrasSrc, int hashCode, int sharedPlus, int extrasPlus) {
            shared = new Object[sharedSrc.length + sharedPlus];
            int extrasSrcLength = (extrasSrc != null) ? extrasSrc.length : 0;
            extras = new Object[extrasSrcLength + extrasPlus];
            this.hashCode = hashCode;
            sharedLength = sharedSrc.length;
            System.arraycopy(sharedSrc, 0, shared, 0, sharedLength);
            extrasLength = extrasSrcLength;
            if (extrasLength > 0) {
                System.arraycopy(extrasSrc, 0, extras, 0, extrasLength);
            }
        }
        
        void add(Object key, Object value) {
            KeyWrapper keyWrapper = sharedKeys.get(key);
            if (keyWrapper != null) {
                addShared(keyWrapper, value);
            } else { // Extra key
                addExtra(key, value);
            }
        }

        void addShared(KeyWrapper keyWrapper, Object value) {
            int i = findKeyWrapperIndex(shared, sharedLength, keyWrapper);
            if (i < 0) { // Does not exist yet
                i = -i - 1;
                if (i < sharedLength) {
                    System.arraycopy(shared, i, shared, i + 2, sharedLength - i);
                }
                shared[i] = keyWrapper;
                hashCode ^= keyWrapper.keyHashCode;
                sharedLength += 2;
            } else { // Already exists => just replace value
                hashCode ^= shared[i + 1].hashCode(); // Unapply present value's hashcode
            }
            shared[i + 1] = value;
            hashCode ^= value.hashCode();
        }

        void addExtra(Object key, Object value) {
            for (int i = 0; i < extrasLength; i += 2) {
                if (extras[i].equals(key)) { // Exists
                    hashCode ^= extras[i + 1].hashCode(); // Unapply present value's hashcode
                    extras[i + 1] = value;
                    hashCode ^= value.hashCode();
                    return;
                }
            }
            extras[extrasLength++] = key;
            hashCode ^= key.hashCode();
            extras[extrasLength++] = value;
            hashCode ^= value.hashCode();
        }

        AttrSet toAttrSet() { // Note: Must be preceded by finish()
            AttrSet attrSet;
            if (extrasLength == 0) { // Try to find in cache first
                // Hack - use equality for AttrSetBuilder to search in the cache
                // So convert the cache to look like holding AttrSetBuilder instances.
                @SuppressWarnings("unchecked")
                SimpleWeakSet<AttrSetBuilder> cacheL = (SimpleWeakSet<AttrSetBuilder>)
                        ((SimpleWeakSet<?>) cache);
                @SuppressWarnings("unchecked")
                SimpleWeakSet.ElementProvider<AttrSetBuilder> elementProvider =
                        (SimpleWeakSet.ElementProvider<AttrSetBuilder>)
                        ((SimpleWeakSet.ElementProvider<?>)this);
                cacheGets++;
                Object o = cacheL.getOrAdd(this, elementProvider);
                attrSet = (AttrSet) o;
                assert (attrSet != null);
            } else { // AttrSets with extras not cached
                attrSet = createElement();
            }
            return attrSet;
        }

        @Override
        public AttrSet createElement() {
            shared = (sharedLength > 0) ? trimArray(shared, sharedLength) : EMPTY_ARRAY;
            extras = (extrasLength > 0) ? trimArray(extras, extrasLength) : null;
            cacheMisses++;
            return new AttrSet(shared, extras, hashCode);
        }

        boolean isEqual(AttrSet attrs) { // Note: Must be preceded by finish()
            return attrs.isEqualExtraPairs(extras, extrasLength) &&
                    attrs.isEqualSharedPairs(shared, sharedLength);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (obj instanceof AttrSet) {
                return isEqual((AttrSet)obj);
            } else if (obj instanceof AttrSetBuilder) {
                throw new IllegalStateException("Unexpected call - must implement."); // NOI18N
            }
            return false;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

    }

}
