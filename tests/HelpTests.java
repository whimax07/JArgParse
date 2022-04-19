import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HelpTests {

    private static final String TARGET_HELP =
            "===================================================================================================\n" +
            "======================================= Set Console Colours =======================================\n" +
            "===================================================================================================\n" +
            "This program set the colours used by this console. \n" +
            "By Max Whitehouse, version 1.0.0.\n" +
            "\n" +
            "  -b, --Set-Background                 This command sets the background colour of the console using \n" +
            "                                       an RGB 0-255 triplet.\n" +
            "                                       Usage: Key-value pair.\n" +
            "                                       Example: ColColorize ... -b (0,0,0) ...\n" +
            "                                       Example: ColColorize ... -Set-Background=(0,0,0) ...\n" +
            "\n" +
            "  -t, --Set-Text                       This command sets the text colour if the console using an RGB\n" +
            "                                       0-255 triplet.\n" +
            "                                       Usage: Key-value pair.\n" +
            "                                       Example: ColColorize ... -t {value} ...\n" +
            "                                       Example: ColColorize ... -Set-Text={value} ...\n" +
            "\n" +
            "  --Use-Defaults                       This command tells the console revert to it's default colour \n" +
            "                                       scheme. This should be used on it's own.\n" +
            "                                       Usage: Key.\n" +
            "                                       Example: ColColourize --Use-Defaults\n" +
            "\n" +
            "\n" +
            "  [SPACE DELIMITED LIST]               This will take the path to json files and read a \"Set \n" +
            "                                       Console Colours\" configuration file.\n" +
            "                                       Usage: List, a space delimited list of values at the end of \n" +
            "                                       the command.\n" +
            "                                       Example: ColColourize ... {value} {value} {value}";

    private enum HelpEnumExample implements ArgsParser.EnumOptions {

        BACKGROUND(new ArgsParser.ArgOption()
                .setShortKey('b')
                .setLongKey("Set-Background")
                .setUsage(ArgsParser.E_Usage.KEY_VALUE)
                .setDescription("This command sets the background colour of the console using an RGB 0-255 triplet.")
                .setShortValueExample("(0,0,0)")
                .setLongKeyValueExample("(0,0,0)")),

        TEXT(new ArgsParser.ArgOption()
                .setShortKey('t')
                .setLongKey("Set-Text")
                .setUsage(ArgsParser.E_Usage.KEY_VALUE)
                .setDescription("This command sets the text colour if the console using an RGB 0-255 triplet.")),

        RESET(new ArgsParser.ArgOption()
                .setLongKey("Use-Defaults")
                .setUsage(ArgsParser.E_Usage.KEY)
                .setDescription("This command tells the console revert to its default colour scheme. This should be used on its own.")),

        CONFIGS(new ArgsParser.ArgOption()
                .setUsage(ArgsParser.E_Usage.LIST)
                .setDescription("This will take the path to json files and read a \"Set Console Colours\" configuration file."));

        private final ArgsParser.ArgOption option;

        HelpEnumExample(ArgsParser.ArgOption option) {
            this.option = option;
        }

        public ArgsParser.ArgOption get() {
            return option;
        }

    }



    @Test
    void check_help_for_set_console_colour_test_example() {
        int lineNumber = 0;
        String a = "";
        String b = "b";
        assertEquals(a, b, "\nFirst mismatch accrued on line " + lineNumber + ".");
        fail();
    }

}
