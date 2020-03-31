# Relevant IC LDAP attributes
TITLE = "extensionAttribute6"
NAME = "givenName"
SURNAME = "sn"
DN = "distinguishedName"
MEMBERSHIPS = "memberOf"

DOC_CN_MEMBERSHIPS = ("doc-all-students", "doc-staff-group")

# List of attributes to be parsed into dictionaries
ATTRIBUTES_TO_SERIALISE = [DN, MEMBERSHIPS]