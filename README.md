# JArgParse
A command line, argument parser that can be included with just one file, [ArgsParser.java](src/ArgsParser.java). See [main.md](docs/main.md) for examples. 


### What it can do.

### Things to finish or add.
 - Add details of what it can and cannot do.
 - Add an option on ArgParse to not include stack traces from arg parse exceptions.
 - Finish adding doc string to the public API.
 - A catch for the windows style help flags aka /? and /h.

### Known bugs or quarks.
 - A list argument cannot start with a value starting with a "-".
 - The default behavior is to error on multiple uses of the same flag.
