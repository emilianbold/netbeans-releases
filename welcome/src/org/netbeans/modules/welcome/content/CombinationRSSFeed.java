/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.welcome.content;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author S. Aubrecht
 */
public class CombinationRSSFeed extends RSSFeed {

    private String url1;
    private String url2;

    /** Creates a new instance of CombinationRSSFeed */
    public CombinationRSSFeed( String url1, String url2 ) {
        this.url1 = url1;
        this.url2 = url2;
    }

    protected ArrayList buildHtmlNodeList() throws SAXException, ParserConfigurationException, IOException {
        Document domDocument = XMLUtil.parse(new InputSource(url1), false, true, new RSSFeed.ErrorCatcher(), org.openide.xml.EntityCatalog.getDefault());
        NodeList items = domDocument.getElementsByTagName("item"); // NOI18N
        ArrayList res = new ArrayList( 2*items.getLength() );
        for( int i=0; i<items.getLength() && i<NEWS_COUNT/2; i++ )
            res.add( items.item( i ) );

        domDocument = XMLUtil.parse(new InputSource(url2), false, true, new RSSFeed.ErrorCatcher(), org.openide.xml.EntityCatalog.getDefault());
        items = domDocument.getElementsByTagName("item"); // NOI18N
        for( int i=0; i<items.getLength() && i<NEWS_COUNT/2; i++ )
            res.add( items.item( i ) );

        return sortNodes( res );
    }

    private ArrayList sortNodes( ArrayList res ) {
        Collections.sort( res, new DateFeedItemComparator() );
        return res;
    }

    private static class DateFeedItemComparator implements Comparator {
    private static DateFormat dateFormat = new SimpleDateFormat( "EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH ); // NOI18N
        public int compare(Object o1, Object o2) {
            Node node1 = (Node)o1;
            Node node2 = (Node)o2;

            Date date1 = extractDate( node1 );
            Date date2 = extractDate( node2 );

            if( null == date1 && null == date2 )
                return 0;
            else if( null == date1 )
                return 1;
            else if( null == date2 )
                return -1;
            if( date1.after( date2 ) ) {
                return -1;
            } else if( date1.before( date2 ) ) {
                return 1;
            }
            return 0;
        }

        private Date extractDate( Node node ) {
            NodeList children = node.getChildNodes();

            String date = null;

            for( int j=0; j<children.getLength(); j++ ) {
                Node child = children.item(j);

                String tag = child.getNodeName();

                String content = getTextContent( child );

                if ((content != null) && content.length() == 0) {
                    content = null;
                }

                if (tag.equals("date") || tag.equals("pubDate")) { // NOI18N // NOI18N
                    date = content;
                    break;
                }
            }
            try {
                if( null != date )
                    return dateFormat.parse( date );
            } catch( ParseException pE ) {
                //ignore
            }
            return null;
        }
    }
}
