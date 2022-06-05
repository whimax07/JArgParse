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
                new ArgsParser.ArgOption().setShortKey('a').setUsage(ArgsParser.E_Usage.KEY)
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
        ArgsParser argsParser = new ArgsParser(makeProgrammeDetails(), EnumArgOptions.class);
        String[] input = new String[] {"-b", "abc"};
        argsParser.pareArgs(input);

        assertTrue(argsParser.isPassed(EnumArgOptions.BACKGROUND));
        assertTrue(argsParser.isPassed("Set-Background"));
        assertTrue(argsParser.isPassed("b"));
        assertTrue(argsParser.isLongPassed("Set-Background"));
        assertTrue(argsParser.isShortPassed('b'));
        assertNotNull(argsParser.getResult(EnumArgOptions.BACKGROUND));
        assertNotNull(argsParser.getResult("Set-Background"));
        assertNotNull(argsParser.getResult("b"));
    }

    /**
     * See {@link MixedInputTests#pass_multiple_same_input_arguments_allowed()}.
     */
    @Test
    void pass_get_input_from_key_value_pair() { }

    @Test
    void pass_confirm_key_value_not_passed() {
        ArgsParser argsParser = new ArgsParser(makeProgrammeDetails(), EnumArgOptions.class);
        String[] input = new String[] {"-t", "Black"};
        argsParser.pareArgs(input);

        assertFalse(argsParser.isPassed(EnumArgOptions.BACKGROUND));
        assertFalse(argsParser.isPassed("Set-Background"));
        assertFalse(argsParser.isPassed("b"));
        assertFalse(argsParser.isLongPassed("Set-Background"));
        assertFalse(argsParser.isShortPassed('b'));
        assertNull(argsParser.getResult(EnumArgOptions.BACKGROUND));
        assertNull(argsParser.getResult("Set-Background"));
        assertNull(argsParser.getResult("b"));
    }



    // ===============================
    //    Key.
    // ===============================
    @Test
    void pass_pass_key_to_key() {
        ArgsParser argsParser = new ArgsParser(makeProgrammeDetails(), EnumArgOptions.class);
        String[] input = new String[] {"-r"};
        assertDoesNotThrow(() -> argsParser.pareArgs(input));
    }

    @Test
    void pass_confirm_key_passed() {
        ArgsParser argsParser = new ArgsParser(
                new ArgsParser.ProgrammeDetails().setCommandName("Test_Prog"),
                new ArgsParser.ArgOption().setShortKey('a').setUsage(ArgsParser.E_Usage.KEY)
        );

        String[] input = new String[] {"-a"};
        argsParser.pareArgs(input);

        assertTrue(argsParser.isPassed("a"));
        assertTrue(argsParser.isShortPassed('a'));
        assertNotNull(argsParser.getResultShort('a'));
    }

    @Test
    void pass_get_key_passed() {
        ArgsParser argsParser = new ArgsParser(makeProgrammeDetails(), EnumArgOptions.class);
        String[] input = new String[] {"-r"};
        argsParser.pareArgs(input);

        String passValue = argsParser.getResult(EnumArgOptions.RESET).getValue();
        assertNotNull(passValue);
        assertTrue(passValue.isEmpty());
    }

    @Test
    void pass_confirm_key_not_passed() {
        ArgsParser argsParser = new ArgsParser(makeProgrammeDetails(), EnumArgOptions.class);
        String[] input = new String[] {"--Set-Text=Hi"};
        argsParser.pareArgs(input);

        assertFalse(argsParser.isPassed(EnumArgOptions.RESET));
        assertFalse(argsParser.isPassed("Use-Defaults"));
        assertFalse(argsParser.isPassed("r"));
        assertFalse(argsParser.isLongPassed("Use-Defaults"));
        assertFalse(argsParser.isShortPassed('r'));
        assertNull(argsParser.getResult(EnumArgOptions.RESET));
        assertNull(argsParser.getResult("Use-Defaults"));
        assertNull(argsParser.getResult("r"));
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

}
