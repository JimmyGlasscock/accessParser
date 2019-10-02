# Apache Access Log Parser

This parser generates a nice CSV of all the sites that have been accessed, using the access log as an input.

## Installation

To begin using this program, use the accessParser.jar file included, or compile the source code in a java IDE if you made changes.

## Usage

```bash
java -jar accessParser.jar
```
This will prompt the user for a date, parse, and then output a few CSV reports, including a list of possible attacker's IP addresses if there has been suspicious activity.