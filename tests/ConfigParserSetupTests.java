import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserConfigParser {

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

    enum ShortLongKey implements ArgsParser.EnumOptions {
        FAIL(new ArgsParser.ArgOption().setLongKey("A").setUsage(ArgsParser.E_Usage.KEY));

        ArgsParser.ArgOption option;
        ShortLongKey(ArgsParser.ArgOption option) {
            this.option = option;
        }

        @Override
        public ArgsParser.ArgOption get() {
            return option;
        }
    }



    @Test
    void constructor_fail_if_programme_details_null() {
        assertThrows(
                NullPointerException.class,
                () -> new ArgsParser(null, (ArgsParser.ArgOption[]) null)
        );

        assertThrows(
                NullPointerException.class,
                () -> new ArgsParser(new ArgsParser.ProgrammeDetails(), (ArgsParser.ArgOption[]) null)
        );
    }

    @Test
    void fail_if_programme_name_is_empty() {
        assertThrows(
                ArgsParser.ArgumentOptionException.class,
                () -> new ArgsParser(
                        new ArgsParser.ProgrammeDetails().setProgrammeName(null),
                        new ArrayList<>()
                )
        );

        assertThrows(
                ArgsParser.ArgumentOptionException.class,
                () -> new ArgsParser(
                        new ArgsParser.ProgrammeDetails().setProgrammeName(""),
                        new ArrayList<>()
                )
        );
    }

    @Test
    void constructor_pass_if_empty_and_command_set() {
        new ArgsParser(makeProgrammeDetails(), new ArrayList<>());
    }

    @Test
    void constructor_pass_enum_class() {
        new ArgsParser(makeProgrammeDetails(), EnumArgOptions.class);
    }

    @Test
    void constructor_fail_on_no_keys() {
        ArgsParser.ArgOption[] options = new ArgsParser.ArgOption[] {
                new ArgsParser.ArgOption()
        };

        ArgsParser.ArgumentOptionException exception = assertThrows(ArgsParser.ArgumentOptionException.class,
                () -> new ArgsParser(makeProgrammeDetails(), options));

        System.out.println(exception.toString());
    }

    @Test
    void constructor_fail_no_argument_usage() {
        ArgsParser.ArgOption[] options = new ArgsParser.ArgOption[] {
                new ArgsParser.ArgOption().setShortKey('a').setLongKey("A1")
        };

        ArgsParser.ArgumentOptionException exception = assertThrows(ArgsParser.ArgumentOptionException.class,
                () -> new ArgsParser(makeProgrammeDetails(), options));

        System.out.println(exception.toString());
    }

    @Test
    void constructor_fail_one_char_long_key() {
        assertThrows(
                ArgsParser.ArgumentOptionException.class,
                () -> new ArgsParser.ArgOption().setShortKey('a').setLongKey("A").setUsage(ArgsParser.E_Usage.KEY)
        );

        ExceptionInInitializerError e = assertThrows(
                ExceptionInInitializerError.class,
                () -> new ArgsParser(makeProgrammeDetails(), ShortLongKey.class)
        );

        assertSame(e.getCause().getClass(), ArgsParser.ArgumentOptionException.class);
    }

    @Test
    void constructor_fail_duplicate_keys() {
        ArgsParser.ArgOption[] options = new ArgsParser.ArgOption[] {
                new ArgsParser.ArgOption().setShortKey('a').setLongKey("A1").setUsage(ArgsParser.E_Usage.KEY),
                new ArgsParser.ArgOption().setShortKey('a').setLongKey("A2").setUsage(ArgsParser.E_Usage.KEY)
        };

        ArgsParser.ArgumentOptionException exception = assertThrows(ArgsParser.ArgumentOptionException.class,
                () -> new ArgsParser(makeProgrammeDetails(), options));

        System.out.println(exception.toString());
    }

    @Test
    void constructor_pass_complex_options() {
        new ArgsParser(makeProgrammeDetails(), colColorizeOptions());
    }

}
