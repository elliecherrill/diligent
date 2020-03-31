import os

from flask import Flask
from flask_jwt_extended import JWTManager
from flask_login import LoginManager
from flask_socketio import SocketIO

from .auth import ldap_handler
from .db import database
from .db.models import *
from .messages import messages
from .mocks import fake_ldap_handler

configuration_switch = {
    "default": "backend.config.DevConfig",  # Development configuration (fake LDAP)
    "staging": "backend.config.StagingConfig",  # Staging configuration (should be as close as possible to prod)
    "production": "backend.config.ProductionConfig",  # Production configuration
}

ENV = os.environ.get("ENV", "default")

# SET UP =====================================

LOGIN_MANAGER = LoginManager()
LOGIN_MANAGER.login_view = "auth.login"
LOGIN_MANAGER.login_message = messages.LOGIN_MANAGER_MESSAGE

LDAP = fake_ldap_handler.FAKE_LDAP if ENV == "default" else ldap_handler.LDAP
DB = database.DB
JWT_MANAGER = JWTManager()


# ===================================================


def create_app(test_configuration=None, test_db=None):
    """Application factory method"""
    app = Flask(__name__, static_folder="../build/static", template_folder="../build")

    # Configure the application
    if test_configuration:
        app.config.update(test_configuration)
    else:
        app.config.from_object(configuration_switch[ENV])

    # Register extensions ################################
    # |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  #
    ######################################################
    LDAP.init_app(app)
    LOGIN_MANAGER.init_app(app)
    JWT_MANAGER.init_app(app)

    if test_configuration:
        test_db.init_app(app)
    else:
        DB.init_app(app)

    ############################################################

    from .views import auth, index, home

    app.register_blueprint(auth.bp)
    app.register_blueprint(index.bp)
    app.register_blueprint(home.bp)

    return app
