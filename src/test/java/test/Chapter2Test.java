package test;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static test.Helpers.parseAndCheck;

@Tag("spec") class Chapter2Test {
    @Test void spec_2_1_Sequence_of_Scalars() {
        parseAndCheck("" +
                        "- Mark McGwire\n" +
                        "- Sammy Sosa\n" +
                        "- Ken Griffey"
                , "" +
                        "%YAML 1.2\n" +
                        "---\n" +
                        "- !!str \"Mark McGwire\"\n" +
                        "- !!str \"Sammy Sosa\"\n" +
                        "- !!str \"Ken Griffey\"\n");
    }

    @Disabled @Test void spec_2_2_Mapping_Scalars_to_Scalars() {
        parseAndCheck("" +
                        "hr:  65    # Home runs\n" +
                        "avg: 0.278 # Batting average\n" +
                        "rbi: 147   # Runs Batted In"
                , "" +
                        "%YAML 1.2\n" +
                        "---\n" +
                        "? !!str \"hr\": !!str \"65\" # Home runs\n" +
                        "? !!str \"avg\": !!str \"0.278\" # Batting average\n" +
                        "? !!str \"rbi\": !!str \"147\" # Runs Batted In\n");
    }
}