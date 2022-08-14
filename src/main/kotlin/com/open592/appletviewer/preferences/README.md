# Preferences

## Overview

This class represents the `jagexappletviewer.preferences` file. The main use
of this file is to store persistent preferences of the user between sessions.

Examples of data which is stored in the file is:

- Language (`Language=0`)
- Membership status (`Member=0`)

Presence of this file is not required to determine any of these settings, as if
they are not present the original routine which set them will be called. They
exist purely for convenience.

## Usage

When using this class, we only expose two methods, `get` and `set`. The `set`
call is overloaded to allow for specifying if you want to write to the
filesystem during that call, or defer to a later time.
