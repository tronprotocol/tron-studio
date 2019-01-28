/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.tron.studio.solc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;

public class CompilationErrorResult {

    private static ArrayList<String> warnings = new ArrayList<>();
    private static ArrayList<String> errors = new ArrayList<>();

    @JsonIgnore
    public static void parse(String raw) throws IOException {
        warnings.clear();
        errors.clear();
        String[] lines = raw.split("\n");
        boolean startLine = false;
        boolean endLine = false;
        boolean isWarningType = false;
        StringBuilder currentErrorList = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String currentLine = lines[i];
            if (StringUtils.startsWith(currentLine, "Warning:")) {
                continue;
            }
            if (currentLine.matches(".*:[1-9]\\d*:[1-9]\\d*:.*")) {
                startLine = true;
                isWarningType = currentLine.matches(".*: Warning:.*");
            } else if (currentLine.matches(".*\\^.*")) {
                endLine = true;
            } else {
                startLine = false;
                endLine = false;
            }
            if (startLine) {
                currentErrorList.append(currentLine).append("\n");
            } else if (endLine) {
                currentErrorList.append(currentLine).append("\n");
                if (isWarningType) {
                    warnings.add(currentErrorList.toString());
                } else {
                    errors.add(currentErrorList.toString());
                }
            } else {
                currentErrorList.append(currentLine).append("\n");
            }
        }
    }

    public static ArrayList<String> getWarnings() {
        return warnings;
    }

    public static ArrayList<String> getErrors() {
        return errors;
    }
}
