/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.

Oracle and Java are registered trademarks of Oracle and/or its affiliates.
Other names may be trademarks of their respective owners.
 * SUN PROPRIETARY/CONFIDENTIAL.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package org.apache.batik.css.engine;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.batik.css.engine.StyleSheet;


/**
 * Style sheet cache, to avoid repeated loading and parsing of the
 * stylesheet, and also to avoid introducing excessive memory overhead
 * such that multiple pages referring to the same stylesheet can use
 * the same instance. This is important since stylesheets can take up
 * a lot of memory when they are large. (The default stylesheet in Creator
 * took up about 5 megabytes for each usage, which quickly adds up when you
 * have many pages!)
 *
 * @todo Clear the cache when stylesheets are updated!
 *
 * @author Tor Norbye, tor.norbye@sun.com
 */
public class StyleSheetCache {
    private static StyleSheetCache instance;
    HashMap sheets;

    /** Construct a new cache */
    public StyleSheetCache() {
    }

    public static StyleSheetCache getInstance() {
        if (instance == null) {
            instance = new StyleSheetCache();
        }

        return instance;
    }

    /** Return number of entries in this cache */
    public int size() {
        return (sheets != null) ? sheets.size() : 0;
    }

    /** Get a style sheet by a particular URL */
    public StyleSheet get(URL url) {
        if ((sheets == null) || (url == null)) {
            return null;
        }

        StyleSheet result = (StyleSheet)sheets.get(url);

        return result;
    }

    /** Put a style sheet into the cache */
    public void put(URL url, StyleSheet sheet) {
        if (sheets == null) {
            sheets = new HashMap(); // TODO - initial size?
        }

        sheets.put(url, sheet);
    }

    /** Clear out the cache */
    public void flush() {
        sheets = null;
    }

    public String toString() {
        return "StyleSheetCache: " + sheets.toString();
    }
}
