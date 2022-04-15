import org.junit.jupiter.api.Test;

import java.util.ArrayList;

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

    @Test
    void pass_multiple_same_input_arguments_allowed() {
        ArgsParser argsParser = new ArgsParser(
                new ArgsParser.ProgrammeDetails().setCommandName("Test_Prog"),
                new ArgsParser.ArgOption[] {
                        new ArgsParser.ArgOption()
                                .setShortKey('a')
                                .setLongKey("Aaa")
                                .setRepeatable(true)
                                .setUsage(ArgsParser.E_Usage.KEY_VALUE)
                }
        );

        String[] input = new String[] {"--Aaa=Hi", "--Aaa=They", "-a", "Some", "--Aaa=Do", "-a", "ABC", "-a", "def"};
        argsParser.pareArgs(input);

        assertTrue(argsParser.isPassed("a"));
        assertEquals(6, argsParser.getArgument("a").getValues().size());

        ArrayList<String> values = argsParser.getArgument("Aaa").getValues();
        assertEquals("Hi", values.get(0));
        assertEquals("They", values.get(1));
        assertEquals("Some", values.get(2));
        assertEquals("Do", values.get(3));
        assertEquals("ABC", values.get(4));
        assertEquals("def", values.get(5));
    }

}
