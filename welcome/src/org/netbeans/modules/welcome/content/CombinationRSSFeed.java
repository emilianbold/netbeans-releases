/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.welcome.content.RSSFeed.ErrorCatcher;
import org.netbeans.modules.welcome.content.RSSFeed.FeedHandler;
import org.netbeans.modules.welcome.content.RSSFeed.FeedItem;
import org.openide.xml.XMLUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

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

    protected ArrayList/*FeedItem*/ buildItemList() throws SAXException, ParserConfigurationException, IOException {
        XMLReader reader = XMLUtil.createXMLReader( false, true );
        FeedHandler handler = new FeedHandler();
        reader.setContentHandler( handler );
        reader.setEntityResolver( org.openide.xml.EntityCatalog.getDefault() );
        reader.setErrorHandler( new ErrorCatcher() );
        reader.parse( new InputSource(url1) );

        ArrayList res = new ArrayList( 2*NEWS_COUNT );
        res.addAll( handler.getItemList() );

        handler = new FeedHandler();
        reader.setContentHandler( handler );
        reader.parse( new InputSource(url2) );

        res.addAll( handler.getItemList() );

        return sortNodes( res );
    }

    private ArrayList sortNodes( ArrayList res ) {
        Collections.sort( res, new DateFeedItemComparator() );
        return res;
    }

    private static class DateFeedItemComparator implements Comparator {
    private static DateFormat dateFormat = new SimpleDateFormat( "EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH ); // NOI18N
        public int compare(Object o1, Object o2) {
            FeedItem item1 = (FeedItem)o1;
            FeedItem item2 = (FeedItem)o2;

            Date date1 = extractDate( item1 );
            Date date2 = extractDate( item2 );

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

        private Date extractDate( FeedItem item ) {
            try {
                if( null != item.dateTime )
                    return dateFormat.parse( item.dateTime );
            } catch( ParseException pE ) {
                //ignore
            }
            return null;
        }
    }
}
