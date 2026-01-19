package com.republicate.stillness;

/**
 * All Stillness constants
 *
 * @author Claude Brisson
 */

/* Rassemble les constantes Stillness
 */
public interface StillnessConstants {

    /**
     * List of the Stillness' directives
     */
    public static String[] _stillnessDirectives = new String[] {"follow", "match", "regexp", "optional"};

    public static String NORMALIZE = "stillness.normalize";

    // debugging color
    public static String _matchColor = "#000000"; // noir
    public static String _codeColor = "#0000D6"; // bleu
    public static String _scrapeColor = "#00D600"; // vert
    public static String _mismatchColor = "#8E8E8E"; // gris
    public static String _endLoopColor = "#ECEC00"; // jaune
    public static String _subMatchColor = "#F7941D"; // orange
    public static String _errorColor = "#F10000"; // rouge

}
