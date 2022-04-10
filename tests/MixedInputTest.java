import org.junit.jupiter.api.Test;

public class MixedInputTest {

    ArgsParser.ProgrammeDetails makeProgrammeDetails() {
        return new ArgsParser.ProgrammeDetails().setCommandName("ColColorize");
    }



    @Test
    void pareArgs_passOnEmptyCommandLine() {
        ArgsParser argsParser = new ArgsParser(makeProgrammeDetails(), EnumArgOptions.class);
        String[] input = new String[] {};
        argsParser.pareArgs(input);
    }

}
