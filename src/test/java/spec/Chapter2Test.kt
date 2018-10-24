package spec

import helpers.parseAndCheck
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("spec") class Chapter2Test {
    @Test fun spec_2_1_Sequence_of_Scalars() {
        parseAndCheck("" +
            "- Mark McGwire\n" +
            "- Sammy Sosa\n" +
            "- Ken Griffey", "" +
            "%YAML 1.2\n" +
            "---\n" +
            "- !!str \"Mark McGwire\"\n" +
            "- !!str \"Sammy Sosa\"\n" +
            "- !!str \"Ken Griffey\"\n")
    }

    @Test fun spec_2_2_Mapping_Scalars_to_Scalars() {
        parseAndCheck("" +
            "hr:  65    # Home runs\n" +
            "avg: 0.278 # Batting average\n" +
            "rbi: 147   # Runs Batted In", "" +
            "%YAML 1.2\n" +
            "---\n" +
            "? !!str \"hr\": !!str \"65\"\n" +
            "? !!str \"avg\": !!str \"0.278\"\n" +
            "? !!str \"rbi\": !!str \"147\"\n")
    }

    @Test fun spec_2_3_Mapping_Scalars_to_Sequences() {
        parseAndCheck("" +
            "american:\n" +
            "  - Boston Red Sox\n" +
            "  - Detroit Tigers\n" +
            "  - New York Yankees\n" +
            "national:\n" +
            "  - New York Mets\n" +
            "  - Chicago Cubs\n" +
            "  - Atlanta Braves", "" +
            "%YAML 1.2\n" +
            "---\n" +
            "? !!str \"american\":\n" +
            "  - !!str \"Boston Red Sox\"\n" +
            "  - !!str \"Detroit Tigers\"\n" +
            "  - !!str \"New York Yankees\"\n" +
            "? !!str \"national\":\n" +
            "  - !!str \"New York Mets\"\n" +
            "  - !!str \"Chicago Cubs\"\n" +
            "  - !!str \"Atlanta Braves\"\n")
    }

    @Test fun spec_2_4_Sequence_of_Mappings() {
        parseAndCheck("" +
            "-\n" +
            "  name: Mark McGwire\n" +
            "  hr:   65\n" +
            "  avg:  0.278\n" +
            "-\n" +
            "  name: Sammy Sosa\n" +
            "  hr:   63\n" +
            "  avg:  0.288", "" +
            "%YAML 1.2\n" +
            "---\n" +
            "-\n" +
            "? !!str \"  name\": !!str \"Mark McGwire\"\n" +
            "? !!str \"  hr\": !!str \"65\"\n" +
            "? !!str \"  avg\": !!str \"0.278\"\n" +
            "-\n" +
            "? !!str \"  name\": !!str \"Sammy Sosa\"\n" +
            "? !!str \"  hr\": !!str \"63\"\n" +
            "? !!str \"  avg\": !!str \"0.288\"\n"
        )
    }

    @Test fun spec_2_5_Sequence_of_Sequences() {
        parseAndCheck("" +
            "- [name        , hr, avg  ]\n" +
            "- [Mark McGwire, 65, 0.278]\n" +
            "- [Sammy Sosa  , 63, 0.288]", "" +
            "%YAML 1.2\n" +
            "---\n" +
            "- [!!str \"name        \", !!str \"hr\", !!str \"avg  \"]\n" +
            "- [!!str \"Mark McGwire\", !!str \"65\", !!str \"0.278\"]\n" +
            "- [!!str \"Sammy Sosa  \", !!str \"63\", !!str \"0.288\"]\n")
    }

    // TODO flow mapping
    @Disabled @Test fun spec_2_6_Mapping_of_Mappings() {
        parseAndCheck("" +
            "Mark McGwire: {hr: 65, avg: 0.278}\n" +
            "Sammy Sosa: {\n" +
            "    hr: 63,\n" +
            "    avg: 0.288\n" +
            "  }", "")
    }

    @Test fun spec_2_7_Two_Documents_in_a_Stream() {
        parseAndCheck("" +
            "# Ranking of 1998 home runs\n" +
            "---\n" +
            "- Mark McGwire\n" +
            "- Sammy Sosa\n" +
            "- Ken Griffey\n" +
            "...\n" + // not in the spec, but makes next comment belong to the second document

            "# Team ranking\n" +
            "---\n" +
            "- Chicago Cubs\n" +
            "- St Louis Cardinals", "" +
            "%YAML 1.2\n" +
            "---\n" +
            "- !!str \"Mark McGwire\"\n" +
            "- !!str \"Sammy Sosa\"\n" +
            "- !!str \"Ken Griffey\"\n" +
            "...\n" +
            "%YAML 1.2\n" +
            "---\n" +
            "- !!str \"Chicago Cubs\"\n" +
            "- !!str \"St Louis Cardinals\"\n")
    }

    @Test fun spec_2_8_Play_by_Play_Feed_from_a_Game() {
        parseAndCheck("" +
            "---\n" +
            "time: 20:03:20\n" +
            "player: Sammy Sosa\n" +
            "action: strike (miss)\n" +
            "...\n" +
            "---\n" +
            "time: 20:03:47\n" +
            "player: Sammy Sosa\n" +
            "action: grand slam\n" +
            "...", "" +
            "%YAML 1.2\n" +
            "---\n" +
            "? !!str \"time\": !!str \"20:03:20\"\n" +
            "? !!str \"player\": !!str \"Sammy Sosa\"\n" +
            "? !!str \"action\": !!str \"strike (miss)\"\n" +
            "...\n" +
            "%YAML 1.2\n" +
            "---\n" +
            "? !!str \"time\": !!str \"20:03:47\"\n" +
            "? !!str \"player\": !!str \"Sammy Sosa\"\n" +
            "? !!str \"action\": !!str \"grand slam\"\n")
    }

    @Disabled @Test fun spec_2_9_Single_Document_with_Two_Comments() {
        parseAndCheck("" +
            "---\n" +
            "hr: # 1998 hr ranking\n" +
            "  - Mark McGwire\n" +
            "  - Sammy Sosa\n" +
            "rbi:\n" +
            // TODO allow comment here: "  # 1998 rbi ranking\n" +
            "  - Sammy Sosa\n" +
            "  - Ken Griffey", "" +
            "%YAML 1.2\n" +
            "---\n" +
            "? !!str \"hr\": !!str \"\"\n" +
            "? !!str \"- Mark McGwire\n" +
            "  - Sammy Sosa\n" +
            "rbi\":\n" +
            "  - !!str \"Sammy Sosa\"\n" +
            "  - !!str \"Ken Griffey\"\n")
    }

    @Test fun spec_2_10_Node_for_Sammy_Sosa_appears_twice_in_this_document() {
        parseAndCheck("" +
            "---\n" +
            "hr:\n" +
            "  - Mark McGwire\n" +
            // TODO allow comment here: "  # Following node labeled SS\n" +
            "  - &SS Sammy Sosa\n" +
            "rbi:\n" +
            "  - *SS" + // TODO allow comment here: " # Subsequent occurrence\n" +

            "  - Ken Griffey", "" +
            "%YAML 1.2\n" +
            "---\n" +
            "? !!str \"hr\":\n" +
            "  - !!str \"Mark McGwire\"\n" +
            "  - !!str \"&SS Sammy Sosa\"\n" +
            "? !!str \"rbi\":\n" +
            "  - !!str \"*SS  - Ken Griffey\"\n")
    }

    @Disabled @Test fun spec_2_11_Mapping_between_Sequences() {
        parseAndCheck("" +
            "? - Detroit Tigers\n" + // TODO sequence as key

            "  - Chicago cubs\n" +
            ":\n" +
            "  - 2001-07-23\n" +
            "\n" +
            "? [ New York Yankees,\n" +
            "    Atlanta Braves ]\n" +
            ": [2001-07-02, 2001-08-12, \n" + // TODO not in spec: no space after '[' but after ','

            "    2001-08-14 ]", "" +
            "%YAML 1.2\n" +
            "---\n" +
            "? !!str \"- Detroit Tigers\n" +
            "  - Chicago cubs\n" +
            "\":\n" +
            "  - !!str \"2001-07-23\"\n" +
            "? !!str \"\n" +
            "? [ New York Yankees,\n" +
            "    Atlanta Braves ]\n" +
            "\": [!!str \"2001-07-02\", !!str \"2001-08-12\", !!str \"\n" +
            "    2001-08-14 \"]\n")
    }

    @Disabled @Test fun spec_2_12_Compact_Nested_Mapping() {
        parseAndCheck("" +
            "---\n" +
            "# Products purchased\n" +
            "- item    : Super Hoop\n" +
            "  quantity: 1\n" +
            "- item    : Basketball\n" +
            "  quantity: 4\n" +
            "- item    : Big Shoes\n" +
            "  quantity: 1", "" +
            "%YAML 1.2\n" +
            "---\n" +
            "- ? !!str \"item    \": !!str \"Super Hoop\"\n" + // TODO spaces before colon are not part of the key

            "? !!str \"  quantity\": !!str \"1\"\n" + // TODO this indent is not part of key

            "- ? !!str \"item    \": !!str \"Basketball\"\n" +
            "? !!str \"  quantity\": !!str \"4\"\n" +
            "- ? !!str \"item    \": !!str \"Big Shoes\"\n" +
            "? !!str \"  quantity\": !!str \"1\"\n")
    }

    // TODO literal scalar
    @Disabled @Test fun spec_2_13_In_literals_newlines_are_preserved() {
        parseAndCheck("" +
            "# ASCII Art\n" +
            "--- |\n" +
            "  \\//||\\/||\n" +
            "  // ||  ||__\n", "" + "")
    }

    // TODO folded scalar
    @Disabled @Test fun spec_2_14_In_the_folded_scalars_newlines_become_spaces() {
        parseAndCheck("" +
            "--- >\n" +
            "  Mark McGwire's\n" +
            "  year was crippled\n" +
            "  by a knee injury.", "")
    }

    // TODO folded scalar
    @Test fun spec_2_15_Folded_newlines_are_preserved_for_more_indented_and_blank_lines() {
        parseAndCheck("" +
            ">\n" +
            " Sammy Sosa completed another\n" +
            " fine season with great stats.\n" +
            "\n" +
            "   63 Home Runs\n" +
            "   0.288 Batting Average\n" +
            "\n" +
            " What a year!", "" +
            "%YAML 1.2\n" +
            "---\n" +
            "!!str \"> Sammy Sosa completed another fine season with great stats.  63 Home Runs 0.288 Batting Average  What a year!\"\n")
    }

    // TODO literal and folded scalar
    @Test fun spec_2_16_Indentation_determines_scope() {
        parseAndCheck("" +
            "name: Mark McGwire\n" +
            "accomplishment: >\n" +
            "  Mark set a major league\n" +
            "  home run record in 1998.\n" +
            "stats: |\n" +
            "  65 Home Runs\n" +
            "  0.278 Batting Average", "" +
            "%YAML 1.2\n" +
            "---\n" +
            "? !!str \"name\": !!str \"Mark McGwire\"\n" +
            "? !!str \"accomplishment\": !!str \"> Mark set a major league home run record in 1998.\"\n" +
            "? !!str \"stats\": !!str \"| 65 Home Runs 0.278 Batting Average\"\n")
    }

    @Test fun spec_2_17_Quoted_Scalars() {
        parseAndCheck("" +
            "unicode: \"Sosa did fine.\\u263A\"\n" + // TODO unicode

            "control: \"\\b1998\\t1999\\t2000\\n\"\n" + // TODO escape

            "hex esc: \"\\x0d\\x0a is \\r\\n\"\n" +
            // "\n" + // TODO allow empty line
            "single: '\"Howdy!\" he cried.'\n" +
            "quoted: ' # Not a ''comment''.'\n" +
            "tie-fighter: '|\\-*-/|'", "" +
            "%YAML 1.2\n" +
            "---\n" +
            "? !!str \"unicode\": !!str \"Sosa did fine.\\u263A\"\n" +
            "? !!str \"control\": !!str \"\\b1998\\t1999\\t2000\\n\"\n" +
            "? !!str \"hex esc\": !!str \"\\x0d\\x0a is \\r\\n\"\n" +
            "? !!str \"single\": !!str \"\"Howdy!\" he cried.\"\n" +
            "? !!str \"quoted\": !!str \" # Not a 'comment'.\"\n" +
            "? !!str \"tie-fighter\": !!str \"|\\-*-/|\"\n")
    }

    @Disabled @Test fun spec_2_18_Multi_line_Flow_Scalars() {
        parseAndCheck("" +
            "plain:\n" +
            "  This unquoted scalar\n" +
            "  spans many lines.\n" +
            "\n" +
            "quoted: \"So does this\n" +
            "  quoted scalar.\"" // TODO escaped NL `\n`
            , "" +
            "%YAML 1.2\n" +
            "---\n" +
            "? !!str \"plain\":\n" +
            "!!str \"  This unquoted scalar spans many lines.\"\n" + // TODO indent not part of string

            "? !!str \"\n" +
            "quoted\": !!str \"So does this\n" +
            "  quoted scalar.\"\n") // TODO escaped NL `\n`
    }

    @Test fun spec_2_19_Integers() {
        parseAndCheck("" +
            "canonical: 12345\n" +
            "decimal: +12345\n" +
            "octal: 0o14\n" +
            "hexadecimal: 0xC", "" +
            "%YAML 1.2\n" +
            "---\n" +
            "? !!str \"canonical\": !!str \"12345\"\n" + // TODO recognize int (tag)

            "? !!str \"decimal\": !!str \"+12345\"\n" +
            "? !!str \"octal\": !!str \"0o14\"\n" + // TODO recognize octal (and canonicalize?)

            "? !!str \"hexadecimal\": !!str \"0xC\"\n") // TODO recognize hex (and canonicalize?)
    }

    @Test fun spec_2_20_Floating_Point() {
        parseAndCheck("" +
            "canonical: 1.23015e+3\n" +
            "exponential: 12.3015e+02\n" +
            "fixed: 1230.15\n" +
            "negative infinity: -.inf\n" +
            "not a number: .NaN", "" +
            "%YAML 1.2\n" +
            "---\n" +
            "? !!str \"canonical\": !!str \"1.23015e+3\"\n" + // TODO recognize float (tag)

            "? !!str \"exponential\": !!str \"12.3015e+02\"\n" +
            "? !!str \"fixed\": !!str \"1230.15\"\n" +
            "? !!str \"negative infinity\": !!str \"-.inf\"\n" + // TODO recognize infinity

            "? !!str \"not a number\": !!str \".NaN\"\n") // TODO recognize NaN
    }

    @Test fun spec_2_21_Miscellaneous() {
        parseAndCheck("" +
            // TODO recognize nil key: "null:\n" +
            "booleans: [true, false ]\n" + // TODO space after '['

            "string: '012345'", "" +
            "%YAML 1.2\n" +
            "---\n" +
            "? !!str \"booleans\": [!!str \"true\", !!str \"false \"]\n" + // TODO recognize boolean (tag)

            "? !!str \"string\": !!str \"012345\"\n")
    }

    @Test fun spec_2_22_Timestamps() {
        parseAndCheck("" +
            "canonical: 2001-12-15T02:59:43.1Z\n" +
            "iso8601: 2001-12-14t21:59:43.10-05:00\n" +
            "spaced: 2001-12-14 21:59:43.10 -5\n" +
            "date: 2002-12-14", "" +
            "%YAML 1.2\n" +
            "---\n" +
            "? !!str \"canonical\": !!str \"2001-12-15T02:59:43.1Z\"\n" + // TODO recognize timestamp (tag)

            "? !!str \"iso8601\": !!str \"2001-12-14t21:59:43.10-05:00\"\n" +
            "? !!str \"spaced\": !!str \"2001-12-14 21:59:43.10 -5\"\n" +
            "? !!str \"date\": !!str \"2002-12-14\"\n")
    }

    // TODO recognize explicit tags
    @Disabled @Test fun spec_2_23_Various_Explicit_Tags() {
        parseAndCheck("" +
            "---\n" +
            "not-date: !!str 2002-04-28\n" +
            "\n" +
            "picture: !!binary |\n" +
            " R0lGODlhDAAMAIQAAP//9/X\n" +
            " 17unp5WZmZgAAAOfn515eXv\n" +
            " Pz7Y6OjuDg4J+fn5OTk6enp\n" +
            " 56enmleECcgggoBADs=\n" +
            "\n" +
            "application specific tag: !something |\n" +
            " The semantics of the tag\n" +
            " above may be different for\n" +
            " different documents.", "")
    }

    // TODO recognize global tags && document right after directives-end
    @Disabled @Test fun spec_2_24_Global_Tags() {
        parseAndCheck("" +
            "%TAG ! tag:clarkevans.com,2002:\n" +
            "--- !shape\n" +
            "  # Use the ! handle for presenting\n" +
            "  # tag:clarkevans.com,2002:circle\n" +
            "- !circle\n" +
            "  center: &ORIGIN {x: 73, y: 129}\n" +
            "  radius: 7\n" +
            "- !line\n" +
            "  start: *ORIGIN\n" +
            "  finish: { x: 89, y: 102 }\n" +
            "- !label\n" +
            "  start: *ORIGIN\n" +
            "  color: 0xFFEEBB\n" +
            "  text: Pretty vector drawing.", "")
    }

    // TODO recognize explicit tags && document right after directives-end
    @Disabled @Test fun spec_2_25_Unordered_Sets() {
        parseAndCheck("" +
            "# Sets are represented as a\n" +
            "# Mapping where each key is\n" +
            "# associated with a null value\n" +
            "--- !!set\n" +
            "? Mark McGwire\n" +
            "? Sammy Sosa\n" +
            "? Ken Griff", "")
    }

    // TODO recognize explicit tags && document right after directives-end
    @Disabled @Test fun spec_2_26_Ordered_Mappings() {
        parseAndCheck("" +
            "# Ordered maps are represented as\n" +
            "# A sequence of mappings, with\n" +
            "# each mapping having one key\n" +
            "--- !!omap\n" +
            "- Mark McGwire: 65\n" +
            "- Sammy Sosa: 63\n" +
            "- Ken Griffy: 58", "")
    }

    @Disabled @Test fun spec_2_27_Invoice() {
        parseAndCheck("" +
            "--- !<tag:clarkevans.com,2002:invoice>\n" +
            "invoice: 34843\n" +
            "date   : 2001-01-23\n" +
            "bill-to: &id001\n" +
            "    given  : Chris\n" +
            "    family : Dumars\n" +
            "    address:\n" +
            "        lines: |\n" +
            "            458 Walkman Dr.\n" +
            "            Suite #292\n" +
            "        city    : Royal Oak\n" +
            "        state   : MI\n" +
            "        postal  : 48046\n" +
            "ship-to: *id001\n" +
            "product:\n" +
            "    - sku         : BL394D\n" +
            "      quantity    : 4\n" +
            "      description : Basketball\n" +
            "      price       : 450.00\n" +
            "    - sku         : BL4438H\n" +
            "      quantity    : 1\n" +
            "      description : Super Hoop\n" +
            "      price       : 2392.00\n" +
            "tax  : 251.42\n" +
            "total: 4443.52\n" +
            "comments:\n" +
            "    Late afternoon is best.\n" +
            "    Backup contact is Nancy\n" +
            "    Billsmer @ 338-4338.", "" + "")
    }

    @Test fun spec_2_28_Log_File() {
        parseAndCheck("" +
            "---\n" +
            "Time: 2001-11-23 15:01:42 -5\n" +
            "User: ed\n" +
            "Warning:\n" +
            "  This is an error message\n" +
            "  for the log file\n" +
            "---\n" +
            "Time: 2001-11-23 15:02:31 -5\n" +
            "User: ed\n" +
            "Warning:\n" +
            "  A slightly different error\n" +
            "  message.\n" +
            "---\n" +
            "Date: 2001-11-23 15:03:17 -5\n" +
            "User: ed\n" +
            "Fatal:\n" +
            "  Unknown variable \"bar\"\n" +
            "Stack:\n" +
            "  - file: TopClass.py\n" +
            "    line: 23\n" +
            "    code: |\n" +
            "      x = MoreObject(\"345\\n\")\n" +
            "  - file: MoreClass.py\n" +
            "    line: 58\n" +
            "    code: |-\n" +
            "      foo = bar", "" +
            "%YAML 1.2\n" +
            "---\n" +
            "? !!str \"Time\": !!str \"2001-11-23 15:01:42 -5\"\n" + // TODO recognize timestamp (tag)

            "? !!str \"User\": !!str \"ed\"\n" +
            "? !!str \"Warning\":\n" +
            "!!str \"  This is an error message for the log file\"\n" + // TODO indent is not part of line

            "...\n" +
            "%YAML 1.2\n" +
            "---\n" +
            "? !!str \"Time\": !!str \"2001-11-23 15:02:31 -5\"\n" + // TODO recognize timestamp (tag)

            "? !!str \"User\": !!str \"ed\"\n" +
            "? !!str \"Warning\":\n" +
            "!!str \"  A slightly different error message.\"\n" + // TODO indent is not part of line

            "...\n" +
            "%YAML 1.2\n" +
            "---\n" +
            "? !!str \"Date\": !!str \"2001-11-23 15:03:17 -5\"\n" + // TODO recognize timestamp (tag)

            "? !!str \"User\": !!str \"ed\"\n" +
            "? !!str \"Fatal\":\n" +
            "!!str \"  Unknown variable \\\"bar\\\"\"\n" + // TODO indent is not part of line

            "? !!str \"Stack\":\n" +
            "  - ? !!str \"file\": !!str \"TopClass.py\"\n" +
            "? !!str \"    line\": !!str \"23\"\n" + // TODO indent && recognize int

            "? !!str \"    code\": !!str \"| x = MoreObject(\\\"345\\n\\\")\"\n" + // TODO literal scalar

            "  - ? !!str \"file\": !!str \"MoreClass.py\"\n" +
            "? !!str \"    line\": !!str \"58\"\n" +
            "? !!str \"    code\": !!str \"|- foo = bar\"\n") // TODO literal scalar
    }
}
