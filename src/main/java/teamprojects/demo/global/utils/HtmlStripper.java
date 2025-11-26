package teamprojects.demo.global.utils;

import java.util.regex.Pattern;

/**
 * 보고서 본문에서 HTML 태그를 제거하고 글자 수를 세는 유틸리티
 */
public class HtmlStripper {

    // HTML 태그 패턴 (모든 <...> 태그 제거)
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");

    public static String stripTags(String htmlText) {
        if (htmlText == null) {
            return "";
        }
        // 모든 HTML 태그를 빈 문자열로 대체하여 순수 텍스트만 남김
        return HTML_TAG_PATTERN.matcher(htmlText).replaceAll("");
    }

    public static int countStrippedCharacters(String htmlText) {
        return stripTags(htmlText).length();
    }
}