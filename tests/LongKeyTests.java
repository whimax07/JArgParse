import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LongKeyTests {

    ArgsParser.ProgrammeDetails makeProgrammeDetails() {
        return new ArgsParser.ProgrammeDetails().setCommandName("ColColorize");
    }



    // ===============================
    //    Key-value pair.
    // ===============================
    @Test
    void fail_key_with_one_dash() {
        ArgsParser argsParser = new ArgsParser(makeProgrammeDetails(), EnumArgOptions.class);
        String[] input = new String[] {"-Set-Text=Hi"};
        assertThrows(ArgsParser.ParseArgumentException.class, () -> argsParser.pareArgs(input));
    }

    @Test
    void fail_key_value_pair_no_value() {
        ArgsParser argsParser = new ArgsParser(makeProgrammeDetails(), EnumArgOptions.class);
        String[] input = new String[] {"--Set-Text"};
        assertThrows(ArgsParser.ParseArgumentException.class, () -> argsParser.pareArgs(input));
    }

    @Test
    void pass_key_value_pair_no_value_with_equals() {
        ArgsParser argsParser = new ArgsParser(makeProgrammeDetails(), EnumArgOptions.class);
        String[] input = new String[] {"--Set-Text="};
        assertDoesNotThrow(() -> argsParser.pareArgs(input));

        assertTrue(argsParser.isPassed(EnumArgOptions.TEXT));
        assertTrue(argsParser.getResult(EnumArgOptions.TEXT).getValue().isEmpty());
    }

    @Test
    void pass_confirm_key_value_pair_passed() {
        ArgsParser argsParser = new ArgsParser(makeProgrammeDetails(), EnumArgOptions.class);
        String[] input = new String[] {"--Set-Text=Hi"};
        assertDoesNotThrow(() -> argsParser.pareArgs(input));

        assertTrue(argsParser.isPassed(EnumArgOptions.TEXT));
        assertTrue(argsParser.isPassed("Set-Text"));
        assertTrue(argsParser.isPassed("t"));
        assertTrue(argsParser.isLongPassed("Set-Text"));
        assertTrue(argsParser.isShortPassed('t'));
        assertNotNull(argsParser.getResult(EnumArgOptions.TEXT));
        assertNotNull(argsParser.getResult("Set-Text"));
        assertNotNull(argsParser.getResult("t"));
    }

    /**
     * See {@link MixedInputTests#pass_multiple_same_input_arguments_allowed()}.
     */
    @Test
    void pass_get_passed_values() { }

    @Test
    void pass_confirm_key_value_pair_not_passed() {
        ArgsParser argsParser = new ArgsParser(makeProgrammeDetails(), EnumArgOptions.class);
        String[] input = new String[] {"--Set-Text=Hi"};
        assertDoesNotThrow(() -> argsParser.pareArgs(input));

        assertFalse(argsParser.isPassed(EnumArgOptions.RESET));
        assertFalse(argsParser.isPassed("Use-Defaults"));
        assertFalse(argsParser.isLongPassed("Use-Defaults"));
        assertFalse(argsParser.isShortPassed('r'));
        assertNull(argsParser.getResult(EnumArgOptions.RESET));
        assertNull(argsParser.getResult("Use-Defaults"));
        assertNull(argsParser.getResult("r"));
    }



    // ===============================
    //    Key.
    // ===============================
    @Test
    void pass_pass_key() {
        ArgsParser argsParser = new ArgsParser(makeProgrammeDetails(), EnumArgOptions.class);
        String[] input = new String[] {"--Use-Defaults"};
        assertDoesNotThrow(() -> argsParser.pareArgs(input));
    }

    @Test
    void pass_confirm_key_passed() {
        ArgsParser argsParser = new ArgsParser(makeProgrammeDetails(), EnumArgOptions.class);
        String[] input = new String[] {"--Use-Defaults"};
        argsParser.pareArgs(input);

        assertTrue(argsParser.isPassed(EnumArgOptions.RESET));
        assertTrue(argsParser.isPassed("Use-Defaults"));
        assertTrue(argsParser.isPassed("r"));
        assertTrue(argsParser.isLongPassed("Use-Defaults"));
        assertTrue(argsParser.isShortPassed('r'));
        assertNotNull(argsParser.getResult(EnumArgOptions.RESET));
        assertNotNull(argsParser.getResult("Use-Defaults"));
        assertNotNull(argsParser.getResult("r"));
    }

    @Test
    void pass_get_key_passed() {
        ArgsParser argsParser = new ArgsParser(makeProgrammeDetails(), EnumArgOptions.class);
        String[] input = new String[] {"--Use-Defaults"};
        argsParser.pareArgs(input);

        String passValue = argsParser.getResult(EnumArgOptions.RESET).getValue();
        assertNotNull(passValue);
        assertTrue(passValue.isEmpty());
    }

    @Test
    void pass_confirm_key_not_passed() {
        ArgsParser argsParser = new ArgsParser(makeProgrammeDetails(), EnumArgOptions.class);
        String[] input = new String[] {"--Use-Defaults"};
        argsParser.pareArgs(input);

        assertFalse(argsParser.isPassed(EnumArgOptions.TEXT));
        assertFalse(argsParser.isPassed("Set-Text"));
        assertFalse(argsParser.isPassed("t"));
        assertFalse(argsParser.isLongPassed("Set-Text"));
        assertFalse(argsParser.isShortPassed('t'));
        assertNull(argsParser.getResult(EnumArgOptions.TEXT));
        assertNull(argsParser.getResult("Set-Text"));
        assertNull(argsParser.getResult("t"));
    }



    // ===============================
    //    Other argument options.
    // ===============================
    @Test
    void fail_pass_the_same_key_more_than_once_without_allowing() {
        ArgsParser argsParser = new ArgsParser(makeProgrammeDetails(), EnumArgOptions.class);
        String[] input = new String[] {"--Set-Text=Hi", "--Set-Text=Yo"};
        assertThrows(ArgsParser.ParseArgumentException.class, () -> argsParser.pareArgs(input));
    }

}
