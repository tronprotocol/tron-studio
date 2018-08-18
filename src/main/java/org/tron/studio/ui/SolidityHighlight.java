package org.tron.studio.ui;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Groovy syntax highlighting.
 *
 * @author Toast kid
 */
public class SolidityHighlight extends Highlight {

    private static final String PATH_TO_KEYWORDS  = "keywords/solidity.txt";
    private static final String PAREN_PATTERN     = "\\(|\\)";
    private static final String BRACE_PATTERN     = "\\{|\\}";
    private static final String BRACKET_PATTERN   = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN    = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN   = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

    private Pattern pattern;

    /**
     * Initialize with CodeArea.
     * @param codeArea
     */
    public SolidityHighlight(final CodeArea codeArea) {
        super(codeArea);
    }

    @Override
    protected StyleSpans<Collection<String>> computeHighlighting(final String text) {
        if (pattern == null) {
            makePattern();
        }
        final Matcher matcher = pattern.matcher(text);
        int lastKwEnd = 0;
        final StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        while (matcher.find()) {
            final String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                    matcher.group("PAREN") != null ? "paren" :
                    matcher.group("BRACE") != null ? "brace" :
                    matcher.group("BRACKET") != null ? "bracket" :
                    matcher.group("SEMICOLON") != null ? "semicolon" :
                    matcher.group("STRING") != null ? "string" :
                    matcher.group("COMMENT") != null ? "comment" :
                    null; /* never happens */ assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    /**
     * Make keyword pattern.
     */
    private void makePattern() {
        try (final Stream<String> lines = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream(PATH_TO_KEYWORDS))).lines();) {
            final String keywords = lines.collect(Collectors.joining("|"));
            final String pattern  = "\\b(" + keywords + ")\\b";
            this.pattern = Pattern.compile(
                    "(?<KEYWORD>" + pattern + ")"
                            + "|(?<PAREN>" + PAREN_PATTERN + ")"
                            + "|(?<BRACE>" + BRACE_PATTERN + ")"
                            + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                            + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                            + "|(?<STRING>" + STRING_PATTERN + ")"
                            + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
                    );
        }
    }
}
