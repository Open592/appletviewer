// We are attempting to create as accurate of a configuration as possible
// for the 592 generation.
//
// That being said there were no public jav_config.ws files at that time
// since the Applet Viewer was in active development and not released yet.
//
// The configuration and content sections are as they would appear when the
// Applet Viewer was released, and the parameter section is from the original
// <Applet /> code from 592

title=RuneScape
viewerversion=100
serverlist=http://127.0.0.1:8000/jav_config_serverlist.ws
cachesubdir=runescape
codebase=http://127.0.0.1:8000/
browsercontrol_win_x86_jar=browsercontrol_0_-1928975093.jar
browsercontrol_win_amd64_jar=browsercontrol_1_1674545273.jar
loader_jar=loader.jar
advert_height=96
window_preferredwidth=1024
window_preferredheight=768
applet_minwidth=765
applet_minheight=540
applet_maxwidth=3840
applet_maxheight=2160
adverturl=http://www.runescape.com/bare_advert.ws
msg=lang0=English
msg=lang1=Deutsch
msg=lang2=Français
msg=lang3=Português
msg=new_version=Update available! You can now launch the client directly from the RuneScape website, and chat timestamps work properly.\nGet the new version here: http://www.runescape.com/download
msg=err_downloading=Error downloading
msg=err_verify_bc=Unable to verify browsercontrol
msg=err_verify_bc64=Unable to verify browsercontrol64
msg=err_save_file=Error saving file
msg=err_target_applet=Unable to create target applet
msg=err_create_advertising=Unable to create advertising
msg=tandc=This game is copyright © 1999 - 2010 Jagex Ltd.\nUse of this game is subject to our ["http://www.runescape.com/terms/terms.ws"Terms and Conditions] and ["http://www.runescape.com/privacy/privacy.ws"Privacy Policy].
msg=copy_paste_url=Please copy and paste the following URL into your web browser
msg=switchserver=Switch server
msg=serverlist=Server list
msg=information=Information
msg=message=Message
msg=ok=OK
msg=cancel=Cancel
msg=error=Error
msg=quit=Quit
msg=language=Language
msg=changes_on_restart=Your changes will take effect when you next start this program.
msg=loading_app_resources=Loading application resources
msg=loading_app=Loading application

// [NOTE!]
//
// There was no external concept of a "jav_config.ws" file when the 592 revision was released.
// These parameters are exactly how they appeared on Feb 25, 2010 within the HTML

param=java_arguments=-Xmx102m -Dsun.java2d.noddraw=true
param=colourid=0
// Dynamic based on world being requested
param=worldid=1
param=modewhere=0
param=modewhat=0
// Dynamic based on language requested
param=lang=0
param=objecttag=0
param=js=1
param=game=0
param=affid=0
param=advert=1
param=settings=Ymi9Liy-EKCQiGNINrnBCT3-kIKZvS3w7HMq48pDu3U
param=country=0

// Based on following logic which originally lived within HTML
//
// <!--[if lt IE 7]>
// <param name=haveie6 value=1>
// <![endif]-->
// <![if gte IE 7]>
// <param name="haveie6" value="0">
// <![endif]>
param=haveie6=0
param=havefirefox=0
param=cookieprefix=
param=cookiehost=.runescape.com
param=cachesubdirid=0
param=sitesettings_member=1
