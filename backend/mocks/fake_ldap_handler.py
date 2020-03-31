import ldap

from ..utils import file_utils


class FakeLdapConnectionHandler:
    def __init__(self, *_):
        pass

    def init_app(self, *_):
        pass

    @staticmethod
    def ldap_login(username, *args, **kwargs):
        test_users = file_utils.read_json_file("backend/mocks/fake_ldap_base/users.json")
        if username not in test_users:
            raise ldap.INVALID_CREDENTIALS
        print("[LDAP] Logging in as '%s'" % username)
        return test_users.get(username)


FAKE_LDAP = FakeLdapConnectionHandler()
