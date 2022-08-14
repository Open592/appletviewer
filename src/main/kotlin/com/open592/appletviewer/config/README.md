# Configuration

## Overview

Configuration of the applet viewer and it's downstream components is handled by
the `jav_config.ws` file. This file includes numerous configuration items in a
number of categories:

### Top level configuration values

_Example_:

```
title=RuneScape
viewerversion=100
serverlist=http://127.0.0.1:8000/jav_config_serverlist.ws
// ...
```

These are configuration items which don't fall into any of the categories below,
and act as default values if an explicit server is not specified, or if the
active server does not define one of the values.

These values configure the applet viewer, and are not visible to any of the
downstream components.

### Localized content strings

_Example_:

```
// ...
msg=switchserver=Switch server
msg=serverlist=Server list
msg=information=Information
msg=message=Message
// ...
```

Localized content strings are provided in the format `msg=<key>=<value>` and
provide a method for localizing user facing strings in the applet viewer.

### Parameters

_Example_:
```
// ...
param=modewhat=0
param=modewhere=0
param=unsignedurl=http://www.runescape.com/p=Ymi9Liy-EKCQiGNINrnBCT3-kIKZvS3w7HMq48pDu3U/slu.ws?j=0
param=cachesubdirid=0
// ...
```

Originally `<Applet>`'s could be configured by supplying `<param>` tags:

```html
<applet name="Runescape">
<param name="modewhere" value="0">
<param name="modewhat" value="0">
<param name="unsignedurl" value="http://www.runescape.com/p=Ymi9Liy-EKCQiGNINrnBCT3-kIKZvS3w7HMq48pDu3U/slu.ws?j=0">
<param name="cachesubdirid" value="0">
</applet>
```

In order to emulate this behavior, the applet viewer configuration file provides
a way to specify "parameters" in the format `param=<key>=<value>`

These are visible to both the applet viewer, and all downstream applications
with access to the `getParameter` method.

### Server Configuration blocks
Server configuration blocks allow for scoping setting within multiple "servers".
This allows for delivering unique configuration for multiple servers within one
`jav_config.ws` file without the need to update `-Dcom.jagex.config` or
`-Dcom.jagex.configFile`

Server configuration blocks are specified using the following format:

```
[serverid]
servername=Human readable server name
# ... Everything after here will override the top level configuration
```

When multiple server configuration blocks are present an additional menu item
will appear in the toolbar allowing you to switch between the available servers.

![Screenshot of server selection dialog](/share/images/config/server-switch-dialog.png?raw=1)

#### `serverlist`

There exists a configuration item called `serverlist` which points to a URl
that resolves to a file defining which servers are enabled/disabled. If a server
block is present in the `jav_config.ws` but the `serverlist` file marks it as
disabled it will not show up within the server selection dialog.

_Example_:

```
serverlist=http://example.com/serverlist.ws
```

Which would resolve to a file with the following format:

```
server1,false
server2,true
```

In this case when opening the server selection dialog only `server2` would be
present
