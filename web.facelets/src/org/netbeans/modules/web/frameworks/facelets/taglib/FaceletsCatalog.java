/*
 * FaceletsCatalog using
 */

package org.netbeans.modules.web.frameworks.facelets.taglib;

import java.io.IOException;
import java.net.URL;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author fye
 */
public class FaceletsCatalog implements org.xml.sax.EntityResolver{



    public FaceletsCatalog(){

    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {

        String dtd = null;
        
        if(systemId.equals(Constants.TABLIB_XHTML1_TRANSITIONAL_SYSTEMID)){
            dtd = Constants.URL_TAGLIB_XHMTL1_TRANSITIONAL;
        }else if(publicId.equals(Constants.TAGLIB_JSP_1_2)){
            dtd = Constants.URL_TAGLIB_JSP_1_2;
        }else if(publicId.equals(Constants.TAGLIB_JSP_1_1)){
            dtd = Constants.URL_TAGLIB_JSP_1_1;
        }else if(publicId.equals(Constants.TAGLIB_FACELETS_1_0)){
            dtd = Constants.URL_TAGLIB_FACELETS_1_0;
        }else{
            dtd = Constants.URL_TAGLIB_DEFAFUALT;
        }
        URL url = Thread.currentThread().getContextClassLoader().getResource(dtd);
        return new InputSource(url.toString());
    }


}
