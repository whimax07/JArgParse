# JArgParse
A command line, argument parser that can be included with just one file, [ArgsParser.java](src/ArgsParser.java). See [main.md](docs/main.md) for examples. Usable with Java 8 and newer.


### What it can do.
 - A single file with no dependence outside the standard library.
 - Automatically generated help and information dialog.
 - Suppress stack traces from arg parse exceptions.
 - Ability to use an enum for result indexing. 

### Things to finish or add.
 - Add details of what it can and cannot do.
 - A catch for the windows style help flags aka /? and /h.
 - Look into if the class should be made generic over the enum.
 - Generated help, do examples line wrap?
 - Add a way to group passed options.
 - A way to pass the same example for short and long keys.

### Known bugs or quarks.
 - A list argument cannot start with a value starting with a "-" or contain a value starting with a "-".
 - The default behavior is to error on multiple uses of the same flag. This can be changed on an option by option bases.
