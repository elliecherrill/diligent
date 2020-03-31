import itertools
import re
from collections import defaultdict

import ldap

from ..auth.ldap_constants import ATTRIBUTES_TO_SERIALISE

# Used to parse key-value LDAP attributes
KEY_VAL_ATT_REGEX = "([A-Za-z0-9]+)=([A-Za-z0-9-@]+)"
USERNAME_FILTER_TEMPLATE = "(&(objectClass=user)(sAMAccountName=%s))"
BINDING_TEMPLATE = "%s@IC.AC.UK"


class LdapConnectionHandler:
    """
    Adapter for the python-LDAP library.
    The class simplifies the interaction with python-LDAP
    to initialise an LDAPObject and handle the retrieval of
    relevant LDAP user attributes.

    EXAMPLE USAGE FOR LOGIN PURPOSES:
        1. An LDAP object is initialised with LDAP server URL and base distinct name
        2. A new connection is established with connect()
        3. The LDAP binding for a given username and password is performed with ldap_login()
        4. Relevant attributes are queried with query_attributes().
    """

    def __init__(self):
        self.base_dn = None
        self.server_url = None

    def init_app(self, app):
        self.base_dn = app.config["LDAP_DN"]
        self.server_url = app.config["LDAP_URL"]

    def ldap_login(self, username, password, query_attrs):
        """
        Performs basic LDAP authentication by binding on a fresh connection with `username` and `password`.
        Throws INVALID_CREDENTIALS exception if authentication fails. On successful authentication,
        retrieves the values stored on the LDAP server associated to `username` for the given `attributes`.
        :param username: username credential
        :param password: password credential
        :param attributes: names of the attributes to filter for
        :return: attr_name -> attr_value dict for given username
        """
        connection = ldap.initialize(self.server_url)
        connection.set_option(ldap.OPT_REFERRALS, 0)
        connection.simple_bind_s(BINDING_TEMPLATE % username, password)
        attributes = parse_ldap_attributes(
            self.raw_attributes(username, query_attrs, connection)
        )
        connection.unbind_s()
        return attributes

    def raw_attributes(self, username, attributes, connection):
        ldap_filter = USERNAME_FILTER_TEMPLATE % username
        raw_res = connection.search(
            self.base_dn, ldap.SCOPE_SUBTREE, ldap_filter, attributes
        )
        res_type, res_data = connection.result(raw_res)
        _, filtered_attributes = res_data[0]
        return filtered_attributes.items()


###################################################################
# U T I L I T I E S                                               #
###################################################################
def parse_ldap_attributes(attributes):
    return {
        k: ldap_attributes_to_dictionary(vs)
        if k in ATTRIBUTES_TO_SERIALISE
        else vs[0].decode("utf-8")
        for k, vs in attributes
    }


def ldap_attributes_to_dictionary(attr_values):
    items = (
        re.findall(KEY_VAL_ATT_REGEX, item.decode("utf-8").replace(",", " "))
        for item in attr_values
    )
    d = defaultdict(set)
    for k, v in itertools.chain.from_iterable(items):
        d[k].add(v)
    return d


LDAP = LdapConnectionHandler()
