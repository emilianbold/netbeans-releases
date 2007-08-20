/*
 *
 * Copyright (c) 2007, Sun Microsystems, Inc.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of Sun Microsystems nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package example.stock;


/**
 * <p>This is a utility class that is used to parse data obtained from either
 * the quote server or the database and break it into fields for easy use
 * rather than spreading the parsing code around to a thousand different places
 * through out the program.</p>
 */
public final class Stock {
    /**
     * Name of the Stock
     */
    private static String name;

    /**
     * Time of the last trade
     */
    private static String time;

    /**
     * Price of the Stock
     */
    private static int price;

    /**
     * $ change
     */
    private static int change;

    /**
     * 52-week high
     */
    private static int high;

    /**
     * 52-week low
     */
    private static int low;

    /**
     * opening price on the day
     */
    private static int open;

    /**
     * previous high
     */
    private static int prev;

    /**
     * <p>Takes a <code>String</code> from the quote server or database and
     * parses the string into each field.  We first have to split it into
     * small strings and then parse each string that should be a number.
     *
     * @param quoteString the <code>String</code> to parse into the fields
     * @throws <code>NumberFormatException</code> is thrown if
     *            data that is not of the correct format is passed in -- where
     *            the number parts of the string cannot be converted to <code>
     *            Integers</code> because there are non-numeric characters
     *            in positions where numbers are expected
     * @throws <code>StringIndexOutOfBoundsException</code> is
     *            thrown if data that is not of the correct format is passed
     *            in -- where the delimiters between the fields are not in
     *            the right spots, or the data is not of the correct length
     */
    public static void parse(String quoteString)
        throws NumberFormatException, StringIndexOutOfBoundsException {
        // get our starting index
        int index = quoteString.indexOf('"');

        if (index == -1) {
            name = quoteString;

            return;
        }

        // split the string up into it's fields
        name = quoteString.substring(++index, (index = quoteString.indexOf('"', index)));
        index += 3;
        time = quoteString.substring(index, (index = quoteString.indexOf('-', index)) - 1);
        index += 5;

        String Sprice = quoteString.substring(index, (index = quoteString.indexOf('<', index)));
        index += 6;

        String Schange = quoteString.substring(index, (index = quoteString.indexOf(',', index)));
        index += 2;

        String Slow = quoteString.substring(index, (index = quoteString.indexOf(' ', index)));
        index += 3;

        String Shigh = quoteString.substring(index, (index = quoteString.indexOf('"', index)));
        index += 2;

        String Sopen = quoteString.substring(index, (index = quoteString.indexOf(',', index)));
        ++index;

        String Sprev = quoteString.substring(index, quoteString.length() - 2);

        // convert the strings that should be numbers into ints
        price = makeInt(Sprice);
        // remove the '+' sign if it exists
        Schange = (Schange.indexOf('+') == -1) ? Schange : Schange.substring(1, Schange.length());
        change = makeInt(Schange);
        prev = makeInt(Sprev);

        if ("N/A".equals(Slow)) {
            low = prev;
        } else {
            low = makeInt(Slow);
        }

        if ("N/A".equals(Shigh)) {
            high = prev;
        } else {
            high = makeInt(Shigh);
        }

        if ("N/A".equals(Sopen)) {
            open = prev;
        } else {
            open = makeInt(Sopen);
        }
    }

    /**
     * <p>Take a <code>String</code> representation of an int and the number of
     * decimal places that the <code>String</code> carries and make an <code>
     * int</code> out of it</p>
     * <p>Since there is no floating point support in MIDP/CLDC, we have to
     * convert the decimal numbers into <code>Integer</code>s.
     * We do this by:</p>
     * <li> Looking at only the first 7 significant characters which, because
     *      of the decimal, means the first 6 numbers from left to right.</li>
     * <li> Looking at a maximum of 4 decimal places</li>
     * <li> We remove the decimal character (if there is one) and concatenate
     *      the numbers before and after the decimal so that we can convert
     *      to an integer and manipulate the value as a number</li>
     * <li> After doing this for each number, we have
     * <code>int</code> values but no
     *      notion of where the decimal place was.  To
     * alleviate this, we make
     * sure that each number has EXACTLY 4 decimal place holders. Therefore,
     * we can divide by 10000 to put the decimal place back in the same
     * spot.<BR>
     *<pre>
     *      Example: 100    -> 1000000 ->            /10000 = 100
     *      Example: 345.67 -> 34567   -> 3456700 -> /10000 = 345.67
     *      Example: 3.4526 -> 34526   ->            /10000 = 3.4526
     *</pre></li>
     *
     * @return the int value of the string
     * @param length
     * @param source the <code>String</code> value to convert
     * to an <code>int</code>
     */
    public static int makeInt(String str)
        throws NumberFormatException, StringIndexOutOfBoundsException {
        // Make sure to remove any whitespace chars that might be present.
        String source = str.trim();

        // cut the entire string down to 6 characters
        if (source.length() > 7) {
            source = source.substring(0, 6);
        }

        // cut the string down to 4 decimal places
        while ((source.length() - (source.indexOf('.') + 1)) > 4) {
            source = source.substring(0, source.length() - 1);
        }

        // convert to an int
        int value =
            (source.indexOf('.') == -1) ? Integer.valueOf(source).intValue()
                                        : Integer.valueOf(new String(source.substring(0,
                        source.indexOf('.')) +
                    source.substring(source.indexOf('.') + 1, source.length()))).intValue();

        // offset to 4 decimal placeholders
        int length =
            (source.indexOf('.') == -1) ? 0
                                        : source.substring(source.indexOf('.') + 1, source.length())
                                                .length();

        if (length < 4) {
            int diff = 4 - length;

            while (diff-- > 0) {
                value *= 10;
            }
        }

        return value;
    }

    /**
     * <p>Return the name of the stock</p>
     *
     * @return name (ticker symbol) of the stock
     * @param  quoteString <code>String</code> to parse for the field data
     */
    public static String getName(String quoteString) {
        parse(quoteString);

        return name;
    }

    /**
     * <p>Return the time of the last trade</p>
     *
     * @return time of the last trade of the stock
     * @param  quoteString <code>String</code> to parse for the field data
     */
    public static String getTime(String quoteString) {
        parse(quoteString);

        return time;
    }

    /**
     * <p>Return the price of the last trade of the stock</p>
     *
     * @return price of the last trade of the stock
     * @param  quoteString <code>String</code> to parse for the field data
     */
    public static int getPrice(String quoteString) {
        parse(quoteString);

        return price;
    }

    /**
     * <p>Return the $ change in the stock</p>
     *
     * @return $ change in the stock today
     * @param  quoteString <code>String</code> to parse for the field data
     */
    public static int getChange(String quoteString) {
        parse(quoteString);

        return change;
    }

    /**
     * <p>Return the 52-week high for the stock</p>
     *
     * @return 52-week high of the stock
     * @param  quoteString <code>String</code> to parse for the field data
     */
    public static int getHigh(String quoteString) {
        parse(quoteString);

        return high;
    }

    /**
     * <p>Return the 52-week low of the stock</p>
     *
     * @return 52-week low of the stock
     * @param  quoteString <code>String</code> to parse for the field data
     */
    public static int getLow(String quoteString) {
        parse(quoteString);

        return low;
    }

    /**
     * <p>Return the opening price of the stock</p>
     *
     * @return opening price of the stock today
     * @param  quoteString <code>String</code> to parse for the field data
     */
    public static int getOpen(String quoteString) {
        parse(quoteString);

        return open;
    }

    /**
     * <p>Return the previous high for the stock</p>
     *
     * @return previous high for the stock
     * @param  quoteString <code>String</code> to parse for the field data
     */
    public static int getPrevious(String quoteString) {
        parse(quoteString);

        return prev;
    }

    /**
     * <p>Convert an <code>int</code> into a <code>String</code>
     * with the decimal placed back in</p>
     *
     * @return The <code>String</code> value of the int
     * @param intNum the <code>int</code> value to convert
     * to a <code>String</code>
     */
    public static String convert(int intNum) {
        String s = String.valueOf(intNum);
        String pre = s.substring(0, ((s.length() < 4) ? s.length() : (s.length() - 4)));
        String suf = s.substring(((s.length() == pre.length()) ? 0 : (s.length() - 4)), s.length());

        if (Integer.valueOf(suf).intValue() == 0) {
            return pre;
        }

        while (Integer.valueOf(suf.substring(suf.length() - 1, suf.length())).intValue() == 0) {
            suf = suf.substring(0, suf.length() - 1);
        }

        return (pre + "." + suf);
    }

    /**
     * <p>String representation of price with decimal placed
     * back in the correct spot</p>
     *
     * @return current stock price
     * @param  quoteString <code>String</code> to parse for the field data
     */
    public static String getStringPrice(String quoteString) {
        parse(quoteString);

        return convert(price);
    }

    /**
     * <p>String representation of change with decimal placed
     * back in the correct spot</p>
     *
     * @return change in stock price today
     * @param  quoteString <code>String</code> to parse for the field data
     */
    public static String getStringChange(String quoteString) {
        parse(quoteString);

        return convert(change);
    }

    /**
     * <p>String representation of the 52-week high with decimal
     * placed back in the correct spot</p>
     *
     * @return 52-week high
     * @param  quoteString <code>String</code> to parse for the field data
     */
    public static String getStringHigh(String quoteString) {
        parse(quoteString);

        return convert(high);
    }

    /**
     * <p>String representation of the 52-week low with decimal
     * placed back in the correct spot</p>
     *
     * @return 52-week low
     * @param  quoteString <code>String</code> to parse for the field data
     */
    public static String getStringLow(String quoteString) {
        parse(quoteString);

        return convert(low);
    }

    /**
     * <p>String representation of the opening price with
     * decimal placed back in the correct spot</p>
     *
     * @return opening stock price
     * @param  quoteString <code>String</code> to parse for the field data
     */
    public static String getStringOpen(String quoteString) {
        parse(quoteString);

        return convert(open);
    }

    /**
     * <p>String representation of previous with decimal
     * placed back in the correct spot</p>
     *
     * @return previous high for the stock
     * @param  quoteString <code>String</code> to parse for the field data
     */
    public static String getStringPrevious(String quoteString) {
        parse(quoteString);

        return convert(prev);
    }
}
