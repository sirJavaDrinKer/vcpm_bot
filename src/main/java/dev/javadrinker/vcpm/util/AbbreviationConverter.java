package dev.javadrinker.vcpm.util;

import java.util.HashMap;
import java.util.Map;

public class AbbreviationConverter {

    private static final Map<String, String> ABBREVIATIONS = new HashMap<>();

    static {
        // Americas
        put("LOUD", "LOUD");
        put("Evil Geniuses", "EG");
        put("KRÜ Esports", "KRÜ");
        put("Leviatán", "LEV");
        put("NRG", "NRG");
        put("Sentinels", "SEN");
        put("Cloud9", "C9");
        put("100 Thieves", "100T");
        put("MIBR", "MIBR");
        put("FURIA", "FUR");
        put("G2 Esports", "G2");
        put("ENVY", "ENVY"); // Ascension 2025

        // EMEA
        put("Fnatic", "FNC");
        put("Team Liquid", "TL");
        put("Natus Vincere", "NAVI");
        put("FUT Esports", "FUT");
        put("Team Vitality", "VIT");
        put("Karmine Corp", "KC");
        put("Gentle Mates", "M8");
        put("BBL Esports", "BBL");
        put("Team Heretics", "TH");
        put("GIANTX", "GX");
        put("ULF Esports", "ULF"); // Ascension 2025
        put("PCIFIC Esports", "PCF"); // Ascension 2025

        // Pacific
        put("DRX", "DRX");
        put("T1", "T1");
        put("Gen.G", "GEN");
        put("Paper Rex", "PRX");
        put("ZETA DIVISION", "ZETA");
        put("DetonatioN FocusMe", "DFM");
        put("Global Esports", "GE");
        put("Rex Regum Qeon", "RRQ");
        put("Team Secret", "TS");
        put("FULL SENSE", "FS");
        put("Nongshim RedForce", "NS"); // Ascension 2024
        put("VARREL", "VL"); // Ascension 2025

        // China
        put("EDward Gaming", "EDG");
        put("FunPlus Phoenix", "FPX");
        put("JD Gaming", "JDG");
        put("All Gamers", "AG");
        put("Dragon Ranger Gaming", "DRG");
        put("Xi Lai Gaming", "XLG");
        put("TYLOO", "TYL");
        put("Nova Esports", "NOVA");
        put("Trace Esports", "TE");
        put("Titan Esports Club", "TEC");
        put("Wolves Esports", "WOL");
        put("Bilibili Gaming", "BLG");








        // Fallbacks / misc
        put("TBD", "TBD");
    }

    private AbbreviationConverter() {
        // utility class
    }

    public static String abbreviate(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "";
        }

        String normalized = normalize(fullName);
        return ABBREVIATIONS.getOrDefault(normalized, fullName);
    }

    public static String abbreviate(String fullName, int maxLength) {
        String result = abbreviate(fullName);
        return result.length() <= maxLength
                ? result
                : result.substring(0, maxLength);
    }

    private static void put(String full, String shortName) {
        ABBREVIATIONS.put(normalize(full), shortName);
    }

    private static String normalize(String name) {
        return name.trim().toUpperCase();
    }
}
