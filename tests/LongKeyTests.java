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
        assertTrue(argsParser.getArgument(EnumArgOptions.TEXT).getValue().isEmpty());
    }

    @Test
    void pass_confirm_key_value_pair_passed() {
        ArgsParser argsParser = new ArgsParser(makeProgrammeDetails(), EnumArgOptions.class);
        String[] input = new String[] {"--Set-Text=Hi"};
        assertDoesNotThrow(() -> argsParser.pareArgs(input));

        assertTrue(argsParser.isPassed(EnumArgOptions.TEXT));
        assertTrue(argsParser.isPassed("Set-Text"));
        assertTrue(argsParser.isLongPassed("Set-Text"));
        assertNotNull(argsParser.getArgument(EnumArgOptions.TEXT));
        assertNotNull(argsParser.getArgument("Set-Text"));
    }

    /**
     * See {@link MixedInputTests#pass_multiple_same_input_arguments_allowed()}.
     */
    @Test
    void pass_confirm_key_value_pair_not_passed() {
    }



    // ===============================
    //    Key.
    // ===============================
    @Test
    void fail_key_passed_key_value() {
        ArgsParser argsParser = new ArgsParser(makeProgrammeDetails(), EnumArgOptions.class);
        // Need to pass set text so it errors. If you don't it just looks like a list arg. Probably why they are bad.
        String[] input = new String[] {"-r", "reset_all", "--Set-Text=(0,0,0)"};
        assertThrows(ArgsParser.ParseArgumentException.class, () -> argsParser.pareArgs(input));
    }

    @Test
    void pass_pass_key() {
        fail();
    }

    @Test
    void pass_confirm_key_passed() {
        fail();
    }

    @Test
    void pass_get_key_passed() {
        fail();
    }

    @Test
    void pass_confirm_key_not_passed() {
        fail();
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
