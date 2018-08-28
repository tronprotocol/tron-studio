package org.tron.studio.ui;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.Subscription;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.BreakIterator;
import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CodeParserUtil {
    private static final String PATH_TO_KEYWORDS  = "keywords/solidity.txt";
    private static final Set<String> dictionary = new HashSet<String>();

    public static void checkInvalidWords(CodeArea textArea)
    {
        String[] lines = textArea.getText().toString().split("\n");
        Dictionary contracts = new Hashtable();

        boolean startFlg = false;
        String contractName = "none";
        List<String> contractContent = new ArrayList<>();

        for(String line_str: lines)
        {
            if (line_str.equals("\n"))
            {
                continue;
            }
            line_str = line_str.trim();

            //
            String[] wordsList = line_str.split(" ");
            if (wordsList[0].equals("contract"))
            {
                if (!startFlg)
                {
                    contractContent.add(line_str);
                    contractName = wordsList[1];
                    startFlg = true;
                } else
                {
                    startFlg = false;
                    contracts.put(contractName, ((ArrayList<String>) contractContent).clone());
                    contractContent.clear();
                }
            }

            if (startFlg)
            {
                contractContent.add(line_str);
            }
        }
    }

    private void readKeywords()
    {
        // load the dictionary
        try (InputStream input = getClass().getResourceAsStream("/keywords/solidity.txt");
             BufferedReader br = new BufferedReader(new InputStreamReader(input))) {
            String line;
            while ((line = br.readLine()) != null) {
                dictionary.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
