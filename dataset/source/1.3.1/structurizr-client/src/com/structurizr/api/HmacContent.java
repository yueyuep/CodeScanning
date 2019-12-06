package com.structurizr.api;

/**
 * Wraps up and combines a number of separate strings.
 */
final class HmacContent {

    private String[] strings;

    HmacContent(String... strings) {
        this.strings = strings;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        for (String string : strings) {
            buf.append(string);
            buf.append("\n");
        }

        return buf.toString();
    }

}