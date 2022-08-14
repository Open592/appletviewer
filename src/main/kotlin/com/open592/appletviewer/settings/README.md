# Settings

This is a simple class which represents a key value store of settings.
The applet viewer depends on a good amount of Java system properties
for dynamic runtime data before the `jav_config.ws` is fetched.

The main reason for bringing this logic out into a class is to aid in unit
testing the code which depends on this functionality. We provide a map based
implementation of the `SettingsStore` for use in tests.
