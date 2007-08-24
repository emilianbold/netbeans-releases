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
import java.util.List;
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
 * Combines two RSS feeds into one.
 * 
 * @author S. Aubrecht
 */
public class CombinationRSSFeed extends RSSFeed {

    private String url1;
    private String url2;
    private int maxItemCount;

    /** Creates a new instance of CombinationRSSFeed */
    public CombinationRSSFeed( String url1, String url2, boolean showProxyButton, int maxItemCount ) {
        super( showProxyButton );
        this.maxItemCount = maxItemCount;
        this.url1 = url1;
        this.url2 = url2;
    }

    @Override
    protected List<FeedItem> buildItemList() throws SAXException, ParserConfigurationException, IOException {
        XMLReader reader = XMLUtil.createXMLReader( false, true );
        FeedHandler handler = new FeedHandler( getMaxItemCount() );
        reader.setContentHandler( handler );
        reader.setEntityResolver( org.openide.xml.EntityCatalog.getDefault() );
        reader.setErrorHandler( new ErrorCatcher() );
        reader.parse( new InputSource(url1) );

        ArrayList<FeedItem> res = new ArrayList<FeedItem>( 2*getMaxItemCount() );
        res.addAll( handler.getItemList() );

        handler = new FeedHandler( getMaxItemCount() );
        reader.setContentHandler( handler );
        reader.parse( new InputSource(url2) );

        res.addAll( handler.getItemList() );

        List<FeedItem> items = sortNodes( res );
        if( items.size() > getMaxItemCount() ) {
            items = items.subList( 0, getMaxItemCount() );
        }
        return items;
    }

    private ArrayList<FeedItem> sortNodes( ArrayList<FeedItem> res ) {
        Collections.sort( res, new DateFeedItemComparator() );
        return res;
    }

    @Override
    protected int getMaxItemCount() {
        return this.maxItemCount;
    }

    private static class DateFeedItemComparator implements Comparator<FeedItem> {
    private static DateFormat dateFormat = new SimpleDateFormat( "EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH ); // NOI18N
    private static DateFormat dateFormatShort = new SimpleDateFormat( "EEE, dd MMM yyyy", Locale.ENGLISH ); // NOI18N
        
    public int compare(FeedItem item1, FeedItem item2) {
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
                try {     
                    return dateFormatShort.parse( item.dateTime );
                } catch( ParseException otherPE ) {
                    //ignore
                }
            }
            return null;
        }
    }
}
