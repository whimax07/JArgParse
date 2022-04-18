import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LongKeyTests {

    ArgsParser.ProgrammeDetails makeProgrammeDetails() {
        return new ArgsParser.ProgrammeDetails().setCommandName("ColColorize");
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
    void pass_key_value_pair() {
        ArgsParser argsParser = new ArgsParser(makeProgrammeDetails(), EnumArgOptions.class);
        String[] input = new String[] {"--Set-Text=Hi"};
        assertDoesNotThrow(() -> argsParser.pareArgs(input));

        assertTrue(argsParser.isPassed(EnumArgOptions.TEXT));
    }

    @Test
    void fail_key_with_one_dash() {
        ArgsParser argsParser = new ArgsParser(makeProgrammeDetails(), EnumArgOptions.class);
        String[] input = new String[] {"-Set-Text=Hi"};
        assertThrows(ArgsParser.ParseArgumentException.class, () -> argsParser.pareArgs(input));
    }

    @Test
    void fail_pass_the_same_key_more_than_once_without_allowing() {
        ArgsParser argsParser = new ArgsParser(makeProgrammeDetails(), EnumArgOptions.class);
        String[] input = new String[] {"--Set-Text=Hi", "--Set-Text=Yo"};
        assertThrows(ArgsParser.ParseArgumentException.class, () -> argsParser.pareArgs(input));
    }

}
