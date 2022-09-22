// The goal of this test case is validate that we properly parse language name
// declarations.
//
// Language name declarations provide a way of specifying the supported locales
// for the applet viewer. Language name declarations should be global and
// should not be placed within named server blocks.
//
// Due to supporting the functionality of the original applet viewer we don't
// fail/ignore language name declarations scoped under a server, our behavior
// will just be to override the global language name list (not scope it within
// the server)
//
// When stored on JavConfig these should be ordered by LanguageId
msg=lang3=Português
msg=lang1=Deutsch
msg=lang0=English
msg=lang2=Français

# We should ignore invalid language codes
msg=langF=Invalid

# We should ignore unsupported language IDs
msg=lang4=Unsupported
