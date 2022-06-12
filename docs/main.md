# Example 1: How to use ArgParse.
To parse the command line arguments passed to your programme do the following.
1) Construct programme details.
2) Define the command line options.
3) Using the programme details and command line construct an instance of ArgParse.
4) Configure any other ArgParse options. (`ParseArgumentException` displaying a stack trace.)
5) Use the String array received by main with the `parse()` method of ArgParse.
6) Use the public methods of ArgParse to inspect the results of the parse. See the class doc string for a list.

## Constructing programme details.
The `ProgrammeDetails` class contains detail about your programme. It may look like this.

```java
import ArgParser;

class ExampleProgrammeDetails {

    public static ArgsParser.ProgrammeDetails makeExample() {
        return new ArgsParser.ProgrammeDetails()
                .setCommandName("command_drinks")
                .setProgrammeName("Command Line Drink Ordering")
                .setProgrammeDescription("This program let you order drinks to your table from "
                        + "your laptops command line.")
                .setAuthor("Max Whitehouse")
                .setVersion("0.0.1");       
    }
    
}
```


## Define the command line options.
This is probably the neatest bit of the API. You can pass the options you would like to define in several ways however each comes down to defining an instance of `ArgOption` either in an enum or by instantiating them.

### Using an enum.
One of the main API features of ArgParse is that you can use an enum to hold your command line options, and therefore you can index into the results of the parse with that enum.

Enum Example:

```java
import ArgParser;

enum CommandDrinksCLOptions implements ArgsParser.EnumOptions {

    TABLE_NUMBER(new ArgsParser.ArgOption()
            .setShortKey('t')
            .setLongKey("Table-Number")
            .setUsage(ArgsParser.E_Usage.KEY_VALUE)
            .setUseOnItsOwn(true)
            .setDescription("Use this to set your table number.")
            .setShortValueExample("15")
            .setLongValueExample("15")
    ),

    DRINKS(new ArgsParser.ArgOption()
            .setShortKey('d')
            .setLongKey("Drink")
            .setUsage(ArgsParser.E_Usage.KEY_VALUE)
            .setRepeatable(true)
            .setDescription("Use this to order a drink. drink_size and quantity can be empty "
                    + "(keep the colons).")
            .setShortValueExample("{drink_name}:{drink_size}:{quantity}")
            .setLongValueExample("{drink_name}:{drink_size}:{quantity}")
    ),

    COMMENTS(new ArgsParser.ArgOption()
            .setUsage(ArgsParser.E_Usage.LIST)
            .setDescription("Use this to add any comments to your order.")
            .setListExample("Can we please get one of the diet-cokes without ice?")
    );

    private final ArgsParser.ArgOption option;

    CommandDrinksCLOptions(ArgsParser.ArgOption option) {
        this.option = option;
    }

    public ArgsParser.ArgOption get() {
        return option;
    }

}
```

Non-enum Example:

```java
import ArgParser;

class ExampleProgram {
    
    // Save the ArgOptions somewhere, so you can use them to index the results.
    private ArgsParser.ArgOption tableNumber;

    // Save the ArgOptions somewhere, so you can use them to index the results.
    private ArgsParser.ArgOption drinks;

    // Save the ArgOptions somewhere, so you can use them to index the results.
    private ArgsParser.ArgOption comments;    
    
    // ...
    
    private void constructParseOptions() {
        tableNumber = new ArgsParser.ArgOption()
                .setShortKey('t')
                .setLongKey("Table-Number")
                .setUsage(ArgsParser.E_Usage.KEY_VALUE)
                .setUseOnItsOwn(true)
                .setDescription("Use this to set your table number.")
                .setShortValueExample("15")
                .setLongValueExample("15");

        drinks = new ArgsParser.ArgOption()
                .setShortKey('d')
                .setLongKey("Drink")
                .setUsage(ArgsParser.E_Usage.KEY_VALUE)
                .setRepeatable(true)
                .setDescription("Use this to order a drink. drink_size and quantity can be empty "
                        + "(keep the colons).")
                .setShortValueExample("{drink_name}:{drink_size}:{quantity}")
                .setLongValueExample("{drink_name}:{drink_size}:{quantity}");

        comments = new ArgsParser.ArgOption()
                .setUsage(ArgsParser.E_Usage.LIST)
                .setDescription("Use this to add any comments to your order.")
                .setListExample("Can we please get one of the diet-cokes without ice?");        
    }
    
    // ...
    
}


```


## Constructing ArgParse

### Using an enum
```java
import ArgParser;

class ExampleProgram {
    
    private ArgsParser argsParser;
    
    // ...
    
    private void makeArgParser() {
        argsParser = new ArgsParser(
                ExampleProgrammeDetails.makeExample(),
                CommandDrinksCLOptions.class
        );
    }
    
    // ...
    
}
```

### Not using an enum
```java
import ArgParser;

class ExampleProgram {
    
    // ...
    
    private ArgsParser argsParser;
    
    // ...
    
    private void makeArgParser() {
        argsParser = new ArgsParser(
                ExampleProgrammeDetails.makeExample(),
                tableNumber,
                drinks,
                comments
        );
    }
    
    // ...
    
}
```

## Configuring ArgParse
The programmer should configure ArgParse before parsing. Currently, that means not printing a stack traces when `ParseArgumentException` is thrown. This is done with `setParseErrorsDisplayStackTrace`.

## Parsing a command line
This is pretty easy, just passed the String array passed to `main` to the `parse` method of ArgParse. 

```java
class ExampleProgram {

    // ...
    
    public ExampleProgram(String[] argv) {
        // If you are NOT using an enum you would call `constructParseOptions()` here.
        makeArgParser();
        argParse.parse(argv);
        
        resolveCommandLineInput();
    }
    
    public static main(String[] argv) {
        new examplePrograme(argv);
    }
    
}
```

REST TBD
