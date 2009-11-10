package org.netbeans.modules.cnd.antlr.ASdebug;

import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.antlr.TokenStream;

/**
 * Default implementation of <code>IASDebugStream</code> methods.
 * @author Prashant Deva
 */
public final class ASDebugStream 
{

    public static String getEntireText(TokenStream stream)
    {
        if (stream instanceof IASDebugStream)
        {
            IASDebugStream dbgStream = (IASDebugStream) stream;
            return dbgStream.getEntireText();
        }
        return null;
    }

    public static TokenOffsetInfo getOffsetInfo(TokenStream stream, Token token)
    {
        if (stream instanceof IASDebugStream)
        {
            IASDebugStream dbgStream = (IASDebugStream) stream;
            return dbgStream.getOffsetInfo(token);
        }
        return null;
    }

       
}

