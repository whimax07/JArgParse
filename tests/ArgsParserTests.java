import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ArgsParserTests {

    ArgsParser.ProgrammeDetails makeProgrammeDetails() {
        return new ArgsParser.ProgrammeDetails().setCommandName("ColColorize");
    }

    ArgsParser.ArgOption[] colColorizeOptions() {
        return new ArgsParser.ArgOption[] {
                new ArgsParser.ArgOption()
                        .setShortKey('b')
                        .setLongKey("Set-Background")
                        .setUsage(ArgsParser.E_Usage.KEY_VALUE)
                        .setDescription("This command sets the background colour of the console using an RGB 0-255 triplet.")
                        .setShortValueExample("(0,0,0)")
                        .setLongKeyValueExample("(0,0,0)"),

                new ArgsParser.ArgOption()
                        .setShortKey('t')
                        .setLongKey("Set-Text")
                        .setUsage(ArgsParser.E_Usage.KEY_VALUE)
                        .setDescription("This command sets the text colour if the console using an RGB 0-255 triplet."),

                new ArgsParser.ArgOption()
                        .setLongKey("Use-Defaults")
                        .setUsage(ArgsParser.E_Usage.KEY)
                        .setDescription("This command tells the console revert to its default colour scheme. This should be used on its own."),

                new ArgsParser.ArgOption()
                        .setUsage(ArgsParser.E_Usage.LIST)
                        .setDescription("This will take the path to json files and read a \"Set Console Colours\" configuration file.")
        };
    }



    @Test
    void parseArgConstructor_failsIfNull() {
        assertThrows(Exception.class,
                () -> new ArgsParser(null, (ArgsParser.ArgOption[]) null));
        assertThrows(Exception.class,
                () -> new ArgsParser(new ArgsParser.ProgrammeDetails(), (ArgsParser.ArgOption[]) null));
    }

    @Test
    void parseArgConstructor_passIfEmptyAndCommandSet() {
        new ArgsParser(makeProgrammeDetails(), new ArrayList<>());
    }

    @Test
    void parseArgConstructor_passEnumList() {
        new ArgsParser(makeProgrammeDetails(), EnumArgOptions.class);
    }

    @Test
    void failOnNoKeys() {
        ArgsParser.ArgOption[] options = new ArgsParser.ArgOption[] {
                new ArgsParser.ArgOption()
        };

        ArgsParser.ArgumentOptionException exception = assertThrows(ArgsParser.ArgumentOptionException.class,
                () -> new ArgsParser(makeProgrammeDetails(), options));

        System.out.println(exception.toString());
    }

    @Test
    void failNoUsage() {
        ArgsParser.ArgOption[] options = new ArgsParser.ArgOption[] {
                new ArgsParser.ArgOption().setShortKey('a').setLongKey("A1")
        };

        ArgsParser.ArgumentOptionException exception = assertThrows(ArgsParser.ArgumentOptionException.class,
                () -> new ArgsParser(makeProgrammeDetails(), options));

        System.out.println(exception.toString());
    }

    @Test
    void failShortLongKey() {
        ArgsParser.ArgOption[] options = new ArgsParser.ArgOption[] {
                new ArgsParser.ArgOption().setShortKey('a').setLongKey("A").setUsage(ArgsParser.E_Usage.KEY)
        };

        ArgsParser.ArgumentOptionException exception = assertThrows(ArgsParser.ArgumentOptionException.class,
                () -> new ArgsParser(makeProgrammeDetails(), options));

        System.out.println(exception.toString());
    }

    @Test
    void failDuplicateKeys() {
        ArgsParser.ArgOption[] options = new ArgsParser.ArgOption[] {
                new ArgsParser.ArgOption().setShortKey('a').setLongKey("A1").setUsage(ArgsParser.E_Usage.KEY),
                new ArgsParser.ArgOption().setShortKey('a').setLongKey("A2").setUsage(ArgsParser.E_Usage.KEY)
        };

        ArgsParser.ArgumentOptionException exception = assertThrows(ArgsParser.ArgumentOptionException.class,
                () -> new ArgsParser(makeProgrammeDetails(), options));

        System.out.println(exception.toString());
    }

    @Test
    void passComplexOptions() {
        new ArgsParser(makeProgrammeDetails(), colColorizeOptions());
    }

    @Test
    void pareArgs() {

    }

    @Test
    void isPassed() {
    }

    @Test
    void isShortPassed() {
    }

    @Test
    void isLongPassed() {
    }

    @Test
    void getArgument() {
    }

    @Test
    void getShortArgument() {
    }

    @Test
    void getLongPassed() {
    }

/* Target help output.
===================================================================================================
======================================= Set Console Colours =======================================
===================================================================================================
This program set the colours used by this console.
By Max Whitehouse, version 1.0.0.


  -b, --Set-Background                 This command sets the background colour of the console using
									   an RGB 0-255 triplet.
									   Usage: Key-value pair.
									   Example: ColColorize ... -b (0,0,0) ...
									   Example: ColColorize ... -Set-Background=(0,0,0) ...

  -t, --Set-Text                       This command sets the text colour if the console using an RGB
									   0-255 triplet.
                                       Usage: Key-value pair.
									   Example: ColColorize ... -t {value} ...
									   Example: ColColorize ... -Set-Text={value} ...

  --Use-Defaults                       This command tells the console revert to its default colour
									   scheme. This should be used on its own.
									   Usage: Key.
									   Example: ColColorize --Use-Defaults

  [SPACE DELIMITED LIST]               This will take the path to json files and read a "Set
								       Console Colours" configuration file.
									   Usage: List, a space delimited list of values at the end of
									   the command.
									   Example: ColColorize ... {value} {value} {value}

*/
    @Test
    void getHelpText() {
    }



    private enum EnumArgOptions implements ArgsParser.EnumOptions {

        BACKGROUND (new ArgsParser.ArgOption()
                .setShortKey('b')
                .setLongKey("Set-Background")
                .setUsage(ArgsParser.E_Usage.KEY_VALUE)
                .setDescription("This command sets the background colour of the console using an RGB 0-255 triplet.")
                .setShortValueExample("(0,0,0)")
                .setLongKeyValueExample("(0,0,0)")),

        TEXT (new ArgsParser.ArgOption()
                .setShortKey('t')
                .setLongKey("Set-Text")
                .setUsage(ArgsParser.E_Usage.KEY_VALUE)
                .setDescription("This command sets the text colour if the console using an RGB 0-255 triplet.")),

        RESET (new ArgsParser.ArgOption()
                .setLongKey("Use-Defaults")
                .setUsage(ArgsParser.E_Usage.KEY)
                .setDescription("This command tells the console revert to its default colour scheme. This should be used on its own.")),

        CONFIGS (new ArgsParser.ArgOption()
                .setUsage(ArgsParser.E_Usage.LIST)
                .setDescription("This will take the path to json files and read a \"Set Console Colours\" configuration file."));

        private final ArgsParser.ArgOption option;

        EnumArgOptions(ArgsParser.ArgOption option) {
            this.option = option;
        }

        public ArgsParser.ArgOption get() {
            return option;
        }

    }

}