package com.glocks.web_parser.constants;

public enum CategoryType {
    Fraud("Fraud"),
    Other("CEIRAdmin"),
    CEIRAdmin("CEIRAdmin");

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    private String category;

    CategoryType(String category) {
        this.category = category;
    }

    public static String getCategory(String category) {
        for (CategoryType names : CategoryType.values()) {
            if (category.equalsIgnoreCase(String.valueOf(names)))
                return names.getCategory();
        }
        return null;
    }
}
