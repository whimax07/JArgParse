// By Max Whitehouse.

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static java.lang.System.exit;

/**
 * <b>ArgParser</b> provides a minimal command line parsing utility to allow you to build a programme that takes command line
 * inputs quickly. It is contained in one file to allow easy copying into a project and only uses very basic standard
 * library imports.<br>
 * <br>
 * It has 3 main selling points.
 * <ol>
 *     <li>A single file with no dependence outside of the standard library.</li>
 *     <li>Automatically generated help and information dialog.</li>
 *     <li>Enum indexing into {@link ArgsParser} to get the results of the parse if an enum was passed to it's
 *     constructor.</li>
 * </ol>
 *
 * <br>
 * To parse a command line defined the arguments/options you would like your programme to take using {@link ArgOption}
 * and a few details of your programme using {@link ProgrammeDetails}. Pass both of these to a contributor of this
 * class. <br><br>
 *
 * Once you have an instance of {@link ArgsParser}, pass the string array you are passed in a main function to the
 * {@link ArgsParser#pareArgs(String[])} and read the results from one of the public methods of {@link ArgsParser}. <br>
 * <br>
 *
 * Parsing a command line should only throw two types of run time error. They are {@link ArgumentOptionException} for a
 * problem with the users configuration and {@link ParseArgumentException} for a problem with the usage of an argument
 * or a failure to parse. <br><br>
 *
 * <b>Example Usage: </b><br>
 *
 * <pre> {@code
 * public int main(String[] args) {
 *     // Using the contractor that takes the enum class so we can index into
 *     // the results with the enum.
 *     ArgParser argParser = new ArgParser(makeProgrammeDetails(), EnumArgOptions.class);
 *
 *     // Pass the command line to be parsed.
 *     argParse.parseArgs(args);
 *
 *     // The rest of your code that uses what the user passed on the command line.
 *     if (argParser.isPassed(EnumArgOptions.OPTION1)) {
 *         // Do somethings.
 *     }
 *     ...
 * }
 *
 * private static ArgParser.ProgrammeDetails makeProgrammeDetails() {
 *     // Construct and return an instance of ProgrammeDetails.
 * }
 *
 * enum EnumArgOptions implements EnumOptions {
 *     OPTION1 (new ArgOption(...)),
 *     ... ;
 *
 *     private ArgOption option;
 *
 *     EnumArgOptions(ArgOption option) {
 *         this.option = option;
 *     }
 *
 *     public ArgOption get() {
 *         return option;
 *     }
 * }
 * }</pre>
 */
@SuppressWarnings("unused")
public class ArgsParser {

    // Parser things.
    private final ProgrammeDetails programmeDetails;

    private final ArrayList<ArgOption> argOptions;

    private final HashMap<String, ArgOption> keyMap = new HashMap<>();

    private ArgOption optionListArg = null;

    private final String help;

    private static final String[] HELP_FLAGS = new String[] {"-h", "--help", "--Help"};

    private boolean parseErrorsDisplayStackTrace = true;

    // Input things.
    private String[] rawInputs;

    private ArgReceived listArg = null;

    private final HashMap<ArgOption, ArgReceived> optionResultMap = new HashMap<>();

    private final HashMap<Character, ArgReceived> shortResultMap = new HashMap<>();

    private final HashMap<String, ArgReceived> longResultMap = new HashMap<>();

    private ArgReceived currentKeyPair;

    private ArgReceived lastKeyPair;

    private boolean expectingKey = false;



    /**
     * This is the recommended constructor. It is the same as
     * {@link ArgsParser#ArgsParser(ProgrammeDetails, ArgOption[])} and
     * {@link ArgsParser#ArgsParser(ProgrammeDetails, ArrayList)} apart from adding the option to index into the
     * results of the parse using the enum passed to the constructor. <br><br>
     *
     * The enum this contractor takes should implement {@link EnumOptions} and look something like this: <br>
     * <pre>{@code
     *  enum EnumArgOptions implements EnumOptions {
     *      OPTION1 (new ArgOption(...)),
     *      ... ;
     *
     *      private ArgOption option;
     *
     *      EnumArgOptions(ArgOption option) {
     *          this.option = option;
     *      }
     *
     *      public ArgOption get() {
     *          return option;
     *      }
     *  }
     * }</pre>
     */
    public <E extends Enum<E> & EnumOptions> ArgsParser(ProgrammeDetails programmeDetails, Class<E> enumArgOptions) {
        this(programmeDetails, convertEnumToOptionsList(enumArgOptions));
    }

    private static <E extends Enum<E> & EnumOptions> ArrayList<ArgOption> convertEnumToOptionsList(
            Class<E> enumArgOptions) {
        ArrayList<ArgOption> argOptions = new ArrayList<>();
        for (EnumOptions enumOption : enumArgOptions.getEnumConstants()) {
            argOptions.add(enumOption.get());
        }
        return argOptions;
    }

    /**
     * Equivalent to {@link ArgsParser#ArgsParser(ProgrammeDetails, ArrayList)}. Using this constructor means you won't
     * get the automatic enum binding to the parse results. Everything else is the same as
     * {@link ArgsParser#ArgsParser(ProgrammeDetails, Class)}.
     */
    public ArgsParser(ProgrammeDetails programmeDetails, ArgOption... argOptions) {
        this(programmeDetails, new ArrayList<>(Arrays.asList(argOptions)));
    }

    /**
     * Equivalent to {@link ArgsParser#ArgsParser(ProgrammeDetails, ArgOption[])}. Using this constructor means you
     * won't get the automatic enum binding to the parse results. Everything else is the same as
     * {@link ArgsParser#ArgsParser(ProgrammeDetails, Class)}.
     */
    public ArgsParser(ProgrammeDetails programmeDetails, ArrayList<ArgOption> argOptions) {
        if (programmeDetails == null) {
            throw new NullPointerException("programmeDetails cannot be null.");
        }

        if (argOptions == null) {
            throw new NullPointerException("argOptions cannot be null.");
        }

        if (programmeDetails.getCommandName() == null || programmeDetails.getCommandName().isEmpty()) {
            throw new ArgumentOptionException("The command mnemonic/name must be set.");
        }

        this.programmeDetails = programmeDetails;
        this.argOptions = argOptions;
        for (ArgOption argOption : argOptions) {
            validateOptions(argOption);
            buildKeyMap(argOption);
        }

        help = new HelpBuilder().buildHelp();
    }

    private void validateOptions(ArgOption argOption) {
        if (argOption.shortKey == '\0' && argOption.longKey.isEmpty() && argOption.usage != E_Usage.LIST) {
            String message = "Nether a short or long key have been provided for the following Argument. \n"
                    + "Argument option: " + argOption;
            throw new ArgumentOptionException(message);
        }

        if (argOption.longKey.length() == 1) {
            throw new ArgumentOptionException("Long keys should be at least 2 charters long. \n"
                    + "Argument option: " + argOption);
        }

        if (argOption.usage == null) {
            throw new ArgumentOptionException("The usage of an argument option must be set. \n"
                    + "Argument option: " + argOption);
        }

        checkForRepeatKeys(argOption);

        identifyUsageList(argOption);
    }

    private void checkForRepeatKeys(ArgOption argOption) {
        boolean dupeShortKey = keyMap.containsKey(String.valueOf(argOption.shortKey));
        boolean dupeLongKey = keyMap.containsKey(argOption.longKey);

        if (dupeShortKey || dupeLongKey) {
            ArgOption argShort = keyMap.get(String.valueOf(argOption.shortKey));
            ArgOption argLong = keyMap.get(argOption.longKey);

            String message;
            if (dupeShortKey && dupeLongKey) {
                message = "Arguments share a short and long key. \n" + "Option in conflict: " + argOption
                        + "\nShort key: " + argLong + "\nLong key: " + argShort;
            } else if (dupeShortKey) {
                message = "Arguments share a short key. \n" + "Option in conflict: " + argOption
                        + "\nShort key: " + argShort;
            } else {
                message = "Arguments share a long key. \n" + "Option in conflict: " + argOption
                        + "\nLong key: " + argLong;
            }
            throw new ArgumentOptionException(message);
        }
    }

    private void identifyUsageList(ArgOption argOption) {
        if (argOption.usage == E_Usage.LIST) {
            if (optionListArg != null) {
                throw new ArgumentOptionException("More than one list argument has been set. \n"
                        + "Options 1: " + optionListArg + "\n" + "Options 2: " + argOption);
            }
            optionListArg = argOption;
        }
    }

    private void buildKeyMap(ArgOption argOption) {
        if (argOption.shortKey != '\0') {
            keyMap.put(String.valueOf(argOption.shortKey), argOption);
        }

        if (!argOption.longKey.isEmpty()) {
            keyMap.put(argOption.longKey, argOption);
        }
    }



    /**
     * When a parse error happens a Runtime Error is generated and thrown with a message about what caused
     * the error. If this is true stack trace is included. If this is false no stack trace is included. This may be
     * desirable for a production release. <br>
     * <br>
     * {@code default = true;}
     */
    public void setParseErrorsDisplayStackTrace(boolean displayStackTrace) {
        parseErrorsDisplayStackTrace = displayStackTrace;
    }



    /**
     * Pass the string array parameter of the main function to this method to parse the command line. Then use this
     * classes other public methods to interrogate the results.
     */
    public void pareArgs(String[] commandLineArgs) {
        rawInputs = commandLineArgs;
        checkForHelpRequest();
        initParseState();
        parseInputs();
    }

    private void checkForHelpRequest() {
        // Note(Max): I still prefer this over streams.
        for (String input : rawInputs) {
            for (String helpFlag : HELP_FLAGS) {
                if (input.equals(helpFlag)) {
                    System.out.println(help);
                    exit(0);
                }
            }
        }
    }

    private void initParseState() {
        if (optionListArg != null) {
            listArg = new ArgReceived(optionListArg);
        }

        // This can be true as pos args are treated as keys.
        expectingKey = true;
    }



    private void parseInputs() {
        for (String rawInput : rawInputs) {
            if (expectingKey) {
                getKey(rawInput);
                continue;
            }

            // Expecting a value.
            checkLooksLikeAValue(rawInput);
            assignValueAndCycleSearch(rawInput);
        }

        // There is only one case where we should exit the loop while not expecting to receive a Key next. That is when
        // we are receiving values for the listArg.
        if (!expectingKey && currentKeyPair != listArg) {
            throw new ParseArgumentException("All input was parsed and a value was still expected."
                    + ((currentKeyPair == null) ? "" : "\nExpected Value Owner: " + currentKeyPair + "."));
        }
    }

    private void getKey(String rawInput) {
        if (rawInput.startsWith("--")) {
            String trimmedInput = rawInput.substring("--".length());
            parseKey(trimmedInput, true);
            return;
        }

        if (rawInput.startsWith("-")) {
            String trimmedInput = rawInput.substring("-".length());
            parseKey(trimmedInput, false);
            return;
        }

        if (listArg == null) {
            throw new ParseArgumentException("A key was expected. Check for spaces. \nReceived: " + rawInput + "\n");
        }
        // This must be the first positional arg.
        listArg.addValue(rawInput);
        currentKeyPair = listArg;
        expectingKey = false;
    }

    private void parseKey(String input, boolean isLongKey) {
        // Get the key and a value. Value maybe empty. This protects the short key from malformed name value pairs.
        String[] splitInput = input.split("=", 2);

        String key = splitInput[0];

        ArgOption argOption = keyMap.get(key);
        ArgReceived argReceived = optionResultMap.computeIfAbsent(argOption, ArgReceived::new);


        if (argOption == null) {
            throw new ParseArgumentException("No key match.\n"
                    + "Passed Key: " + key + ".\n"
                    + "Input: " + input + ".");
        }

        if (!isLongKey && key.equals(argOption.longKey)) {
            throw new ParseArgumentException("A long key has been passed with only one dash this effects argument formatting.\n"
                    + "Please add a dash.\n"
                    + "Key: " + key + ".\n"
                    + "Input: " + Arrays.toString(rawInputs));
        } else if (isLongKey && key.charAt(0) == argOption.shortKey) {
            throw new ParseArgumentException("A short key has been passed with two dash this effects argument formatting.\n"
                    + "Please remove the extra dash.\n"
                    + "Key: " + key + ".\n"
                    + "Input: " + Arrays.toString(rawInputs));
        }

        if (!expectingKey) {
            throw new ParseArgumentException("New key received while not expecting a new key.\n"
                    + "This is likely due to a missing key-value missing a value.\n"
                    + "New Key: " + key + ".\n"
                    + "Input: " + input + ".");
        }

        if (!argReceived.getValues().isEmpty() && !argOption.isRepeatable()) {
            String errorStart = "An argument has been used multiply times that should only be used once.\n";
            String errorMid = "";
            String errorEnd = "Short Key: '" + argOption.getShortKey() + "', Long Key: \"" + argOption.getLongKey() + "\".\n"
                    + "Input: " + Arrays.toString(rawInputs);

            if (argOption.getUsage() == E_Usage.KEY_VALUE) {
                String shortError = "First Usage Value: " + argReceived.getValue() + ".\n";
                String longError = "Second Usage: " + input + "First Usage Value: " + argReceived.getValue() + ".\n";

                errorMid = (isLongKey) ? longError : shortError;
            }

            throw new ParseArgumentException(errorStart + errorMid + errorEnd);
        }

        expectingKey = false;

        switch (argOption.getUsage()) {
            case KEY:
                if (isLongKey) {
                    checkLongKeyKeyErrors(splitInput, argOption);
                } else {
                    checkShortKeyKeyErrors(splitInput, argOption);
                }
                updateResultMaps(argReceived);
                argReceived.addValue("");
                expectingKey = true;
                lastKeyPair = argReceived;
                currentKeyPair = null;
                break;

            case KEY_VALUE:
                if (isLongKey) {
                    checkLongKeyKeyValueErrors(splitInput, argOption);
                    updateResultMaps(argReceived);
                    argReceived.addValue(splitInput[1]);
                    expectingKey = true;
                    lastKeyPair = argReceived;
                    currentKeyPair = null;
                } else {
                    checkShortKeyKeyValueErrors(splitInput, argOption);
                    expectingKey = false;
                    lastKeyPair = currentKeyPair;
                    currentKeyPair = argReceived;
                }
                break;

            case LIST:
                throw new ArgsParser.ParseArgumentException("During command line key parsing, a key of type list was "
                        + "found.");
        }
    }

    private void checkLongKeyKeyErrors(String[] splitInput, ArgOption argOption) {
        if (splitInput.length >= 2) {
            String message = "A long key was used as a pair with a value instead of a lone key. Good format: ... --" + argOption.longKey + "... \n"
                    + "Current parse: " + Arrays.toString(splitInput) + "\n"
                    + "Key options: " + argOption;
            throw new ParseArgumentException(message);
        }
    }

    private void checkLongKeyKeyValueErrors(String[] splitInput, ArgOption argOption) {
        if (splitInput.length < 2) {
            String message = "A long key for a key-value pair was used without the value. Good format: ... --" + argOption.longKey + "=value ... \n"
                    + "Current parse: " + Arrays.toString(splitInput) + "\n"
                    + "Key-value pair options: " + argOption;
            throw new ParseArgumentException(message);
        }
    }

    private void checkShortKeyKeyErrors(String[] splitInput, ArgOption argOption) {
        if (splitInput.length >= 2) {
            String message = "A short key was used with an equals sign."
                    + "Current parse: " + Arrays.toString(splitInput) + "\n"
                    + "Key options: " + argOption;
            throw new ParseArgumentException(message);
        }
    }

    private void checkShortKeyKeyValueErrors(String[] splitInput, ArgOption argOption) {
        if (splitInput.length >= 2) {
            String message = "A short key was used as a pair with a value join via an equals. Good format: ... -" + argOption.shortKey + " value ..."
                    + "Current parse: " + Arrays.toString(splitInput) + "\n"
                    + "Key-value pair options: " + argOption;
            throw new ParseArgumentException(message);
        }
    }

    private void updateResultMaps(ArgReceived argReceived) {
        ArgOption option = argReceived.option;
        optionResultMap.put(option, argReceived);

        if (option.shortKey != '\0') {
            shortResultMap.put(option.shortKey, argReceived);
        }

        if (!option.longKey.isEmpty()) {
            longResultMap.put(option.longKey, argReceived);
        }
    }

    private void checkLooksLikeAValue(String rawInput) {
        // Note(Max): I have done this type of error detection a bunch, and it doesn't tell me why this is an error. I
        //  think it is just bad.
        if (rawInput.startsWith("-")) {
            String errorMessage =
                    "Expected a value, got a key. Look for spaces and check if a key can except a value.\n"
                            + "Last group: " + lastKeyPair + "\n"
                            + "Malformed value: " + rawInput;
            throw new ParseArgumentException(errorMessage);
        }
    }

    private void assignValueAndCycleSearch(String value) {
        if (currentKeyPair == null) {
            throw new ParseArgumentException("A value was passed while no key has been parsed to attach the value to.\n"
                    + "Passed Value: " + value + "."
                    + ((lastKeyPair == null) ? "" : "\nLast Argument Parsed: " + lastKeyPair + "."));
        }

        // Note(Max): Once the first argument assigned to `listArg` is passed the all subsequent values should be a part
        // of the listArg value.
        if (listArg != null && listArg == currentKeyPair) {
            listArg.addValue(value);
            expectingKey = false;
            return;
        }

        updateResultMaps(currentKeyPair);
        currentKeyPair.addValue(value);
        lastKeyPair = currentKeyPair;
        currentKeyPair = null;
        expectingKey = true;
    }



    /**
     * @param option An enum value where the enum class implements an {@link EnumOptions}.
     *
     * @return True if the user passed the option at least once.
     */
    public <E extends Enum<E> & ArgsParser.EnumOptions> boolean isPassed(E option) {
        return isPassed(option.get());
    }

    /**
     * @return True if the user passed the option at least once.
     */
    public boolean isPassed(ArgOption option) {
        if (option == null) {
            throw new NullPointerException("The option received to look for a result was null.");
        }

        return optionResultMap.containsKey(option) && optionResultMap.get(option) != null;
    }

    /**
     * @param key A short or long key that is bound to an {@link ArgOption} for an argument you have configured. It is
     *            not checked if the key is bound to an option. Therefore, it is recommended that you use a version of
     *            this function that takes an {@link ArgOption} or a {@link EnumOptions} enum.
     *
     * @return True if the user used the argument the key is associated with at least once. False if the key is not used
     * or if the key is not bound to an option.
     */
    public boolean isPassed(String key) {
        boolean isShortKey = key.length() == 1;
        boolean hasShortKey = isShortKey && isShortPassed(key.charAt(0));
        return hasShortKey || longResultMap.containsKey(key);
    }

    /**
     * @param key Is not checked to see if it is bound to an option. Therefore, it is suggested that you use a version
     *            of this function that takes an {@link ArgOption} or a {@link EnumOptions} enum.
     *
     * @return True if the user used the argument the short key is associated with at least once. False if the key is
     * not used or if the key is not bound to an option.
     */
    public boolean isShortPassed(char key) {
        return shortResultMap.containsKey(key);
    }

    /**
     * @param longKey Is not checked to see if it is bound to an option. Therefore, it is suggested that you use a
     *                version of this function that takes an {@link ArgOption} or a {@link EnumOptions} enum.
     *
     * @return True if the user used the argument the long key is associated with at least once. False if the key is not
     * used or if the key is not bound to an option.
     */
    public boolean isLongPassed(String longKey) {
        return longResultMap.containsKey(longKey);
    }

    /**
     * @return If the option the key is a part of was used it will return a container with the passed value or values.
     * Null is returned if option was not passed.
     */
    public <E extends Enum<E> & ArgsParser.EnumOptions> ArgReceived getResult(E option) {
        return optionResultMap.get(option.get());
    }

    /**
     * @param key A short or long key that is bound to an {@link ArgOption} for an option you have configured.
     *
     * @return If the option the key is a part of was used it will return a container with the passed value or values.
     * Null is returned if option was not passed.
     *
     * @throws ArgumentOptionException If {@code key} is not bound to an option.
     */
    public ArgReceived getResult(String key) {
        if (!keyMap.containsKey(key)) {
            throw new ArgumentOptionException("The key (\"" + key + "\") is not bound to an option.");
        }
        if (longResultMap.containsKey(key)) {
            return longResultMap.get(key);
        }

        boolean isShortKey = key.length() == 1;
        if (isShortKey && shortResultMap.containsKey(key.charAt(0))) {
            return shortResultMap.get(key.charAt(0));
        }

        return null;
    }

    /**
     * @param shortKey Should be bound to an {@link ArgOption} this is not checked. Therefore, it is suggested that you
     *                 use a version of this function that takes an {@link ArgOption} or a {@link EnumOptions} enum.
     *
     * @return If the option the key is a part of was used it will return a container with the passed value or values.
     * Null is returned if option was not passed. If the key is not bound null is returned.
     */
    public ArgReceived getResultShort(char shortKey) {
        return shortResultMap.get(shortKey);
    }

    /**
     * {@code longKey} is not checked to see if it is bound to an option. Therefore, it is suggested that you use a
     * version of this function that takes an {@link ArgOption} or a {@link EnumOptions} enum. <br>
     * <br>
     * Returns a container with the passed value or values if the option the key is a part of was used.
     * Null is returned if option was not passed. If the key is not bound null is returned.
     */
    public ArgReceived getResultLong(String longKey) {
        return longResultMap.get(longKey);
    }

    /**
     * Returns the automatically generated help text.
     */
    public String getHelpText() {
        return help;
    }



    /**
     * The user should instantiate this class for each argument/option they would like there programme to accept. The instances are
     * then passed to a constructor of {@link ArgsParser}. Alternately the user can instantiate instances of this class inside
     * the fields of an enum implementing {@link EnumOptions} and then pass the {@code .class} of that enum to {@link
     * ArgsParser#ArgsParser(ProgrammeDetails, Class)}, see the constructor for details. <br>
     * <br>
     * The user can configure: <br>
     * <ul>
     *     <li> The short key, {@link ArgOption#shortKey}. </li>
     *     <li> The long key, {@link ArgOption#longKey}. </li>
     *     <li> The format to use the argument, {@link ArgOption#usage}. </li>
     *     <li> An example of how to use the short key, {@link ArgOption#shortValueExample}. </li>
     *     <li> An example of how to use the long key, {@link ArgOption#longValueExample}. </li>
     *     <li> An example of how to use the argument if it is a list, {@link ArgOption#listExample}. </li>
     *     <li> A description of what the argument does and what it is used for, {@link ArgOption#description}. </li>
     *     <li> Whether the argument should be passed on it's own, {@link ArgOption#useOnItsOwn}. </li>
     *     <li> Whether the argument can be used more than once, {@link ArgOption#repeatable}. </li>
     * </ul>
     *
     * <br>
     * There are several reversed keys to print the help, they are saved in {@link ArgsParser#HELP_FLAGS}. They print
     * the help and stop excursion.
     */
    public static class ArgOption {

        /**
         * The short key used for an argument. Should be a single char.
         * <br>
         * Example: 'w' -> '-w'
         */
        private char shortKey = '\0';

        /**
         * The long key used for an argument. Should be more than one char long.
         * <br>
         * {@code Example: 'Window-Name' -> '--Window-Name'}
         */
        private String longKey = "";

        /**
         * The way to use the argument. See {@link E_Usage} for a description of the options.
         */
        private E_Usage usage = null;

        /**
         * An example usage of the short key that will be printed for the user if they use one of the help flags and: <br>
         * 1) The short key has been set. <br>
         * 2) {@link ArgOption#usage} is {@link E_Usage#KEY_VALUE}. <br>
         * <br>
         * {@code Example: '(1, 2, 3)' -> 'ColColorize ... -t (1,2,3) ...'}
         */
        private String shortValueExample = "{value}";

        /**
         * An example usage of the long key that will be printed for the user if they use one of the help flags and: <br>
         * 1) The short key has been set. <br>
         * 2) {@link ArgOption#usage} is {@link E_Usage#KEY_VALUE}. <br>
         * <br>
         * {@code Example: 'Bright Blue' -> 'ColColorize ... -Text-Colour="Bright Blue" ...'}
         */
        private String longValueExample = "{value}";

        /**
         * An example usage of the space delimited list positional argument that will be printed for the user if they
         * use one of the help flags.
         * <br>
         * {@code Example: 'Max, Maximus and Maximilian' -> 'BuildProfiles ... Max Maximus Maximilian'}
         */
        private String listExample = "{value} {value} {value}";

        /**
         * A description of what the argument is used for in your programme.
         */
        private String description = "";

        /**
         * If true then the option should not be passed with any other option, if it is passed with another argument a
         * {@link ParseArgumentException} is thrown. If false the user can pass this argument with others. An example
         * argument with this set to true is {@code --help}.
         */
        private boolean useOnItsOwn = false;

        /**
         * If true the user can pass this argument more than once. If false then should the user passes this argument
         * more than once an {@link ParseArgumentException} will be thrown.
         */
        private boolean repeatable = false;



        /**
         * Return the long key if the argument has one set if not the short key is returned.
         */
        public String getName() {
            return (longKey.isEmpty()) ? String.valueOf(shortKey) : longKey;
        }

        /**
         * Return the short key, if not set will be {@code '\0'}. See {@link ArgsParser.ArgOption#shortKey}.
         */
        public char getShortKey() {
            return shortKey;
        }

        /**
         * Sets the short key. See {@link ArgsParser.ArgOption#shortKey}.
         */
        public ArgOption setShortKey(char shortKey) {
            this.shortKey = shortKey;
            return this;
        }

        /**
         * Return the long key, if not set will be empty. See {@link ArgsParser.ArgOption#longKey}.
         */
        public String getLongKey() {
            return longKey;
        }

        /**
         * Sets the long key. See {@link ArgsParser.ArgOption#longKey}.
         *
         * @throws ArgumentOptionException if the long key is not at least 2 chars long.
         */
        public ArgOption setLongKey(String longKey) {
            if (longKey.length() < 2) {
                throw new ArgumentOptionException("Long keys should be at least 2 charters long. \n");
            }
            this.longKey = longKey;
            return this;
        }

        /**
         * Returns the usage. See {@link ArgsParser.ArgOption#usage}.
         */
        public E_Usage getUsage() {
            return usage;
        }

        /**
         * Sets the way the argument is used. See {@link ArgsParser.ArgOption#usage}.
         */
        public ArgOption setUsage(E_Usage usage) {
            this.usage = usage;
            return this;
        }

        /**
         * Returns the example that will be used for the short key in the generated help text if the short key is set
         * and the argument has usage, KEY_VALUE. <br>
         * <br>
         * See {@link ArgOption#shortValueExample}.
         */
        public String getShortValueExample() {
            return shortValueExample;
        }

        /**
         * Sets the example that will be used for the short key in the generated help text if the short key is set and
         * the argument has usage, {@link E_Usage#KEY_VALUE}. <br>
         * <br>
         * {@code default = "{value}";} <br>
         * <br>
         * See {@link ArgOption#shortValueExample}.
         */
        public ArgOption setShortValueExample(String shortValueExample) {
            this.shortValueExample = shortValueExample;
            return this;
        }

        /**
         * Returns the example that will be used for the long key in the generated help text if the long key is set. <br>
         * <br>
         * See {@link ArgOption#longValueExample}.
         */
        public String getLongValueExample() {
            return longValueExample;
        }

        /**
         * Sets the example that will be used for the long key in the generated help text if the long key is set and the
         * argument has usage, {@link E_Usage#KEY_VALUE}. <br>
         * <br>
         * {@code default = "{value}";} <br>
         * <br>
         * See {@link ArgOption#longValueExample}.
         */
        public ArgOption setLongValueExample(String longValueExample) {
            this.longValueExample = longValueExample;
            return this;
        }

        /**
         * Returns the example that will be included in the generated help if this argument has usage,
         * {@link E_Usage#LIST}, a space delimited list. <br>
         * <br>
         * See {@link ArgOption#listExample}.
         */
        public String getListExample() {
            return listExample;
        }

        /**
         * Set the example that will be included in the generated help if this argument has usage, {@link E_Usage#LIST},
         * a space delimited list. <br>
         * <br>
         * {@code default = "{value} {value} {value}";} <br>
         * <br>
         * See {@link ArgOption#listExample}.
         */
        public ArgOption setListExample(String listExample) {
            this.listExample = listExample;
            return this;
        }

        /**
         * Returns the description that will be included in the generated help for what this argument is used for and
         * what it does.
         */
        public String getDescription() {
            return description;
        }

        /**
         * Set the description that will be included in the generated help for what this argument is used for and what
         * it does. <br>
         * <br>
         * {@code default = "";} <br>
         * <br>
         * See {@link ArgOption#description}.
         */
        public ArgOption setDescription(String description) {
            this.description = description;
            return this;
        }

        /**
         * Returns the boolean {@link ArgOption#useOnItsOwn}.
         */
        public boolean isUseOnItsOwn() {
            return useOnItsOwn;
        }

        /**
         * If this is true then the argument should not be passed with another
         * argument, if it is passed with any other arguments a {@link ParseArgumentException} is thrown. If it is set
         * to false the user can pass this argument with others. An example argument with this set to true is
         * {@code --help}. <br>
         * <br>
         * {@code default = false;} <br>
         * <br>
         * Sets {@link ArgOption#useOnItsOwn}.
         */
        public ArgOption setUseOnItsOwn(boolean useOnItsOwn) {
            this.useOnItsOwn = useOnItsOwn;
            return this;
        }

        /**
         * Returns the boolean {@link ArgOption#repeatable}.
         */
        public boolean isRepeatable() {
            return repeatable;
        }

        /**
         * If this is true the user can pass this argument more than once. If this is
         * set to false then if the user passes this argument more than once a {@link ParseArgumentException} will be
         * thrown. <br>
         * <br>
         * {@code default = false;} <br>
         * <br>
         * Sets {@link ArgOption#repeatable}.
         */
        public ArgOption setRepeatable(boolean repeatable) {
            this.repeatable = repeatable;
            return this;
        }



        @Override
        public String toString() {
            return "ArgOption{" +
                    "shortKey='" + shortKey + "', " +
                    "longKey=\"" + longKey + "\", " +
                    "usage=" + usage + ", " +
                    "shortValueExample=\"" + shortValueExample + "\", " +
                    "longKeyValueExample=\"" + longValueExample + "\", " +
                    "listExample=\"" + listExample + "\", " +
                    "description=\"" + description + "\"" +
                    "}";
        }

    }

    /**
     * This class holds any values passed by the user, {@link ArgReceived#values}, for a given argument,
     * {@link ArgReceived#option}.
     */
    public static class ArgReceived {

        /**
         * The argument used to pass {@link ArgReceived#values}.
         */
        private final ArgOption option;

        /**
         * The results array for {@link ArgReceived#option}. This will be empty if the argument is not passed. The size
         * of this array is the number of times the argument is passed.
         */
        private final ArrayList<String> values = new ArrayList<>();



        public ArgReceived(ArgOption option) {
            this.option = option;
        }



        private void addValue(String value) {
            values.add(value);
        }



        /**
         * Returns the argument that the results were passed with.
         */
        public ArgOption getOption() {
            return option;
        }

        /**
         * @return A list of length n, where n is the number of times the argument has been used. If the argument is a
         * key type then the string will be empty.
         */
        public ArrayList<String> getValues() {
            return values;
        }

        /**
         * If the argument is not used {@code null} is returned. <br>
         * <br>
         * If the argument is used at least once a string is returned. The string returned is based on the usage of the
         * argument. <br>
         * <ul>
         *     <li>{@link E_Usage#KEY}: an empty string.</li>
         *     <li>{@link E_Usage#KEY_VALUE}: the string passed with the first instance of the argument in the command
         *     passed.</li>
         *     <li>{@link E_Usage#LIST}: the first item in the list.</li>
         * </ul>
         */
        public String getValue() {
            return (values.isEmpty()) ? null : values.get(0);
        }



        @Override
        public String toString() {
            return "ArgReceived{Values=\"" + values + "\", Option=" + option + "}";
        }

    }

    /**
     * This class holds details about the programme and is mainly used by {@link HelpBuilder}.
     */
    public static class ProgrammeDetails {

        /**
         * This is the mnemonic used to call the programme. This field must be set by the user.
         */
        private String commandName = "";

        /**
         * This field is used in the help text. If this field is not set by the user commandName will be used instead.
         */
        private String programmeName = "";

        /**
         * This is a description of the programme that will is used in the help text.
         */
        private String programmeDescription = "";

        /**
         * The author or authors of the programme that is used in the help text.
         */
        private String author = "";

        /**
         * The version of the programme that is used in the help text.
         */
        private String version = "";



        /**
         * Returns the command mnemonic used in the help text. See {@link ProgrammeDetails#commandName}.
         */
        public String getCommandName() {
            return commandName;
        }

        /**
         * Sets the command mnemonic used to call the programme. This is used in the help text. See
         * {@link ProgrammeDetails#commandName}.
         */
        public ProgrammeDetails setCommandName(String commandName) {
            this.commandName = commandName;
            return this;
        }

        /**
         * Get the programme name. See {@link ProgrammeDetails#programmeName}.
         */
        public String getProgrammeName() {
            return programmeName;
        }

        /**
         * Sets the programme name, the proper name of the programme e.g. Google Chrome, used in the help text. If this
         * is not set {@link ProgrammeDetails#commandName} is used instead. See {@link ProgrammeDetails#programmeName}.
         */
        public ProgrammeDetails setProgrammeName(String programmeName) {
            this.programmeName = programmeName;
            return this;
        }

        /**
         * Gets the description of the programme that is used in the help text. See
         * {@link ProgrammeDetails#programmeDescription}.
         */
        public String getProgrammeDescription() {
            return programmeDescription;
        }

        /**
         * Sets the description of the programme that is used in the help text. See
         * {@link ProgrammeDetails#programmeDescription}.
         */
        public ProgrammeDetails setProgrammeDescription(String programmeDescription) {
            this.programmeDescription = programmeDescription;
            return this;
        }

        /**
         * Get the author or authors of the programme that are printed in the help text. See
         * {@link ProgrammeDetails#author}.
         */
        public String getAuthor() {
            return author;
        }

        /**
         * Sets the author or authors of the programme that are printed in the help text. See
         * {@link ProgrammeDetails#author}.
         */
        public ProgrammeDetails setAuthor(String author) {
            this.author = author;
            return this;
        }

        /**
         * Get the version that is printed in the help text. See {@link ProgrammeDetails#version}.
         */
        public String getVersion() {
            return version;
        }

        /**
         * Sets the version that is printed in the help text. See {@link ProgrammeDetails#version}.
         */
        public ProgrammeDetails setVersion(String version) {
            this.version = version;
            return this;
        }



        @Override
        public String toString() {
            return "ProgrammeDetails{" +
                    "commandName='" + commandName + "', " +
                    "programmeName=\"" + programmeName + "\", " +
                    "programmeDescription=" + programmeDescription + ", " +
                    "author=\"" + author + "\", " +
                    "version=\"" + version + "\"" +
                    "}";
        }

    }

    /**
     * This class builds the help/info message displayed if any of the arguments in {@link ArgsParser#HELP_FLAGS} are
     * passed.
     */
    private class HelpBuilder {

        public StringBuilder stringBuilder = new StringBuilder();

        // All measurements are mono-font cells.
        private int infoWidth;

        /**
         * Maximum number or total number of monospaced charters per line.
         */
        private static final int LINE_WIDTH = 100;

        /**
         * Minimum gap between the left and the right side of the name lines in the name box.
         */
        private static final int NAME_MARGIN = 5;

        /**
         * The starting width of the key column before it is expanded to fit longer long keys.
         */
        private static final int BASE_KEY_COL_WIDTH = 40;

        /**
         * The maximum extra space that can be used for longer long keys. If the width of key column exceeds the sum of
         * this and {@link HelpBuilder#BASE_KEY_COL_WIDTH} an error is thrown.
         */
        private static final int EXTRA_KEY_SPACE = 10;

        /**
         * The margin on the left-hand side of the key column.
         */
        private static final int LEFT_MARGIN_WIDTH = 2;

        /**
         * The minimum gap between the key column and the argument information.
         */
        private static final int KEY_DESCRIPTION_GAP = 3;

        /**
         * Default description for the special help argument.
         */
        private static final String HELP_FLAG_DESCRIPTION = "Use to print this help.";

        /**
         * The default "key" used in the key column for an argument with usage {@link E_Usage#LIST}.
         */
        private static final String LIST_USAGE_KEY = "[SPACE DELIMITED LIST]";

        /**
         * The usage text for an argument with usage {@link E_Usage#LIST}.
         */
        private static final String LIST_USAGE = "List, a space delimited list of values at the end of the command.";

        /**
         * The prefix that comes before each example block. See {@link ArgOption#shortValueExample} and
         * {@link ArgOption#longValueExample}.
         */
        private static final String EXAMPLE_PREFIX = "Example: ";



        public String buildHelp() {
            buildNameBox();
            buildProgrammeDescription();
            buildProgrammeDetail();
            buildOptionHelpBlocks();
            stringBuilder.append("\n");
            return stringBuilder.toString();
        }

        private void buildNameBox() {
            int nameSpace = LINE_WIDTH - (NAME_MARGIN * 2);

            String programmeName = (programmeDetails.programmeName.isEmpty())
                    ? programmeDetails.commandName : programmeDetails.programmeName;

            ArrayList<String> nameLines = lineWrapString(programmeName, nameSpace);

            boxProgrammeNameLines(nameLines);

            addLinesToHelpText(nameLines);
        }

        private void boxProgrammeNameLines(ArrayList<String> nameLines) {
            for (int i = 0; i < nameLines.size(); i++) {
                String line = nameLines.get(i);

                int leftSpace = (LINE_WIDTH - line.length()) / 2;
                int rightSpace = LINE_WIDTH - (leftSpace + line.length());

                String newLine = dupeString("=", leftSpace - 1) + " " + line
                        + " " + dupeString("=", rightSpace - 1);

                nameLines.set(i, newLine);
            }

            nameLines.add(0, dupeString("=", LINE_WIDTH));
            nameLines.add(dupeString("=", LINE_WIDTH));
        }

        private void buildProgrammeDescription() {
            String description = programmeDetails.programmeDescription;

            if (description.isEmpty()) {
                return;
            }

            addLinesToHelpText(lineWrapString(description, LINE_WIDTH));
        }

        private void buildProgrammeDetail() {
            String author = programmeDetails.author;
            String version = programmeDetails.version;

            if (!author.isEmpty()) {
                author = "By " + author;
                author += (version.isEmpty()) ? "." : ", ";
            }

            if (!version.isEmpty()) {
                String v = (author.isEmpty()) ? "Version " : "version ";
                version = v + version + ".";
            }

            String details = author + version;

            if (details.isEmpty()) {
                return;
            }

            addLinesToHelpText(lineWrapString(details, LINE_WIDTH));
        }

        private void buildOptionHelpBlocks() {
            // The fist option to be printed is help.

            handBuildHelpOptionBlock();

            // Note(Max): Have a temp variable so access to the parents classes private members isn't needed.
            // We pull listOption out so that we can make sure it is the last option printed in the help and has one
            // more new line.
            ArgOption listOption = null;

            for (ArgOption option : argOptions) {
                if (option.usage == E_Usage.LIST) {
                    listOption = option;
                    continue;
                }

                buildOptionBlock(option);
            }

            if (listOption != null) {
                stringBuilder.append("\n");
                buildOptionBlock(listOption);
            }
        }

        private void handBuildHelpOptionBlock() {
            stringBuilder.append("\n");

            StringBuilder keyList = new StringBuilder();

            for (String helpFlag : HELP_FLAGS) {
                keyList.append(helpFlag).append(", ");
            }

            keyList.delete(keyList.length() - 2, keyList.length() - 1);
            String keyLine = dupeString(" ", LEFT_MARGIN_WIDTH) + keyList;


            infoWidth = calcInfoWidth(keyLine);
            ArrayList<String> description = lineWrapString(HELP_FLAG_DESCRIPTION, infoWidth);

            ArrayList<String> example = lineWrapString(
                    EXAMPLE_PREFIX + programmeDetails.commandName + " " + HELP_FLAGS[0],
                    infoWidth
            );

            ArrayList<String> infoLines = new ArrayList<>();
            infoLines.addAll(description);
            infoLines.addAll(example);


            mergeAndIndent(keyLine, infoLines);

            addLinesToHelpText(infoLines);
        }

        private void buildOptionBlock(ArgOption option) {
            stringBuilder.append("\n");

            String keyLine = buildKeyLine(option);

            ArrayList<String> infoLines = buildInfo(option, keyLine);

            mergeAndIndent(keyLine, infoLines);

            addLinesToHelpText(infoLines);
        }

        private String buildKeyLine(ArgOption option) {
            if (option.usage == E_Usage.LIST) {
                return dupeString(" ", LEFT_MARGIN_WIDTH) + LIST_USAGE_KEY;
            }

            String keyLine = buildShortKeyString(option.shortKey);
            keyLine = buildLongKeyString(option.longKey, keyLine);
            return keyLine;
        }

        private String buildShortKeyString(char shortKey) {
            if (shortKey == '\0') {
                int keyWidth = "-x, ".length();
                return dupeString(" ", LEFT_MARGIN_WIDTH + keyWidth);
            }

            return dupeString(" ", LEFT_MARGIN_WIDTH) + "-" + shortKey + ", ";
        }

        private String buildLongKeyString(String longKey, String keyLine) {
            int spaceForLongKey = (BASE_KEY_COL_WIDTH + EXTRA_KEY_SPACE) - (keyLine.length());

            if ("--".length() + longKey.length() + KEY_DESCRIPTION_GAP > spaceForLongKey) {
                int maxLongKeyLength = spaceForLongKey - (2 + KEY_DESCRIPTION_GAP);
                throw new ArgumentOptionException("Long key is too long for the help block.\n" +
                        "With the current setting the charter limit for a long key is " + maxLongKeyLength + ".\n" +
                        "Long key: " + longKey);
            }

            // Note(Max): Reassigning to keyLine looks like it might be wrong.
            keyLine += "--" + longKey + dupeString(" ", KEY_DESCRIPTION_GAP);

            // This is where you would change the layout so that you only let the extra space be used a chunk at a time
            // rather than a space at a time I.E. use 5 spaces rather than 2.
            int extraSpace = keyLine.length() - BASE_KEY_COL_WIDTH;
            if (extraSpace > 0) {
                keyLine += dupeString(" ", extraSpace);
            }

            return keyLine;
        }

        private ArrayList<String> buildInfo(ArgOption option, String keyLine) {
            infoWidth = calcInfoWidth(keyLine);

            ArrayList<String> description = buildDescription(option);

            ArrayList<String> usageOption = buildUsage(option);

            ArrayList<String> examples = buildExamples(option);

            ArrayList<String> infoLines = new ArrayList<>();
            infoLines.addAll(description);
            infoLines.addAll(usageOption);
            infoLines.addAll(examples);
            return infoLines;
        }

        private int calcInfoWidth(String keyLine) {
            // Note(Max): I would be easy to argue for this to set the field and not return a value, but I like how it
            // looks when it returns a value, so I am going to leave it like this for the moment.
            int indentWidth = Math.max(keyLine.length(), BASE_KEY_COL_WIDTH);

            return LINE_WIDTH - indentWidth;
        }

        private ArrayList<String> buildDescription(ArgOption option) {
            if (option.description.isEmpty()) {
                return new ArrayList<>(0);
            }

            return lineWrapString(option.description, infoWidth);
        }

        private ArrayList<String> buildUsage(ArgOption option) {
            String usage = "";
            switch (option.usage) {
                case KEY: usage = "Key"; break;
                case KEY_VALUE: usage = "Key-value pair"; break;
                case LIST: usage = LIST_USAGE.replaceAll(".$", ""); break;
            }

            if (option.isRepeatable()) {
                usage += ", Repeatable";
            }

            if (option.isUseOnItsOwn()) {
                usage += ", Exclusive";
            }

            return lineWrapString("Usage: " + usage + ".", infoWidth);
        }

        private ArrayList<String> buildExamples(ArgOption option) {
            ArrayList<String> out = null;

            switch (option.usage) {
                case KEY:
                case KEY_VALUE:
                    out = buildValueExamples(option); break;
                case LIST: out = buildListExample(option); break;
            }

            return out;
        }

        private ArrayList<String> buildListExample(ArgOption option) {
            String ellipses = (option.useOnItsOwn) ? " " : " ... ";
            String exampleBuilder = EXAMPLE_PREFIX + programmeDetails.commandName + ellipses + option.getListExample();
            return lineWrapString(exampleBuilder, infoWidth);
        }

        private ArrayList<String> buildValueExamples(ArgOption option) {
            String commandName = programmeDetails.commandName;
            ArrayList<String> exampleLines = new ArrayList<>();

            String ellipses = (option.useOnItsOwn) ? " " : " ... ";

            if (option.shortKey != '\0') {
                String valueExample = (option.usage == E_Usage.KEY) ? "" : " " + option.getShortValueExample();

                String shortExample = EXAMPLE_PREFIX + commandName + ellipses + "-" + option.getShortKey()
                        + valueExample + ellipses;

                exampleLines.addAll(lineWrapString(shortExample, infoWidth));
            }

            if (!option.longKey.isEmpty()) {
                String valueExample = (option.usage == E_Usage.KEY) ? "" : "=" + option.getLongValueExample();

                String longExample = EXAMPLE_PREFIX + commandName + ellipses + "--" + option.getLongKey()
                        + valueExample + ellipses;

                exampleLines.addAll(lineWrapString(longExample, infoWidth));
            }

            return exampleLines;
        }

        private void mergeAndIndent(String keyLine, ArrayList<String> infoLines) {
            if (infoLines.size() >= 1) {
                String padding = dupeString(" ", LINE_WIDTH - (infoWidth + keyLine.length()));
                infoLines.set(0, keyLine + padding + infoLines.get(0));
            }

            for (int i = 1; i < infoLines.size(); i++) {
                String newLine = dupeString(" ", LINE_WIDTH - infoWidth) + infoLines.get(i);
                infoLines.set(i, newLine);
            }
        }



        private ArrayList<String> lineWrapString(String input, int lineWidth) {
            ArrayList<String> wrappedLines = new ArrayList<>();

            String[] inputWords = input.split(" ");
            StringBuilder lineBuilder = new StringBuilder(lineWidth);
            int lineLength = 0;

            for (String word : inputWords) {
                if (lineLength + word.length() <= lineWidth) {
                    lineBuilder.append(word).append(" ");
                    lineLength += 1 + word.length();
                    continue;
                }

                wrappedLines.add(lineBuilder.toString().trim());
                lineLength = word.length();
                lineBuilder = new StringBuilder(lineWidth);
                lineBuilder.append(word).append(" ");
            }

            wrappedLines.add(lineBuilder.toString().trim());
            return wrappedLines;
        }

        private String dupeString(String string, int count) {
            StringBuilder stringBuilder = new StringBuilder(string.length() * count);
            for (int i = 0; i < count; i++) {
                stringBuilder.append(string);
            }
            return stringBuilder.toString();
        }

        private void addLinesToHelpText(ArrayList<String> lines) {
            if (lines.isEmpty()) {
                return;
            }

            for (String line : lines) {
                stringBuilder.append(line).append(System.lineSeparator());
            }
        }

    }

    /**
     * A runtime exception class that is thrown when there is a problem with how the code writer has configured
     * {@link  ArgsParser} using {@link ArgOption}s. (Mostly throw from a call to a constructor of {@link ArgsParser}.)
     */
    public static class ArgumentOptionException extends RuntimeException {

        public ArgumentOptionException() {
            super();
        }

        public ArgumentOptionException(String message) {
            super(message);
        }

    }

    /**
     * A runtime exception class that is thrown when there is a problem with either how the arguments are used in the
     * command line or if there is an error during the parse of command line. (Mostly thrown by
     * {@link ArgsParser#pareArgs(String[])}.)
     */
    public class ParseArgumentException extends RuntimeException {

        public ParseArgumentException() {
            super();
        }

        public ParseArgumentException(String message) {
            super(message + "\n" + "Use -h, --help or --Help for help.");
        }



        @Override
        public synchronized Throwable fillInStackTrace() {
            return (parseErrorsDisplayStackTrace) ? super.fillInStackTrace() : null;
        }

    }



    /**
     * The usages as they relate to each key type are: <br>
     * <ol>
     *      <li> -k </li>
     *      <li> --Key-Word </li>
     *      <li> -k arg </li>
     *      <li> --Key-Word=arg </li>
     *      <li> ... argList </li>
     * </ol>
     * KEY is 1 and 2. KEY_VALUE is 3 and 4. LIST is 5. <br>
     * <br>
     * The short key is `k` and the long key is `Key-Word`. argList is a space delimited list. There can only be one
     * list arg, and they are always at the end of the command.
     */
    public enum E_Usage {
        /**
         * Use this is the key should be used on its own without a value argument. Aka a flag.
         */
        KEY,
        /**
         * Use this if the argument should accept and require a value argument. Aka a key-value pair.
         */
        KEY_VALUE,
        /**
         * Use this if the argument should be a positional list, space delimited, always passed as the last part of the
         * command line. This can only be used by one {@link ArgOption} per {@link ArgsParser} instances.
         */
        LIST
    }

    /**
     * The interface used with an enum to allow for indexing the results of a parse with an enum. See
     * {@link ArgsParser#ArgsParser(ProgrammeDetails, Class)} for details and an example.
     */
    public interface EnumOptions {
        ArgOption get();
    }

}
