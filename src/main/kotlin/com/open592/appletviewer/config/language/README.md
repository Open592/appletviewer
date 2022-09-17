# Localization

## Overview

The applet viewer (as it existed when it was originally released) supports the
following four languages:

- English
- German
- French
- Brazilian Portuguese

In order to support this feature there can't exist any static user facing
strings within the applet viewer. This class provides the functionality for
tying content keys to localized content based on the pre-determined user locale.

## Details

When the user first loads the applet viewer we attempt to determine their
appropriate language using the following logic:

- First we check for a pre-existing value from the preferences file.
- Second we check for `isO3Language` from `Locale.getDefault()`
  - If this value is `eng`, `ger`, `deu`, `fre`, `fra`, or `por` we resolve to the
    appropriate supported language
- Third we check for `isO3Country` from `Locale.getDefault()`
  - If this value is `GB`, `US`, `DE`, `FR`, or `BR` we resolve to appropriate
    supported language.
- Otherwise, we default to English

## `jav_config.ws` file

It is the job of the `jav_config.ws` file to deliver us our localized content.
All localized content strings are delivered in that file using the following
format:

```
msg=key=value
```

It is the job of the `config` package to parse out all localized content strings
from the `jav_config.ws` file and expose the dynamic content to the rest of the
applet viewer.

### Initial locale strings

In the case of strings which need to be displayed to the user before the
`jav_config.ws` file is loaded we provide a small number of content bundled
with the applet viewer code. These include things like error handling, and
initial strings.

## Support for user specifying their desired language

In order to support user specifying the supported language that works best for
them, there exists a "language" menu within the toolbar. If they specify a
different language than the one pre-selected for them, we write that language
to the preferences file and ask them to restart the applet viewer. On the next
load they will see the new language.
