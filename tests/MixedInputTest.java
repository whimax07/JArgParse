import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MixedInputTest {

    ArgsParser.ProgrammeDetails makeProgrammeDetails() {
        return new ArgsParser.ProgrammeDetails().setCommandName("ColColorize");
    }



    @Test
    void pass_on_empty_command_line() {
        ArgsParser argsParser = new ArgsParser(makeProgrammeDetails(), EnumArgOptions.class);
        String[] input = new String[] {};
        assertDoesNotThrow(() -> argsParser.pareArgs(input));
    }

    @Test
    void pass_return_null_for_not_used_arg() {
        ArgsParser argsParser = new ArgsParser(makeProgrammeDetails(), EnumArgOptions.class);
        String[] input = new String[] {"--Set-Text=Hi"};
        argsParser.pareArgs(input);
        assertNull(argsParser.getArgument(EnumArgOptions.BACKGROUND));
    }

    @Test
    void pass_enum_and_arg_string_return_the_same() {
        ArgsParser argsParser = new ArgsParser(makeProgrammeDetails(), EnumArgOptions.class);
        String[] input = new String[] {"--Set-Text=Hi"};
        argsParser.pareArgs(input);
        assertSame(
                argsParser.getArgument(EnumArgOptions.TEXT),
                argsParser.getArgument(EnumArgOptions.TEXT.get().getLongKey())
        );
    }

}
