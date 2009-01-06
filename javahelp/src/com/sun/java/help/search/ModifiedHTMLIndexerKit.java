/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.java.help.search;

import com.sun.java.help.search.HTMLIndexerKit.HTMLParserCallback.HiddenAction;
import javax.help.search.IndexerKit;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.*;

public class ModifiedHTMLIndexerKit extends HTMLIndexerKit {
    @Override
    public HTMLParserCallback getParserCallback(IndexerKit kit) {
        return new ModifiedHTMLParserCallback(kit);
    }
    public class ModifiedHTMLParserCallback extends HTMLIndexerKit.HTMLParserCallback {
        private IndexerKit kit;
        public ModifiedHTMLParserCallback (IndexerKit kit) {
            super(kit);
            this.kit = kit;
            tagMap.remove(HTML.Tag.META);
            tagMap.put(HTML.Tag.META, new MetaAction());
        }

        class MetaAction extends HiddenAction {
            public void start(HTML.Tag t, MutableAttributeSet a) {
                super.start(t, a);
                Object attr = a.getAttribute(HTML.Attribute.NAME);
                if (attr != null)
                    if (attr.toString().contentEquals("keywords")) {
                        kit.parseIntoTokens(new String(a.getAttribute(HTML.Attribute.CONTENT).toString()), Integer.MAX_VALUE / 4);
                        System.out.println("keywords found !!!: " + new String(a.getAttribute(HTML.Attribute.CONTENT).toString()));
                    }
            }

            private boolean isEmpty(HTML.Tag t) {
                return true;
            }
        }
    }
}