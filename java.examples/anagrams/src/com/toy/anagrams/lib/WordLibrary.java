/* Anagram Game Application - Library
 *
 * WordLibrary.java
 *
 * Created on March 12, 2003, 3:08 PM
 */

package com.toy.anagrams.lib;

public final class WordLibrary {
    
    private static final String WORD_LIST[] = {
        "abstraction",
        "ambiguous",
        "arithmetic",
        "backslash",
        "bitmap",
        "circumstance",
        "combination",
        "consequently",
        "consortium",
        "decrementing",
        "dependency",
        "disambiguate",
        "dynamic",
        "encapsulation",
        "equivalent",
        "expression",
        "facilitate",
        "fragment",
        "hexadecimal",
        "implementation",
        "indistinguishable",
        "inheritance",
        "internet",
        "java",
        "localization",
        "microprocessor",
        "navigation",
        "optimization",
        "parameter",
        "patrick",
        "pickle",
        "polymorphic",
        "rigorously",
        "simultaneously",
        "specification",
        "structure",
        "lexical",
        "likewise",
        "management",
        "manipulate",
        "mathematics",
        "hotjava",
        "vertex",
        "unsigned",
        "traditional"};
        
    private static final String SCRAMBLED_WORD_LIST[] = {
        "batsartcoin",
        "maibuguos",
        "ratimhteci",
        "abkclssha",
        "ibmtpa",
        "iccrmutsnaec",
        "ocbmnitaoni",
        "ocsnqeeutnyl",
        "ocsnroitmu",
        "edrcmeneitgn",
        "edepdnneyc",
        "idasbmgiauet",
        "ydanicm",
        "neacsplutaoni",
        "qeiuaveltn",
        "xerpseisno",
        "aficilatet",
        "rfgaemtn",
        "ehaxedicalm",
        "milpmeneatitno",
        "niidtsniugsiahleb",
        "niehiratcen",
        "nietnret",
        "ajav",
        "olacilazitno",
        "imrcpoorecssro",
        "anivagitno",
        "poitimazitno",
        "aparemert",
        "aprtcki",
        "ipkcel",
        "opylomprich",
        "irogorsuyl",
        "isumtlnaoesuyl",
        "psceficitaoni",
        "tsurtcreu",
        "elixalc",
        "ilekiwse",
        "amanegemtn",
        "aminupalet",
        "amhtmetacsi",
        "ohjtvaa",
        "evtrxe",
        "nuisngde",
        "rtdatioialn"
    };
        
    /** Singleton class
     */
    private WordLibrary() {
    }

    /** Gets the word at given index.
     * @param idx Index of required word
     * @retrun word at given index
     */
    public static String getWord(int idx) {
        return WORD_LIST[idx];
    }

    /** Gets the word at given index is its scrambled form.
     * @param idx Index of required word
     * @retrun Word at given index in its scrambled form
     */
    public static String getScrambledWord(int idx) {
        return SCRAMBLED_WORD_LIST[idx];
    }

    /** Gets number of the words in the library.
     * @retrun Number of the word in the library
     */
    public static int getSize() {
        return WORD_LIST.length;
    }

    /** Checks whether the usres guess of word at given index is correct.
     * @param index Index of the word guessed
     * @return true if the guess was correct false otherwise
     */
    public static boolean isCorrect(int idx, String userGuess) {
        return userGuess.equals(getWord(idx));
    }

}

