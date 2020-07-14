package ru.bloodsoft.gibddchecker.util;

import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;

public class SanitizeHelper {
    public static String sanitizeString (String string) {
        string = string.replace(" ", "");
        string = string.replace("/", "");
        string = string.replace("+", "");
        string = string.replace("'", "");
        string = string.replace("*", "");
        string = string.replace("&", "");

        return string;
    }

    public static String transliterate (String plateNumber) {
        //А, В, Е, К, М, Н, О, Р, С, Т, У, Х
        String result = plateNumber.toLowerCase();
        result = SanitizeHelper.sanitizeString(result);

        result = result.replace("а", "a");
        result = result.replace("в", "b");
        result = result.replace("е", "e");
        result = result.replace("к", "k");
        result = result.replace("м", "m");
        result = result.replace("н", "h");
        result = result.replace("о", "o");
        result = result.replace("р", "p");
        result = result.replace("с", "c");
        result = result.replace("т", "t");
        result = result.replace("у", "y");
        result = result.replace("х", "x");

        return result;
    }

    public static String antiTransliterate (String plateNumber) {
        //А, В, Е, К, М, Н, О, Р, С, Т, У, Х
        String result = plateNumber.toLowerCase();
        result = SanitizeHelper.sanitizeString(result);

        result = result.replace("a", "а");
        result = result.replace("b", "в");
        result = result.replace("e", "е");
        result = result.replace("k", "к");
        result = result.replace("m", "м");
        result = result.replace("h", "н");
        result = result.replace("o", "о");
        result = result.replace("p", "р");
        result = result.replace("c", "с");
        result = result.replace("t", "т");
        result = result.replace("y", "у");
        result = result.replace("x", "х");

        return result;
    }

    public static String encryptString (String message) {
        String password = "asdf==";
        String encryptedMsg = "";

        try {
            encryptedMsg = AESCrypt.encrypt(password, message);
        } catch (GeneralSecurityException e){
            e.printStackTrace();
        }

        return encryptedMsg;
    }

    public static String decryptString (String message) {
        String password = "dfgasd==";
        String messageAfterDecrypt = "";

        try {
            messageAfterDecrypt = AESCrypt.decrypt(password, message);
        } catch (GeneralSecurityException e){
            e.printStackTrace();
        }

        return messageAfterDecrypt;
    }
}