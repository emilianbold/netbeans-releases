/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.java.help.search;


import com.sun.java.help.search.Indexer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.help.search.IndexerKit;

public class ModifiedIndexer extends Indexer {
    public ModifiedIndexer() {
        super();
        try {
            setIndexerKitForContentType("text/html", (IndexerKit) Class.forName("com.sun.java.help.search.ModifiedHTMLIndexerKit").newInstance());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    public static void main(String args[]) {
	ModifiedIndexer compiler = new ModifiedIndexer();
	try {
	    compiler.compile(args);
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
