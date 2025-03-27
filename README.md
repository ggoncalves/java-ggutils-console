# Java GGUtils Console

A lightweight utility library for Java console applications with a focus on file validation and exception handling.

[![Release](https://jitpack.io/v/com.github.yourusername/java-ggutils-console.svg)](https://jitpack.io/#com.github.yourusername/java-ggutils-console)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## Overview

Java GGUtils Console provides a set of tools to simplify the development of command-line applications in Java. This library offers robust file validation, standardized exception handling, and streamlined command-line argument processing.

## Features

- **File validation**: Verify existence, permissions, and file type
- **Command-line argument processing**: Fluent API for handling CLI arguments
- **Exception handling**: Standardized approach to error handling and reporting
- **Separation of concerns**: Modular design for easy integration

## Installation

### Maven

Add the JitPack repository to your pom.xml:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Then add the dependency:

```xml
<dependency>
    <groupId>com.github.yourusername</groupId>
    <artifactId>java-ggutils-console</artifactId>
    <version>v1.0.0</version>
</dependency>
```

### Gradle
Add the JitPack repository to your build.gradle:

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
```

Add the dependency:

```groovy
dependencies {
    implementation 'com.github.yourusername:java-ggutils-console:v1.0.0'
}
```

## Usage Examples

### Basic Command Line Application

```java
import com.ggoncalves.toolkit.cli.CommandLineHandler;
import com.ggoncalves.toolkit.exception.ExceptionHandler;
import com.ggoncalves.toolkit.exception.InvalidFileException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

public class SimpleApp {
    public static void main(String[] args) {
        // Create a command line handler
        CommandLineHandler handler = new CommandLineHandler();
        
        // Define required options with fluent API
        handler.addRequiredOption("i", "input", true, "Input file path")
               .addRequiredOption("o", "output", true, "Output directory path")
               .addOption("v", "verbose", false, "Enable verbose output");
        
        // Create exception handler
        ExceptionHandler exceptionHandler = new ExceptionHandler();
        
        try {
            // Parse command line arguments
            CommandLine cmd = handler.parseArgs(args);
            
            // Validate input and output paths
            handler.validateInputFile(cmd.getOptionValue("i"), "input");
            handler.validateOutputDir(cmd.getOptionValue("o"));
            
            // Application logic here
            System.out.println("Processing input file: " + cmd.getOptionValue("i"));
            System.out.println("Output will be saved to: " + cmd.getOptionValue("o"));
            
            if (cmd.hasOption("v")) {
                System.out.println("Verbose mode enabled");
            }
            
            // Do something with the files...
            
        } catch (ParseException e) {
            System.err.println("Error parsing command line: " + e.getMessage());
            handler.printHelp("SimpleApp");
        } catch (InvalidFileException e) {
            // Use exception handler for standardized error reporting
            exceptionHandler.handle(e);
            handler.printHelp("SimpleApp");
        } catch (Exception e) {
            exceptionHandler.handle(e);
        }
    }
}
```

### Advanced File Validation

```java
import com.ggoncalves.toolkit.validation.FilePathValidator;
import com.ggoncalves.toolkit.validation.ValidationResult;

public class FileValidationExample {
    public static void main(String[] args) {
        FilePathValidator validator = new FilePathValidator();
        
        // Basic validation
        if (validator.isValidExistingFilePath("/path/to/file.txt")) {
            System.out.println("File exists");
        }
        
        // Check if the path syntax is valid
        if (validator.isValidPathSyntax("/path/with/invalid\\characters")) {
            System.out.println("Path syntax is valid");
        }
        
        // Comprehensive validation
        ValidationResult result = validator.validateFilePath("/path/to/file.txt");
        if (result.isValid()) {
            System.out.println("Path: " + result.getFilePath());
            System.out.println("Exists: " + result.isExists());
            System.out.println("Is Directory: " + result.isDirectory());
            System.out.println("Is Readable: " + result.isReadable());
            System.out.println("Is Writable: " + result.isWritable());
            System.out.println("Is Empty: " + result.isBlank());
        } else {
            System.err.println("Invalid path: " + result.getErrorMessage());
        }
    }
}
```

### Exception Handling

```java
import com.ggoncalves.toolkit.exception.ExceptionHandler;
import com.ggoncalves.toolkit.exception.InvalidFileException;
import com.ggoncalves.toolkit.exception.FilePermissionException;

public class ExceptionHandlingExample {
    private static final ExceptionHandler exceptionHandler = new ExceptionHandler();
    
    public static void main(String[] args) {
        try {
            processFile("/non/existent/file.txt");
        } catch (Exception e) {
            exceptionHandler.handle(e);
        }
    }
    
    private static void processFile(String path) {
        if (!new java.io.File(path).exists()) {
            throw new InvalidFileException("File does not exist: " + path);
        }
        
        if (!new java.io.File(path).canRead()) {
            throw new FilePermissionException("Cannot read file: " + path);
        }
        
        // Process file...
    }
}
```

## API Doc

### CommandLineHandler

The primary class for handling command-line arguments and file validation.

```java
// Create a new handler
CommandLineHandler handler = new CommandLineHandler();

// Add options
handler.addRequiredOption(String opt, String longOpt, boolean hasArg, String description);
handler.addOption(String opt, String longOpt, boolean hasArg, String description);

// Parse arguments
CommandLine cmd = handler.parseArgs(String[] args);

// Validate files
handler.validateInputFile(String path, String fileType);
handler.validateOutputDir(String path);

// Display help
handler.printHelp(String appName);
```

### FilePathValidator

Provides methods for validating file paths.

```java
// Create a validator
FilePathValidator validator = new FilePathValidator();

// Simple validation
boolean exists = validator.isValidExistingFilePath(String path);
boolean validSyntax = validator.isValidPathSyntax(String path);

// Comprehensive validation
ValidationResult result = validator.validateFilePath(String path);
```

### ValidationResult

Contains detailed results of file validation.

Properties:

`filePath`: The path that was validated
`valid`: Whether the path is valid
`exists`: Whether the file/directory exists
`isDirectory`: Whether the path points to a directory
`readable`: Whether the file is readable
`writable`: Whether the file is writable
`executable`: Whether the file is executable
`isBlank`: Whether the file is empty
`errorMessage`: Error message if validation failed

### ExceptionHandler

Handles exceptions in a standardized way.

```java
// Create handler
ExceptionHandler handler = new ExceptionHandler();

// Handle exceptions
handler.handle(Throwable e);
```

### Exception Classes

`InvalidFileException`: Thrown when a file does not exist, is of wrong type, etc.
`FilePermissionException`: Thrown when permission issues occur with files

## Design Principles

This library follows several key design principles:

1. Separation of Concerns: Each class has a single, well-defined responsibility
1. Fluent API: Method chaining for improved readability
1. Fail-Fast Validation: Early detection of configuration problems
1. Comprehensive Error Reporting: Detailed error messages for troubleshooting
1. Minimal Dependencies: Core functionality with few external dependencies

## Requirements

- Java 17 or higher
- Apache Commons CLI
- Log4j2

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Development

To build the project locally:

```bash
git clone https://github.com/yourusername/java-ggutils-console.git
cd java-ggutils-console
./mvnw clean install
```

