import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ShortKeyTests {

    ArgsParser.ProgrammeDetails makeProgrammeDetails() {
        return new ArgsParser.ProgrammeDetails().setCommandName("ColColorize");
    }



    @Test
    void pareArgs_fail_key_value_pair_no_value() {
        ArgsParser argsParser = new ArgsParser(makeProgrammeDetails(), EnumArgOptions.class);
        String[] input = new String[] {"-b"};
        assertThrows(ArgsParser.ParseArgumentException.class, () -> argsParser.pareArgs(input));
    }

    @Test
    void pareArgs_pass_key_with_value() {
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
    void pareArgs_pass_key_value_pair() {
        ArgsParser argsParser = new ArgsParser(makeProgrammeDetails(), EnumArgOptions.class);
        String[] input = new String[] {"-b", "abc"};
        assertDoesNotThrow(() -> argsParser.pareArgs(input));
    }

    @Test
    void isPassed_pass_key() {
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

}
