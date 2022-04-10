import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LongKeyTest {

    ArgsParser.ProgrammeDetails makeProgrammeDetails() {
        return new ArgsParser.ProgrammeDetails().setCommandName("ColColorize");
    }



    @Test
    void parse_args_fail_key_value_pair_no_value() {
        ArgsParser argsParser = new ArgsParser(makeProgrammeDetails(), EnumArgOptions.class);
        String[] input = new String[] {"--Set-Text"};
        ArgsParser.ParseArgumentException e = assertThrows(ArgsParser.ParseArgumentException.class, () -> argsParser.pareArgs(input));
        System.out.println(e.toString());
    }

    @Test
    void parse_args_pass_key_value_pair_no_value_with_equals() {
        ArgsParser argsParser = new ArgsParser(makeProgrammeDetails(), EnumArgOptions.class);
        String[] input = new String[] {"--Set-Text="};
        assertDoesNotThrow(() -> argsParser.pareArgs(input));

        assertTrue(argsParser.isPassed(EnumArgOptions.TEXT));
        assertTrue(
                argsParser.getArgument(EnumArgOptions.TEXT).getValue().isEmpty()
        );
    }

    @Test
    void parse_args_pass_key_value_pair() {
        ArgsParser argsParser = new ArgsParser(makeProgrammeDetails(), EnumArgOptions.class);
        String[] input = new String[] {"--Set-Text=Hi"};
        assertDoesNotThrow(() -> argsParser.pareArgs(input));
        assertTrue(argsParser.isPassed(EnumArgOptions.TEXT));
    }

}
