import ldap

from ..auth.ldap_constants import DN, MEMBERSHIPS, TITLE, NAME, SURNAME, DOC_CN_MEMBERSHIPS
from .. import LDAP

WHITE_LIST = ["ictsec"]


def login(username, password):
    """
    Perform (a) LDAP authentication and (b) additional (app specific) verifications
    before granting access and returning the user LDAP attributes 'name, surname, title and memberships'.
    """
    ldap_attributes = LDAP.ldap_login(
        username, password, query_attrs=(TITLE, NAME, SURNAME, DN, MEMBERSHIPS)
    )
    return custom_authentication_checks(username, ldap_attributes)


def custom_authentication_checks(username, ldap_attributes):
    if "doc" not in ldap_attributes[DN]["OU"]:
        if not any (
            (
                doc_cn in ldap_attributes[MEMBERSHIPS]["CN"]
                for doc_cn in DOC_CN_MEMBERSHIPS
            )
        ):
            raise ldap.INVALID_CREDENTIALS
    return ldap_attributes
