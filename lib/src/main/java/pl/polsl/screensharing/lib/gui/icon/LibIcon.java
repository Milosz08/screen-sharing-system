/*
 * Copyright (c) 2023 by MULTIPLE AUTHORS
 * Part of the CS study course project.
 */
package pl.polsl.screensharing.lib.gui.icon;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LibIcon implements AppIcon {
    CANCEL("Cancel"),
    CHECK_BOX_LIST("CheckBoxList"),
    CODE_INFORMATION_RULE("CodeInformationRule"),
    DELETE_CLAUSE("DeleteClause"),
    DELETE_TABLE("DeleteTable"),
    HELP_TABLE_OF_CONTENTS("HelpTableOfContents"),
    SAVE("Save"),
    ;

    private final String name;
}