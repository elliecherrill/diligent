import os

class BaseConfig:
    """Base configuration extended by environment-specific subclasses."""

    # Secret Keys ===============================================
    SECRET_KEY = os.environb.get(b"SECRET_KEY", "dev_secret_key")
    JWT_SECRET_KEY = os.environb.get(b"JWT_SECRET_KEY", "dev_jwt_secret_key")
    WTF_CSRF_SECRET_KEY = os.environb.get(b"WTF_CSRF_SECRET_KEY", "dev_wtf_secret_key")

    # JWT =======================================================
    # Make JWT token last for a day
    JWT_ACCESS_TOKEN_EXPIRES = 60 * 60 * 24

    # LDAP Service ==============================================
    LDAP_URL = "ldaps://ldaps-vip.cc.ic.ac.uk:636"
    LDAP_DN = "OU=Users,OU=Imperial College (London),DC=ic,DC=ac,DC=uk"

    # Database ===================================================
    MONGO_URI = "mongodb://localhost:27017/diligent"

class DevConfig(BaseConfig):
    MONGO_URI = "mongodb://localhost:27017/diligent"
    DEBUG = True


class StagingConfig(BaseConfig):
    MONGO_URI = "mongodb://localhost:27017/diligent"

class ProductionConfig(BaseConfig):
    MONGO_URI = "mongodb://localhost:27017/diligent"
