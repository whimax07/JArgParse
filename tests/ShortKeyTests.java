import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ShortKeyTests {

    ArgsParser.ProgrammeDetails makeProgrammeDetails() {
        return new ArgsParser.ProgrammeDetails().setCommandName("ColColorize");
    }



    // ===============================
    //    Key-value pair.
    // ===============================
    @Test
    void fail_key_value_pair_no_value() {
        ArgsParser argsParser = new ArgsParser(makeProgrammeDetails(), EnumArgOptions.class);
        String[] input = new String[] {"-b"};
        assertThrows(ArgsParser.ParseArgumentException.class, () -> argsParser.pareArgs(input));
    }

    @Test
    void fail_passed_with_two_dashes() {
        ArgsParser argsParser = new ArgsParser(makeProgrammeDetails(), EnumArgOptions.class);
        String[] input = new String[] {"--b", "abc"};
        assertThrows(ArgsParser.ParseArgumentException.class, () -> argsParser.pareArgs(input));
    }

    @Test
    void fail_pass_value_to_key() {
        ArgsParser argsParser = new ArgsParser(
                new ArgsParser.ProgrammeDetails().setCommandName("Test_Prog"),
                new ArgsParser.ArgOption[] {
                        new ArgsParser.ArgOption().setShortKey('a').setUsage(ArgsParser.E_Usage.KEY)
                }
        );
        String[] input = new String[] {"-a", "abc"};

        assertThrows(ArgsParser.ParseArgumentException.class, () -> argsParser.pareArgs(input));
    }

    @Test
    void pass_pass_value_to_key_value_pair() {
        ArgsParser argsParser = new ArgsParser(makeProgrammeDetails(), EnumArgOptions.class);
        String[] input = new String[] {"-b", "abc"};
        assertDoesNotThrow(() -> argsParser.pareArgs(input));
    }

    @Test
    void pass_confirm_key_value_pair_passed() {
        fail();
    }

    /**
     * See {@link MixedInputTests#pass_multiple_same_input_arguments_allowed()}.
     */
    @Test
    void pass_get_input_from_key_value_pair() {
    }

    @Test
    void pass_confirm_key_value_not_passed() {
        fail();
    }



    // ===============================
    //    Key.
    // ===============================
    @Test
    void pass_pass_key_to_key() {
        fail();
    }

    @Test
    void pass_confirm_key_passed() {
        ArgsParser argsParser = new ArgsParser(
                new ArgsParser.ProgrammeDetails().setCommandName("Test_Prog"),
                new ArgsParser.ArgOption[] {
                        new ArgsParser.ArgOption().setShortKey('a').setUsage(ArgsParser.E_Usage.KEY)
                }
        );
        String[] input = new String[] {"-a"};
        argsParser.pareArgs(input);

        assertTrue(argsParser.isPassed("a"));
        assertTrue(argsParser.isShortPassed('a'));
        assertNotNull(argsParser.getShortArgument('a'));
    }

    @Test
    void pass_confirm_key_not_passed() {
        fail();
    }

    @Test
    void pass_get_input_from_key() {
        fail();
    }



    // ===============================
    //    Other argument options.
    // ===============================
    @Test
    void fail_pass_the_same_key_more_than_once_without_allowing() {
        ArgsParser argsParser = new ArgsParser(makeProgrammeDetails(), EnumArgOptions.class);
        String[] input = new String[] {"-t", "Hi", "-t", "Yo"};
        assertThrows(ArgsParser.ParseArgumentException.class, () -> argsParser.pareArgs(input));
    }

    @Test
    void pass_pass_the_same_key_more_than_once_allowing_that() {
        fail();
    }

    @Test
    void pass_get_values_for_multi_pass() {
        fail();
    }

}
