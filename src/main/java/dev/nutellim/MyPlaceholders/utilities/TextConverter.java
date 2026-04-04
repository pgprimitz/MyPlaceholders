package dev.nutellim.MyPlaceholders.utilities;

import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class TextConverter {

    private static final int[]    ROMAN_VALUES  = {1000,900,500,400,100,90,50,40,10,9,5,4,1};
    private static final String[] ROMAN_SYMBOLS = {"M","CM","D","CD","C","XC","L","XL","X","IX","V","IV","I"};

    public String toRoman(int number) {
        if (number < 1 || number > 3999) return String.valueOf(number);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < ROMAN_VALUES.length; i++) {
            while (number >= ROMAN_VALUES[i]) {
                result.append(ROMAN_SYMBOLS[i]);
                number -= ROMAN_VALUES[i];
            }
        }
        return result.toString();
    }

    public String toRoman(String text) {
        try {
            return toRoman(Integer.parseInt(text.trim()));
        } catch (NumberFormatException e) {
            return text;
        }
    }

    private static final Map<Character, Character> SMALLCAPS_MAP = new HashMap<>();
    static {
        String normal = "abcdefghijklmnopqrstuvwxyz";
        String small  = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘqʀꜱᴛᴜᴠᴡxʏᴢ";
        for (int i = 0; i < normal.length(); i++) {
            SMALLCAPS_MAP.put(normal.charAt(i), small.charAt(i));
        }
    }

    public String toSmallCaps(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            sb.append(SMALLCAPS_MAP.getOrDefault(Character.toLowerCase(c), c));
        }
        return sb.toString();
    }

    private static final Map<Character, Character> SUPERSCRIPT_MAP = new HashMap<>();
    static {
        char[] from = {'a','b','c','d','e','f','g','h','i','j','k','l','m',
                       'n','o','p','r','s','t','u','v','w','x','y','z',
                       '0','1','2','3','4','5','6','7','8','9'};
        char[] to   = {'ᵃ','ᵇ','ᶜ','ᵈ','ᵉ','ᶠ','ᵍ','ʰ','ⁱ','ʲ','ᵏ','ˡ','ᵐ',
                       'ⁿ','ᵒ','ᵖ','ʳ','ˢ','ᵗ','ᵘ','ᵛ','ʷ','ˣ','ʸ','ᶻ',
                       '⁰','¹','²','³','⁴','⁵','⁶','⁷','⁸','⁹'};
        for (int i = 0; i < from.length; i++) {
            SUPERSCRIPT_MAP.put(from[i], to[i]);
        }
    }

    public String toSuperscript(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            sb.append(SUPERSCRIPT_MAP.getOrDefault(Character.toLowerCase(c), c));
        }
        return sb.toString();
    }

    public String toCircled(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c >= 'a' && c <= 'z') sb.append((char)('ⓐ' + (c - 'a')));
            else if (c >= 'A' && c <= 'Z') sb.append((char)('Ⓐ' + (c - 'A')));
            else if (c >= '1' && c <= '9') sb.append((char)('①' + (c - '1')));
            else if (c == '0') sb.append('⓪');
            else sb.append(c);
        }
        return sb.toString();
    }

    public String toFullwidth(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c >= '!' && c <= '~') sb.append((char)(c + 0xFEE0));
            else if (c == ' ') sb.append('　');
            else sb.append(c);
        }
        return sb.toString();
    }
}
