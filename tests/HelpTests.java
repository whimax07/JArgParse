import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HelpTests {

    private static final String TARGET_HELP =
            "====================================================================================================\n" +
            "======================================= Set Console Colours ========================================\n" +
            "====================================================================================================\n" +
            "This program set the colours used by this console.\n" +
            "By Max Whitehouse, version 1.0.0.\n" +
            "\n" +
            "  -h, --help, --Help                    Use to print this help.\n" +
            "                                        Example: ColColorize -h\n" +
            "\n" +
            "  -b, --Set-Background                  This command sets the background colour of the console using\n" +
            "                                        an RGB 0-255 triplet.\n" +
            "                                        Usage: Key-value pair, Repeatable.\n" +
            "                                        Example: ColColorize ... -b (0,0,0) ...\n" +
            "                                        Example: ColColorize ... --Set-Background=(0,0,0) ...\n" +
            "\n" +
            "  -t, --Set-Text                        This command sets the text colour if the console using an\n" +
            "                                        RGB 0-255 triplet.\n" +
            "                                        Usage: Key-value pair.\n" +
            "                                        Example: ColColorize ... -t {value} ...\n" +
            "                                        Example: ColColorize ... --Set-Text={value} ...\n" +
            "\n" +
            "      --Use-Defaults                    This command tells the console revert to its default colour\n" +
            "                                        scheme. This should be used on its own.\n" +
            "                                        Usage: Key, Exclusive.\n" +
            "                                        Example: ColColorize --Use-Defaults\n" +
            "\n" +
            "\n" +
            "  [SPACE DELIMITED LIST]                This will take the path to json files and read a \"Set\n" +
            "                                        Console Colours\" configuration file.\n" +
            "                                        Usage: List, a space delimited list of values at the end of\n" +
            "                                        the command.\n" +
            "                                        Example: ColColorize ... {value} {value} {value}";



    static ArgsParser.ProgrammeDetails makeProgrammeDetails() {
        return new ArgsParser.ProgrammeDetails()
                .setCommandName("ColColorize")
                .setProgrammeName("Set Console Colours")
                .setProgrammeDescription("This program set the colours used by this console.")
                .setAuthor("Max Whitehouse")
                .setVersion("1.0.0");
    }



    @Test
    void check_help_for_set_console_colour_test_example() {
        ArgsParser argsParser = new ArgsParser(makeProgrammeDetails(), HelpEnumExample.class);
        String helpText = argsParser.getHelpText();
        System.out.println("Help text: \n" + helpText);

        String[] splitTarget = TARGET_HELP.split("\\r?\\n");
        String[] splitMade = helpText.split("\\r?\\n");

        for (int i = 0; i < splitMade.length && i < splitTarget.length; i++) {
            assertEquals(splitTarget[i], splitMade[i], "\nFirst mismatch accrued on line " + i + ".");
        }

        if (splitMade.length != splitTarget.length) {
            fail("The target help and the generated help have a different number of lines.");
        }
    }

    private enum HelpEnumExample implements ArgsParser.EnumOptions {

        BACKGROUND(new ArgsParser.ArgOption()
                .setShortKey('b')
                .setLongKey("Set-Background")
                .setUsage(ArgsParser.E_Usage.KEY_VALUE)
                .setRepeatable(true)
                .setDescription("This command sets the background colour of the console using an RGB 0-255 triplet.")
                .setShortValueExample("(0,0,0)")
                .setLongKeyValueExample("(0,0,0)")
        ),

        TEXT(new ArgsParser.ArgOption()
                .setShortKey('t')
                .setLongKey("Set-Text")
                .setUsage(ArgsParser.E_Usage.KEY_VALUE)
                .setDescription("This command sets the text colour if the console using an RGB 0-255 triplet.")
        ),

        RESET(new ArgsParser.ArgOption()
                .setLongKey("Use-Defaults")
                .setUsage(ArgsParser.E_Usage.KEY)
                .setDescription("This command tells the console revert to its default colour scheme. This should be used on its own.")
                .setUseOnItsOwn(true)
        ),

        CONFIGS(new ArgsParser.ArgOption()
                .setUsage(ArgsParser.E_Usage.LIST)
                .setDescription("This will take the path to json files and read a \"Set Console Colours\" configuration file.")
        );

        private final ArgsParser.ArgOption option;

        HelpEnumExample(ArgsParser.ArgOption option) {
            this.option = option;
        }

        public ArgsParser.ArgOption get() {
            return option;
        }

    }

}
